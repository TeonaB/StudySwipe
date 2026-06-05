package com.example.studyswipe.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studyswipe.R
import com.example.studyswipe.ui.components.EmailInputField
import com.example.studyswipe.ui.theme.StudySwipeTheme
import com.example.studyswipe.utils.isValidEmail
import com.example.studyswipe.utils.isValidPassword

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    onRegisterClick: () -> Unit = {},
    onLoginClickFirebase: (email: String, password: String, onSuccess: () -> Unit) -> Unit = { _, _, _ -> },
    onLoginClickApi: (email: String, password: String, onSuccess: () -> Unit) -> Unit = { _, _, _ -> },
    onLoginSuccess: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val invalidEmailMessage = stringResource(R.string.invalid_email)
    val invalidPasswordMessage = stringResource(R.string.invalid_password)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.welcome),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.login_page),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        EmailInputField(
            modifier = Modifier,
            value = email,
            errorMessage = emailError,
            imeAction = ImeAction.Next
        ) { newValue ->
            email = newValue
            emailError = null
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue
                passwordError = null
            },
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Password, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            isError = passwordError != null,
            supportingText = passwordError?.let { msg -> { Text(msg) } },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                var isValid = true
                if (!email.isValidEmail()) { emailError = invalidEmailMessage; isValid = false }
                if (!password.isValidPassword()) { passwordError = invalidPasswordMessage; isValid = false }
                if (isValid) onLoginClickFirebase(email, password, onLoginSuccess)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            when (isLoading) {
                true -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                false -> Text(stringResource(R.string.login_firebase))
            }
        }

        errorMessage?.let { errMsg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                text = errMsg
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onRegisterClick) {
            Text(stringResource(R.string.goto_register))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPagePreview() {
    StudySwipeTheme { LoginPage() }
}
