package com.example.studyswipe.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studyswipe.model.Subject
import com.example.studyswipe.ui.theme.StudySwipeTheme

// @OptIn spune compilatorului că știm că folosim o funcție experimentală (FlowRow)
// și ne asumăm responsabilitatea dacă API-ul se schimbă în viitor.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupPage(
    modifier: Modifier = Modifier,
    userName: String = "",
    onSaveClick: (subjects: Set<Subject>, bio: String) -> Unit = { _, _ -> }
) {
    // mutableStateOf<Set<Subject>> — stocăm materiile selectate ca un Set.
    // emptySet() = nicio materie selectată la început.
    var selectedSubjects by remember { mutableStateOf<Set<Subject>>(emptySet()) }
    var bio by remember { mutableStateOf("") }

    // Validare: butonul e activ doar dacă a selectat cel puțin o materie
    val isValid = selectedSubjects.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Titlu personalizat cu numele utilizatorului
        Text(
            text = "Bună, $userName! 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Completează-ți profilul pentru a începe.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ======= SECȚIUNEA MATERII =======
        Text(
            text = "La ce materii ești interesat?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Selectează cel puțin una",
            style = MaterialTheme.typography.bodySmall,
            color = if (isValid) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // FlowRow = Row care "se rupe" automat pe linia următoare când nu mai e spațiu.
        // horizontalArrangement = Arrangement.spacedBy(8.dp) → spațiu de 8dp între chips.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Subject.entries returnează lista tuturor valorilor din enum.
            // Pentru fiecare materie, desenăm un FilterChip.
            Subject.entries.forEach { subject ->

                // Verificăm dacă această materie este deja selectată
                val isSelected = subject in selectedSubjects

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        // Dacă e deja selectată → o scoatem din Set (- operator)
                        // Dacă nu e selectată → o adăugăm în Set (+ operator)
                        selectedSubjects = if (isSelected) {
                            selectedSubjects - subject
                        } else {
                            selectedSubjects + subject
                        }
                    },
                    // displayName este câmpul din enum: "Matematică", "Fizică", etc.
                    label = { Text(subject.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ======= SECȚIUNEA BIO =======
        Text(
            text = "Despre tine (opțional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // minLines = 3 → câmpul are minim 3 linii înălțime (nu e singleLine)
        // maxLines = 5 → nu poate crește mai mult de 5 linii
        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 200) bio = it },
            placeholder = { Text("Ex: Sunt bun la matematică și pot ajuta cu exerciții de algebră...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
            // Afișăm un contor de caractere în dreapta jos
            supportingText = {
                Text(
                    text = "${bio.length}/200",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ======= BUTON SALVARE =======
        Button(
            onClick = { onSaveClick(selectedSubjects, bio) },
            modifier = Modifier.fillMaxWidth(),
            // enabled = false dezactivează butonul și îl face gri
            enabled = isValid
        ) {
            Text(
                text = "Hai să începem! 🚀",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSetupPagePreview() {
    StudySwipeTheme {
        ProfileSetupPage(userName = "Alexandru")
    }
}
