package pt.isec.a2020133455.tp1.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.osmdroid.util.GeoPoint
import pt.isec.a2020133455.tp1.FirebaseViewModel
import pt.isec.a2020133455.tp1.R
import pt.isec.a2020133455.tp1.utils.FAuthUtil
import pt.isec.a2020133455.tp1.utils.FileUtils

enum class SubmissionType {
    LOCATION, CATEGORY, POINTOFINTEREST
}

@Composable
fun SubmissionsPanelScreen(
    viewModel: FirebaseViewModel,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val user by remember { viewModel.user }
    val coordinates = viewModel.currentLocation.observeAsState()
    var geoPoint by remember {
        mutableStateOf(
            GeoPoint(
            coordinates.value?.latitude ?: 0.0,
            coordinates.value?.longitude ?: 0.0
            )
        )
    }
    val locationList = remember { mutableListOf<String>() }
    val categoryList = remember { mutableListOf<String>() }
    var submissionType by remember { mutableStateOf(SubmissionType.LOCATION) }

    LaunchedEffect(key1 = user) {
        if (user == null)
            navController.navigate("LoginScreen")
    }

    LaunchedEffect(key1 = coordinates) {
        geoPoint = GeoPoint(
            coordinates.value?.latitude ?: 0.0,
            coordinates.value?.longitude ?: 0.0
        )
    }

    LaunchedEffect(key1 = submissionType) {
        viewModel.imagePath.value = null
        viewModel.getAllCategoriesNames { categoryNames ->
            categoryList.clear()
            categoryList.addAll(categoryNames)
        }
        viewModel.getAllLocationsNames { locationNames ->
            locationList.clear()
            locationList.addAll(locationNames)
        }
    }

    Column (
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {submissionType = SubmissionType.LOCATION}
            ) {
                Text(text = stringResource(R.string.location))
            }
            Button(
                onClick = {submissionType = SubmissionType.CATEGORY}
            ) {
                Text(text = stringResource(R.string.category))
            }
            Button(
                onClick = {submissionType = SubmissionType.POINTOFINTEREST}
            ) {
                Text(text = stringResource(R.string.pointOfInterest))
            }
        }

        when(submissionType) {
            SubmissionType.LOCATION -> SubmitLocation(
                imagePath = viewModel.imagePath,
                geoPoint = geoPoint,
                viewModel = viewModel,
                navController = navController
            )
            SubmissionType.CATEGORY -> SubmitCategory(
                imagePath = viewModel.imagePath,
                viewModel = viewModel,
                navController = navController
            )
            SubmissionType.POINTOFINTEREST -> SubmitPointOfInterest(
                geoPoint = geoPoint,
                locationList = locationList,
                categoryList = categoryList,
                viewModel = viewModel,
                navController = navController,
                imagePath = viewModel.imagePath
            )
        }
    }
}

@Composable
fun SubmitLocation(
    imagePath : MutableState<String?>,
    geoPoint : GeoPoint,
    viewModel : FirebaseViewModel,
    navController: NavController
) {
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        uri ->
        if(uri == null) {
            imagePath.value = null
            return@rememberLauncherForActivityResult
        }

        imagePath.value = FileUtils.createFileFromUri(context, uri)
    }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    )
    {
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text(text = stringResource(R.string.name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text(text = stringResource(R.string.description)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
        )
        {
            val latitudeString = geoPoint.latitude.toString()
            val longitudeString = geoPoint.longitude.toString()
            val latitudeFormated = latitudeString.substring(0, latitudeString.indexOf(".") + 8)
            val longitudeFormated = longitudeString.substring(0, longitudeString.indexOf(".") + 8)

            OutlinedTextField(
                value = latitudeFormated,
                onValueChange = { /*unchangeable*/ },
                label = { Text(text = stringResource(R.string.latitude)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = longitudeFormated,
                onValueChange = { /*unchangeable*/ },
                label = { Text(text = stringResource(R.string.longitude)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier
            .padding(8.dp)
            .size(300.dp)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
            .clickable { galleryLauncher.launch(PickVisualMediaRequest()) }

        ) {
            if (imagePath.value == null) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Default Background Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                AsyncImage(
                    model = imagePath.value,
                    contentDescription = "User Selected Background Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
        Button(onClick = {
            imagePath.value?.let {
                viewModel.submitLocation(
                    name.value,
                    description.value,
                    geoPoint.latitude,
                    geoPoint.longitude,
                    it,
                    FAuthUtil.currentUser?.email ?: "error"
                )
            }
            navController.navigateUp()
        }) {
            Text(text = stringResource(R.string.submit))
        }
    }
}

@Composable
fun SubmitCategory(
    imagePath : MutableState<String?>,
    viewModel: FirebaseViewModel,
    navController: NavController
) {
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
            uri ->
        if(uri == null) {
            imagePath.value = null
            return@rememberLauncherForActivityResult
        }

        imagePath.value = FileUtils.createFileFromUri(context, uri)
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    )
    {
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text(text = stringResource(R.string.name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text(text = stringResource(R.string.description)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier
            .padding(8.dp)
            .size(300.dp)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
            .clickable { galleryLauncher.launch(PickVisualMediaRequest()) }

        ) {
            if (imagePath.value == null) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Default Background Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                AsyncImage(
                    model = imagePath.value,
                    contentDescription = "User Selected Background Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
        Button(onClick = {
            imagePath.value?.let {
                viewModel.submitCategory(
                    name.value,
                    description.value,
                    it,
                    FAuthUtil.currentUser?.email ?: "error"
                )
            }
            navController.navigateUp()
        }) {
            Text(text = stringResource(R.string.submit))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitPointOfInterest(
    geoPoint : GeoPoint,
    viewModel: FirebaseViewModel,
    locationList : MutableList<String>,
    categoryList : MutableList<String>,
    imagePath : MutableState<String?>,
    navController: NavController
) {
    val name = remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    var isLocationExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
            uri ->
        if(uri == null) {
            imagePath.value = null
            return@rememberLauncherForActivityResult
        }

        imagePath.value = FileUtils.createFileFromUri(context, uri)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(
                rememberScrollState(),
                enabled = true,
                )
    )
    {
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text(text = stringResource(R.string.name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text(text = stringResource(R.string.description)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
        )
        {
            val latitudeString = geoPoint.latitude.toString()
            val longitudeString = geoPoint.longitude.toString()
            val latitudeFormated = latitudeString.substring(0, latitudeString.indexOf(".") + 8)
            val longitudeFormated = longitudeString.substring(0, longitudeString.indexOf(".") + 8)
            OutlinedTextField(
                value = latitudeFormated,
                onValueChange = { /*unchangeable*/ },
                label = { Text(text = stringResource(R.string.latitude)) },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            Spacer(modifier = Modifier.width(6.dp))


            OutlinedTextField(
                value = longitudeFormated,
                onValueChange = { /*unchangeable*/ },
                label = { Text(text = stringResource(R.string.longitude)) },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }

        ExposedDropdownMenuBox(
            expanded = isLocationExpanded,
            onExpandedChange = {
                isLocationExpanded = it
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            TextField(
                value = location,
                onValueChange = {location = it},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLocationExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                label = { Text(text = stringResource(R.string.location)) },
                modifier = Modifier.menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = isLocationExpanded,
                onDismissRequest = { isLocationExpanded = false },

            ) {
                locationList.forEach { locationItem ->
                    DropdownMenuItem(
                        text = { Text(text = locationItem) },
                        onClick = {
                            isLocationExpanded = false
                            location = locationItem
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = isCategoryExpanded,
            onExpandedChange = {
                isCategoryExpanded = it
            },
            modifier = Modifier.padding(16.dp)
        ) {
            TextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                label = { Text(text = stringResource(R.string.category)) },
                modifier = Modifier.menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = isCategoryExpanded,
                onDismissRequest = { isCategoryExpanded = false },
            ) {
                categoryList.forEach { categoryItem ->
                    DropdownMenuItem(
                        text = { Text(text = categoryItem) },
                        onClick = {
                            isCategoryExpanded = false
                            category = categoryItem
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier
            .padding(8.dp)
            .size(300.dp)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
            .clickable { galleryLauncher.launch(PickVisualMediaRequest()) }

        ) {
            if (imagePath.value == null) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Default Background Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                AsyncImage(
                    model = imagePath.value,
                    contentDescription = "User Selected Background Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            imagePath.value?.let {
                viewModel.submitPointOfInterest(
                    name.value,
                    location,
                    category,
                    description.value,
                    geoPoint.latitude,
                    geoPoint.longitude,
                    it,
                    FAuthUtil.currentUser?.email ?: "error"
                )
            }
            navController.navigateUp()
        }) {
            Text(text = stringResource(R.string.submit))
        }
    }
}