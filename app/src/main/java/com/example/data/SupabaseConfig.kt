package com.example.data

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

/**
 * Initializes the [SupabaseClient] using [BuildConfig.SUPABASE_URL] and
 * [BuildConfig.SUPABASE_ANON_KEY] along with the Ktor [OkHttp] engine configuration.
 */
object SupabaseConfig {

    val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL.trim()

    val supabaseAnonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY.trim()

    /**
     * Checks whether live Supabase project credentials have been provided via BuildConfig.
     */
    val isConfigured: Boolean
        get() {
            val url = BuildConfig.SUPABASE_URL.trim()
            val key = BuildConfig.SUPABASE_ANON_KEY.trim()
            return url.startsWith("https://") &&
                url.contains(".supabase.co") &&
                !url.contains("your-project.supabase.co") &&
                !url.contains("YOUR_SUPABASE") &&
                key.length > 20 &&
                !key.contains("YOUR_SUPABASE")
        }

    /**
     * Ktor [OkHttp] engine configuration for Supabase network requests.
     */
    val ktorEngine by lazy {
        OkHttp.create {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }
    }

    /**
     * Primary [SupabaseClient] initialized with [BuildConfig.SUPABASE_URL] and
     * [BuildConfig.SUPABASE_ANON_KEY].
     */
    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            httpEngine = ktorEngine
            install(Auth)
            install(Postgrest)
        }
    }

    val client: SupabaseClient
        get() = supabaseClient

    val auth: Auth
        get() = supabaseClient.auth

    val postgrest: Postgrest
        get() = supabaseClient.postgrest
}
