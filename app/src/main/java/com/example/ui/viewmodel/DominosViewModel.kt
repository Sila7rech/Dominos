package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartCalculationSummary
import com.example.data.DominosRepository
import com.example.data.OrderSubmissionResult
import com.example.data.OrderValidationError
import com.example.data.PreferencesManager
import com.example.model.AppLanguage
import com.example.model.CartItem
import com.example.model.CatalogSortOption
import com.example.model.Category
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductExtra
import com.example.model.ProductVariant
import com.example.model.PromoDeal
import com.example.model.RestaurantSettings
import com.example.model.SavedAddress
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class DominosViewModel @JvmOverloads constructor(
    application: Application,
    val repository: DominosRepository = DominosRepository(),
    private val preferencesManager: PreferencesManager = PreferencesManager(application)
) : AndroidViewModel(application) {

    // Persistent French-default language state
    val language: StateFlow<AppLanguage> = preferencesManager.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppLanguage.FR
    )

    val onboardingDone: StateFlow<Boolean> = preferencesManager.onboardingDoneFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val keepSignedIn: StateFlow<Boolean> = preferencesManager.keepSignedInFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val notifOrderUpdates: StateFlow<Boolean> = preferencesManager.notifOrderUpdatesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val notifPromos: StateFlow<Boolean> = preferencesManager.notifPromosFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val favoriteIds: StateFlow<Set<String>> = preferencesManager.favoriteIdsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = setOf("prod-pepperoni-feast", "prod-cheese-burger")
    )

    val isOnlineSupabaseMode: StateFlow<Boolean> = repository.isOnlineSupabaseMode
    val supabaseTablesMissing: StateFlow<Boolean> = repository.supabaseTablesMissing
    val supabaseStatusMessage: StateFlow<String?> = repository.supabaseStatusMessage
    val restaurantSettings: StateFlow<RestaurantSettings> = repository.restaurantSettings
    val categories: StateFlow<List<Category>> = repository.categories
    val products: StateFlow<List<Product>> = repository.products
    val deals: StateFlow<List<PromoDeal>> = repository.deals
    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val savedAddresses: StateFlow<List<SavedAddress>> = repository.savedAddresses
    val orders: StateFlow<List<OrderRecord>> = repository.orders

    private val _selectedCategoryId = MutableStateFlow<String?>(null) // null = All
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(CatalogSortOption.POPULAR)
    val sortOption: StateFlow<CatalogSortOption> = _sortOption.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedPromoCode = MutableStateFlow<String?>(null)
    val appliedPromoCode: StateFlow<String?> = _appliedPromoCode.asStateFlow()

    private val _selectedAddressLabel = MutableStateFlow("Domicile")
    val selectedAddressLabel: StateFlow<String> = _selectedAddressLabel.asStateFlow()

    private val _selectedDeliveryAddress = MutableStateFlow("12 Avenue Habib Bourguiba, Tunis")
    val selectedDeliveryAddress: StateFlow<String> = _selectedDeliveryAddress.asStateFlow()

    private val _checkoutError = MutableStateFlow<OrderValidationError?>(null)
    val checkoutError: StateFlow<OrderValidationError?> = _checkoutError.asStateFlow()

    private val _lastCreatedOrder = MutableStateFlow<OrderRecord?>(null)
    val lastCreatedOrder: StateFlow<OrderRecord?> = _lastCreatedOrder.asStateFlow()

    private val _isSubmittingOrder = MutableStateFlow(false)
    val isSubmittingOrder: StateFlow<Boolean> = _isSubmittingOrder.asStateFlow()

    val cartSummary: StateFlow<CartCalculationSummary> = combine(
        _cartItems,
        _appliedPromoCode,
        products,
        restaurantSettings
    ) { items, promo, _, _ ->
        repository.calculateCartTotals(items, promo)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = repository.calculateCartTotals(emptyList(), null)
    )

    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        categories,
        _selectedCategoryId,
        _searchQuery,
        _sortOption
    ) { allProducts, allCategories, catId, query, sort ->
        val activeCategoryIds = allCategories.filter { it.isActive }.map { it.id }.toSet()
        val trimmed = query.trim().lowercase()

        val filtered = allProducts.filter { product ->
            val inActiveCategory = activeCategoryIds.contains(product.categoryId)
            val matchesCategory = catId == null || product.categoryId == catId
            val matchesSearch = trimmed.isEmpty() ||
                product.nameFr.lowercase().contains(trimmed) ||
                product.nameAr.contains(trimmed) ||
                product.nameEn.lowercase().contains(trimmed) ||
                product.descriptionFr.lowercase().contains(trimmed) ||
                product.descriptionEn.lowercase().contains(trimmed)
            inActiveCategory && matchesCategory && matchesSearch
        }

        when (sort) {
            CatalogSortOption.POPULAR -> filtered.sortedByDescending { it.isFeatured }
            CatalogSortOption.PRICE_ASC -> filtered.sortedBy { it.priceMillimes }
            CatalogSortOption.PRICE_DESC -> filtered.sortedByDescending { it.priceMillimes }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = repository.products.value
    )

    init {
        viewModelScope.launch {
            repository.refreshFromSupabaseIfConfigured()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            preferencesManager.setLanguage(lang)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingDone(true)
        }
    }

    fun setKeepSignedIn(keep: Boolean) {
        viewModelScope.launch {
            preferencesManager.setKeepSignedIn(keep)
        }
    }

    fun setNotifOrderUpdates(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotifOrderUpdates(enabled)
        }
    }

    fun setNotifPromos(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotifPromos(enabled)
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            preferencesManager.toggleFavorite(productId)
        }
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOption(option: CatalogSortOption) {
        _sortOption.value = option
    }

    fun selectDeliveryAddress(savedAddress: SavedAddress) {
        _selectedAddressLabel.value = savedAddress.label
        _selectedDeliveryAddress.value = savedAddress.address
    }

    fun applyGpsLocation(formattedAddress: String) {
        val saved = repository.addSavedAddress(
            label = "GPS",
            address = formattedAddress,
            instructions = "Position GPS actuelle"
        )
        _selectedAddressLabel.value = saved.label
        _selectedDeliveryAddress.value = saved.address
    }

    fun seedSupabaseTables(onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = repository.seedInitialDataToSupabase()
            onDone(ok)
        }
    }

    fun addToCart(
        product: Product,
        quantity: Int = 1,
        selectedVariant: ProductVariant? = product.variants.firstOrNull(),
        selectedExtras: List<ProductExtra> = emptyList(),
        specialInstructions: String = ""
    ) {
        if (!product.isAvailable || quantity <= 0) return
        _cartItems.update { current ->
            val extraIds = selectedExtras.map { it.id }.sorted()
            val existingIndex = current.indexOfFirst { item ->
                item.product.id == product.id &&
                    item.selectedVariant?.id == selectedVariant?.id &&
                    item.selectedExtras.map { it.id }.sorted() == extraIds &&
                    item.specialInstructions.trim() == specialInstructions.trim()
            }
            if (existingIndex >= 0) {
                current.mapIndexed { idx, item ->
                    if (idx == existingIndex) item.copy(quantity = item.quantity + quantity) else item
                }
            } else {
                current + CartItem(
                    id = "cart-${UUID.randomUUID().toString().take(8)}",
                    product = product,
                    quantity = quantity,
                    selectedVariant = selectedVariant,
                    selectedExtras = selectedExtras,
                    specialInstructions = specialInstructions.trim()
                )
            }
        }
    }

    fun updateCartItemQuantity(cartItemId: String, newQuantity: Int) {
        _cartItems.update { current ->
            if (newQuantity <= 0) {
                current.filterNot { it.id == cartItemId }
            } else {
                current.map { item ->
                    if (item.id == cartItemId) item.copy(quantity = newQuantity) else item
                }
            }
        }
    }

    fun removeFromCart(cartItemId: String) {
        _cartItems.update { current -> current.filterNot { it.id == cartItemId } }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedPromoCode.value = null
    }

    fun applyPromoCode(code: String?) {
        _appliedPromoCode.value = code?.trim()?.uppercase()?.ifBlank { null }
    }

    fun clearCheckoutError() {
        _checkoutError.value = null
    }

    fun submitCodOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryInstructions: String,
        onOrderSuccess: (OrderRecord) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingOrder.value = true
            _checkoutError.value = null
            val result = repository.submitOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress,
                deliveryInstructions = deliveryInstructions,
                cartItems = _cartItems.value,
                appliedPromoCode = _appliedPromoCode.value,
                lang = language.value
            )
            _isSubmittingOrder.value = false
            when (result) {
                is OrderSubmissionResult.Success -> {
                    _lastCreatedOrder.value = result.order
                    clearCart()
                    onOrderSuccess(result.order)
                }
                is OrderSubmissionResult.Error -> {
                    _checkoutError.value = result.error
                }
            }
        }
    }

    fun login(emailOrPhone: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.login(emailOrPhone, password)
            onResult(ok)
        }
    }

    fun signUp(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val ok = repository.signUp(fullName, email, phone, password)
            onResult(ok)
        }
    }

    fun loginAsDemoCustomer() {
        repository.loginAsDemoCustomer()
    }

    fun loginAsDemoAdmin() {
        repository.loginAsDemoAdmin()
    }

    fun logout() {
        repository.logout()
    }

    fun updateProfile(displayName: String, phone: String) {
        repository.updateProfile(displayName, phone)
    }

    fun addSavedAddress(label: String, address: String, instructions: String) {
        repository.addSavedAddress(label, address, instructions)
    }

    fun refreshOrders() {
        viewModelScope.launch {
            repository.refreshFromSupabaseIfConfigured()
        }
    }

    // Admin actions
    fun adminUpdateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean =
        repository.adminUpdateOrderStatus(orderId, newStatus)

    fun adminSaveProduct(product: Product): Boolean =
        repository.adminSaveProduct(product)

    fun adminDeleteProduct(productId: String): Boolean =
        repository.adminDeleteProduct(productId)

    fun adminToggleProductAvailability(productId: String): Boolean =
        repository.adminToggleProductAvailability(productId)

    fun adminSaveCategory(category: Category): Boolean =
        repository.adminSaveCategory(category)

    fun adminToggleCategoryActive(categoryId: String): Boolean =
        repository.adminToggleCategoryActive(categoryId)

    fun adminUpdateRestaurantSettings(newSettings: RestaurantSettings): Boolean =
        repository.adminUpdateRestaurantSettings(newSettings)
}
