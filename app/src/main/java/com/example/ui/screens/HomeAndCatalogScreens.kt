package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.CatalogSortOption
import com.example.model.Category
import com.example.model.CurrencyFormatter
import com.example.model.Product
import com.example.model.ProductExtra
import com.example.model.ProductVariant
import com.example.model.PromoDeal
import com.example.model.RestaurantSettings
import com.example.ui.components.CategoryChipCard
import com.example.ui.components.DealPromoCard
import com.example.ui.components.DominosSearchBar
import com.example.ui.components.DominosSubScreenTopBar
import com.example.ui.components.FoodNetworkImage
import com.example.ui.components.GreenDeliveryHeader
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.QuantitySelector
import com.example.ui.components.ScreenshotProductGridCard
import com.example.ui.components.SupabaseTableSetupCard
import com.example.ui.theme.CardPureWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DominosBlue
import com.example.ui.theme.DominosRed
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenMid
import com.example.ui.theme.InputSurfaceSage
import com.example.ui.theme.LimeGreenBorder
import com.example.ui.theme.LimeGreenBright
import com.example.ui.theme.LimeGreenSoft
import com.example.ui.theme.NotificationDotRed
import com.example.ui.theme.PromoRibbonRed
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextMutedOlive

/**
 * Home & Product Catalog Screen in Domino's Blue & Crimson Red theme,
 * with live GPS location button and Supabase Table Setup Assistant.
 */
@Composable
fun HomeAndCatalogScreen(
    lang: AppLanguage,
    isOnlineSupabase: Boolean,
    supabaseTablesMissing: Boolean = false,
    restaurantSettings: RestaurantSettings,
    addressLabel: String,
    deliveryAddress: String,
    categories: List<Category>,
    selectedCategoryId: String?,
    products: List<Product>,
    allProducts: List<Product>,
    deals: List<PromoDeal>,
    appliedPromoCode: String?,
    favoriteIds: Set<String>,
    searchQuery: String,
    sortOption: CatalogSortOption,
    cartItemCount: Int,
    onSelectCategory: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (CatalogSortOption) -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAddToCart: (Product) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onApplyDeal: (String) -> Unit,
    onGpsLocationResolved: (String) -> Unit = {},
    onSeedSupabaseTables: ((Boolean) -> Unit) -> Unit = {},
    onCartShortcutClick: () -> Unit,
    onProfileShortcutClick: () -> Unit
) {
    val activeCategories = remember(categories) {
        categories.filter { it.isActive }.sortedBy { it.sortOrder }
    }
    val featuredOrderAgain = remember(allProducts) {
        allProducts.filter { it.isFeatured && it.isAvailable }.take(4)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Curved Domino's Royal Blue Delivery Header + Live GPS Button
        item {
            GreenDeliveryHeader(
                addressLabel = addressLabel,
                deliveryAddress = deliveryAddress,
                isOnlineSupabase = isOnlineSupabase,
                cartItemCount = cartItemCount,
                onAddressClick = onProfileShortcutClick,
                onGpsLocationResolved = onGpsLocationResolved,
                onCartClick = onCartShortcutClick,
                onProfileClick = onProfileShortcutClick,
                bottomContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.22f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (restaurantSettings.isOpen) Color(0xFF4ADE80) else ErrorCrimson)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (restaurantSettings.isOpen) {
                                    "${restaurantSettings.name} • ${stringResource(R.string.status_open)}"
                                } else {
                                    "${restaurantSettings.name} • ${stringResource(R.string.status_closed)}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${stringResource(R.string.delivery_est_format, restaurantSettings.estimatedDeliveryMinutes)} • ${
                                CurrencyFormatter.formatMillimes(restaurantSettings.deliveryFeeMillimes, lang)
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            )
        }

        // If Supabase tables are missing, show 1-click SQL setup & seed assistant banner
        if (supabaseTablesMissing) {
            item {
                SupabaseTableSetupCard(
                    onSeedTablesClick = onSeedSupabaseTables,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        // 2. Domino's Blue & Crimson Red Promotional Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF003B57),
                                Color(0xFF006491),
                                Color(0xFFE31837)
                            )
                        )
                    )
                    .clickable {
                        onApplyDeal("FREESHIP")
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_dominos_launcher_1791739912765),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(106.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.promo_banner_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.promo_banner_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PromoRibbonRed)
                                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.promo_order_now),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 3. Search Bar
        item {
            DominosSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Categories Section
        item {
            SectionHeaderRow(
                title = stringResource(R.string.section_categories),
                actionLabel = stringResource(R.string.action_see_all),
                onActionClick = { onSelectCategory(null) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    CategoryChipCard(
                        label = stringResource(R.string.category_all),
                        imageUrl = null,
                        isSelected = selectedCategoryId == null,
                        onClick = { onSelectCategory(null) },
                        modifier = Modifier.testTag("category_chip_all")
                    )
                }
                items(activeCategories, key = { it.id }) { category ->
                    CategoryChipCard(
                        label = category.localizedName(lang),
                        imageUrl = category.imageUrl,
                        isSelected = selectedCategoryId == category.id,
                        onClick = {
                            onSelectCategory(if (selectedCategoryId == category.id) null else category.id)
                        },
                        modifier = Modifier.testTag("category_chip_${category.id}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. "Order again" / Featured Section
        if (featuredOrderAgain.isNotEmpty() && searchQuery.isBlank() && selectedCategoryId == null) {
            item {
                SectionHeaderRow(
                    title = stringResource(R.string.section_order_again),
                    actionLabel = null,
                    onActionClick = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredOrderAgain, key = { "again_${it.id}" }) { meal ->
                        OrderAgainCompactCard(
                            product = meal,
                            lang = lang,
                            onCardClick = { onProductClick(meal) },
                            onRepeatClick = { onQuickAddToCart(meal) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // 6. "Deals for you" Section
            item {
                SectionHeaderRow(
                    title = stringResource(R.string.section_deals_for_you),
                    actionLabel = stringResource(R.string.action_see_all),
                    onActionClick = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deals, key = { it.code }) { deal ->
                        val isApplied = appliedPromoCode.equals(deal.code, ignoreCase = true)
                        DealPromoCard(
                            deal = deal,
                            lang = lang,
                            isApplied = isApplied,
                            onApplyClick = {
                                onApplyDeal(if (isApplied) "" else deal.code)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // 7. Product Catalog Header & Sort Filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_popular_products),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextCharcoal,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${stringResource(R.string.min_order_label)}: ${
                        CurrencyFormatter.formatMillimes(restaurantSettings.minimumOrderMillimes, lang)
                    }",
                    style = MaterialTheme.typography.labelMedium,
                    color = DominosBlue
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogSortOption.entries.forEach { option ->
                    val selected = sortOption == option
                    val label = when (option) {
                        CatalogSortOption.POPULAR -> stringResource(R.string.sort_popular)
                        CatalogSortOption.PRICE_ASC -> stringResource(R.string.sort_price_asc)
                        CatalogSortOption.PRICE_DESC -> stringResource(R.string.sort_price_desc)
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { onSortOptionChange(option) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeGreenSoft,
                            selectedLabelColor = DominosBlue
                        )
                    )
                }
            }
        }

        // 8. 2-Column Product Grid
        if (products.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = TextMutedOlive,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.product_unavailable),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMutedOlive
                        )
                    }
                }
            }
        } else {
            val chunkedRows = products.chunked(2)
            items(chunkedRows.size, key = { idx -> "row_$idx" }) { rowIndex ->
                val pair = chunkedRows[rowIndex]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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

@Composable
private fun SectionHeaderRow(
    title: String,
    actionLabel: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextCharcoal,
            fontWeight = FontWeight.ExtraBold
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = DominosRed,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
private fun OrderAgainCompactCard(
    product: Product,
    lang: AppLanguage,
    onCardClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardPureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.width(248.dp)
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
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.localizedName(lang),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextCharcoal,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${stringResource(R.string.yesterday_label)} · ${
                        CurrencyFormatter.formatMillimes(product.priceMillimes, lang)
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedOlive
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(LimeGreenSoft)
                    .clickable(onClick = onRepeatClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = stringResource(R.string.add_to_cart_btn),
                    tint = DominosBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProductDetailsScreen(
    product: Product,
    lang: AppLanguage,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAddToCart: (Int, ProductVariant?, List<ProductExtra>, String) -> Unit,
    onBack: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedVariant by remember(product.id) {
        mutableStateOf(product.variants.firstOrNull())
    }
    val selectedExtras = remember(product.id) {
        mutableStateListOf<ProductExtra>()
    }
    var specialInstructions by remember { mutableStateOf("") }

    val unitPriceMillimes = product.priceMillimes +
        (selectedVariant?.priceDeltaMillimes ?: 0) +
        selectedExtras.sumOf { it.priceMillimes }
    val calculatedTotalMillimes = unitPriceMillimes * quantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        DominosSubScreenTopBar(
            title = stringResource(R.string.product_details_title),
            onBack = onBack,
            trailingAction = {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.nav_saved),
                        tint = if (isFavorite) NotificationDotRed else TextCharcoal
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Large Product Image Card
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CardPureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FoodNetworkImage(
                        imageUrl = product.imageUrl,
                        contentDescription = product.localizedName(lang),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.localizedName(lang),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextCharcoal,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = CurrencyFormatter.formatMillimes(product.priceMillimes, lang),
                            style = MaterialTheme.typography.titleLarge,
                            color = DominosBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calories & Prep Time Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = FlameOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.calories_format, product.calories),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextCharcoal
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TextMutedOlive,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.delivery_est_format, product.prepMinutes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedOlive
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = product.localizedDescription(lang),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMutedOlive,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Optional Variants
            if (product.variants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_size_variant),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.variants.forEach { variant ->
                        val isSelected = selectedVariant?.id == variant.id
                        Surface(
                            color = if (isSelected) LimeGreenSoft else CardPureWhite,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) DominosBlue else Color(0xFFDCE6F0),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedVariant = variant }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = variant.localizedName(lang),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = TextCharcoal
                                )
                                Text(
                                    text = if (variant.priceDeltaMillimes > 0) {
                                        "+${CurrencyFormatter.formatMillimes(variant.priceDeltaMillimes, lang)}"
                                    } else {
                                        stringResource(R.string.free_label)
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = DominosBlue
                                )
                            }
                        }
                    }
                }
            }

            // Optional Extras
            if (product.extras.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_extras),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.extras.forEach { extra ->
                        val isChecked = selectedExtras.any { it.id == extra.id }
                        Surface(
                            color = if (isChecked) LimeGreenSoft else CardPureWhite,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isChecked) DominosBlue else Color(0xFFDCE6F0),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (isChecked) {
                                        selectedExtras.removeAll { it.id == extra.id }
                                    } else {
                                        selectedExtras.add(extra)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isChecked) LimeGreenBright else InputSurfaceSage),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = extra.localizedName(lang),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextCharcoal
                                    )
                                }
                                Text(
                                    text = "+${CurrencyFormatter.formatMillimes(extra.priceMillimes, lang)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = DominosBlue
                                )
                            }
                        }
                    }
                }
            }

            // Special Instructions
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.special_instructions_label),
                style = MaterialTheme.typography.titleMedium,
                color = TextCharcoal
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = specialInstructions,
                onValueChange = { specialInstructions = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.special_instructions_hint),
                        color = TextMutedOlive
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardPureWhite,
                    unfocusedContainerColor = CardPureWhite,
                    focusedBorderColor = LimeGreenBorder,
                    unfocusedBorderColor = Color(0xFFDCE6F0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("details_instructions_input")
            )
        }

        // Bottom Bar: Quantity Selector + Calculated Total + Add to Cart Button
        Surface(
            color = CardPureWhite,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.total_price_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedOlive
                        )
                        Text(
                            text = CurrencyFormatter.formatMillimes(calculatedTotalMillimes, lang),
                            style = MaterialTheme.typography.headlineMedium,
                            color = ForestGreenDark,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("details_calculated_total")
                        )
                    }

                    QuantitySelector(
                        quantity = quantity,
                        onDecrement = { if (quantity > 1) quantity-- },
                        onIncrement = { quantity++ }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryGradientButton(
                    text = if (product.isAvailable) {
                        "${stringResource(R.string.add_to_cart_btn)} • ${
                            CurrencyFormatter.formatMillimes(calculatedTotalMillimes, lang)
                        }"
                    } else {
                        stringResource(R.string.product_unavailable)
                    },
                    enabled = product.isAvailable,
                    onClick = {
                        onAddToCart(
                            quantity,
                            selectedVariant,
                            selectedExtras.toList(),
                            specialInstructions
                        )
                    },
                    modifier = Modifier.testTag("details_add_to_cart_button")
                )
            }
        }
    }
}
