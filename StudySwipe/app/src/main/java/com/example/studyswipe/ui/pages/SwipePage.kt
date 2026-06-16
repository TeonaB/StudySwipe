package com.example.studyswipe.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studyswipe.model.Subject
import com.example.studyswipe.model.User
import com.example.studyswipe.model.UserRole
import com.example.studyswipe.ui.theme.StudySwipeTheme
import com.example.studyswipe.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SwipePage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit = {},
    onMatchCreated: (String) -> Unit = {}
) {
    val candidates by authViewModel.swipeCandidates.collectAsState()
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swipe Feed", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (candidates.isNotEmpty()) {
                val candidate = candidates.first()

                // Tinder-like Card Layout
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Large Initials Avatar
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = candidate.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Candidate Name
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Role badge
                        val roleLabel = when (candidate.role) {
                            UserRole.STUDENT -> "Student 📚"
                            UserRole.TUTOR -> "Tutor 🎓"
                            UserRole.BOTH -> "Student & Tutor 📚🎓"
                        }
                        val roleBadgeColor = when (candidate.role) {
                            UserRole.STUDENT -> MaterialTheme.colorScheme.secondaryContainer
                            UserRole.TUTOR -> MaterialTheme.colorScheme.tertiaryContainer
                            UserRole.BOTH -> MaterialTheme.colorScheme.primaryContainer
                        }
                        val roleBadgeTextColor = when (candidate.role) {
                            UserRole.STUDENT -> MaterialTheme.colorScheme.onSecondaryContainer
                            UserRole.TUTOR -> MaterialTheme.colorScheme.onTertiaryContainer
                            UserRole.BOTH -> MaterialTheme.colorScheme.onPrimaryContainer
                        }

                        Surface(
                            color = roleBadgeColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = roleBadgeTextColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bio
                        if (candidate.bio.isNotBlank()) {
                            Text(
                                text = "\"${candidate.bio}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        } else {
                            Text(
                                text = "Nicio descriere adăugată.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Subjects label
                        if (candidate.subjects.isNotEmpty()) {
                            Text(
                                text = "Materii de interes:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // List of subjects as chips
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                candidate.subjects.forEach { subject ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(subject.displayName) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Buttons at the bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Red Dislike Button
                    IconButton(
                        onClick = {
                            isProcessing = true
                            authViewModel.dislikeUser(candidate.id) {
                                isProcessing = false
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                color = if (isProcessing) Color(0xFFE57373).copy(alpha = 0.5f) else Color(0xFFE57373),
                                shape = CircleShape
                            ),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (isProcessing) Color.White.copy(alpha = 0.5f) else Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dislike / Nu sunt interesat",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Green Like Button
                    IconButton(
                        onClick = {
                            isProcessing = true
                            authViewModel.likeUser(candidate.id, onMatchCreated) {
                                isProcessing = false
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                color = if (isProcessing) Color(0xFF81C784).copy(alpha = 0.5f) else Color(0xFF81C784),
                                shape = CircleShape
                            ),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (isProcessing) Color.White.copy(alpha = 0.5f) else Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like / Interesat",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            } else {
                // Empty State Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Niciun utilizator disponibil",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ai evaluat toți utilizatorii compatibili momentan. Revino mai târziu!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
