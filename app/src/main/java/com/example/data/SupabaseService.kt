package com.example.data

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

data class CategoryDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name_fr") val nameFr: String,
    @param:Json(name = "name_ar") val nameAr: String,
    @param:Json(name = "name_en") val nameEn: String,
    @param:Json(name = "image_url") val imageUrl: String,
    @param:Json(name = "is_active") val isActive: Boolean = true,
    @param:Json(name = "sort_order") val sortOrder: Int = 0,
    @param:Json(name = "created_at") val createdAt: String? = null
)

data class ProductDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name_fr") val nameFr: String,
    @param:Json(name = "name_ar") val nameAr: String,
    @param:Json(name = "name_en") val nameEn: String,
    @param:Json(name = "description_fr") val descriptionFr: String,
    @param:Json(name = "description_ar") val descriptionAr: String,
    @param:Json(name = "description_en") val descriptionEn: String,
    @param:Json(name = "price_millimes") val priceMillimes: Int,
    @param:Json(name = "image_url") val imageUrl: String,
    @param:Json(name = "category_id") val categoryId: String,
    @param:Json(name = "is_available") val isAvailable: Boolean = true,
    @param:Json(name = "is_featured") val isFeatured: Boolean = false,
    @param:Json(name = "created_at") val createdAt: String? = null,
    @param:Json(name = "updated_at") val updatedAt: String? = null
)

data class RestaurantSettingsDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "logo_url") val logoUrl: String,
    @param:Json(name = "phone") val phone: String,
    @param:Json(name = "address") val address: String,
    @param:Json(name = "delivery_fee_millimes") val deliveryFeeMillimes: Int,
    @param:Json(name = "minimum_order_millimes") val minimumOrderMillimes: Int,
    @param:Json(name = "is_open") val isOpen: Boolean,
    @param:Json(name = "estimated_delivery_minutes") val estimatedDeliveryMinutes: Int,
    @param:Json(name = "currency") val currency: String = "TND"
)

data class ProfileDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "display_name") val displayName: String?,
    @param:Json(name = "phone") val phone: String?,
    @param:Json(name = "preferred_language") val preferredLanguage: String = "fr",
    @param:Json(name = "role") val role: String = "customer",
    @param:Json(name = "created_at") val createdAt: String? = null
)

data class OrderDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "order_number") val orderNumber: String,
    @param:Json(name = "user_id") val userId: String?,
    @param:Json(name = "customer_name") val customerName: String,
    @param:Json(name = "customer_phone") val customerPhone: String,
    @param:Json(name = "delivery_address") val deliveryAddress: String,
    @param:Json(name = "delivery_instructions") val deliveryInstructions: String?,
    @param:Json(name = "subtotal_millimes") val subtotalMillimes: Int,
    @param:Json(name = "delivery_fee_millimes") val deliveryFeeMillimes: Int,
    @param:Json(name = "discount_millimes") val discountMillimes: Int,
    @param:Json(name = "total_millimes") val totalMillimes: Int,
    @param:Json(name = "payment_method") val paymentMethod: String = "cash_on_delivery",
    @param:Json(name = "payment_status") val paymentStatus: String = "pending_cod",
    @param:Json(name = "status") val status: String = "pending",
    @param:Json(name = "created_at") val createdAt: String? = null,
    @param:Json(name = "updated_at") val updatedAt: String? = null
)

data class CreateVerifiedOrderRpcRequest(
    @param:Json(name = "p_customer_name") val customerName: String,
    @param:Json(name = "p_customer_phone") val customerPhone: String,
    @param:Json(name = "p_delivery_address") val deliveryAddress: String,
    @param:Json(name = "p_delivery_instructions") val deliveryInstructions: String,
    @param:Json(name = "p_promo_code") val promoCode: String?,
    @param:Json(name = "p_items") val items: List<OrderItemRpcInput>
)

data class OrderItemRpcInput(
    @param:Json(name = "product_id") val productId: String,
    @param:Json(name = "quantity") val quantity: Int,
    @param:Json(name = "selected_options") val selectedOptions: String
)

data class SupabaseAuthRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "data") val data: Map<String, String>? = null
)

data class SupabaseAuthUserDto(
    @param:Json(name = "id") val id: String?,
    @param:Json(name = "email") val email: String?
)

/**
 * Handles both Supabase Auth response shapes:
 * - Auto-confirm enabled: returns `access_token` + nested `user: { id, email }`
 * - Confirm-email enabled: returns root `id` and `email` directly without `access_token`
 */
data class SupabaseAuthResponse(
    @param:Json(name = "access_token") val accessToken: String? = null,
    @param:Json(name = "user") val user: SupabaseAuthUserDto? = null,
    @param:Json(name = "id") val rootId: String? = null,
    @param:Json(name = "email") val rootEmail: String? = null
) {
    val resolvedUserId: String?
        get() = user?.id ?: rootId

    val resolvedEmail: String?
        get() = user?.email ?: rootEmail
}

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

    @POST("rest/v1/profiles")
    suspend fun upsertProfile(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body profile: ProfileDto
    ): Response<List<ProfileDto>>

    @GET("rest/v1/orders?select=*&order=created_at.desc")
    suspend fun getOrders(): Response<List<OrderDto>>

    @POST("rest/v1/orders")
    suspend fun insertOrderDirect(
        @Header("Prefer") prefer: String = "return=representation",
        @Body order: OrderDto
    ): Response<List<OrderDto>>

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

    @POST("rest/v1/products")
    suspend fun upsertProductsBatch(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body products: List<ProductDto>
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

    @POST("rest/v1/categories")
    suspend fun upsertCategoriesBatch(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body categories: List<CategoryDto>
    ): Response<List<CategoryDto>>

    @POST("rest/v1/restaurant_settings")
    suspend fun upsertRestaurantSettings(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body settings: RestaurantSettingsDto
    ): Response<List<RestaurantSettingsDto>>

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
