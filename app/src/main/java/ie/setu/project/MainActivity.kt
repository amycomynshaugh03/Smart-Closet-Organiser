package ie.setu.project

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.utils.log
import timber.log.Timber
import timber.log.Timber.i

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //private var counter = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Timber.plant(Timber.DebugTree())
        i("Welcome to your Closet Organiser!")


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAdd.setOnClickListener() {
            i("add Button Pressed")
        }



    }
}
