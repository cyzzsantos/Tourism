package pt.isec.a2020133455.tp1.ui.screens

import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pt.isec.a2020133455.tp1.FirebaseViewModel
import pt.isec.a2020133455.tp1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificPoIScreen(
    viewModel: FirebaseViewModel,
    navController: NavController
) {
    val pointOfInterest = viewModel.chosenPointOfInterest!!
    val category = viewModel.chosenCategory!!
    val error by remember { viewModel.error }
    val coordinates = viewModel.currentLocation.observeAsState()
    val geoPoint by remember {
        mutableStateOf(
            GeoPoint(
                pointOfInterest.latitude,
                pointOfInterest.longitude
            )
        )
    }
    val location = Location(LocationManager.GPS_PROVIDER)
    location.latitude = geoPoint.latitude
    location.longitude = geoPoint.longitude
    val distance = coordinates.value?.distanceTo(location)
    val formatedDistance = String.format("%.2f", distance)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if(error != null) {
            Text(text="Error: $error", Modifier.background(Color.Red))
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            border = BorderStroke(0.5.dp, Color.Black),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
                ),
            ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberAsyncImagePainter(pointOfInterest.imagePath),
                    contentScale = ContentScale.Crop,
                    contentDescription = "PoI Image",
                    modifier = Modifier
                        .weight(3f, fill = true)
                        .fillMaxWidth()
                )
                Divider(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                )
                Text(
                    text = pointOfInterest.name,
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    )
                Text(
                    text = category.name,
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .padding(horizontal = 16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = pointOfInterest.description,
                    modifier = Modifier
                        .weight(2f, fill = true)
                        .padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .weight(4f, fill = true)
                        .background(Color(255, 240, 218)),
                ) {
                    AndroidView(
                        factory = {
                            context -> MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(18.0)
                        controller.setCenter(geoPoint)
                        overlays.add(
                            Marker(this).apply {
                                position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                        )
                    }
                    }, update = {
                            it.controller.setCenter(geoPoint)
                        })
                }
                Text(
                    text = "${stringResource(R.string.distance)} $formatedDistance ${stringResource(R.string.meters)}",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .padding(horizontal = 16.dp),
                )
                Text(
                    text = "${stringResource(R.string.author)}: ${pointOfInterest.author}",
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}