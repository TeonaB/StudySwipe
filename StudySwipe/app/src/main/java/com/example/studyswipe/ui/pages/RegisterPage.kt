package com.example.studyswipe.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studyswipe.R
import com.example.studyswipe.model.UserRole
import com.example.studyswipe.ui.components.EmailInputField
import com.example.studyswipe.ui.theme.StudySwipeTheme
import com.example.studyswipe.utils.isValidEmail
import com.example.studyswipe.utils.isValidPassword

@Composable
fun RegisterPage(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRegisterClick: (name: String, email: String, password: String, role: UserRole) -> Unit = { _, _, _, _ -> },
    onLoginClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val invalidNameMsg = stringResource(R.string.invalid_name)
    val invalidEmailMsg = stringResource(R.string.invalid_email)
    val invalidPasswordMsg = stringResource(R.string.invalid_password)
    val passwordsNotMatchMsg = stringResource(R.string.passwords_not_match)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text(stringResource(R.string.full_name)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = nameError != null,
            supportingText = nameError?.let { msg -> { Text(msg) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        EmailInputField(
            modifier = Modifier,
            value = email,
            errorMessage = emailError,
            imeAction = ImeAction.Next
        ) { email = it; emailError = null }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { msg -> { Text(msg) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = { Text(stringResource(R.string.confirm_password)) },
            leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { msg -> { Text(msg) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.select_role),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedRole == UserRole.STUDENT,
                onClick = { selectedRole = UserRole.STUDENT },
                label = { Text(stringResource(R.string.role_student)) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedRole == UserRole.TUTOR,
                onClick = { selectedRole = UserRole.TUTOR },
                label = { Text(stringResource(R.string.role_tutor)) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedRole == UserRole.BOTH,
                onClick = { selectedRole = UserRole.BOTH },
                label = { Text(stringResource(R.string.role_both)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                var isValid = true
                if (name.trim().length < 2) { nameError = invalidNameMsg; isValid = false }
                if (!email.isValidEmail()) { emailError = invalidEmailMsg; isValid = false }
                if (!password.isValidPassword()) { passwordError = invalidPasswordMsg; isValid = false }
                if (password != confirmPassword) { confirmPasswordError = passwordsNotMatchMsg; isValid = false }
                if (isValid) onRegisterClick(name.trim(), email.trim(), password, selectedRole)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            when (isLoading) {
                true -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                false -> Text(stringResource(R.string.register_button), fontWeight = FontWeight.SemiBold)
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onLoginClick) {
            Text(stringResource(R.string.goto_login))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPagePreview() {
    StudySwipeTheme { RegisterPage() }
}
