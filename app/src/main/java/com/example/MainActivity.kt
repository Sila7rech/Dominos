package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.AppLanguage
import com.example.ui.components.BottomNavTab
import com.example.ui.components.DominosBottomNavigationBar
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.AuthLoginScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.FavoritesSavedScreen
import com.example.ui.screens.HomeAndCatalogScreen
import com.example.ui.screens.OffersAndOrderTrackingScreen
import com.example.ui.screens.ProductDetailsScreen
import com.example.ui.screens.ProfileSettingsAndHelpScreen
import com.example.ui.screens.ShoppingCartScreen
import com.example.ui.screens.SplashAndOnboardingScreen
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DominosViewModel
import kotlinx.coroutines.launch
import java.util.Locale

object NavRoutes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
    const val SAVED = "saved"
    const val CART = "cart"
    const val OFFERS = "offers"
    const val PROFILE = "profile"
    const val PRODUCT_DETAILS = "product_details/{productId}"
    const val CHECKOUT = "checkout"
    const val ADMIN = "admin"

    fun productDetails(productId: String) = "product_details/$productId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DominosViewModel = viewModel()
            DominosAppRoot(viewModel = viewModel)
        }
    }
}

@Composable
fun DominosAppRoot(viewModel: DominosViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val baseContext = LocalContext.current

    // Create localized Context so stringResource() resolves French (default), Arabic, or English immediately
    val localizedContext = remember(baseContext, language) {
        baseContext.createLocalizedContext(language)
    }
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides layoutDirection
    ) {
        MyApplicationTheme(isArabic = language == AppLanguage.AR) {
            DominosNavigationHost(
                viewModel = viewModel,
                localizedContext = localizedContext
            )
        }
    }
}

@Composable
private fun DominosNavigationHost(
    viewModel: DominosViewModel,
    localizedContext: Context
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val language by viewModel.language.collectAsStateWithLifecycle()
    val keepSignedIn by viewModel.keepSignedIn.collectAsStateWithLifecycle()
    val notifOrderUpdates by viewModel.notifOrderUpdates.collectAsStateWithLifecycle()
    val notifPromos by viewModel.notifPromos.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val isOnlineSupabase by viewModel.isOnlineSupabaseMode.collectAsStateWithLifecycle()
    val restaurantSettings by viewModel.restaurantSettings.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val deals by viewModel.deals.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val savedAddresses by viewModel.savedAddresses.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()
    val appliedPromoCode by viewModel.appliedPromoCode.collectAsStateWithLifecycle()
    val addressLabel by viewModel.selectedAddressLabel.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.selectedDeliveryAddress.collectAsStateWithLifecycle()
    val checkoutError by viewModel.checkoutError.collectAsStateWithLifecycle()
    val isSubmittingOrder by viewModel.isSubmittingOrder.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        NavRoutes.HOME,
        NavRoutes.SAVED,
        NavRoutes.CART,
        NavRoutes.OFFERS,
        NavRoutes.PROFILE
    )

    val currentBottomTab = when (currentRoute) {
        NavRoutes.SAVED -> BottomNavTab.SAVED
        NavRoutes.CART -> BottomNavTab.CART
        NavRoutes.OFFERS -> BottomNavTab.OFFERS
        NavRoutes.PROFILE -> BottomNavTab.PROFILE
        else -> BottomNavTab.HOME
    }

    val totalCartItemsCount = remember(cartItems) {
        cartItems.sumOf { it.quantity }
    }

    fun navigateToBottomTab(tab: BottomNavTab) {
        val route = when (tab) {
            BottomNavTab.HOME -> NavRoutes.HOME
            BottomNavTab.SAVED -> NavRoutes.SAVED
            BottomNavTab.CART -> NavRoutes.CART
            BottomNavTab.OFFERS -> NavRoutes.OFFERS
            BottomNavTab.PROFILE -> NavRoutes.PROFILE
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CreamBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                DominosBottomNavigationBar(
                    currentTab = currentBottomTab,
                    cartCount = totalCartItemsCount,
                    onSelectTab = { navigateToBottomTab(it) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(NavRoutes.SPLASH) {
                SplashAndOnboardingScreen(
                    currentLanguage = language,
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onContinueToLogin = {
                        viewModel.completeOnboarding()
                        navController.navigate(NavRoutes.AUTH)
                    },
                    onSkipToHome = {
                        viewModel.completeOnboarding()
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.AUTH) {
                AuthLoginScreen(
                    keepSignedIn = keepSignedIn,
                    onKeepSignedInChange = { viewModel.setKeepSignedIn(it) },
                    onLoginSubmit = { emailOrPhone, password, onResult ->
                        viewModel.login(emailOrPhone, password) { success ->
                            onResult(success)
                            if (success) {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.AUTH) { inclusive = true }
                                }
                            }
                        }
                    },
                    onSignUpSubmit = { fullName, email, phone, password, onResult ->
                        viewModel.signUp(fullName, email, phone, password) { success ->
                            onResult(success)
                            if (success) {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.AUTH) { inclusive = true }
                                }
                            }
                        }
                    },
                    onQuickDemoCustomer = {
                        viewModel.loginAsDemoCustomer()
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    },
                    onQuickDemoAdmin = {
                        viewModel.loginAsDemoAdmin()
                        navController.navigate(NavRoutes.ADMIN)
                    },
                    onSkipAsGuest = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.HOME) {
                HomeAndCatalogScreen(
                    lang = language,
                    isOnlineSupabase = isOnlineSupabase,
                    restaurantSettings = restaurantSettings,
                    addressLabel = addressLabel,
                    deliveryAddress = deliveryAddress,
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    products = filteredProducts,
                    allProducts = allProducts,
                    deals = deals,
                    appliedPromoCode = appliedPromoCode,
                    favoriteIds = favoriteIds,
                    searchQuery = searchQuery,
                    sortOption = sortOption,
                    cartItemCount = totalCartItemsCount,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onSortOptionChange = { viewModel.updateSortOption(it) },
                    onProductClick = { product ->
                        navController.navigate(NavRoutes.productDetails(product.id))
                    },
                    onQuickAddToCart = { product ->
                        viewModel.addToCart(product)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                localizedContext.getString(R.string.added_to_cart_toast)
                            )
                        }
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onApplyDeal = { code ->
                        viewModel.applyPromoCode(code.ifBlank { null })
                    },
                    onCartShortcutClick = { navigateToBottomTab(BottomNavTab.CART) },
                    onProfileShortcutClick = { navigateToBottomTab(BottomNavTab.PROFILE) }
                )
            }

            composable(
                route = NavRoutes.PRODUCT_DETAILS,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { entry ->
                val productId = entry.arguments?.getString("productId")
                val product = allProducts.firstOrNull { it.id == productId } ?: allProducts.first()
                ProductDetailsScreen(
                    product = product,
                    lang = language,
                    isFavorite = favoriteIds.contains(product.id),
                    onToggleFavorite = { viewModel.toggleFavorite(product.id) },
                    onAddToCart = { qty, variant, extras, instructions ->
                        viewModel.addToCart(
                            product = product,
                            quantity = qty,
                            selectedVariant = variant,
                            selectedExtras = extras,
                            specialInstructions = instructions
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                localizedContext.getString(R.string.added_to_cart_toast)
                            )
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.SAVED) {
                val favProducts = remember(allProducts, favoriteIds) {
                    allProducts.filter { favoriteIds.contains(it.id) }
                }
                FavoritesSavedScreen(
                    lang = language,
                    favoriteProducts = favProducts,
                    favoriteIds = favoriteIds,
                    onProductClick = { product ->
                        navController.navigate(NavRoutes.productDetails(product.id))
                    },
                    onQuickAddToCart = { product ->
                        viewModel.addToCart(product)
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }

            composable(NavRoutes.CART) {
                ShoppingCartScreen(
                    lang = language,
                    cartItems = cartItems,
                    summary = cartSummary,
                    appliedPromoCode = appliedPromoCode,
                    deals = deals,
                    onUpdateQuantity = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                    onRemoveItem = { id -> viewModel.removeFromCart(id) },
                    onClearCart = { viewModel.clearCart() },
                    onApplyPromoCode = { code -> viewModel.applyPromoCode(code) },
                    onBrowseMenuClick = { navigateToBottomTab(BottomNavTab.HOME) },
                    onProceedToCheckout = { navController.navigate(NavRoutes.CHECKOUT) }
                )
            }

            composable(NavRoutes.CHECKOUT) {
                CheckoutScreen(
                    lang = language,
                    currentUser = currentUser,
                    savedAddresses = savedAddresses,
                    cartItems = cartItems,
                    summary = cartSummary,
                    isSubmitting = isSubmittingOrder,
                    validationError = checkoutError,
                    onClearError = { viewModel.clearCheckoutError() },
                    onSubmitCodOrder = { name, phone, addr, instr, onSuccess ->
                        viewModel.submitCodOrder(name, phone, addr, instr, onSuccess)
                    },
                    onOrderConfirmedNavigateToTracking = {
                        navigateToBottomTab(BottomNavTab.OFFERS)
                    },
                    onOrderConfirmedBackHome = {
                        navigateToBottomTab(BottomNavTab.HOME)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.OFFERS) {
                OffersAndOrderTrackingScreen(
                    lang = language,
                    orders = orders,
                    deals = deals,
                    appliedPromoCode = appliedPromoCode,
                    estimatedDeliveryMinutes = restaurantSettings.estimatedDeliveryMinutes,
                    onApplyDeal = { code -> viewModel.applyPromoCode(code.ifBlank { null }) },
                    onRefreshOrders = { viewModel.refreshOrders() }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileSettingsAndHelpScreen(
                    lang = language,
                    isOnlineSupabase = isOnlineSupabase,
                    currentUser = currentUser,
                    savedAddresses = savedAddresses,
                    restaurantSettings = restaurantSettings,
                    notifOrderUpdates = notifOrderUpdates,
                    notifPromos = notifPromos,
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onUpdateProfile = { name, ph -> viewModel.updateProfile(name, ph) },
                    onAddAddress = { label, addr, instr ->
                        viewModel.addSavedAddress(label, addr, instr)
                    },
                    onSelectAddressForDelivery = { addr ->
                        viewModel.selectDeliveryAddress(addr)
                    },
                    onNotifOrderUpdatesChange = { viewModel.setNotifOrderUpdates(it) },
                    onNotifPromosChange = { viewModel.setNotifPromos(it) },
                    onOpenAdminPortal = { navController.navigate(NavRoutes.ADMIN) },
                    onLogoutOrSwitchAccount = {
                        viewModel.logout()
                        navController.navigate(NavRoutes.AUTH)
                    }
                )
            }

            composable(NavRoutes.ADMIN) {
                AdminPortalScreen(
                    lang = language,
                    currentUser = currentUser,
                    orders = orders,
                    products = allProducts,
                    categories = categories,
                    restaurantSettings = restaurantSettings,
                    onSwitchToDemoAdmin = { viewModel.loginAsDemoAdmin() },
                    onUpdateOrderStatus = { orderId, status ->
                        viewModel.adminUpdateOrderStatus(orderId, status)
                    },
                    onSaveProduct = { viewModel.adminSaveProduct(it) },
                    onDeleteProduct = { viewModel.adminDeleteProduct(it) },
                    onToggleProductAvailability = { viewModel.adminToggleProductAvailability(it) },
                    onSaveCategory = { viewModel.adminSaveCategory(it) },
                    onToggleCategoryActive = { viewModel.adminToggleCategoryActive(it) },
                    onUpdateRestaurantSettings = { viewModel.adminUpdateRestaurantSettings(it) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

fun Context.createLocalizedContext(language: AppLanguage): Context {
    val locale = Locale(language.code)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
