package com.example.mddassignment1

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class LetterActivity : AppCompatActivity() {

    private var currentLetterIndex = 0
    private val letters = arrayOf<String>("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z")
    private val sharedPrefFile = "SharedPrefs"
    private var isOverview = false

    // Variables to assist in storing and reading files externally
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_letter)

        val letter = intent.getStringExtra(LETTER)
        currentLetterIndex = getIndexFromLetter(letter.toString())

        // Requesting permission to load from and save images to SD Card
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
            isWritePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWritePermissionGranted
        }
        requestPermission()

        /* Save the images to SD card
        val dir = File(getExternalStorageDirectory(), "AlphabetBookImages") // make folder to store images if does not yet exist
        if (!dir.exists()) {
            Log.d("img_folder", dir.mkdirs().toString())
        }
        else
            Log.d("img_folder","Folder already exists")
        for (i in letters.indices) {
            // Saving images in a different thread as to not block main thread
            GlobalScope.launch {
                saveImageToSD(i)
            }
        } */

        // Open up on desired letter
        updateLetterPage()
    }


    private fun requestPermission(){
        // To read and write to SD card
        val isReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val isWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdkLevel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        isReadPermissionGranted = isReadPermission
        isWritePermissionGranted = isWritePermission || minSdkLevel

        val permissionRequest = mutableListOf<String>()
        if (!isWritePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!isReadPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    /* Save images to the external storage of phone
    private fun saveImageToSD(letterIndex: Int) {
        val imgName = "slide" + String.format("%02d", (letterIndex + 1)) + ".gif"
        val dir = File(getExternalStorageDirectory(), "AlphabetBookImages")

        // store image on SD Card if does not yet exist
        val img = File(dir.absolutePath, imgName);
        if (!img.exists()) {
            val drawableResourceId = this.resources.getIdentifier(imgName, "drawable", this.packageName) //corresponding image named using its position in the alphabet
            var bm = BitmapFactory.decodeResource(resources, drawableResourceId)

            try {
                val outStream: FileOutputStream? = FileOutputStream(img)
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream) // saving image to external storage
                outStream?.flush()
                outStream?.close()
                Log.d("img_save","Image saved succesfully")
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
        else
            Log.d("img_save","Image already exists")
    }
    */

    private fun loadImage() {
        val imgName = "slide" + String.format("%02d", (currentLetterIndex + 1)) // corresponding image named using its position in the alphabet
        val drawableResourceId = this.resources.getIdentifier(imgName, "drawable", this.packageName)

        // Loading images in a different thread as to not block main thread
        GlobalScope.launch {
            var bm = BitmapFactory.decodeResource(resources, drawableResourceId)
            val imgLetter: ImageView = findViewById(R.id.imgLetter)
            imgLetter.setImageBitmap(bm)
        }

        /* Used to load images from external storage rather than drawable
        GlobalScope.launch {
            val file = File(
                getExternalStorageDirectory().toString() + "/AlphabetBookImages/slide" + String.format(
                    "%02d",
                    (currentLetterIndex + 1)
                ) + ".gif"
            )
            val imgLetter: ImageView = findViewById(R.id.imgLetter)
            val bm = BitmapFactory.decodeFile(file.absolutePath)
            imgLetter.setImageBitmap(bm)
        }
         */
    }


    fun goToFirstLetterPage(view: View) {
        isOverview = false
        currentLetterIndex = 0
        updateLetterPage()
    }
    fun goToLastLetterPage(view: View) {
        isOverview = false
        currentLetterIndex = 25
        updateLetterPage()
    }
    fun nextLetter(view: View) {
        isOverview = false
        currentLetterIndex += 1
        updateLetterPage()
    }
    fun previousLetter(view: View) {
        isOverview = false
        currentLetterIndex -= 1
        updateLetterPage()
    }
    fun goToOverviewPage(view: View) {
        isOverview = true
        saveData() // app is no longer on the
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    private fun updateLetterPage() {
        // Disable next/last and previous/first button if on last or first letter respectively
        val prevButton: Button = findViewById(R.id.btnPrevious)
        prevButton.isEnabled = currentLetterIndex != 0;
        val firstButton: Button = findViewById(R.id.btnFirstPage)
        firstButton.isEnabled = currentLetterIndex != 0;

        val nextButton: Button = findViewById(R.id.btnNext)
        nextButton.isEnabled = currentLetterIndex != 25;
        val lastButton: Button = findViewById(R.id.btnLastPage)
        lastButton.isEnabled = currentLetterIndex != 25;

        // Update letter description
        val textView: TextView = findViewById(R.id.textLetterDesc)
        textView.text = "The letter \"" + letters[currentLetterIndex] +  "\" is letter " + (currentLetterIndex+1) + " of 26 in the alphabet"

        // Update Image
        loadImage()

        // Update Title
        title = letters[currentLetterIndex]

        // Save the new Shared preferences (to start on the letter you left off on)
        saveData()
    }


    private fun getIndexFromLetter(letter: String): Int {
        for (i in letters.indices) {
            if (letters[i] == letter)
                return i
        }
        return 0
    }


    private fun saveData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        editor.apply() {
            putBoolean("is_overview",isOverview) // Were you on a letter page or the overview last
            putString("recent_letter",letters[currentLetterIndex]) // Save data about the last letter you visited
        }.apply()
    }
}