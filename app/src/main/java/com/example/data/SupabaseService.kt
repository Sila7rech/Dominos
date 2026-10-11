package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// DTOs matching the exact Supabase PostgreSQL schema in Section 8
data class CategoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "name_fr") val nameFr: String,
    @Json(name = "name_ar") val nameAr: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "sort_order") val sortOrder: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null
)

data class ProductDto(
    @Json(name = "id") val id: String,
    @Json(name = "name_fr") val nameFr: String,
    @Json(name = "name_ar") val nameAr: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "description_fr") val descriptionFr: String,
    @Json(name = "description_ar") val descriptionAr: String,
    @Json(name = "description_en") val descriptionEn: String,
    @Json(name = "price_millimes") val priceMillimes: Int,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "is_available") val isAvailable: Boolean = true,
    @Json(name = "is_featured") val isFeatured: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

data class RestaurantSettingsDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "logo_url") val logoUrl: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "address") val address: String,
    @Json(name = "delivery_fee_millimes") val deliveryFeeMillimes: Int,
    @Json(name = "minimum_order_millimes") val minimumOrderMillimes: Int,
    @Json(name = "is_open") val isOpen: Boolean,
    @Json(name = "estimated_delivery_minutes") val estimatedDeliveryMinutes: Int,
    @Json(name = "currency") val currency: String = "TND"
)

data class ProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "phone") val phone: String?,
    @Json(name = "preferred_language") val preferredLanguage: String = "fr",
    @Json(name = "role") val role: String = "customer",
    @Json(name = "created_at") val createdAt: String? = null
)

data class OrderDto(
    @Json(name = "id") val id: String,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "user_id") val userId: String?,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "delivery_address") val deliveryAddress: String,
    @Json(name = "delivery_instructions") val deliveryInstructions: String?,
    @Json(name = "subtotal_millimes") val subtotalMillimes: Int,
    @Json(name = "delivery_fee_millimes") val deliveryFeeMillimes: Int,
    @Json(name = "discount_millimes") val discountMillimes: Int,
    @Json(name = "total_millimes") val totalMillimes: Int,
    @Json(name = "payment_method") val paymentMethod: String = "cash_on_delivery",
    @Json(name = "payment_status") val paymentStatus: String = "pending_cod",
    @Json(name = "status") val status: String = "pending",
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

data class CreateVerifiedOrderRpcRequest(
    @Json(name = "p_customer_name") val customerName: String,
    @Json(name = "p_customer_phone") val customerPhone: String,
    @Json(name = "p_delivery_address") val deliveryAddress: String,
    @Json(name = "p_delivery_instructions") val deliveryInstructions: String,
    @Json(name = "p_promo_code") val promoCode: String?,
    @Json(name = "p_items") val items: List<OrderItemRpcInput>
)

data class OrderItemRpcInput(
    @Json(name = "product_id") val productId: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "selected_options") val selectedOptions: String
)

data class SupabaseAuthRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

data class SupabaseAuthUserDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String?
)

data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "user") val user: SupabaseAuthUserDto?
)

interface SupabaseRestApi {
    @POST("auth/v1/token?grant_type=password")
    suspend fun signInWithPassword(@Body request: SupabaseAuthRequest): Response<SupabaseAuthResponse>

    @POST("auth/v1/signup")
    suspend fun signUp(@Body request: SupabaseAuthRequest): Response<SupabaseAuthResponse>

    @GET("rest/v1/categories?select=*&order=sort_order.asc")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("rest/v1/products?select=*&order=created_at.desc")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("rest/v1/restaurant_settings?select=*&limit=1")
    suspend fun getRestaurantSettings(): Response<List<RestaurantSettingsDto>>

    @GET("rest/v1/profiles?select=*")
    suspend fun getProfile(@Query("id") idFilter: String): Response<List<ProfileDto>>

    @GET("rest/v1/orders?select=*&order=created_at.desc")
    suspend fun getOrders(): Response<List<OrderDto>>

    @POST("rest/v1/rpc/create_verified_order")
    suspend fun createVerifiedOrder(
        @Body request: CreateVerifiedOrderRpcRequest
    ): Response<OrderDto>

    @PATCH("rest/v1/orders")
    suspend fun updateOrderStatus(
        @Query("id") idFilter: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body statusUpdate: Map<String, String>
    ): Response<List<OrderDto>>

    @POST("rest/v1/products")
    suspend fun upsertProduct(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body product: ProductDto
    ): Response<List<ProductDto>>

    @DELETE("rest/v1/products")
    suspend fun deleteProduct(
        @Query("id") idFilter: String
    ): Response<Unit>

    @POST("rest/v1/categories")
    suspend fun upsertCategory(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body category: CategoryDto
    ): Response<List<CategoryDto>>

    @PATCH("rest/v1/restaurant_settings")
    suspend fun updateRestaurantSettings(
        @Query("id") idFilter: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body settings: RestaurantSettingsDto
    ): Response<List<RestaurantSettingsDto>>
}

object SupabaseClientProvider {
    @Volatile
    var currentAccessToken: String? = null

    val isSupabaseConfigured: Boolean
        get() = SupabaseClient.isConfigured

    val api: SupabaseRestApi? by lazy {
        if (!isSupabaseConfigured) return@lazy null
        val baseUrl = SupabaseClient.supabaseUrl.trimEnd('/') + "/"
        val anonKey = SupabaseClient.supabaseAnonKey

        val authInterceptor = Interceptor { chain ->
            val token = currentAccessToken ?: anonKey
            val request = chain.request().newBuilder()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseRestApi::class.java)
    }
}
