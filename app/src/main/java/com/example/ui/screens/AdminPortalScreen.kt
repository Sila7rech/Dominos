package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.AppLanguage
import com.example.model.Category
import com.example.model.CurrencyFormatter
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.RestaurantSettings
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.components.DominosSubScreenTopBar
import com.example.ui.components.FoodNetworkImage
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.theme.CardPureWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenMid
import com.example.ui.theme.LimeGreenBright
import com.example.ui.theme.LimeGreenSoft
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextMutedOlive
import java.util.UUID

/**
 * Protected Restaurant Administration Screen (Section 7):
 * Enforces verified role check (UserRole.ADMIN), views sales totals,
 * filters & updates order statuses, manages trilingual products & categories,
 * and edits restaurant settings.
 */
@Composable
fun AdminPortalScreen(
    lang: AppLanguage,
    currentUser: UserProfile?,
    orders: List<OrderRecord>,
    products: List<Product>,
    categories: List<Category>,
    restaurantSettings: RestaurantSettings,
    onSwitchToDemoAdmin: () -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onToggleProductAvailability: (String) -> Unit,
    onSaveCategory: (Category) -> Unit,
    onToggleCategoryActive: (String) -> Unit,
    onUpdateRestaurantSettings: (RestaurantSettings) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var statusFilter by rememberSaveable { mutableStateOf<OrderStatus?>(null) }

    val isAdmin = currentUser?.role == UserRole.ADMIN
    val totalSalesMillimes = orders
        .filter { it.status != OrderStatus.CANCELLED }
        .sumOf { it.totalMillimes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .testTag("admin_portal_screen")
    ) {
        DominosSubScreenTopBar(
            title = stringResource(R.string.admin_title),
            onBack = onBack
        )

        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = ErrorCrimson,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = stringResource(R.string.admin_unauthorized),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextCharcoal,
                            textAlign = TextAlign.Center
                        )
                        PrimaryGradientButton(
                            text = stringResource(R.string.admin_switch_demo_admin),
                            onClick = onSwitchToDemoAdmin,
                            modifier = Modifier.testTag("admin_switch_demo_btn")
                        )
                    }
                }
            }
            return@Column
        }

        // Sales Summary Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = ForestGreenDark,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.admin_sales_total),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = CurrencyFormatter.formatMillimes(totalSalesMillimes, lang),
                        style = MaterialTheme.typography.titleLarge,
                        color = LimeGreenBright,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Surface(
                color = CardPureWhite,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.admin_orders_count),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedOlive
                    )
                    Text(
                        text = orders.size.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextCharcoal,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardPureWhite,
            contentColor = ForestGreenDark
        ) {
            val tabs = listOf(
                stringResource(R.string.admin_tab_orders),
                stringResource(R.string.admin_tab_products),
                stringResource(R.string.admin_tab_categories),
                stringResource(R.string.admin_tab_settings)
            )
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == idx) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminOrdersTab(
                lang = lang,
                orders = orders,
                statusFilter = statusFilter,
                onSelectStatusFilter = { statusFilter = it },
                onUpdateOrderStatus = onUpdateOrderStatus
            )
            1 -> AdminProductsTab(
                lang = lang,
                products = products,
                categories = categories,
                onSaveProduct = onSaveProduct,
                onDeleteProduct = onDeleteProduct,
                onToggleAvailability = onToggleProductAvailability
            )
            2 -> AdminCategoriesTab(
                lang = lang,
                categories = categories,
                onSaveCategory = onSaveCategory,
                onToggleActive = onToggleCategoryActive
            )
            3 -> AdminRestaurantSettingsTab(
                settings = restaurantSettings,
                onSaveSettings = onUpdateRestaurantSettings
            )
        }
    }
}

@Composable
private fun AdminOrdersTab(
    lang: AppLanguage,
    orders: List<OrderRecord>,
    statusFilter: OrderStatus?,
    onSelectStatusFilter: (OrderStatus?) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit
) {
    val filtered = remember(orders, statusFilter) {
        if (statusFilter == null) orders else orders.filter { it.status == statusFilter }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { onSelectStatusFilter(null) },
                        label = { Text(stringResource(R.string.category_all)) }
                    )
                }
                items(OrderStatus.entries) { st ->
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { onSelectStatusFilter(if (statusFilter == st) null else st) },
                        label = { Text(st.name) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { order ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${order.orderNumber} • ${order.customerName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = CurrencyFormatter.formatMillimes(order.totalMillimes, lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreenMid,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "${order.customerPhone} • ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedOlive
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(OrderStatus.entries) { targetStatus ->
                            val active = order.status == targetStatus
                            Surface(
                                color = if (active) ForestGreenMid else LimeGreenSoft,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable {
                                    onUpdateOrderStatus(order.id, targetStatus)
                                }
                            ) {
                                Text(
                                    text = targetStatus.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) Color.White else ForestGreenDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProductsTab(
    lang: AppLanguage,
    products: List<Product>,
    categories: List<Category>,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onToggleAvailability: (String) -> Unit
) {
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var nameFr by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var descFr by remember { mutableStateOf("") }
    var priceMillimesText by remember { mutableStateOf("18500") }
    var imageUrl by remember {
        mutableStateOf("https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=700&q=80")
    }
    var showForm by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PrimaryGradientButton(
                text = stringResource(R.string.admin_add_product),
                onClick = {
                    editingProduct = null
                    nameFr = ""
                    nameAr = ""
                    nameEn = ""
                    descFr = ""
                    priceMillimesText = "18500"
                    showForm = !showForm
                },
                height = 46.dp
            )
        }

        if (showForm) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nameFr,
                            onValueChange = { nameFr = it },
                            label = { Text(stringResource(R.string.admin_name_fr)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nameAr,
                            onValueChange = { nameAr = it },
                            label = { Text(stringResource(R.string.admin_name_ar)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it },
                            label = { Text(stringResource(R.string.admin_name_en)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = descFr,
                            onValueChange = { descFr = it },
                            label = { Text(stringResource(R.string.admin_desc_fr)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = priceMillimesText,
                            onValueChange = { priceMillimesText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(stringResource(R.string.admin_price_millimes)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text(stringResource(R.string.admin_image_url)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PrimaryGradientButton(
                                text = stringResource(R.string.admin_save),
                                onClick = {
                                    val price = priceMillimesText.toIntOrNull() ?: 15000
                                    if (nameFr.isNotBlank()) {
                                        val base = editingProduct
                                        val saved = Product(
                                            id = base?.id ?: "prod-${UUID.randomUUID().toString().take(6)}",
                                            nameFr = nameFr.trim(),
                                            nameAr = nameAr.trim().ifBlank { nameFr.trim() },
                                            nameEn = nameEn.trim().ifBlank { nameFr.trim() },
                                            descriptionFr = descFr.trim(),
                                            descriptionAr = base?.descriptionAr ?: descFr.trim(),
                                            descriptionEn = base?.descriptionEn ?: descFr.trim(),
                                            priceMillimes = price,
                                            imageUrl = imageUrl.trim(),
                                            categoryId = base?.categoryId ?: categories.firstOrNull()?.id ?: "cat-pizza",
                                            isAvailable = true,
                                            isFeatured = true
                                        )
                                        onSaveProduct(saved)
                                        showForm = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                height = 44.dp
                            )
                        }
                    }
                }
            }
        }

        items(products, key = { it.id }) { product ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FoodNetworkImage(
                        imageUrl = product.imageUrl,
                        contentDescription = product.localizedName(lang),
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.localizedName(lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = CurrencyFormatter.formatMillimes(product.priceMillimes, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestGreenMid
                        )
                    }
                    Switch(
                        checked = product.isAvailable,
                        onCheckedChange = { onToggleAvailability(product.id) }
                    )
                    IconButton(onClick = {
                        editingProduct = product
                        nameFr = product.nameFr
                        nameAr = product.nameAr
                        nameEn = product.nameEn
                        descFr = product.descriptionFr
                        priceMillimesText = product.priceMillimes.toString()
                        imageUrl = product.imageUrl
                        showForm = true
                    }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = ForestGreenDark)
                    }
                    IconButton(onClick = { onDeleteProduct(product.id) }) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = ErrorCrimson)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoriesTab(
    lang: AppLanguage,
    categories: List<Category>,
    onSaveCategory: (Category) -> Unit,
    onToggleActive: (String) -> Unit
) {
    var nameFr by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.admin_add_category),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = nameFr,
                            onValueChange = { nameFr = it },
                            placeholder = { Text("FR") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = nameAr,
                            onValueChange = { nameAr = it },
                            placeholder = { Text("AR") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it },
                            placeholder = { Text("EN") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    PrimaryGradientButton(
                        text = stringResource(R.string.admin_save),
                        onClick = {
                            if (nameFr.isNotBlank()) {
                                onSaveCategory(
                                    Category(
                                        id = "cat-${UUID.randomUUID().toString().take(6)}",
                                        nameFr = nameFr.trim(),
                                        nameAr = nameAr.trim().ifBlank { nameFr.trim() },
                                        nameEn = nameEn.trim().ifBlank { nameFr.trim() },
                                        imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=500&q=80",
                                        isActive = true,
                                        sortOrder = categories.size + 1
                                    )
                                )
                                nameFr = ""
                                nameAr = ""
                                nameEn = ""
                            }
                        },
                        height = 42.dp
                    )
                }
            }
        }

        items(categories, key = { it.id }) { cat ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${cat.nameFr} / ${cat.nameAr} / ${cat.nameEn}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = cat.isActive,
                        onCheckedChange = { onToggleActive(cat.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminRestaurantSettingsTab(
    settings: RestaurantSettings,
    onSaveSettings: (RestaurantSettings) -> Unit
) {
    var name by remember(settings) { mutableStateOf(settings.name) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var deliveryFee by remember(settings) { mutableStateOf(settings.deliveryFeeMillimes.toString()) }
    var minOrder by remember(settings) { mutableStateOf(settings.minimumOrderMillimes.toString()) }
    var estMinutes by remember(settings) { mutableStateOf(settings.estimatedDeliveryMinutes.toString()) }
    var isOpen by remember(settings) { mutableStateOf(settings.isOpen) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.admin_Toggle_open),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isOpen,
                            onCheckedChange = {
                                isOpen = it
                                onSaveSettings(settings.copy(isOpen = it))
                            },
                            modifier = Modifier.testTag("admin_restaurant_open_switch")
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Restaurant Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.contact_phone_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(stringResource(R.string.contact_address_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deliveryFee,
                        onValueChange = { deliveryFee = it.filter { ch -> ch.isDigit() } },
                        label = { Text("${stringResource(R.string.delivery_fee_label)} (millimes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minOrder,
                        onValueChange = { minOrder = it.filter { ch -> ch.isDigit() } },
                        label = { Text("${stringResource(R.string.min_order_label)} (millimes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estMinutes,
                        onValueChange = { estMinutes = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Estimated Delivery (min)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    PrimaryGradientButton(
                        text = stringResource(R.string.admin_save),
                        onClick = {
                            onSaveSettings(
                                settings.copy(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    address = address.trim(),
                                    deliveryFeeMillimes = deliveryFee.toIntOrNull() ?: settings.deliveryFeeMillimes,
                                    minimumOrderMillimes = minOrder.toIntOrNull() ?: settings.minimumOrderMillimes,
                                    estimatedDeliveryMinutes = estMinutes.toIntOrNull() ?: settings.estimatedDeliveryMinutes,
                                    isOpen = isOpen
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
