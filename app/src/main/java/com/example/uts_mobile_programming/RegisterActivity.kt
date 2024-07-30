package com.example.uts_mobile_programming

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nmaeEditText: EditText = findViewById(R.id.name_editText)
        val usernameEditText: EditText = findViewById(R.id.username_editText)
        val passwordEditText: EditText = findViewById(R.id.password_editText)
        val registerButton: Button = findViewById(R.id.register_button)

        registerButton.setOnClickListener {
            val name = nmaeEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Log event
            Log.d("RegisterActivity", "Username: $username, Password: $password")

            // Tampilkan toast
           // Toast.makeText(this@RegisterActivity, "Tombol Register diklik", Toast.LENGTH_SHORT).show()

// Save to Firestore
            val user = hashMapOf(
                "nama" to name,
                "username" to username,
                "password" to password
            )

            val db = FirebaseFirestore.getInstance()

            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("RegisterActivity", "DocumentSnapshot added with ID: ${documentReference.id}")

                    // Display success toast in Indonesian
                    Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()

                    // Navigate to login page after successful save
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.w("RegisterActivity", "Error adding document", e)

                    // Display an error toast in Indonesian
                    Toast.makeText(this@RegisterActivity, "Error menyimpan data pengguna", Toast.LENGTH_SHORT).show()
                }
        }
    }
}