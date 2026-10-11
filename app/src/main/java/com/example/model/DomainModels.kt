package com.example.model

import java.util.Locale

enum class AppLanguage(val code: String, val nativeLabel: String, val isRtl: Boolean) {
    FR("fr", "Français", false),
    AR("ar", "العربية", true),
    EN("en", "English", false);

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FR
        }
    }
}

enum class UserRole(val dbValue: String) {
    CUSTOMER("customer"),
    ADMIN("admin");

    companion object {
        fun fromString(value: String?): UserRole {
            return if (value.equals("admin", ignoreCase = true)) ADMIN else CUSTOMER
        }
    }
}

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val phone: String,
    val preferredLanguage: AppLanguage = AppLanguage.FR,
    val role: UserRole = UserRole.CUSTOMER,
    val createdAt: String = "2026-10-11T10:00:00Z"
)

data class Category(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val nameEn: String,
    val imageUrl: String,
    val emojiIcon: String = "🍕",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: String = "2026-10-11T10:00:00Z"
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> nameFr.ifBlank { nameEn }
        AppLanguage.AR -> nameAr.ifBlank { nameFr }
        AppLanguage.EN -> nameEn.ifBlank { nameFr }
    }
}

data class ProductVariant(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val nameEn: String,
    val priceDeltaMillimes: Int
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> nameFr
        AppLanguage.AR -> nameAr
        AppLanguage.EN -> nameEn
    }
}

data class ProductExtra(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val nameEn: String,
    val priceMillimes: Int
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> nameFr
        AppLanguage.AR -> nameAr
        AppLanguage.EN -> nameEn
    }
}

data class Product(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionFr: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val priceMillimes: Int,
    val imageUrl: String,
    val categoryId: String,
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val calories: Int = 320,
    val prepMinutes: Int = 20,
    val variants: List<ProductVariant> = emptyList(),
    val extras: List<ProductExtra> = emptyList(),
    val createdAt: String = "2026-10-11T10:00:00Z",
    val updatedAt: String = "2026-10-11T10:00:00Z"
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> nameFr.ifBlank { nameEn }
        AppLanguage.AR -> nameAr.ifBlank { nameFr }
        AppLanguage.EN -> nameEn.ifBlank { nameFr }
    }

    fun localizedDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> descriptionFr.ifBlank { descriptionEn }
        AppLanguage.AR -> descriptionAr.ifBlank { descriptionFr }
        AppLanguage.EN -> descriptionEn.ifBlank { descriptionFr }
    }
}

data class RestaurantSettings(
    val id: String = "rest-dominos-tunis-01",
    val name: String = "Dominos • HoumaFood",
    val logoUrl: String = "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=400&q=80",
    val phone: String = "+216 71 800 900",
    val address: String = "12 Avenue Habib Bourguiba, Centre Urbain Nord, Tunis",
    val deliveryFeeMillimes: Int = 4000, // 4,000 DT
    val minimumOrderMillimes: Int = 12000, // 12,000 DT
    val isOpen: Boolean = true,
    val estimatedDeliveryMinutes: Int = 25,
    val currency: String = "TND",
    val openingHoursJson: String = "Lun - Dim : 11h00 - 00h30",
    val whatsappUrl: String = "https://wa.me/21671800900"
)

data class CartItem(
    val id: String,
    val product: Product,
    val quantity: Int,
    val selectedVariant: ProductVariant? = null,
    val selectedExtras: List<ProductExtra> = emptyList(),
    val specialInstructions: String = ""
) {
    val unitPriceMillimes: Int
        get() = product.priceMillimes +
            (selectedVariant?.priceDeltaMillimes ?: 0) +
            selectedExtras.sumOf { it.priceMillimes }

    val lineTotalMillimes: Int
        get() = unitPriceMillimes * quantity

    fun optionsSummary(lang: AppLanguage): String {
        val parts = mutableListOf<String>()
        selectedVariant?.let { parts.add(it.localizedName(lang)) }
        if (selectedExtras.isNotEmpty()) {
            parts.add(selectedExtras.joinToString(", ") { "+${it.localizedName(lang)}" })
        }
        if (specialInstructions.isNotBlank()) {
            parts.add("\"${specialInstructions.trim()}\"")
        }
        return parts.joinToString(" • ")
    }
}

data class PromoDeal(
    val code: String,
    val badgeFr: String,
    val badgeAr: String,
    val badgeEn: String,
    val headlineFr: String,
    val headlineAr: String,
    val headlineEn: String,
    val discountPercent: Int,
    val isFreeDelivery: Boolean,
    val imageUrl: String
) {
    fun localizedBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> badgeFr
        AppLanguage.AR -> badgeAr
        AppLanguage.EN -> badgeEn
    }

    fun localizedHeadline(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> headlineFr
        AppLanguage.AR -> headlineAr
        AppLanguage.EN -> headlineEn
    }
}

enum class OrderStatus(val dbValue: String, val stepIndex: Int) {
    PENDING("pending", 0),
    CONFIRMED("confirmed", 1),
    PREPARING("preparing", 2),
    READY("ready", 3),
    OUT_FOR_DELIVERY("out_for_delivery", 4),
    DELIVERED("delivered", 5),
    CANCELLED("cancelled", -1);

    companion object {
        fun fromDbValue(value: String?): OrderStatus {
            return entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

data class OrderItemRecord(
    val id: String,
    val orderId: String,
    val productId: String,
    val productNameFr: String,
    val productNameAr: String,
    val productNameEn: String,
    val imageUrlSnapshot: String,
    val unitPriceMillimes: Int,
    val quantity: Int,
    val selectedOptionsSummary: String,
    val lineTotalMillimes: Int
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> productNameFr.ifBlank { productNameEn }
        AppLanguage.AR -> productNameAr.ifBlank { productNameFr }
        AppLanguage.EN -> productNameEn.ifBlank { productNameFr }
    }
}

data class OrderRecord(
    val id: String,
    val orderNumber: String,
    val userId: String?,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryInstructions: String,
    val subtotalMillimes: Int,
    val deliveryFeeMillimes: Int,
    val discountMillimes: Int,
    val totalMillimes: Int,
    val paymentMethod: String = "cash_on_delivery",
    val paymentStatus: String = "pending_cod",
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItemRecord> = emptyList(),
    val isLocalDemo: Boolean = true
)

data class SavedAddress(
    val id: String,
    val userId: String,
    val label: String,
    val address: String,
    val instructions: String = "",
    val isDefault: Boolean = false,
    val createdAt: String = "2026-10-11T10:00:00Z"
)

enum class CatalogSortOption {
    POPULAR,
    PRICE_ASC,
    PRICE_DESC
}

object CurrencyFormatter {
    /**
     * Formats integer millimes into Tunisian Dinars (TND).
     * Example: 18500 -> "18,500 DT" (or "18.500 DT").
     * Preserves Western Arabic numerals (0-9) even in RTL so prices are always readable.
     */
    fun formatMillimes(millimes: Int, lang: AppLanguage = AppLanguage.FR): String {
        val safeMillimes = millimes.coerceAtLeast(0)
        val dinars = safeMillimes / 1000
        val remMillimes = safeMillimes % 1000
        val formattedNumber = String.format(Locale.US, "%d,%03d", dinars, remMillimes)
        return when (lang) {
            AppLanguage.AR -> "\u200E$formattedNumber DT" // Left-to-Right mark preserves number order in RTL
            else -> "$formattedNumber DT"
        }
    }

    /**
     * Validates a Tunisian phone number:
     * Accepts 8 digits (e.g. 22123456, 55123456, 98123456, 71800900)
     * or +216 / 00216 followed by 8 digits (spaces/dashes allowed).
     */
    fun isValidTunisianPhone(rawPhone: String): Boolean {
        val digitsOnly = rawPhone.replace(Regex("[\\s\\-()]"), "")
        val normalized = when {
            digitsOnly.startsWith("+216") -> digitsOnly.removePrefix("+216")
            digitsOnly.startsWith("00216") -> digitsOnly.removePrefix("00216")
            digitsOnly.startsWith("216") && digitsOnly.length == 11 -> digitsOnly.removePrefix("216")
            else -> digitsOnly
        }
        return normalized.matches(Regex("^[234579]\\d{7}$"))
    }
}
