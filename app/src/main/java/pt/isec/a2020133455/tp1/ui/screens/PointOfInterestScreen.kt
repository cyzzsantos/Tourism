package pt.isec.a2020133455.tp1.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import pt.isec.a2020133455.tp1.FirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointOfInterestScreen(
    viewModel: FirebaseViewModel,
    navController: NavController
)
{
    val poiList by viewModel.pointOfInterestList.observeAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        items(poiList)
        {pointOfInterestItem ->
                if (pointOfInterestItem.category == (viewModel.chosenCategory?.name) &&
                    pointOfInterestItem.location == (viewModel.chosenLocation?.name)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(240.dp)
                            .width(180.dp),
                        onClick = {
                            viewModel.chosenPointOfInterest = pointOfInterestItem
                            navController.navigate("SpecificPoIScreen")
                        },
                        elevation = CardDefaults.cardElevation(8.dp),
                        border = BorderStroke(0.5.dp, Color.Black),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                    )
                    {
                        Column(modifier = Modifier.fillMaxSize())
                        {
                            Image(
                                painter = rememberAsyncImagePainter(pointOfInterestItem.imagePath),
                                contentScale = ContentScale.Crop,
                                contentDescription = "PoI Image",
                                modifier = Modifier
                                    .weight(2f, fill = false)
                                    .fillMaxWidth()
                            )
                            Text(
                                text = pointOfInterestItem.name,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .weight(1.5f, fill = false),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,

                                )
                            Text(
                                text = pointOfInterestItem.description,
                                modifier = Modifier
                                    .padding(16.dp, top = 0.dp)
                                    .weight(2f, fill = false),
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
            }
        }
    }