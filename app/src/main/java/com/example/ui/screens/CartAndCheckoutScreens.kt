package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CartCalculationSummary
import com.example.data.OrderValidationError
import com.example.model.AppLanguage
import com.example.model.CartItem
import com.example.model.CurrencyFormatter
import com.example.model.OrderRecord
import com.example.model.PromoDeal
import com.example.model.SavedAddress
import com.example.model.UserProfile
import com.example.ui.components.DominosSubScreenTopBar
import com.example.ui.components.FoodNetworkImage
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.QuantitySelector
import com.example.ui.theme.CardPureWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenMid
import com.example.ui.theme.InputSurfaceSage
import com.example.ui.theme.LimeGreenBorder
import com.example.ui.theme.LimeGreenBright
import com.example.ui.theme.LimeGreenSoft
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextMutedOlive
import com.example.ui.theme.WarningAmber

/**
 * Shopping Cart Screen (Section 6F):
 * Add/remove products, increase/decrease quantities, display selected variants & extras,
 * calculate subtotal, delivery fee, discounts, final total in TND, enforce minimum-order requirement,
 * and preserve cart contents during navigation.
 */
@Composable
fun ShoppingCartScreen(
    lang: AppLanguage,
    cartItems: List<CartItem>,
    summary: CartCalculationSummary,
    appliedPromoCode: String?,
    deals: List<PromoDeal>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onApplyPromoCode: (String?) -> Unit,
    onBrowseMenuClick: () -> Unit,
    onProceedToCheckout: () -> Unit
) {
    var promoInput by rememberSaveable(appliedPromoCode) {
        mutableStateOf(appliedPromoCode ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .testTag("shopping_cart_screen")
    ) {
        DominosSubScreenTopBar(
            title = stringResource(R.string.cart_title),
            onBack = onBrowseMenuClick,
            trailingAction = {
                if (cartItems.isNotEmpty()) {
                    TextButton(
                        onClick = onClearCart,
                        modifier = Modifier.testTag("cart_clear_button")
                    ) {
                        Text(
                            text = stringResource(R.string.cart_clear),
                            color = ErrorCrimson,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        )

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(LimeGreenSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = ForestGreenMid,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.cart_empty_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.cart_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedOlive,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    PrimaryGradientButton(
                        text = stringResource(R.string.cart_browse_menu),
                        onClick = onBrowseMenuClick,
                        modifier = Modifier.testTag("cart_empty_browse_btn")
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FoodNetworkImage(
                                imageUrl = item.product.imageUrl,
                                contentDescription = item.product.localizedName(lang),
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.product.localizedName(lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextCharcoal,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onRemoveItem(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = stringResource(R.string.admin_delete),
                                            tint = TextMutedOlive,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                val opts = item.optionsSummary(lang)
                                if (opts.isNotBlank()) {
                                    Text(
                                        text = opts,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMutedOlive
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = CurrencyFormatter.formatMillimes(item.lineTotalMillimes, lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ForestGreenMid,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    QuantitySelector(
                                        quantity = item.quantity,
                                        onDecrement = { onUpdateQuantity(item.id, item.quantity - 1) },
                                        onIncrement = { onUpdateQuantity(item.id, item.quantity + 1) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Promo Code Box
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = stringResource(R.string.cart_promo_code_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = TextCharcoal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = promoInput,
                                    onValueChange = { promoInput = it.uppercase() },
                                    placeholder = { Text("FOODIE30 / FREESHIP") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = ForestGreenMid
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = InputSurfaceSage,
                                        unfocusedContainerColor = InputSurfaceSage,
                                        focusedBorderColor = LimeGreenBorder,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cart_promo_input")
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(LimeGreenBright)
                                        .clickable { onApplyPromoCode(promoInput) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                        .testTag("cart_apply_promo_btn")
                                ) {
                                    Text(
                                        text = stringResource(R.string.apply_btn),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextCharcoal,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(deals) { deal ->
                                    val active = appliedPromoCode.equals(deal.code, ignoreCase = true)
                                    Surface(
                                        color = if (active) ForestGreenMid else LimeGreenSoft,
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.clickable {
                                            promoInput = deal.code
                                            onApplyPromoCode(if (active) null else deal.code)
                                        }
                                    ) {
                                        Text(
                                            text = "${deal.code} (${deal.localizedHeadline(lang)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (active) Color.White else ForestGreenDark,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Totals & Minimum Order Validation Bar
            Surface(
                color = CardPureWhite,
                shadowElevation = 14.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (!summary.meetsMinimumOrder) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.cart_min_order_warning,
                                    CurrencyFormatter.formatMillimes(summary.minimumOrderMillimes, lang),
                                    CurrencyFormatter.formatMillimes(summary.remainingForMinimumMillimes, lang)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningAmber,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    PriceRow(
                        label = stringResource(R.string.cart_subtotal),
                        value = CurrencyFormatter.formatMillimes(summary.subtotalMillimes, lang)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PriceRow(
                        label = stringResource(R.string.cart_delivery_fee),
                        value = if (summary.deliveryFeeMillimes == 0) {
                            stringResource(R.string.free_label)
                        } else {
                            CurrencyFormatter.formatMillimes(summary.deliveryFeeMillimes, lang)
                        }
                    )
                    if (summary.discountMillimes > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        PriceRow(
                            label = stringResource(R.string.cart_discount),
                            value = "-${CurrencyFormatter.formatMillimes(summary.discountMillimes, lang)}",
                            valueColor = SuccessEmerald
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFFE5E8DF)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cart_total),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextCharcoal,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = CurrencyFormatter.formatMillimes(summary.totalMillimes, lang),
                            style = MaterialTheme.typography.headlineMedium,
                            color = ForestGreenDark,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("cart_final_total_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryGradientButton(
                        text = stringResource(R.string.cart_proceed_checkout),
                        enabled = summary.meetsMinimumOrder,
                        onClick = onProceedToCheckout,
                        modifier = Modifier.testTag("cart_proceed_checkout_btn")
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    valueColor: Color = TextCharcoal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMutedOlive
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Checkout Screen (Section 6G):
 * Customer name, Tunisian phone, delivery address, optional instructions,
 * Cash on Delivery (COD) only, full validation before submission,
 * and Order Confirmation dialog with unique order number.
 */
@Composable
fun CheckoutScreen(
    lang: AppLanguage,
    currentUser: UserProfile?,
    savedAddresses: List<SavedAddress>,
    cartItems: List<CartItem>,
    summary: CartCalculationSummary,
    isSubmitting: Boolean,
    validationError: OrderValidationError?,
    onClearError: () -> Unit,
    onSubmitCodOrder: (String, String, String, String, (OrderRecord) -> Unit) -> Unit,
    onOrderConfirmedNavigateToTracking: () -> Unit,
    onOrderConfirmedBackHome: () -> Unit,
    onBack: () -> Unit
) {
    var customerName by rememberSaveable {
        mutableStateOf(currentUser?.displayName ?: "Yassine Ben Ali")
    }
    var customerPhone by rememberSaveable {
        mutableStateOf(currentUser?.phone ?: "+216 22 456 789")
    }
    var deliveryAddress by rememberSaveable {
        mutableStateOf(savedAddresses.firstOrNull()?.address ?: "12 Avenue Habib Bourguiba, Tunis")
    }
    var deliveryInstructions by rememberSaveable {
        mutableStateOf(savedAddresses.firstOrNull()?.instructions ?: "")
    }
    var confirmedOrder by remember { mutableStateOf<OrderRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .testTag("checkout_screen")
    ) {
        DominosSubScreenTopBar(
            title = stringResource(R.string.checkout_title),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (validationError != null) {
                val errText = when (validationError) {
                    OrderValidationError.RESTAURANT_CLOSED -> stringResource(R.string.error_restaurant_closed)
                    OrderValidationError.INVALID_NAME -> stringResource(R.string.error_invalid_name)
                    OrderValidationError.INVALID_PHONE -> stringResource(R.string.error_invalid_phone)
                    OrderValidationError.INVALID_ADDRESS -> stringResource(R.string.error_invalid_address)
                    OrderValidationError.PRODUCT_UNAVAILABLE -> stringResource(R.string.error_product_unavailable)
                    OrderValidationError.MIN_ORDER_NOT_MET -> stringResource(R.string.error_min_order_not_met)
                    OrderValidationError.NETWORK_UNAVAILABLE -> stringResource(R.string.error_network_unavailable)
                    OrderValidationError.DUPLICATE_SUBMISSION -> stringResource(R.string.error_duplicate_order)
                }
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorCrimson
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorCrimson,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Saved addresses quick selector
            if (savedAddresses.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.checkout_saved_addresses),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCharcoal
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(savedAddresses, key = { it.id }) { addr ->
                        val selected = deliveryAddress == addr.address
                        Surface(
                            color = if (selected) ForestGreenMid else CardPureWhite,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (selected) ForestGreenMid else Color(0xFFE5E8DF),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    deliveryAddress = addr.address
                                    deliveryInstructions = addr.instructions
                                    onClearError()
                                }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = addr.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color.White else TextCharcoal
                                )
                                Text(
                                    text = addr.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) Color.White.copy(alpha = 0.85f) else TextMutedOlive
                                )
                            }
                        }
                    }
                }
            }

            // Customer Delivery Form Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.checkout_customer_info),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            onClearError()
                        },
                        label = { Text(stringResource(R.string.checkout_name_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = {
                            customerPhone = it
                            onClearError()
                        },
                        label = { Text(stringResource(R.string.checkout_phone_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input")
                    )

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = {
                            deliveryAddress = it
                            onClearError()
                        },
                        label = { Text(stringResource(R.string.checkout_address_label)) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_address_input")
                    )

                    OutlinedTextField(
                        value = deliveryInstructions,
                        onValueChange = { deliveryInstructions = it },
                        label = { Text(stringResource(R.string.checkout_instructions_label)) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_instructions_input")
                    )
                }
            }

            // Payment Method Card (Cash on Delivery Only — Strict Free Policy)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = LimeGreenSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ForestGreenMid),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalAtm,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.checkout_cod_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreenDark,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.checkout_cod_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestGreenDark.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Order Summary Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.checkout_order_summary),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.product.localizedName(lang)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextCharcoal
                            )
                            Text(
                                text = CurrencyFormatter.formatMillimes(item.lineTotalMillimes, lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextCharcoal
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE5E8DF))
                    PriceRow(
                        label = stringResource(R.string.cart_subtotal),
                        value = CurrencyFormatter.formatMillimes(summary.subtotalMillimes, lang)
                    )
                    PriceRow(
                        label = stringResource(R.string.cart_delivery_fee),
                        value = if (summary.deliveryFeeMillimes == 0) {
                            stringResource(R.string.free_label)
                        } else {
                            CurrencyFormatter.formatMillimes(summary.deliveryFeeMillimes, lang)
                        }
                    )
                    if (summary.discountMillimes > 0) {
                        PriceRow(
                            label = stringResource(R.string.cart_discount),
                            value = "-${CurrencyFormatter.formatMillimes(summary.discountMillimes, lang)}",
                            valueColor = SuccessEmerald
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.cart_total),
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreenDark,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = CurrencyFormatter.formatMillimes(summary.totalMillimes, lang),
                            style = MaterialTheme.typography.titleLarge,
                            color = ForestGreenDark,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Surface(
            color = CardPureWhite,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                PrimaryGradientButton(
                    text = if (isSubmitting) {
                        stringResource(R.string.checkout_submitting)
                    } else {
                        "${stringResource(R.string.checkout_confirm_btn)} • ${
                            CurrencyFormatter.formatMillimes(summary.totalMillimes, lang)
                        }"
                    },
                    enabled = !isSubmitting && cartItems.isNotEmpty(),
                    onClick = {
                        onSubmitCodOrder(
                            customerName,
                            customerPhone,
                            deliveryAddress,
                            deliveryInstructions
                        ) { created ->
                            confirmedOrder = created
                        }
                    },
                    modifier = Modifier.testTag("checkout_confirm_order_btn")
                )
            }
        }
    }

    // Order Confirmation Modal Dialog with Unique Order Number
    confirmedOrder?.let { order ->
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessEmerald,
                    modifier = Modifier.size(54.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.order_confirmed_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.order_number_label, order.orderNumber),
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreenDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stringResource(R.string.cart_total)}: ${
                            CurrencyFormatter.formatMillimes(order.totalMillimes, lang)
                        }",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextCharcoal
                    )
                    if (order.isLocalDemo) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.order_confirmed_demo_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningAmber,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryGradientButton(
                    text = stringResource(R.string.order_track_btn),
                    onClick = {
                        confirmedOrder = null
                        onOrderConfirmedNavigateToTracking()
                    },
                    modifier = Modifier.testTag("order_confirmed_track_btn")
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        confirmedOrder = null
                        onOrderConfirmedBackHome()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.order_back_home_btn))
                }
            }
        )
    }
}
