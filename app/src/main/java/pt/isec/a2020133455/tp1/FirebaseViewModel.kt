package pt.isec.a2020133455.tp1
import android.location.Location
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import pt.isec.a2020133455.tp1.utils.CategoryItem
import pt.isec.a2020133455.tp1.utils.FAuthUtil
import pt.isec.a2020133455.tp1.utils.FStorageUtil
import pt.isec.a2020133455.tp1.utils.LocationItem
import pt.isec.a2020133455.tp1.utils.PointOfInterestItem
import pt.isec.a2020133455.tp1.utils.location.LocationHandler

@Suppress("UNCHECKED_CAST")
class FirebaseViewModelFactory(private val locationHandler : LocationHandler) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FirebaseViewModel(locationHandler) as T
    }
}
data class User(val name: String, val email: String,
                val picture: String, val password: String?)

fun FirebaseUser.toUser() : User {
    val displayName = this.displayName ?: ""
    val strEmail = this.email ?: ""
    val picture = this.photoUrl?.toString()

    return User(displayName, strEmail, picture ?: "", null)
}

class FirebaseViewModel(
    private val locationHandler : LocationHandler
) : ViewModel() {
    var coarseLocationPermission = false
    var fineLocationPermission = false
    var backgroundLocationPermission = false
    val imagePath: MutableState<String?> = mutableStateOf(null)

    private val _user = mutableStateOf(FAuthUtil.currentUser?.toUser())
    val user: MutableState<User?>
        get() = _user

    private val _error = mutableStateOf<String?>(null)
    val error: MutableState<String?>
        get() = _error

    private val _currentLocation = MutableLiveData(Location(null))
    val currentLocation : LiveData<Location>
        get() = _currentLocation

    private val _locationList = MutableLiveData<List<LocationItem>>()
    val locationList: LiveData<List<LocationItem>> = _locationList

    private val _categoryList = MutableLiveData<List<CategoryItem>>()
    val categoryList: LiveData<List<CategoryItem>> = _categoryList

    private val _pointOfInterestList = MutableLiveData<List<PointOfInterestItem>>()
    val pointOfInterestList: LiveData<List<PointOfInterestItem>> = _pointOfInterestList

    var chosenLocation : LocationItem? = null
    var chosenCategory : CategoryItem? = null
    var chosenPointOfInterest : PointOfInterestItem? = null

    init {
        locationHandler.onLocation = {
            _currentLocation.value = it
        }
    }

    fun fetchCurrentLocation() {

    }

    fun fetchLocations() {
        getLocations { locations ->
            _locationList.postValue(locations)
        }
    }

    fun fetchCategories() {
        getCategories { categories ->
            _categoryList.postValue(categories)
        }
    }

    fun fetchPointsOfInterest() {
        getPointsOfInterest { pointsOfInterest ->
            _pointOfInterestList.postValue(pointsOfInterest)
        }
    }

    fun clearError() {
        error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    fun createUserWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank())
            return

        viewModelScope.launch {
            FAuthUtil.createUserWithEmail(email, password) { exception ->
                if (exception == null)
                    _user.value = FAuthUtil.currentUser?.toUser()
                _error.value = exception?.message
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank())
            return
        viewModelScope.launch {
            FAuthUtil.signInWithEmail(email, password) { exception ->
                if (exception == null)
                    _user.value = FAuthUtil.currentUser?.toUser()
                _error.value = exception?.message
            }
        }
    }

    fun signOut() {
        FAuthUtil.signOut()
        _user.value = null
        _error.value = null
    }

    fun submitLocation(name: String,
                       description: String,
                       latitude: Double,
                       longitude: Double,
                       imagePath: String,
                       author: String) {
        if(name.isBlank() || description.isBlank() || imagePath.isBlank() || author == "error")
            return

        val descriptionString = description.trim()

        val db = Firebase.firestore
        val docRef = db.collection("Locations").document(name)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    _error.value = "Location already exists"
                } else {
                    viewModelScope.launch {
                        FStorageUtil.submitLocation(
                            name,
                            descriptionString,
                            latitude,
                            longitude,
                            imagePath,
                            author) {
                                exception -> _error.value = exception?.message
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _error.value = e.message
            }
    }

    fun submitCategory(name: String,
                       description: String,
                       imagePath: String,
                       author: String) {
        if(name.isBlank() || description.isBlank() || imagePath.isBlank() || author == "error")
            return

        val descriptionString = description.trim()

        val db = Firebase.firestore
        val docRef = db.collection("Categories").document(name)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    _error.value = "Category already exists"
                } else {
                    viewModelScope.launch {
                        FStorageUtil.submitCategory(
                            name,
                            descriptionString,
                            imagePath,
                            author) {
                                exception -> _error.value = exception?.message
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _error.value = e.message
            }
    }

    fun submitPointOfInterest(name: String,
                              location: String,
                              category: String,
                              description: String,
                              latitude: Double,
                              longitude: Double,
                              imagePath: String,
                              author: String,
    ) {
        if (name.isBlank() || location.isBlank() || category.isBlank() ||
            description.isBlank() || imagePath.isBlank() || author == "error"
        )
            return

        val descriptionString = description.trim()

        val db = Firebase.firestore
        val docRef = db.collection("PointsOfInterest").document(name)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    _error.value = "Point of Interest already exists"
                } else {
                    viewModelScope.launch {
                        FStorageUtil.submitPointOfInterest(
                            name,
                            location,
                            category,
                            descriptionString,
                            latitude,
                            longitude,
                            imagePath,
                            author
                        ) { exception ->
                            _error.value = exception?.message
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _error.value = e.message
            }
    }

    fun getAllCategoriesNames(callback: (List<String>) -> Unit) {
        val db = Firebase.firestore
        val listOfCollectionsRef = db.collection("Categories")

        listOfCollectionsRef.get()
            .addOnSuccessListener { documents ->
                val collectionNames = mutableListOf<String>()

                for (document in documents) {
                    val collectionName = document.getString("name")
                    collectionName?.let {
                        collectionNames.add(it)
                    }
                }
                callback(collectionNames)
            }
            .addOnFailureListener { e ->
                // Handle failure
                callback(emptyList()) // Return an empty list in case of failure
            }
    }

    private fun getLocations(callback: (List<LocationItem>) -> Unit) {
        val db = Firebase.firestore
        val ref = db.collection("Locations")

        ref.get()
            .addOnSuccessListener { documents ->
                val locations = mutableListOf<LocationItem>()

                for (document in documents) {
                    val name = document.getString("name")
                    val description = document.getString("description")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    val image = document.getString("imagePath")
                    val author = document.getString("author")
                    val nrApprovements = document.getLong("nrApprovements")?.toInt()

                    if(name == null || description == null || latitude == null || longitude == null || image == null || author == null || nrApprovements == null)
                        continue

                    val location = LocationItem(name, description, latitude, longitude, image, author, nrApprovements)
                    locations.add(location)
                }
                callback(locations)
            }
            .addOnFailureListener { e ->
                callback(emptyList())
            }
    }

    private fun getCategories(callback: (List<CategoryItem>) -> Unit) {
        val db = Firebase.firestore
        val ref = db.collection("Categories")

        ref.get()
            .addOnSuccessListener { documents ->
                val categories = mutableListOf<CategoryItem>()

                for (document in documents) {
                    val name = document.getString("name")
                    val description = document.getString("description")
                    val author = document.getString("author")
                    val image = document.getString("imagePath")
                    val nrApprovements = document.getLong("nrApprovements")?.toInt()

                    if(name == null || description == null || author == null || image == null || nrApprovements == null)
                        continue

                    val category = CategoryItem(name, description, image, author, nrApprovements)
                    categories.add(category)
                }
                callback(categories)
            }
            .addOnFailureListener { e ->
                callback(emptyList())
            }
    }

    private fun getPointsOfInterest(callback: (List<PointOfInterestItem>) -> Unit) {
        val db = Firebase.firestore
        val ref = db.collection("PointsOfInterest")

        ref.get()
            .addOnSuccessListener { documents ->
                val pointsOfInterest = mutableListOf<PointOfInterestItem>()

                for (document in documents) {
                    val name = document.getString("name")
                    val location = document.getString("location")
                    val category = document.getString("category")
                    val description = document.getString("description")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    val image = document.getString("imagePath")
                    val author = document.getString("author")
                    val nrApprovements = document.getLong("nrApprovements")?.toInt()

                    if(name == null || location == null || category == null || description == null || latitude == null || longitude == null || image == null || author == null || nrApprovements == null)
                        continue

                    val pointOfInterest = PointOfInterestItem(name, description, location, category, latitude, longitude, image, author, nrApprovements)
                    pointsOfInterest.add(pointOfInterest)
                }
                callback(pointsOfInterest)
            }
            .addOnFailureListener { e ->
                callback(emptyList())
            }
    }

    fun getAllLocationsNames(callback: (List<String>) -> Unit) {
        val db = Firebase.firestore
        val listOfCollectionsRef = db.collection("Locations")

        listOfCollectionsRef.get()
            .addOnSuccessListener { documents ->
                val collectionNames = mutableListOf<String>()

                for (document in documents) {
                    val collectionName = document.getString("name")
                    collectionName?.let {
                        collectionNames.add(it)
                    }
                }
                callback(collectionNames)
            }
            .addOnFailureListener { e ->
                // Handle failure
                callback(emptyList()) // Return an empty list in case of failure
            }
    }

    fun startLocationUpdates() {
        if (fineLocationPermission && coarseLocationPermission) {
            locationHandler.startLocationUpdates()
        }
    }

    fun stopLocationUpdates() {
        locationHandler.stopLocationUpdates()
    }
}
