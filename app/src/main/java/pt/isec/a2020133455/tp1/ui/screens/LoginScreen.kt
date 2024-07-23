package pt.isec.a2020133455.tp1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.isec.a2020133455.tp1.FirebaseViewModel
import pt.isec.a2020133455.tp1.R

@Composable
fun LoginScreen(
    viewModel: FirebaseViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val error by remember { viewModel.error }
    val user by remember { viewModel.user }

    LaunchedEffect(key1 = user) {
        if (user != null && error == null) {
            navController.navigate("MainScreen")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if(error != null) {
            Text(text="Error: $error", Modifier.background(Color.Red))
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))
        Button(onClick = {viewModel.signInWithEmail(email.value, password.value)}) {
            Text(text = stringResource(R.string.login))
        }

        Spacer(Modifier.height(30.dp))
        Text(text = stringResource(R.string.newUser))

        Button(onClick = {navController.navigate("RegisterScreen")}) {
            Text(text = stringResource(R.string.register))
        }

        Button(
            onClick = {navController.navigate("CreditScreen")},
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = stringResource(R.string.credits))
        }
    }
}
