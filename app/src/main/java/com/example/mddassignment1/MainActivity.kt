package com.example.mddassignment1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button

const val LETTER = "com.example.mddassignment1.LETTER"

class MainActivity : AppCompatActivity() {

    private val sharedPrefFile = "SharedPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Overview"
        loadData() // open to the last page visited on this app
    }

    // When any of the 26 letter buttons are pressed
    fun goToLetterPage(view: View) {
        var letter = (view as Button).text.first().toString(); // get the letter of the button pushed to pass to letter activity
        saveData(letter) // saving the last page visited on the app
        val intent = Intent(this, LetterActivity::class.java).apply {
            putExtra(LETTER, letter)
        }
        startActivity(intent)
    }

    //Saving shared preferences to open at last activity
    private fun saveData(letter: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        editor.apply() {
            putBoolean("is_overview",false)
            putString("recent_letter",letter)
        }.apply()
    }

    private fun loadData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val isOverview = sharedPreferences.getBoolean("is_overview",true)
        val recentLetter = sharedPreferences.getString("recent_letter",null)

        // If the last page that the user was on was a specific letter and not the overview page, go to that letter
        if (!isOverview && recentLetter!=null) {
            val intent = Intent(this, LetterActivity::class.java).apply {
                putExtra(LETTER, recentLetter)
            }
            startActivity(intent)
        }
    }

}