package com.example.studyswipe.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.studyswipe.model.Subject
import com.example.studyswipe.model.User
import com.example.studyswipe.model.UserRole
import com.example.studyswipe.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListPage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    role: UserRole,
    onBackClick: () -> Unit
) {
    val allUsers by authViewModel.allUsers.collectAsState()
    val filteredUsers = allUsers.filter { it.role == role && it.id != "admin-fixed-uuid" }

    var selectedUserForDetails by remember { mutableStateOf<User?>(null) }
    var selectedUserForEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    val roleNameRomanian = when (role) {
        UserRole.STUDENT -> "Studenți"
        UserRole.TUTOR -> "Tutori / Profesori"
        UserRole.BOTH -> "Conturi Duble (Student & Tutor)"
        UserRole.ADMIN -> "Administratori"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(roleNameRomanian, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (filteredUsers.isEmpty()) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Niciun cont în această categorie",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(filteredUsers, key = { it.id }) { user ->
                    AdminUserItem(
                        user = user,
                        onViewDetails = { selectedUserForDetails = user },
                        onEdit = { selectedUserForEdit = user },
                        onDelete = { userToDelete = user }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Details Dialog
        selectedUserForDetails?.let { user ->
            AdminUserDetailsDialog(
                user = user,
                onDismiss = { selectedUserForDetails = null }
            )
        }

        // Edit Dialog
        selectedUserForEdit?.let { user ->
            AdminUserEditDialog(
                user = user,
                authViewModel = authViewModel,
                onDismiss = { selectedUserForEdit = null },
                onSaveSuccess = { selectedUserForEdit = null }
            )
        }

        // Delete Confirmation Dialog
        userToDelete?.let { user ->
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Ștergere cont", fontWeight = FontWeight.Bold) },
                text = { Text("Sigur dorești să ștergi definitiv contul lui ${user.name} (${user.email})? Această acțiune va șterge și toate potrivirile/mesajele asociate.") },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.deleteUserByAdmin(user.id) {
                                userToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Șterge")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("Renunță")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminUserItem(
    user: User,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick actions
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editează", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Șterge", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminUserDetailsDialog(
    user: User,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detalii utilizator", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Nume: ${user.name}", fontWeight = FontWeight.SemiBold)
                Text(text = "Email: ${user.email}")
                Text(text = "Parolă: ${user.password}")
                Text(text = "Rol: ${user.role.name}")
                Text(
                    text = "Descriere:\n${if (user.bio.isNotBlank()) user.bio else "Fără descriere"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (user.subjects.isNotEmpty()) {
                    Text(text = "Materii selectate:", fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        user.subjects.forEach { subject ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = subject.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Închide")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminUserEditDialog(
    user: User,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf(user.password) }
    var bio by remember { mutableStateOf(user.bio) }
    var selectedSubjects by remember { mutableStateOf(user.subjects) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && selectedSubjects.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editează cont", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nume") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Parolă") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Descriere") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Materii selectate:", fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Subject.entries.forEach { subject ->
                        val isSelected = subject in selectedSubjects
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSubjects = if (isSelected) {
                                    selectedSubjects - subject
                                } else {
                                    selectedSubjects + subject
                                }
                            },
                            label = { Text(subject.displayName) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    errorMessage = null
                    authViewModel.updateUserProfileByAdmin(
                        targetUserId = user.id,
                        name = name,
                        email = email,
                        password = password,
                        subjects = selectedSubjects,
                        bio = bio,
                        onSuccess = {
                            isSaving = false
                            onSaveSuccess()
                        },
                        onError = {
                            isSaving = false
                            errorMessage = it
                        }
                    )
                },
                enabled = isValid && !isSaving
            ) {
                Text("Salvează")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Anulează")
            }
        }
    )
}
