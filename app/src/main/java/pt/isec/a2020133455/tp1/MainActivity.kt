package pt.isec.a2020133455.tp1

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.osmdroid.config.Configuration
import pt.isec.a2020133455.tp1.ui.screens.CategoryScreen
import pt.isec.a2020133455.tp1.ui.screens.CreditScreen
import pt.isec.a2020133455.tp1.ui.screens.LoginScreen
import pt.isec.a2020133455.tp1.ui.screens.MainScreen
import pt.isec.a2020133455.tp1.ui.screens.PointOfInterestScreen
import pt.isec.a2020133455.tp1.ui.screens.RegisterScreen
import pt.isec.a2020133455.tp1.ui.screens.SpecificPoIScreen
import pt.isec.a2020133455.tp1.ui.screens.SubmissionsPanelScreen
import pt.isec.a2020133455.tp1.ui.theme.TP1Theme


class MainActivity : ComponentActivity() {
    private val app by lazy { application as TP1App }

    private val viewModel : FirebaseViewModel by viewModels {
        FirebaseViewModelFactory(app.locationHandler)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("OSM", MODE_PRIVATE))

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGPSEnabled) {
            AlertDialog.Builder(this)
                .setTitle("Location Access Required")
                .setMessage("Some features require the use of your GPS Location. Please enable it.")
                .setPositiveButton("Enable") { dialog, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        setContent {
            val navController = rememberNavController()
            TP1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "LoginScreen") {
                        composable("LoginScreen") {
                            LoginScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("RegisterScreen") {
                            RegisterScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("MainScreen") {
                            MainScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("SubmitLocationScreen") {
                            SubmissionsPanelScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("CategoryScreen") {
                            CategoryScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("PointOfInterestScreen") {
                            PointOfInterestScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("SpecificPoIScreen") {
                            SpecificPoIScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("CreditScreen") {
                            CreditScreen(navController = navController)
                        }
                    }
                }
            }
        }
        verifyPermissions()

        // Observe changes in locationList
        viewModel.locationList.observe(this) { locations ->
        }

        // Observe changes in categoryList
        viewModel.categoryList.observe(this) { categories ->
        }

        // Observe changes in pointOfInterestList
        viewModel.pointOfInterestList.observe(this) { pointsOfInterest ->
        }

        // Trigger fetching data using ViewModel functions
        viewModel.fetchLocations()
        viewModel.fetchCategories()
        viewModel.fetchPointsOfInterest()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startLocationUpdates()
    }

    private fun verifyPermissions() : Boolean{
        viewModel.coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewModel.backgroundLocationPermission = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else
            viewModel.backgroundLocationPermission = viewModel.coarseLocationPermission || viewModel.fineLocationPermission

        if (!viewModel.coarseLocationPermission && !viewModel.fineLocationPermission) {
            basicPermissionsAuthorization.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return false
        } else
            verifyBackgroundPermission()
        return true
    }

    private val basicPermissionsAuthorization = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.coarseLocationPermission = results[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.fineLocationPermission = results[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        viewModel.startLocationUpdates()
        verifyBackgroundPermission()
    }

    private fun verifyBackgroundPermission() {
        if (!(viewModel.coarseLocationPermission || viewModel.fineLocationPermission))
            return

        if (!viewModel.backgroundLocationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                val dlg = AlertDialog.Builder(this)
                    .setTitle("Background Location")
                    .setMessage(
                        "This application needs your permission to use location while in the background.\n" +
                                "Please choose the correct option in the following screen" +
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                    " (\"${packageManager.backgroundPermissionOptionLabel}\")."
                                else
                                    "."
                    )
                    .setPositiveButton("Ok") { _, _ ->
                        backgroundPermissionAuthorization.launch(
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    }
                    .create()
                dlg.show()
            }
        }
    }

    private val backgroundPermissionAuthorization = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        viewModel.backgroundLocationPermission = result
        Toast.makeText(this,"Background location enabled: $result", Toast.LENGTH_LONG).show()
    }
}