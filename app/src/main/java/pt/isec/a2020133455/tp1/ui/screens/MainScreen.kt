package pt.isec.a2020133455.tp1.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import pt.isec.a2020133455.tp1.FirebaseViewModel
import pt.isec.a2020133455.tp1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FirebaseViewModel,
    navController: NavController
) {
    val error by remember { viewModel.error }
    val user by remember { viewModel.user }

    viewModel.fetchLocations()
    viewModel.fetchCategories()
    viewModel.fetchPointsOfInterest()
    viewModel.currentLocation

    LaunchedEffect(key1 = user) {
        if(user == null)
            navController.navigateUp()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        if(error != null) {
            Text(text="Error: $error", Modifier.background(Color.Red))
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row (
            modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController.navigate("SubmitLocationScreen") }) {
                Text(text = stringResource(R.string.submissionsPanel))
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.signOut() }) {
                Text(text = stringResource(R.string.signOut))
            }
        }

        Divider(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
        )

        val locationList by viewModel.locationList.observeAsState(initial = emptyList())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items (locationList) { locationItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(240.dp)
                        .width(180.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(255, 255, 255),
                    ),
                    onClick = {
                        viewModel.chosenLocation = locationItem
                        navController.navigate("CategoryScreen")
                    },
                    border = BorderStroke(0.5.dp, Color.Black)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(locationItem.imagePath),
                            contentDescription = "Location Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth()
                        )
                        Text(
                            text = locationItem.name,
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1.5f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = locationItem.description,
                            modifier = Modifier
                                .padding(16.dp, top = 0.dp)
                                .weight(2f),
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}
