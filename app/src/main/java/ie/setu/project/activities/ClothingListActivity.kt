package ie.setu.project.activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import ie.setu.project.R
import ie.setu.project.closet.main.MainApp

class ClothingListActivity : AppCompatActivity() {

    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clothing_list)
        app = application as MainApp
    }
}
