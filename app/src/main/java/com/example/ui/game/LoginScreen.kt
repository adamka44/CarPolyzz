package com.example.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    onSignIn: (email: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("gamer@gmail.com") }
    var name by remember { mutableStateOf("PolyRacerX") }
    var emailError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic geometric low-poly background grid
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Game Logo heading / branded typography
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CarPolyzz",
                    color = ThemePrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "SKILL-BASED COMPETITIVE RACING",
                    color = ThemeSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Central glassmorphic login gate card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GOOGLE INTEGRITY SECURE SIGN-IN",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Access is restricted to Google-verified accounts to prevent competitive rating manipulation, anonymous bot profiles, and leaderboard cheating.",
                        color = AccentMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Nickname input
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = ""
                        },
                        label = { Text("Racer Handle / Nickname") },
                        isError = nameError.isNotEmpty(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = ThemePrimary,
                            unfocusedLabelColor = AccentMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )
                    if (nameError.isNotEmpty()) {
                        Text(
                            text = nameError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email input
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = ""
                        },
                        label = { Text("Google Account Email Address") },
                        isError = emailError.isNotEmpty(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemeSecondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = ThemeSecondary,
                            unfocusedLabelColor = AccentMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Branded action button (Artistic Flair 2rem rounded/emerald glow styling)
                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                nameError = "Handle cannot be blank."
                            }
                            if (email.trim().isEmpty() || !email.contains("@")) {
                                emailError = "Enter valid google account email."
                            }
                            if (nameError.isEmpty() && emailError.isEmpty()) {
                                onSignIn(email, name)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemePrimary
                        ),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("login_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CONTINUE WITH GOOGLE SECURE AUTH",
                                color = DarkBackground,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Footer assurance policy
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No gameplay altering unlocks. No pay-to-win. Strictly fairplay.",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
