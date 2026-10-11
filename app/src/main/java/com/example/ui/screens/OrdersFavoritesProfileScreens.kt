package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.CurrencyFormatter
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.PromoDeal
import com.example.model.RestaurantSettings
import com.example.model.SavedAddress
import com.example.model.UserProfile
import com.example.ui.components.DealPromoCard
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.ScreenshotProductGridCard
import com.example.ui.theme.CardPureWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenMid
import com.example.ui.theme.InputSurfaceSage
import com.example.ui.theme.LimeGreenBright
import com.example.ui.theme.LimeGreenSoft
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextMutedOlive
import com.example.ui.theme.WarningAmber

/**
 * Offers & Order Tracking Screen (Section 6H):
 * Displays active deals AND full Order Status Timeline (Pending -> Confirmed -> Preparing -> Ready -> Out for delivery -> Delivered)
 * with manual refresh and order history.
 */
@Composable
fun OffersAndOrderTrackingScreen(
    lang: AppLanguage,
    orders: List<OrderRecord>,
    deals: List<PromoDeal>,
    appliedPromoCode: String?,
    estimatedDeliveryMinutes: Int,
    onApplyDeal: (String) -> Unit,
    onRefreshOrders: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .testTag("offers_and_orders_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.order_tracking_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextCharcoal,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(
                    onClick = onRefreshOrders,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LimeGreenSoft)
                        .testTag("refresh_orders_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.order_refresh_btn),
                        tint = ForestGreenDark
                    )
                }
            }
        }

        // Deals section
        item {
            Text(
                text = stringResource(R.string.section_deals_for_you),
                style = MaterialTheme.typography.titleMedium,
                color = TextCharcoal,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                deals.forEach { deal ->
                    val isApplied = appliedPromoCode.equals(deal.code, ignoreCase = true)
                    DealPromoCard(
                        deal = deal,
                        lang = lang,
                        isApplied = isApplied,
                        onApplyClick = { onApplyDeal(if (isApplied) "" else deal.code) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = ForestGreenMid,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.order_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextCharcoal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.order_empty_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedOlive,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
                OrderTimelineCard(
                    order = order,
                    lang = lang,
                    estimatedDeliveryMinutes = estimatedDeliveryMinutes
                )
            }
        }
    }
}

@Composable
private fun OrderTimelineCard(
    order: OrderRecord,
    lang: AppLanguage,
    estimatedDeliveryMinutes: Int
) {
    val steps = listOf(
        OrderStatus.PENDING to stringResource(R.string.status_pending),
        OrderStatus.CONFIRMED to stringResource(R.string.status_confirmed),
        OrderStatus.PREPARING to stringResource(R.string.status_preparing),
        OrderStatus.READY to stringResource(R.string.status_ready),
        OrderStatus.OUT_FOR_DELIVERY to stringResource(R.string.status_out_for_delivery),
        OrderStatus.DELIVERED to stringResource(R.string.status_delivered)
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardPureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.order_number_label, order.orderNumber),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = order.createdAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedOlive
                    )
                }

                if (order.isLocalDemo) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.demo_mode_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                Text(
                    text = stringResource(R.string.order_est_arrival, estimatedDeliveryMinutes),
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreenMid
                )
            }

            // 6-Step Status Timeline
            if (order.status == OrderStatus.CANCELLED) {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.status_cancelled),
                        color = ErrorCrimson,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.forEach { (stepStatus, stepLabel) ->
                        val reached = order.status.stepIndex >= stepStatus.stepIndex
                        val isCurrent = order.status == stepStatus
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = when {
                                    isCurrent -> LimeGreenBright
                                    reached -> ForestGreenMid
                                    else -> Color(0xFFD5DBD0)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stepLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (reached) TextCharcoal else TextMutedOlive
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E8DF))

            // Order items snapshot
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.localizedName(lang)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextCharcoal
                    )
                    Text(
                        text = CurrencyFormatter.formatMillimes(item.lineTotalMillimes, lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedOlive
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.deliveryAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedOlive,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = CurrencyFormatter.formatMillimes(order.totalMillimes, lang),
                    style = MaterialTheme.typography.titleMedium,
                    color = ForestGreenDark,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Favorites ("Saved") Screen (Section 6I)
 */
@Composable
fun FavoritesSavedScreen(
    lang: AppLanguage,
    favoriteProducts: List<Product>,
    favoriteIds: Set<String>,
    onProductClick: (Product) -> Unit,
    onQuickAddToCart: (Product) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .testTag("favorites_screen")
    ) {
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TextCharcoal,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(16.dp)
        )

        if (favoriteProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = ForestGreenMid,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.favorites_empty_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextCharcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.favorites_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedOlive,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val chunked = favoriteProducts.chunked(2)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chunked.size) { rowIdx ->
                    val pair = chunked[rowIdx]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { product ->
                            ScreenshotProductGridCard(
                                product = product,
                                lang = lang,
                                isFavorite = favoriteIds.contains(product.id),
                                onCardClick = { onProductClick(product) },
                                onQuickAddClick = { onQuickAddToCart(product) },
                                onToggleFavorite = { onToggleFavorite(product.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Profile, Settings, Language Selector (FR / AR RTL / EN), Contact & Help Screen (Sections 6J & 6K)
 */
@Composable
fun ProfileSettingsAndHelpScreen(
    lang: AppLanguage,
    isOnlineSupabase: Boolean,
    currentUser: UserProfile?,
    savedAddresses: List<SavedAddress>,
    restaurantSettings: RestaurantSettings,
    notifOrderUpdates: Boolean,
    notifPromos: Boolean,
    onSelectLanguage: (AppLanguage) -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onAddAddress: (String, String, String) -> Unit,
    onSelectAddressForDelivery: (SavedAddress) -> Unit,
    onNotifOrderUpdatesChange: (Boolean) -> Unit,
    onNotifPromosChange: (Boolean) -> Unit,
    onOpenAdminPortal: () -> Unit,
    onLogoutOrSwitchAccount: () -> Unit
) {
    val context = LocalContext.current
    var displayName by rememberSaveable(currentUser?.displayName) {
        mutableStateOf(currentUser?.displayName ?: "Yassine Ben Ali")
    }
    var phone by rememberSaveable(currentUser?.phone) {
        mutableStateOf(currentUser?.phone ?: "+216 22 456 789")
    }
    var newAddrLabel by rememberSaveable { mutableStateOf("") }
    var newAddrValue by rememberSaveable { mutableStateOf("") }
    var showProfileSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .testTag("profile_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextCharcoal,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isOnlineSupabase) {
                    stringResource(R.string.supabase_connected_notice)
                } else {
                    stringResource(R.string.demo_mode_notice)
                },
                style = MaterialTheme.typography.bodySmall,
                color = ForestGreenDark
            )
        }

        // 1. Language Selector Card (FR Default, AR RTL, EN)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = ForestGreenMid
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.profile_language_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextCharcoal,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(
                            AppLanguage.FR to stringResource(R.string.lang_french),
                            AppLanguage.AR to stringResource(R.string.lang_arabic),
                            AppLanguage.EN to stringResource(R.string.lang_english)
                        )
                        options.forEach { (optionLang, label) ->
                            val selected = lang == optionLang
                            Surface(
                                color = if (selected) ForestGreenMid else InputSurfaceSage,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectLanguage(optionLang) }
                                    .testTag("profile_lang_${optionLang.code}")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else TextCharcoal,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Customer Profile Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_section_account),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = {
                            displayName = it
                            showProfileSaved = false
                        },
                        label = { Text(stringResource(R.string.auth_full_name)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            showProfileSaved = false
                        },
                        label = { Text(stringResource(R.string.auth_phone)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showProfileSaved) {
                        Text(
                            text = stringResource(R.string.profile_saved_toast),
                            color = SuccessEmerald,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    PrimaryGradientButton(
                        text = stringResource(R.string.profile_save_btn),
                        onClick = {
                            onUpdateProfile(displayName, phone)
                            showProfileSaved = true
                        },
                        height = 48.dp
                    )
                }
            }
        }

        // 3. Saved Delivery Addresses Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_addresses_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )
                    savedAddresses.forEach { addr ->
                        Surface(
                            color = InputSurfaceSage,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectAddressForDelivery(addr) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = addr.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ForestGreenDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = addr.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextCharcoal
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newAddrLabel,
                            onValueChange = { newAddrLabel = it },
                            placeholder = { Text(stringResource(R.string.profile_address_label_hint)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(0.4f)
                        )
                        OutlinedTextField(
                            value = newAddrValue,
                            onValueChange = { newAddrValue = it },
                            placeholder = { Text(stringResource(R.string.checkout_address_label)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (newAddrValue.isNotBlank()) {
                                onAddAddress(newAddrLabel, newAddrValue, "")
                                newAddrLabel = ""
                                newAddrValue = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AddLocationAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.profile_add_address))
                    }
                }
            }
        }

        // 4. Notification Preferences
        item {
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
                        text = stringResource(R.string.profile_notifications_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.profile_notif_order_updates),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextCharcoal,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = notifOrderUpdates,
                            onCheckedChange = onNotifOrderUpdatesChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LimeGreenBright)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.profile_notif_promos),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextCharcoal,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = notifPromos,
                            onCheckedChange = onNotifPromosChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LimeGreenBright)
                        )
                    }
                }
            }
        }

        // 5. Contact, Help & FAQ (Section 6K)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_contact_help_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${stringResource(R.string.contact_phone_label)}: ${restaurantSettings.phone}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${stringResource(R.string.contact_address_label)}: ${restaurantSettings.address}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${stringResource(R.string.contact_hours_label)}: ${restaurantSettings.openingHoursJson}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestGreenMid,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${restaurantSettings.phone}"))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.contact_call_btn))
                        }

                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurantSettings.whatsappUrl))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.contact_whatsapp_btn))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E8DF))

                    Text(
                        text = stringResource(R.string.faq_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ForestGreenDark,
                        fontWeight = FontWeight.Bold
                    )
                    FaqEntry(stringResource(R.string.faq_q1), stringResource(R.string.faq_a1))
                    FaqEntry(stringResource(R.string.faq_q2), stringResource(R.string.faq_a2))
                    FaqEntry(stringResource(R.string.faq_q3), stringResource(R.string.faq_a3))

                    HorizontalDivider(color = Color(0xFFE5E8DF))
                    Text(
                        text = stringResource(R.string.profile_about_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextCharcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.profile_about_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedOlive
                    )
                }
            }
        }

        // 6. Admin Portal & Logout Buttons
        item {
            Surface(
                color = LimeGreenSoft,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAdminPortal)
                    .testTag("open_admin_portal_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = ForestGreenDark
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.profile_admin_portal_btn),
                        style = MaterialTheme.typography.titleSmall,
                        color = ForestGreenDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLogoutOrSwitchAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = ErrorCrimson
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentUser != null) {
                        stringResource(R.string.profile_logout_btn)
                    } else {
                        stringResource(R.string.profile_login_btn)
                    },
                    color = ErrorCrimson,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FaqEntry(question: String, answer: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "• $question",
            style = MaterialTheme.typography.labelLarge,
            color = TextCharcoal
        )
        Text(
            text = answer,
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedOlive
        )
    }
}
