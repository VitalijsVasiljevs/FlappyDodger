package com.example.flappydodger

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.View
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // iegūstam saites uz pogām
        val exitButton: Button = findViewById(R.id.exitButton)
        val startButton: Button = findViewById(R.id.button)

        // reaģē uz izejas pogas nospiešanu
        exitButton.setOnClickListener {
            finish()
            System.exit(0) // iziet no programmas
        }

        // reaģē uz starta pogas nospiešanu
        startButton.setOnClickListener {
            openGameActivity()
        }
    }

    // GameActivity atvēršanas metode
    private fun openGameActivity() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
}
