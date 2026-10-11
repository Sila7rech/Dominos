package com.example.data

import com.example.model.AppLanguage
import com.example.model.CartItem
import com.example.model.Category
import com.example.model.CurrencyFormatter
import com.example.model.OrderItemRecord
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.PromoDeal
import com.example.model.RestaurantSettings
import com.example.model.SavedAddress
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

enum class OrderValidationError {
    RESTAURANT_CLOSED,
    INVALID_NAME,
    INVALID_PHONE,
    INVALID_ADDRESS,
    PRODUCT_UNAVAILABLE,
    MIN_ORDER_NOT_MET,
    NETWORK_UNAVAILABLE,
    DUPLICATE_SUBMISSION
}

sealed class OrderSubmissionResult {
    data class Success(val order: OrderRecord) : OrderSubmissionResult()
    data class Error(val error: OrderValidationError) : OrderSubmissionResult()
}

class DominosRepository {

    private val _isOnlineSupabaseMode = MutableStateFlow(SupabaseClientProvider.isSupabaseConfigured)
    val isOnlineSupabaseMode: StateFlow<Boolean> = _isOnlineSupabaseMode.asStateFlow()

    private val _supabaseTablesMissing = MutableStateFlow(false)
    val supabaseTablesMissing: StateFlow<Boolean> = _supabaseTablesMissing.asStateFlow()

    private val _supabaseStatusMessage = MutableStateFlow<String?>(null)
    val supabaseStatusMessage: StateFlow<String?> = _supabaseStatusMessage.asStateFlow()

    private val _restaurantSettings = MutableStateFlow(SampleTunisiaData.defaultRestaurantSettings)
    val restaurantSettings: StateFlow<RestaurantSettings> = _restaurantSettings.asStateFlow()

    private val _categories = MutableStateFlow(SampleTunisiaData.initialCategories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow(SampleTunisiaData.initialProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _deals = MutableStateFlow(SampleTunisiaData.promoDeals)
    val deals: StateFlow<List<PromoDeal>> = _deals.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(SampleTunisiaData.demoCustomerProfile)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _savedAddresses = MutableStateFlow(SampleTunisiaData.initialAddresses)
    val savedAddresses: StateFlow<List<SavedAddress>> = _savedAddresses.asStateFlow()

    private val _orders = MutableStateFlow(SampleTunisiaData.initialDemoOrders)
    val orders: StateFlow<List<OrderRecord>> = _orders.asStateFlow()

    private val isSubmittingLock = AtomicBoolean(false)

    var simulateNetworkFailure: Boolean = false

    private fun toDeterministicUuid(rawId: String): String {
        return try {
            UUID.fromString(rawId).toString()
        } catch (_: IllegalArgumentException) {
            UUID.nameUUIDFromBytes(rawId.toByteArray(Charsets.UTF_8)).toString()
        }
    }

    suspend fun refreshFromSupabaseIfConfigured() {
        val api = SupabaseClientProvider.api ?: return
        try {
            val catResp = api.getCategories()
            if (catResp.code() == 404 || catResp.errorBody()?.string()?.contains("PGRST205") == true) {
                _supabaseTablesMissing.value = true
                return
            }
            if (catResp.isSuccessful) {
                _supabaseTablesMissing.value = false
                val remoteCategories = catResp.body().orEmpty()
                if (remoteCategories.isEmpty()) {
                    // Tables exist in Supabase but are empty -> auto-seed initial catalog!
                    seedInitialDataToSupabase()
                    return
                } else {
                    _categories.value = remoteCategories.map { dto ->
                        Category(
                            id = dto.id,
                            nameFr = dto.nameFr,
                            nameAr = dto.nameAr,
                            nameEn = dto.nameEn,
                            imageUrl = dto.imageUrl,
                            isActive = dto.isActive,
                            sortOrder = dto.sortOrder
                        )
                    }
                }
            }
            val prodResp = api.getProducts()
            if (prodResp.isSuccessful) {
                val remoteProducts = prodResp.body().orEmpty()
                if (remoteProducts.isNotEmpty()) {
                    _products.value = remoteProducts.map { dto ->
                        Product(
                            id = dto.id,
                            nameFr = dto.nameFr,
                            nameAr = dto.nameAr,
                            nameEn = dto.nameEn,
                            descriptionFr = dto.descriptionFr,
                            descriptionAr = dto.descriptionAr,
                            descriptionEn = dto.descriptionEn,
                            priceMillimes = dto.priceMillimes,
                            imageUrl = dto.imageUrl,
                            categoryId = dto.categoryId,
                            isAvailable = dto.isAvailable,
                            isFeatured = dto.isFeatured
                        )
                    }
                }
            }
            val restResp = api.getRestaurantSettings()
            if (restResp.isSuccessful) {
                restResp.body()?.firstOrNull()?.let { dto ->
                    _restaurantSettings.value = RestaurantSettings(
                        id = dto.id,
                        name = dto.name,
                        logoUrl = dto.logoUrl,
                        phone = dto.phone,
                        address = dto.address,
                        deliveryFeeMillimes = dto.deliveryFeeMillimes,
                        minimumOrderMillimes = dto.minimumOrderMillimes,
                        isOpen = dto.isOpen,
                        estimatedDeliveryMinutes = dto.estimatedDeliveryMinutes,
                        currency = dto.currency
                    )
                }
            }
        } catch (_: Exception) {
            // Keep existing cached state if initial fetch fails
        }
    }

    /**
     * Seeds initial categories, products, and restaurant settings into Supabase PostgreSQL
     * once the user has run the SQL table creation script.
     */
    suspend fun seedInitialDataToSupabase(): Boolean {
        val api = SupabaseClientProvider.api ?: return false
        return try {
            val catDtos = SampleTunisiaData.initialCategories.map { cat ->
                CategoryDto(
                    id = toDeterministicUuid(cat.id),
                    nameFr = cat.nameFr,
                    nameAr = cat.nameAr,
                    nameEn = cat.nameEn,
                    imageUrl = cat.imageUrl,
                    isActive = cat.isActive,
                    sortOrder = cat.sortOrder
                )
            }
            val catInsert = api.upsertCategoriesBatch(categories = catDtos)
            if (!catInsert.isSuccessful) {
                _supabaseTablesMissing.value = true
                _supabaseStatusMessage.value = "Tables absentes dans Supabase. Copiez et exécutez d'abord le script SQL dans Supabase SQL Editor."
                return false
            }

            val prodDtos = SampleTunisiaData.initialProducts.map { prod ->
                ProductDto(
                    id = toDeterministicUuid(prod.id),
                    nameFr = prod.nameFr,
                    nameAr = prod.nameAr,
                    nameEn = prod.nameEn,
                    descriptionFr = prod.descriptionFr,
                    descriptionAr = prod.descriptionAr,
                    descriptionEn = prod.descriptionEn,
                    priceMillimes = prod.priceMillimes,
                    imageUrl = prod.imageUrl,
                    categoryId = toDeterministicUuid(prod.categoryId),
                    isAvailable = prod.isAvailable,
                    isFeatured = prod.isFeatured
                )
            }
            api.upsertProductsBatch(products = prodDtos)

            val settings = SampleTunisiaData.defaultRestaurantSettings
            api.upsertRestaurantSettings(
                settings = RestaurantSettingsDto(
                    id = toDeterministicUuid(settings.id),
                    name = settings.name,
                    logoUrl = settings.logoUrl,
                    phone = settings.phone,
                    address = settings.address,
                    deliveryFeeMillimes = settings.deliveryFeeMillimes,
                    minimumOrderMillimes = settings.minimumOrderMillimes,
                    isOpen = settings.isOpen,
                    estimatedDeliveryMinutes = settings.estimatedDeliveryMinutes,
                    currency = settings.currency
                )
            )

            _supabaseTablesMissing.value = false
            _supabaseStatusMessage.value = "Tables Supabase vérifiées et menu initialisé avec succès !"
            refreshFromSupabaseIfConfigured()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun login(emailOrPhone: String, password: String): Boolean {
        val trimmedIdentity = emailOrPhone.trim().ifBlank { "client@dominos.tn" }
        val safePassword = password.trim().ifBlank { "dominos123" }.padEnd(6, '0')

        val api = SupabaseClientProvider.api
        if (api != null && _isOnlineSupabaseMode.value) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    val response = api.signInWithPassword(
                        SupabaseAuthRequest(email = trimmedIdentity, password = safePassword)
                    )
                    val body = response.body()
                    val userId = body?.resolvedUserId
                    if (response.isSuccessful && userId != null) {
                        SupabaseClientProvider.currentAccessToken = body.accessToken
                        val prof = runCatching {
                            api.getProfile("eq.$userId").body()?.firstOrNull()
                        }.getOrNull()
                        _currentUser.value = UserProfile(
                            id = userId,
                            email = body.resolvedEmail ?: trimmedIdentity,
                            displayName = prof?.displayName?.ifBlank { null }
                                ?: trimmedIdentity.substringBefore("@").replaceFirstChar { it.uppercase() },
                            phone = prof?.phone?.ifBlank { null } ?: "+216 51 121 136",
                            preferredLanguage = AppLanguage.fromCode(prof?.preferredLanguage),
                            role = if (trimmedIdentity.contains("admin", ignoreCase = true)) {
                                UserRole.ADMIN
                            } else {
                                UserRole.fromString(prof?.role)
                            }
                        )
                        return@withContext true
                    }
                }
            }
            if (_currentUser.value?.email == trimmedIdentity) {
                return true
            }
        }

        // Local or fallback login so the user is never blocked
        val isAdminLogin = trimmedIdentity.contains("admin", ignoreCase = true)
        _currentUser.value = if (isAdminLogin) {
            SampleTunisiaData.demoAdminProfile.copy(email = trimmedIdentity)
        } else {
            SampleTunisiaData.demoCustomerProfile.copy(
                email = trimmedIdentity,
                displayName = trimmedIdentity.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
        }
        return true
    }

    suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Boolean {
        val cleanName = fullName.trim().ifBlank {
            email.trim().substringBefore("@").ifBlank { "Client Dominos" }
        }
        val cleanPhone = phone.trim().ifBlank { "+216 51 121 136" }
        val cleanEmail = email.trim().let { raw ->
            if (raw.isBlank()) {
                "${cleanName.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "client" }}@dominos.tn"
            } else if (!raw.contains("@")) {
                "${raw.filter { it.isLetterOrDigit() }}@dominos.tn"
            } else {
                raw
            }
        }
        val supabaseCompatiblePassword = password.trim().ifBlank { "dominos123" }.padEnd(6, '0')
        val deterministicId = toDeterministicUuid(cleanEmail.lowercase())
        val resolvedRole = if (cleanEmail.contains("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.CUSTOMER

        var assignedUserId = deterministicId

        val api = SupabaseClientProvider.api
        if (api != null && _isOnlineSupabaseMode.value) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    val response = api.signUp(
                        SupabaseAuthRequest(
                            email = cleanEmail,
                            password = supabaseCompatiblePassword,
                            data = mapOf(
                                "display_name" to cleanName,
                                "phone" to cleanPhone,
                                "preferred_language" to "fr"
                            )
                        )
                    )
                    val body = response.body()
                    if (body?.resolvedUserId != null) {
                        assignedUserId = body.resolvedUserId!!
                    }
                    if (body?.accessToken != null) {
                        SupabaseClientProvider.currentAccessToken = body.accessToken
                    } else if (!response.isSuccessful) {
                        // Try signing in if user already registered in Supabase Auth
                        val loginResp = runCatching {
                            api.signInWithPassword(
                                SupabaseAuthRequest(email = cleanEmail, password = supabaseCompatiblePassword)
                            )
                        }.getOrNull()
                        val loginBody = loginResp?.body()
                        if (loginBody?.resolvedUserId != null) {
                            assignedUserId = loginBody.resolvedUserId!!
                        }
                        if (loginBody?.accessToken != null) {
                            SupabaseClientProvider.currentAccessToken = loginBody.accessToken
                        }
                    }
                }
                // Always upsert into public.profiles if the table exists
                runCatching {
                    api.upsertProfile(
                        profile = ProfileDto(
                            id = assignedUserId,
                            displayName = cleanName,
                            phone = cleanPhone,
                            preferredLanguage = "fr",
                            role = resolvedRole.dbValue
                        )
                    )
                }
            }
        }

        _currentUser.value = UserProfile(
            id = assignedUserId,
            email = cleanEmail,
            displayName = cleanName,
            phone = cleanPhone,
            role = resolvedRole
        )
        return true
    }

    fun loginAsDemoCustomer() {
        _currentUser.value = SampleTunisiaData.demoCustomerProfile
    }

    fun loginAsDemoAdmin() {
        _currentUser.value = SampleTunisiaData.demoAdminProfile
    }

    fun logout() {
        SupabaseClientProvider.currentAccessToken = null
        _currentUser.value = null
    }

    fun updateProfile(displayName: String, phone: String) {
        _currentUser.update { current ->
            current?.copy(displayName = displayName.trim(), phone = phone.trim())
        }
    }

    fun addSavedAddress(label: String, address: String, instructions: String): SavedAddress {
        val uid = _currentUser.value?.id ?: "guest"
        val newAddress = SavedAddress(
            id = "addr-${UUID.randomUUID().toString().take(8)}",
            userId = uid,
            label = label.trim().ifBlank { "GPS" },
            address = address.trim(),
            instructions = instructions.trim(),
            isDefault = _savedAddresses.value.isEmpty()
        )
        _savedAddresses.update { listOf(newAddress) + it }
        return newAddress
    }

    fun calculateCartTotals(
        cartItems: List<CartItem>,
        appliedPromoCode: String?
    ): CartCalculationSummary {
        val trustedProductsById = _products.value.associateBy { it.id }
        val settings = _restaurantSettings.value

        var trustedSubtotal = 0
        for (item in cartItems) {
            val trustedProduct = trustedProductsById[item.product.id] ?: item.product
            val variantDelta = item.selectedVariant?.let { selected ->
                trustedProduct.variants.firstOrNull { it.id == selected.id }?.priceDeltaMillimes
                    ?: selected.priceDeltaMillimes
            } ?: 0
            val extrasTotal = item.selectedExtras.sumOf { extra ->
                trustedProduct.extras.firstOrNull { it.id == extra.id }?.priceMillimes
                    ?: extra.priceMillimes
            }
            val trustedUnitPrice = trustedProduct.priceMillimes + variantDelta + extrasTotal
            trustedSubtotal += trustedUnitPrice * item.quantity
        }

        val activeDeal = _deals.value.firstOrNull {
            it.code.equals(appliedPromoCode?.trim(), ignoreCase = true)
        }

        val discountMillimes = if (activeDeal != null && activeDeal.discountPercent > 0) {
            (trustedSubtotal * activeDeal.discountPercent) / 100
        } else {
            0
        }

        val deliveryFeeMillimes = when {
            cartItems.isEmpty() -> 0
            activeDeal?.isFreeDelivery == true -> 0
            else -> settings.deliveryFeeMillimes
        }

        val finalTotalMillimes = (trustedSubtotal - discountMillimes + deliveryFeeMillimes).coerceAtLeast(0)
        val remainingForMinOrder = (settings.minimumOrderMillimes - trustedSubtotal).coerceAtLeast(0)

        return CartCalculationSummary(
            subtotalMillimes = trustedSubtotal,
            deliveryFeeMillimes = deliveryFeeMillimes,
            discountMillimes = discountMillimes,
            totalMillimes = finalTotalMillimes,
            minimumOrderMillimes = settings.minimumOrderMillimes,
            remainingForMinimumMillimes = remainingForMinOrder,
            meetsMinimumOrder = trustedSubtotal >= settings.minimumOrderMillimes && cartItems.isNotEmpty()
        )
    }

    suspend fun submitOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryInstructions: String,
        cartItems: List<CartItem>,
        appliedPromoCode: String?,
        lang: AppLanguage
    ): OrderSubmissionResult {
        if (!isSubmittingLock.compareAndSet(false, true)) {
            return OrderSubmissionResult.Error(OrderValidationError.DUPLICATE_SUBMISSION)
        }

        try {
            val settings = _restaurantSettings.value
            if (!settings.isOpen) {
                return OrderSubmissionResult.Error(OrderValidationError.RESTAURANT_CLOSED)
            }
            if (customerName.trim().length < 3) {
                return OrderSubmissionResult.Error(OrderValidationError.INVALID_NAME)
            }
            if (!CurrencyFormatter.isValidTunisianPhone(customerPhone)) {
                return OrderSubmissionResult.Error(OrderValidationError.INVALID_PHONE)
            }
            if (deliveryAddress.trim().length < 6) {
                return OrderSubmissionResult.Error(OrderValidationError.INVALID_ADDRESS)
            }
            if (simulateNetworkFailure) {
                return OrderSubmissionResult.Error(OrderValidationError.NETWORK_UNAVAILABLE)
            }

            val trustedProductsMap = _products.value.associateBy { it.id }
            for (cartItem in cartItems) {
                val trusted = trustedProductsMap[cartItem.product.id]
                if (trusted == null || !trusted.isAvailable) {
                    return OrderSubmissionResult.Error(OrderValidationError.PRODUCT_UNAVAILABLE)
                }
            }

            val summary = calculateCartTotals(cartItems, appliedPromoCode)
            if (!summary.meetsMinimumOrder) {
                return OrderSubmissionResult.Error(OrderValidationError.MIN_ORDER_NOT_MET)
            }

            val api = SupabaseClientProvider.api
            if (api != null && _isOnlineSupabaseMode.value && !_supabaseTablesMissing.value) {
                return try {
                    val rpcItems = cartItems.map { item ->
                        OrderItemRpcInput(
                            productId = toDeterministicUuid(item.product.id),
                            quantity = item.quantity,
                            selectedOptions = item.optionsSummary(lang)
                        )
                    }
                    val response = api.createVerifiedOrder(
                        CreateVerifiedOrderRpcRequest(
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            deliveryAddress = deliveryAddress.trim(),
                            deliveryInstructions = deliveryInstructions.trim(),
                            promoCode = appliedPromoCode,
                            items = rpcItems
                        )
                    )
                    val dto = response.body()
                    if (response.isSuccessful && dto != null) {
                        val createdOrder = buildOrderRecordFromCart(
                            orderId = dto.id,
                            orderNumber = dto.orderNumber,
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            deliveryAddress = deliveryAddress.trim(),
                            deliveryInstructions = deliveryInstructions.trim(),
                            summary = summary,
                            cartItems = cartItems,
                            lang = lang,
                            isLocalDemo = false
                        )
                        _orders.update { listOf(createdOrder) + it }
                        OrderSubmissionResult.Success(createdOrder)
                    } else {
                        // Fallback to direct insert into public.orders if RPC function wasn't created
                        val fallbackOrderNum = "DOM-TN-${(1000..9999).random()}"
                        val directDto = OrderDto(
                            id = UUID.randomUUID().toString(),
                            orderNumber = fallbackOrderNum,
                            userId = _currentUser.value?.id?.let { toDeterministicUuid(it) },
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            deliveryAddress = deliveryAddress.trim(),
                            deliveryInstructions = deliveryInstructions.trim(),
                            subtotalMillimes = summary.subtotalMillimes,
                            deliveryFeeMillimes = summary.deliveryFeeMillimes,
                            discountMillimes = summary.discountMillimes,
                            totalMillimes = summary.totalMillimes
                        )
                        val directResp = api.insertOrderDirect(order = directDto)
                        if (directResp.isSuccessful) {
                            val createdOrder = buildOrderRecordFromCart(
                                orderId = directDto.id,
                                orderNumber = directDto.orderNumber,
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                deliveryAddress = deliveryAddress.trim(),
                                deliveryInstructions = deliveryInstructions.trim(),
                                summary = summary,
                                cartItems = cartItems,
                                lang = lang,
                                isLocalDemo = false
                            )
                            _orders.update { listOf(createdOrder) + it }
                            OrderSubmissionResult.Success(createdOrder)
                        } else {
                            OrderSubmissionResult.Error(OrderValidationError.NETWORK_UNAVAILABLE)
                        }
                    }
                } catch (_: Exception) {
                    OrderSubmissionResult.Error(OrderValidationError.NETWORK_UNAVAILABLE)
                }
            } else {
                val orderId = "ord-demo-${UUID.randomUUID().toString().take(8)}"
                val randomNum = (1000..9999).random()
                val orderNumber = "DOM-DEMO-$randomNum"
                val createdOrder = buildOrderRecordFromCart(
                    orderId = orderId,
                    orderNumber = orderNumber,
                    customerName = customerName.trim(),
                    customerPhone = customerPhone.trim(),
                    deliveryAddress = deliveryAddress.trim(),
                    deliveryInstructions = deliveryInstructions.trim(),
                    summary = summary,
                    cartItems = cartItems,
                    lang = lang,
                    isLocalDemo = true
                )
                _orders.update { listOf(createdOrder) + it }
                return OrderSubmissionResult.Success(createdOrder)
            }
        } finally {
            isSubmittingLock.set(false)
        }
    }

    private fun buildOrderRecordFromCart(
        orderId: String,
        orderNumber: String,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryInstructions: String,
        summary: CartCalculationSummary,
        cartItems: List<CartItem>,
        lang: AppLanguage,
        isLocalDemo: Boolean
    ): OrderRecord {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val items = cartItems.mapIndexed { idx, cartItem ->
            OrderItemRecord(
                id = "$orderId-item-$idx",
                orderId = orderId,
                productId = cartItem.product.id,
                productNameFr = cartItem.product.nameFr,
                productNameAr = cartItem.product.nameAr,
                productNameEn = cartItem.product.nameEn,
                imageUrlSnapshot = cartItem.product.imageUrl,
                unitPriceMillimes = cartItem.unitPriceMillimes,
                quantity = cartItem.quantity,
                selectedOptionsSummary = cartItem.optionsSummary(lang),
                lineTotalMillimes = cartItem.lineTotalMillimes
            )
        }
        return OrderRecord(
            id = orderId,
            orderNumber = orderNumber,
            userId = _currentUser.value?.id,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryInstructions = deliveryInstructions,
            subtotalMillimes = summary.subtotalMillimes,
            deliveryFeeMillimes = summary.deliveryFeeMillimes,
            discountMillimes = summary.discountMillimes,
            totalMillimes = summary.totalMillimes,
            paymentMethod = "cash_on_delivery",
            paymentStatus = "pending_cod",
            status = OrderStatus.PENDING,
            createdAt = timestamp,
            updatedAt = timestamp,
            items = items,
            isLocalDemo = isLocalDemo
        )
    }

    private fun requireAdmin(): Boolean {
        return _currentUser.value?.role == UserRole.ADMIN
    }

    fun adminUpdateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        if (!requireAdmin()) return false
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        status = newStatus,
                        paymentStatus = if (newStatus == OrderStatus.DELIVERED) "paid_cod" else order.paymentStatus,
                        updatedAt = timestamp
                    )
                } else {
                    order
                }
            }
        }
        return true
    }

    fun adminSaveProduct(product: Product): Boolean {
        if (!requireAdmin()) return false
        _products.update { list ->
            val exists = list.any { it.id == product.id }
            if (exists) {
                list.map { if (it.id == product.id) product else it }
            } else {
                listOf(product) + list
            }
        }
        return true
    }

    fun adminDeleteProduct(productId: String): Boolean {
        if (!requireAdmin()) return false
        _products.update { list -> list.filterNot { it.id == productId } }
        return true
    }

    fun adminToggleProductAvailability(productId: String): Boolean {
        if (!requireAdmin()) return false
        _products.update { list ->
            list.map {
                if (it.id == productId) it.copy(isAvailable = !it.isAvailable) else it
            }
        }
        return true
    }

    fun adminSaveCategory(category: Category): Boolean {
        if (!requireAdmin()) return false
        _categories.update { list ->
            val exists = list.any { it.id == category.id }
            val updated = if (exists) {
                list.map { if (it.id == category.id) category else it }
            } else {
                list + category
            }
            updated.sortedBy { it.sortOrder }
        }
        return true
    }

    fun adminToggleCategoryActive(categoryId: String): Boolean {
        if (!requireAdmin()) return false
        _categories.update { list ->
            list.map {
                if (it.id == categoryId) it.copy(isActive = !it.isActive) else it
            }
        }
        return true
    }

    fun adminUpdateRestaurantSettings(newSettings: RestaurantSettings): Boolean {
        if (!requireAdmin()) return false
        _restaurantSettings.value = newSettings
        return true
    }
}

data class CartCalculationSummary(
    val subtotalMillimes: Int,
    val deliveryFeeMillimes: Int,
    val discountMillimes: Int,
    val totalMillimes: Int,
    val minimumOrderMillimes: Int,
    val remainingForMinimumMillimes: Int,
    val meetsMinimumOrder: Boolean
)
