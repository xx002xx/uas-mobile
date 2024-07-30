package com.example.uts_mobile_programming

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.uts_mobile_programming.R.string.nonexistent_string

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val message = getString(nonexistent_string)

        val textView = findViewById<TextView>(R.id.textViewHello)
        textView.text = message
    }
}