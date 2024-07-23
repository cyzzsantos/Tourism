package pt.isec.a2020133455.tp1.utils

import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import pt.isec.a2020133455.tp1.FirebaseViewModel
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class LocationItem(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String,
    val author: String,
    val nrApprovements : Int)

class CategoryItem(val name: String,
                   val description: String,
                   val imagePath: String,
                   val author: String,
                   val nrApprovements: Int)

class PointOfInterestItem(val name: String,
                          val description: String,
                          val location: String,
                          val category: String,
                          val latitude: Double,
                          val longitude: Double,
                          val imagePath: String,
                          val author: String,
                          val nrApprovements: Int)

class FStorageUtil {
    companion object {
        fun submitPointOfInterest(name : String,
                                  location: String,
                                  category: String,
                                  description: String,
                                  latitude: Double,
                                  longitude: Double,
                                  imagePath: String,
                                  author: String,
                                  onResult: (Throwable?) -> Unit) {
            val db = Firebase.firestore
            val data = hashMapOf(
                "name" to name,
                "description" to description,
                "location" to location,
                "category" to category,
                "latitude" to latitude,
                "longitude" to longitude,
                "author" to author,
                "nrApprovements" to 0,
            )

            db.collection("PointsOfInterest").document(name).set(data, SetOptions.merge())
                .addOnCompleteListener { result ->
                    onResult(result.exception)
                    if(result.exception == null)
                        uploadFile(imagePath, "PointsOfInterest", name)
                }
        }

        fun submitCategory(name : String,
                           description: String,
                           imagePath: String,
                           author: String,
                            onResult: (Throwable?) -> Unit) {
            val db = Firebase.firestore
            val data = hashMapOf(
                "name" to name,
                "description" to description,
                "imagePath" to imagePath,
                "author" to author,
                "nrApprovements" to 0,
            )

            db.collection("Categories").document(name).set(data)
                .addOnCompleteListener { result ->
                    onResult(result.exception)
                    if(result.exception == null)
                        uploadFile(imagePath, "Categories", name)
                }
        }

        fun submitLocation(name : String,
                           description: String,
                           latitude: Double,
                           longitude: Double,
                           imagePath: String,
                           author: String,
                           onResult: (Throwable?) -> Unit) {
            val db = Firebase.firestore
            val data = hashMapOf(
                "name" to name,
                "description" to description,
                "latitude" to latitude,
                "longitude" to longitude,
                "imagePath" to imagePath,
                "author" to author,
                "nrApprovements" to 0,
            )

            db.collection("Locations").document(name).set(data)
                .addOnCompleteListener { result ->
                    onResult(result.exception)
                    uploadFile(imagePath, "Locations", name)
                }
        }

        fun addCategoryToCategoriesList(collectionName: String) {
            val db = Firebase.firestore
            val listOfCollectionsRef = db.collection("Categories")
            val newCollectionDocument = listOfCollectionsRef.document()

            val data = hashMapOf(
                "categoryName" to collectionName
            )

            newCollectionDocument.set(data)
                .addOnSuccessListener {
                    // Collection name added successfully
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        }

        fun addLocationToLocationsList(collectionName: String) {
            val db = Firebase.firestore
            val listOfCollectionsRef = db.collection("Locations")
            val newCollectionDocument = listOfCollectionsRef.document()

            val data = hashMapOf(
                "locationName" to collectionName
            )

            newCollectionDocument.set(data)
                .addOnSuccessListener {
                    // Collection name added successfully
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        }

        /*fun updateDataInFirestore(onResult: (Throwable?) -> Unit) {
            val db = Firebase.firestore
            val v = db.collection("Scores").document("Level1")

            v.get(Source.SERVER)
                .addOnSuccessListener {
                    val exists = it.exists()
                    Log.i("Firestore", "updateDataInFirestore: Success? $exists")
                    if (!exists) {
                        onResult(Exception("Doesn't exist"))
                        return@addOnSuccessListener
                    }
                    val value = it.getLong("nrgames") ?: 0
                    v.update("nrgames", value + 1)
                    onResult(null)
                }
                .addOnFailureListener { e ->
                    onResult(e)
                }
        }

        fun updateDataInFirestoreTrans(onResult: (Throwable?) -> Unit) {
            val db = Firebase.firestore
            val v = db.collection("Scores").document("Level1")

            db.runTransaction { transaction ->
                val doc = transaction.get(v)
                if (doc.exists()) {
                    val newnrgames = (doc.getLong("nrgames") ?: 0) + 1
                    val newtopscore = (doc.getLong("topscore") ?: 0) + 100
                    transaction.update(v, "nrgames", newnrgames)
                    transaction.update(v, "topscore", newtopscore)
                    null
                } else
                    throw FirebaseFirestoreException(
                        "Doesn't exist",
                        FirebaseFirestoreException.Code.UNAVAILABLE
                    )
            }.addOnCompleteListener { result ->
                onResult(result.exception)
            }
        }*/

        fun removeDataFromFirestore(onResult: (Throwable?) -> Unit) {
            val db = Firebase.firestore
            val v = db.collection("Scores").document("Level1")

            v.delete()
                .addOnCompleteListener { onResult(it.exception) }
        }

        private var listenerRegistration: ListenerRegistration? = null

        fun startObserver(onNewValues: (Long, Long) -> Unit) {
            stopObserver()
            val db = Firebase.firestore
            listenerRegistration = db.collection("Scores").document("Level1")
                .addSnapshotListener { docSS, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    if (docSS != null && docSS.exists()) {
                        val nrgames = docSS.getLong("nrgames") ?: 0
                        val topscore = docSS.getLong("topscore") ?: 0
                        Log.i("Firestore", "$nrgames : $topscore")
                        onNewValues(nrgames, topscore)
                    }
                }
        }

        fun stopObserver() {
            listenerRegistration?.remove()
        }

// Storage

        fun getFileFromAsset(assetManager: AssetManager, strName: String): InputStream? {
            var istr: InputStream? = null
            try {
                istr = assetManager.open(strName)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return istr
        }

//https://firebase.google.com/docs/storage/android/upload-files

        fun uploadFile(imagePath: String, collection: String, document: String) {
            val storage = Firebase.storage
            val pathString = "images/${UUID.randomUUID()}.jpg"
            val ref1 = storage.reference
            val ref2 = ref1.child(pathString)
            val stream = FileInputStream(File(imagePath))

            val uploadTask = ref2.putStream(stream)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref2.downloadUrl // Retrieve the download URL
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString() // Get the URL as a string
                    val data = hashMapOf(
                        "imagePath" to downloadUrl
                    )
                    Firebase.firestore.collection(collection).document(document).set(data, SetOptions.merge())
                } else {
                    println("Error uploading image")
                }
            }
        }

        fun retrieveFiles(collection : String) {
            val db = Firebase.firestore
            val paths = mutableListOf<String>()
            val collections = db.collection(collection).get()
                .addOnSuccessListener {
                    documents ->

                    for(document in documents)
                        paths.add(document.get("imagePath").toString())
                }

            for(path in paths) {
                var storageRef = Firebase.storage.reference
                var ref = storageRef.child(path)

                ref.getBytes(5 * 1024 * 1024).addOnSuccessListener {

                }
            }
        }
    }
}