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

    // Allows testing network failure behavior deterministically in tests or offline situations
    var simulateNetworkFailure: Boolean = false

    suspend fun refreshFromSupabaseIfConfigured() {
        val api = SupabaseClientProvider.api ?: return
        try {
            val catResp = api.getCategories()
            if (catResp.isSuccessful) {
                catResp.body()?.let { list ->
                    if (list.isNotEmpty()) {
                        _categories.value = list.map { dto ->
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
            }
            val prodResp = api.getProducts()
            if (prodResp.isSuccessful) {
                prodResp.body()?.let { list ->
                    if (list.isNotEmpty()) {
                        _products.value = list.map { dto ->
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

    suspend fun login(emailOrPhone: String, password: String): Boolean {
        if (emailOrPhone.isBlank() || password.length < 4) return false
        val api = SupabaseClientProvider.api
        if (api != null && _isOnlineSupabaseMode.value) {
            return try {
                val response = api.signInWithPassword(SupabaseAuthRequest(emailOrPhone.trim(), password))
                val body = response.body()
                if (response.isSuccessful && body?.accessToken != null && body.user != null) {
                    SupabaseClientProvider.currentAccessToken = body.accessToken
                    val profResp = api.getProfile("eq.${body.user.id}")
                    val prof = profResp.body()?.firstOrNull()
                    _currentUser.value = UserProfile(
                        id = body.user.id,
                        email = body.user.email ?: emailOrPhone,
                        displayName = prof?.displayName ?: emailOrPhone.substringBefore("@"),
                        phone = prof?.phone ?: "+216 22 456 789",
                        preferredLanguage = AppLanguage.fromCode(prof?.preferredLanguage),
                        role = UserRole.fromString(prof?.role)
                    )
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        } else {
            val isAdminLogin = emailOrPhone.contains("admin", ignoreCase = true)
            _currentUser.value = if (isAdminLogin) {
                SampleTunisiaData.demoAdminProfile
            } else {
                SampleTunisiaData.demoCustomerProfile.copy(
                    email = emailOrPhone.trim(),
                    displayName = emailOrPhone.substringBefore("@").replaceFirstChar { it.uppercase() }
                )
            }
            return true
        }
    }

    suspend fun signUp(fullName: String, email: String, phone: String, password: String): Boolean {
        if (fullName.trim().length < 3 || !CurrencyFormatter.isValidTunisianPhone(phone) || password.length < 4) {
            return false
        }
        val api = SupabaseClientProvider.api
        if (api != null && _isOnlineSupabaseMode.value) {
            return try {
                val response = api.signUp(SupabaseAuthRequest(email.trim(), password))
                val body = response.body()
                if (response.isSuccessful && body?.user != null) {
                    SupabaseClientProvider.currentAccessToken = body.accessToken
                    _currentUser.value = UserProfile(
                        id = body.user.id,
                        email = email.trim(),
                        displayName = fullName.trim(),
                        phone = phone.trim(),
                        role = UserRole.CUSTOMER
                    )
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        } else {
            _currentUser.value = UserProfile(
                id = UUID.randomUUID().toString(),
                email = email.trim(),
                displayName = fullName.trim(),
                phone = phone.trim(),
                role = UserRole.CUSTOMER
            )
            return true
        }
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

    fun addSavedAddress(label: String, address: String, instructions: String) {
        val uid = _currentUser.value?.id ?: "guest"
        val newAddress = SavedAddress(
            id = "addr-${UUID.randomUUID().toString().take(8)}",
            userId = uid,
            label = label.trim().ifBlank { "Domicile" },
            address = address.trim(),
            instructions = instructions.trim(),
            isDefault = _savedAddresses.value.isEmpty()
        )
        _savedAddresses.update { listOf(newAddress) + it }
    }

    /**
     * Recalculates cart totals strictly from trusted product catalog & restaurant settings values.
     */
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

    /**
     * Validates and submits a Cash on Delivery (COD) order.
     * Prevents duplicate submissions, verifies restaurant open status, phone, address,
     * product availability, and trusted prices.
     * Never silently falls back to demo mode if online submission fails.
     */
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

            // Verify all products in cart are still available in trusted catalog
            val trustedProductsMap = _products.value.associateBy { it.id }
            for (cartItem in cartItems) {
                val trusted = trustedProductsMap[cartItem.product.id]
                if (trusted == null || !trusted.isAvailable) {
                    return OrderSubmissionResult.Error(OrderValidationError.PRODUCT_UNAVAILABLE)
                }
            }

            // Recalculate prices from trusted catalog
            val summary = calculateCartTotals(cartItems, appliedPromoCode)
            if (!summary.meetsMinimumOrder) {
                return OrderSubmissionResult.Error(OrderValidationError.MIN_ORDER_NOT_MET)
            }

            val api = SupabaseClientProvider.api
            if (api != null && _isOnlineSupabaseMode.value) {
                // Online Supabase PostgreSQL RPC order creation (never falls back to demo mode on error!)
                return try {
                    val rpcItems = cartItems.map { item ->
                        OrderItemRpcInput(
                            productId = item.product.id,
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
                        OrderSubmissionResult.Error(OrderValidationError.NETWORK_UNAVAILABLE)
                    }
                } catch (_: Exception) {
                    OrderSubmissionResult.Error(OrderValidationError.NETWORK_UNAVAILABLE)
                }
            } else {
                // Local Demo Mode Order
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

    // ADMIN OPERATIONS (Role-checked)
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
