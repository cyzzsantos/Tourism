package pt.isec.a2020133455.tp1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.isec.a2020133455.tp1.FirebaseViewModel
import pt.isec.a2020133455.tp1.R

@Composable
fun CreditScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.nuno),
            contentDescription = "Nuno Santos",
            contentScale = ContentScale.Fit,
            modifier = Modifier.border(1.dp, color = Black)
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Nuno Santos",
        )
        Text(
            text = "2020133455",
        )
        Text(
            text = "Licenciatura em Engenharia Informática",
        )
        Text(
            text = "2023/2024"
        )
    }
}