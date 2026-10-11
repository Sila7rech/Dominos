package com.example.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.SupabaseSchemaHelper
import com.example.model.AppLanguage
import com.example.model.CurrencyFormatter
import com.example.model.Product
import com.example.model.PromoDeal
import com.example.ui.theme.CardPureWhite
import com.example.ui.theme.DealCardDark
import com.example.ui.theme.DealCardOverlayGradient
import com.example.ui.theme.DominosBlue
import com.example.ui.theme.DominosRed
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenMid
import com.example.ui.theme.HeaderGreenGradient
import com.example.ui.theme.InputSurfaceSage
import com.example.ui.theme.LimeGreenBorder
import com.example.ui.theme.LimeGreenBright
import com.example.ui.theme.LimeGreenSoft
import com.example.ui.theme.NotificationDotRed
import com.example.ui.theme.PrimaryButtonGradient
import com.example.ui.theme.SelectedCategoryGradient
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextMutedOlive
import com.example.util.LocationHelper
import kotlinx.coroutines.launch

/**
 * Two-tone Domino's Brand Logo ("Domi" in Domino's Blue/White + "nos." in Domino's Red)
 */
@Composable
fun DominosBrandLogo(
    fontSize: TextUnit = 34.sp,
    lightOnDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val firstColor = if (lightOnDark) Color.White else DominosBlue
    val secondColor = DominosRed
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = firstColor, fontWeight = FontWeight.ExtraBold)) {
                append(stringResource(R.string.brand_prefix))
            }
            withStyle(SpanStyle(color = secondColor, fontWeight = FontWeight.ExtraBold)) {
                append(stringResource(R.string.brand_suffix))
            }
            withStyle(SpanStyle(color = firstColor, fontWeight = FontWeight.ExtraBold)) {
                append(".")
            }
        },
        fontSize = fontSize,
        modifier = modifier
    )
}

/**
 * Reusable GPS Location Action Button that requests runtime location permission
 * and resolves the user's current GPS coordinates & street address.
 */
@Composable
fun GpsLocationChipButton(
    onLocationResolved: (String) -> Unit,
    modifier: Modifier = Modifier,
    compactOnDarkHeader: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLocating by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLocating = true
            scope.launch {
                val res = LocationHelper.fetchCurrentGpsAddress(context)
                isLocating = false
                if (res != null) {
                    onLocationResolved(res.formattedAddress)
                }
            }
        } else {
            // Even if denied on emulator, provide fallback Tunis GPS coordinates so the user can test
            onLocationResolved("Avenue Habib Bourguiba, Tunis • GPS (36.8065, 10.1815)")
        }
    }

    val onClickGps: () -> Unit = {
        if (LocationHelper.hasLocationPermission(context)) {
            isLocating = true
            scope.launch {
                val res = LocationHelper.fetchCurrentGpsAddress(context)
                isLocating = false
                if (res != null) {
                    onLocationResolved(res.formattedAddress)
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (compactOnDarkHeader) {
        Surface(
            color = DominosRed,
            shape = RoundedCornerShape(50),
            modifier = modifier
                .clickable(onClick = onClickGps)
                .testTag("header_gps_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    } else {
        Surface(
            color = LimeGreenSoft,
            shape = RoundedCornerShape(14.dp),
            modifier = modifier
                .border(1.dp, DominosBlue, RoundedCornerShape(14.dp))
                .clickable(onClick = onClickGps)
                .testTag("gps_locate_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLocating) {
                    CircularProgressIndicator(
                        color = DominosBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS",
                        tint = DominosRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLocating) "Localisation GPS en cours…" else "📍 Utiliser ma position GPS actuelle",
                    style = MaterialTheme.typography.labelLarge,
                    color = DominosBlue,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Interactive Supabase Table Setup Assistant Banner & Dialog.
 * Allows the user to copy the complete SQL creation script in 1 tap and auto-seed
 * all initial categories, products, and restaurant settings into their Supabase project.
 */
@Composable
fun SupabaseTableSetupCard(
    onSeedTablesClick: ((Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSqlDialog by remember { mutableStateOf(false) }
    var copiedToast by remember { mutableStateOf(false) }
    var seedStatus by remember { mutableStateOf<String?>(null) }
    var isSeeding by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FA)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DominosBlue, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = DominosBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Assistant Base de Données Supabase (Tables & Menu)",
                    style = MaterialTheme.typography.titleSmall,
                    color = DominosBlue,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "Aucune table dans votre projet Supabase ? Copiez le script SQL ci-dessous en 1 clic, collez-le dans Supabase -> SQL Editor -> Run, puis cliquez sur 'Remplir les tables' !",
                style = MaterialTheme.typography.bodySmall,
                color = TextCharcoal
            )
            if (copiedToast) {
                Text(
                    text = "✅ Script SQL copié dans le presse-papiers ! Collez-le dans Supabase SQL Editor.",
                    style = MaterialTheme.typography.labelMedium,
                    color = SuccessEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
            if (seedStatus != null) {
                Text(
                    text = seedStatus!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = DominosBlue,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("Supabase SQL Schema", SupabaseSchemaHelper.FULL_SUPABASE_SQL_SCRIPT)
                        )
                        copiedToast = true
                        showSqlDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copier SQL", style = MaterialTheme.typography.labelMedium)
                }

                PrimaryGradientButton(
                    text = if (isSeeding) "Envoi…" else "Remplir les tables",
                    enabled = !isSeeding,
                    onClick = {
                        isSeeding = true
                        onSeedTablesClick { ok ->
                            isSeeding = false
                            seedStatus = if (ok) {
                                "✅ Menu et catégories envoyés dans Supabase avec succès !"
                            } else {
                                "⚠️ Exécutez d'abord le script SQL copié dans Supabase SQL Editor, puis réessayez."
                            }
                        }
                    },
                    height = 40.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showSqlDialog) {
        AlertDialog(
            onDismissRequest = { showSqlDialog = false },
            title = {
                Text(
                    text = "Créer les tables dans Supabase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Le script SQL complet (tables profiles, categories, products, restaurant_settings, orders, order_items, addresses, favorites + RLS) a été copié dans votre presse-papiers !\n\n1. Ouvrez votre projet sur supabase.com/dashboard\n2. Cliquez sur 'SQL Editor' -> 'New Query'\n3. Collez le script et cliquez sur 'Run'\n4. Revenez ici et appuyez sur 'Remplir les tables'.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://supabase.com/dashboard"))
                            )
                        }
                        showSqlDialog = false
                    }
                ) {
                    Text("Ouvrir Supabase Dashboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSqlDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

/**
 * External HTTPS image loader using Coil with loading skeleton and local bundled fallback image.
 */
@Composable
fun FoodNetworkImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    if (imageUrl.isBlank() || !imageUrl.startsWith("http")) {
        Image(
            painter = painterResource(id = R.drawable.img_food_fallback_1791739940588),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(InputSurfaceSage),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DominosBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            error = {
                Image(
                    painter = painterResource(id = R.drawable.img_food_fallback_1791739940588),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )
    }
}

/**
 * Top Curved Domino's Royal Blue Header with Live GPS Button, Avatar, Address, and Cart Badge
 */
@Composable
fun GreenDeliveryHeader(
    addressLabel: String,
    deliveryAddress: String,
    isOnlineSupabase: Boolean,
    cartItemCount: Int,
    onAddressClick: () -> Unit,
    onGpsLocationResolved: (String) -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(HeaderGreenGradient)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Top Row: Mode Status Badge + Live GPS Quick Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.25f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("mode_status_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isOnlineSupabase) Color(0xFF4ADE80) else Color(0xFFFFCC00))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnlineSupabase) {
                            stringResource(R.string.supabase_online_badge)
                        } else {
                            stringResource(R.string.demo_mode_badge)
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            GpsLocationChipButton(
                onLocationResolved = onGpsLocationResolved,
                compactOnDarkHeader = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Domino's Logo Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable(onClick = onProfileClick)
                    .testTag("header_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dominos_launcher_1791739912765),
                    contentDescription = stringResource(R.string.nav_profile),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // "Deliver to" + Address
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onAddressClick)
                    .testTag("header_address_selector")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DominosRed,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.deliver_to),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = addressLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = deliveryAddress,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cart shortcut with item count badge
            IconButton(
                onClick = onCartClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .testTag("header_cart_shortcut")
            ) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = DominosRed,
                                contentColor = Color.White
                            ) {
                                Text(cartItemCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = stringResource(R.string.nav_cart),
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Notification Bell
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = stringResource(R.string.profile_notifications_title),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(DominosRed)
                )
            }
        }

        if (bottomContent != null) {
            Spacer(modifier = Modifier.height(12.dp))
            bottomContent()
        }
    }
}

@Composable
fun DominosSubScreenTopBar(
    title: String,
    onBack: () -> Unit,
    trailingAction: (@Composable () -> Unit)? = null
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Surface(
        color = CardPureWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_bar_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.admin_cancel),
                    tint = TextCharcoal,
                    modifier = if (isRtl) Modifier.scale(scaleX = -1f, scaleY = 1f) else Modifier
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextCharcoal,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            if (trailingAction != null) {
                trailingAction()
            }
        }
    }
}

@Composable
fun CategoryChipCard(
    label: String,
    imageUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (isSelected) {
        modifier
            .width(82.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SelectedCategoryGradient)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    } else {
        modifier
            .width(82.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardPureWhite)
            .border(1.dp, Color(0xFFDCE6F0), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    }

    Column(
        modifier = cardModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.White.copy(alpha = 0.22f) else InputSurfaceSage),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                FoodNetworkImage(
                    imageUrl = imageUrl,
                    contentDescription = label,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_food_fallback_1791739940588),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextCharcoal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScreenshotProductGridCard(
    product: Product,
    lang: AppLanguage,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onQuickAddClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardPureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!product.isAvailable) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.product_unavailable),
                            color = Color(0xFFB91C1C),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (product.isFeatured) {
                    Surface(
                        color = LimeGreenSoft,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.product_featured),
                            color = DominosBlue,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("fav_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.nav_saved),
                        tint = if (isFavorite) DominosRed else TextMutedOlive,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = product.localizedName(lang),
                style = MaterialTheme.typography.titleMedium,
                color = TextCharcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = CurrencyFormatter.formatMillimes(product.priceMillimes, lang),
                style = MaterialTheme.typography.bodyMedium,
                color = DominosBlue,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            FoodNetworkImage(
                imageUrl = product.imageUrl,
                contentDescription = product.localizedName(lang),
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.calories_format, product.calories),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextCharcoal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextMutedOlive,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.delivery_est_format, product.prepMinutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedOlive
                        )
                    }
                }

                // Domino's Crimson Red '+' Button with White '+' Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (product.isAvailable) DominosRed else Color.LightGray)
                        .clickable(enabled = product.isAvailable, onClick = onQuickAddClick)
                        .testTag("add_to_cart_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_to_cart_btn),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DealPromoCard(
    deal: PromoDeal,
    lang: AppLanguage,
    isApplied: Boolean,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(265.dp)
            .height(136.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DealCardDark)
    ) {
        FoodNetworkImage(
            imageUrl = deal.imageUrl,
            contentDescription = deal.localizedHeadline(lang),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(140.dp)
                .height(136.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DealCardOverlayGradient)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = deal.localizedBadge(lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = deal.localizedHeadline(lang),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = deal.code,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isApplied) SuccessEmerald else DominosRed)
                            .clickable(onClick = onApplyClick)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("apply_deal_${deal.code}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isApplied) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isApplied) {
                                    stringResource(R.string.applied_btn)
                                } else {
                                    stringResource(R.string.apply_btn)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 54.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = if (enabled) {
                    PrimaryButtonGradient
                } else {
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color.Gray, Color.LightGray)
                    )
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DominosSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedOlive
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = DominosBlue
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(26.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardPureWhite,
            unfocusedContainerColor = CardPureWhite,
            focusedBorderColor = DominosBlue,
            unfocusedBorderColor = Color(0xFFDCE6F0),
            focusedTextColor = TextCharcoal,
            unfocusedTextColor = TextCharcoal
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("catalog_search_input")
    )
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(InputSurfaceSage)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(36.dp)
                .testTag("qty_minus_button")
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "-",
                tint = TextCharcoal,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = TextCharcoal,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DominosRed)
                .clickable(onClick = onIncrement)
                .testTag("qty_plus_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "+",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

enum class BottomNavTab {
    HOME,
    SAVED,
    CART,
    OFFERS,
    PROFILE
}

@Composable
fun DominosBottomNavigationBar(
    currentTab: BottomNavTab,
    cartCount: Int,
    onSelectTab: (BottomNavTab) -> Unit
) {
    Surface(
        color = CardPureWhite,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val label = when (tab) {
                    BottomNavTab.HOME -> stringResource(R.string.nav_home)
                    BottomNavTab.SAVED -> stringResource(R.string.nav_saved)
                    BottomNavTab.CART -> stringResource(R.string.nav_cart)
                    BottomNavTab.OFFERS -> stringResource(R.string.nav_offers)
                    BottomNavTab.PROFILE -> stringResource(R.string.nav_profile)
                }
                val icon = when (tab) {
                    BottomNavTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                    BottomNavTab.SAVED -> if (isSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                    BottomNavTab.CART -> if (isSelected) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag
                    BottomNavTab.OFFERS -> if (isSelected) Icons.Filled.LocalOffer else Icons.Outlined.LocalOffer
                    BottomNavTab.PROFILE -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clickable { onSelectTab(tab) }
                        .testTag("bottom_nav_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(if (isSelected) DominosRed else Color.Transparent)
                    )

                    BadgedBox(
                        badge = {
                            if (tab == BottomNavTab.CART && cartCount > 0) {
                                Badge(
                                    containerColor = DominosRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = cartCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) DominosBlue else TextMutedOlive,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DominosBlue else TextMutedOlive,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}
