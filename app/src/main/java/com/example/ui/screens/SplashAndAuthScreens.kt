package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.ui.components.DominosBrandLogo
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.SupabaseTableSetupCard
import com.example.ui.theme.CardPureWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DominosBlue
import com.example.ui.theme.DominosRed
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenMid
import com.example.ui.theme.HeaderGreenGradient
import com.example.ui.theme.InputSurfaceSage
import com.example.ui.theme.LimeGreenBorder
import com.example.ui.theme.LimeGreenBright
import com.example.ui.theme.LimeGreenSoft
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextMutedOlive

/**
 * Splash & Onboarding Screen in Domino's Blue, Red & Crisp White palette.
 */
@Composable
fun SplashAndOnboardingScreen(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onContinueToLogin: () -> Unit,
    onSkipToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Row: Language quick pills + Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppLanguage.entries.forEach { lang ->
                    val selected = lang == currentLanguage
                    Surface(
                        color = if (selected) DominosBlue else CardPureWhite,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clickable { onSelectLanguage(lang) }
                            .testTag("splash_lang_${lang.code}")
                    ) {
                        Text(
                            text = lang.nativeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else TextCharcoal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            TextButton(
                onClick = onSkipToHome,
                modifier = Modifier.testTag("splash_skip_button")
            ) {
                Text(
                    text = stringResource(R.string.action_skip),
                    color = DominosRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Hero
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_dominos_launcher_1791739912765),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(36.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            DominosBrandLogo(fontSize = 46.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.brand_sub_houma),
                style = MaterialTheme.typography.labelLarge,
                color = DominosBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = TextMutedOlive,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.onboarding_desc_1),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedOlive,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Bottom CTA buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryGradientButton(
                text = stringResource(R.string.action_continue),
                onClick = onContinueToLogin,
                modifier = Modifier.testTag("splash_continue_button")
            )

            TextButton(
                onClick = onSkipToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("splash_start_ordering_button")
            ) {
                Text(
                    text = stringResource(R.string.action_start_ordering),
                    style = MaterialTheme.typography.titleSmall,
                    color = DominosBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Auth / Login / Sign Up Screen in Domino's Blue & Red theme,
 * with reliable account creation (Supabase Auth + automatic profile creation)
 * and built-in Supabase SQL Table Setup Assistant.
 */
@Composable
fun AuthLoginScreen(
    keepSignedIn: Boolean,
    onKeepSignedInChange: (Boolean) -> Unit,
    onLoginSubmit: (String, String, (Boolean) -> Unit) -> Unit,
    onSignUpSubmit: (String, String, String, String, (Boolean) -> Unit) -> Unit,
    onSeedSupabaseTables: ((Boolean) -> Unit) -> Unit = {},
    onQuickDemoCustomer: () -> Unit,
    onQuickDemoAdmin: () -> Unit,
    onSkipAsGuest: () -> Unit
) {
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var emailOrPhone by rememberSaveable { mutableStateOf("client@dominos.tn") }
    var phone by rememberSaveable { mutableStateOf("+216 22 456 789") }
    var password by rememberSaveable { mutableStateOf("dominos123") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotInfo by remember { mutableStateOf(false) }

    val invalidCredentialsMsg = stringResource(R.string.error_auth_invalid)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardPureWhite)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        // Top Domino's Royal Blue Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(HeaderGreenGradient)
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    DominosBrandLogo(
                        fontSize = 36.sp,
                        lightOnDark = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.auth_hero_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.92f),
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_dominos_launcher_1791739912765),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(116.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }
        }

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            // Mode Switch Tabs (Se connecter / Créer un compte) for instant, obvious switching
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(InputSurfaceSage)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = if (!isSignUpMode) DominosBlue else Color.Transparent,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isSignUpMode = false
                            errorMessage = null
                        }
                ) {
                    Text(
                        text = stringResource(R.string.auth_login_btn),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isSignUpMode) Color.White else TextCharcoal,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                Surface(
                    color = if (isSignUpMode) DominosRed else Color.Transparent,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isSignUpMode = true
                            errorMessage = null
                            if (emailOrPhone == "client@dominos.tn") {
                                emailOrPhone = ""
                            }
                        }
                        .testTag("auth_signup_tab_btn")
                ) {
                    Text(
                        text = stringResource(R.string.auth_signup_btn),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSignUpMode) Color.White else TextCharcoal,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignUpMode) {
                    stringResource(R.string.auth_create_account)
                } else {
                    stringResource(R.string.auth_welcome_back)
                },
                style = MaterialTheme.typography.headlineLarge,
                color = TextCharcoal,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSignUpMode) {
                PillInputField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    placeholder = stringResource(R.string.auth_full_name),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = DominosBlue
                        )
                    },
                    testTag = "auth_fullname_input"
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            PillInputField(
                value = emailOrPhone,
                onValueChange = {
                    emailOrPhone = it
                    errorMessage = null
                },
                placeholder = stringResource(R.string.auth_email_or_phone),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = DominosBlue
                    )
                },
                testTag = "auth_email_input"
            )

            if (isSignUpMode) {
                Spacer(modifier = Modifier.height(14.dp))
                PillInputField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMessage = null
                    },
                    placeholder = stringResource(R.string.auth_phone),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = DominosBlue
                        )
                    },
                    testTag = "auth_phone_input"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Password field with visibility toggle
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.auth_password),
                        color = TextMutedOlive
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = DominosBlue
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextMutedOlive
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputSurfaceSage,
                    unfocusedContainerColor = InputSurfaceSage,
                    focusedBorderColor = LimeGreenBorder,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextCharcoal,
                    unfocusedTextColor = TextCharcoal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // "Keep me signed in" + "Forgot Password?" row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onKeepSignedInChange(!keepSignedIn) }
                        .testTag("auth_keep_signed_in_checkbox")
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (keepSignedIn) LimeGreenBright else InputSurfaceSage)
                            .border(
                                width = 1.dp,
                                color = if (keepSignedIn) LimeGreenBright else TextMutedOlive,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (keepSignedIn) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.auth_keep_signed_in),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextCharcoal
                    )
                }

                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreenMid,
                    modifier = Modifier.clickable { showForgotInfo = !showForgotInfo }
                )
            }

            if (showForgotInfo) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = LimeGreenSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.auth_password_reset_info),
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestGreenDark,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = ErrorCrimson,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Gradient Pill Button ("Se connecter" / "Créer mon compte")
            PrimaryGradientButton(
                text = if (isSubmitting) {
                    "Connexion en cours…"
                } else if (isSignUpMode) {
                    stringResource(R.string.auth_signup_btn)
                } else {
                    stringResource(R.string.auth_login_btn)
                },
                enabled = !isSubmitting,
                onClick = {
                    val effectiveEmail = emailOrPhone.trim().let { raw ->
                        when {
                            raw.isBlank() && fullName.isNotBlank() ->
                                "${fullName.trim().lowercase().filter { it.isLetterOrDigit() }}@dominos.tn"
                            raw.isBlank() -> "client@dominos.tn"
                            !raw.contains("@") -> "${raw.filter { it.isLetterOrDigit() }}@dominos.tn"
                            else -> raw
                        }
                    }
                    isSubmitting = true
                    errorMessage = null
                    if (isSignUpMode) {
                        onSignUpSubmit(fullName, effectiveEmail, phone, password) {
                            isSubmitting = false
                        }
                    } else {
                        onLoginSubmit(effectiveEmail, password) {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.testTag("auth_submit_button")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "Or continue with" Divider Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFDCE6F0)
                )
                Text(
                    text = stringResource(R.string.auth_or_continue_with),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedOlive,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFDCE6F0)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two side-by-side pill buttons (Demo Customer & Demo Admin)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = InputSurfaceSage,
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable(onClick = onQuickDemoCustomer)
                        .testTag("auth_demo_customer_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = DominosBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.auth_demo_customer),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextCharcoal
                        )
                    }
                }

                Surface(
                    color = InputSurfaceSage,
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable(onClick = onQuickDemoAdmin)
                        .testTag("auth_demo_admin_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = DominosRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.auth_demo_admin),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom "Don't have an account? Sign Up"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUpMode) {
                        stringResource(R.string.auth_have_account)
                    } else {
                        stringResource(R.string.auth_no_account)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedOlive
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSignUpMode) {
                        stringResource(R.string.auth_sign_in_link)
                    } else {
                        stringResource(R.string.auth_sign_up_link)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = DominosRed,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clickable {
                            isSignUpMode = !isSignUpMode
                            errorMessage = null
                        }
                        .testTag("auth_toggle_mode_link")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = onSkipAsGuest,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.auth_guest_continue),
                    style = MaterialTheme.typography.labelMedium,
                    color = DominosBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Supabase Table Setup Assistant right on the Auth screen
            SupabaseTableSetupCard(
                onSeedTablesClick = onSeedSupabaseTables
            )
        }
    }
}

@Composable
private fun PillInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = TextMutedOlive
            )
        },
        leadingIcon = leadingIcon,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InputSurfaceSage,
            unfocusedContainerColor = InputSurfaceSage,
            focusedBorderColor = LimeGreenBorder,
            unfocusedBorderColor = LimeGreenBorder.copy(alpha = 0.45f),
            focusedTextColor = TextCharcoal,
            unfocusedTextColor = TextCharcoal
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
