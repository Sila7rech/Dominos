package com.example.data

import io.github.jan.supabase.SupabaseClient as JanSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Delegates to [SupabaseConfig] for backward compatibility across the data layer.
 */
object SupabaseClient {
    val supabaseUrl: String
        get() = SupabaseConfig.supabaseUrl

    val supabaseAnonKey: String
        get() = SupabaseConfig.supabaseAnonKey

    val isConfigured: Boolean
        get() = SupabaseConfig.isConfigured

    val client: JanSupabaseClient
        get() = SupabaseConfig.client

    val auth: Auth
        get() = SupabaseConfig.auth

    val database: Postgrest
        get() = SupabaseConfig.postgrest
}
