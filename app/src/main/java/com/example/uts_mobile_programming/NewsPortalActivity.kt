package com.example.uts_mobile_programming

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NewsPortalActivity : AppCompatActivity() {
    private lateinit var newsTitleEditText: EditText
    private lateinit var newsEditText: EditText
    private lateinit var uploadFileButton: Button
    private lateinit var postNewsButton: Button
    private var selectedFileUri: Uri? = null
    private val PICK_FILE_REQUEST = 1

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var newsListView: ListView
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_portal)
        newsListView = findViewById(R.id.news_list_view)
        newsTitleEditText = findViewById(R.id.news_title_edit_text)
        newsEditText = findViewById(R.id.news_edit_text)
        uploadFileButton = findViewById(R.id.upload_file_button)
        postNewsButton = findViewById(R.id.post_news_button)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

        uploadFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }

        postNewsButton.setOnClickListener {
            val newsTitle = newsTitleEditText.text.toString().trim()
            val newsText = newsEditText.text.toString().trim()

            if (newsTitle.isEmpty() || newsText.isEmpty()) {
                Toast.makeText(this, "Judul dan teks berita tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedFileUri != null) {
                uploadFileAndPostNews(newsTitle, newsText, selectedFileUri!!)
            } else {
                postNews(newsTitle, newsText, null)
            }
        }

        fetchNews()


    }

    private fun fetchNews() {
        db.collection("news")
            .get()
            .addOnSuccessListener { result ->
                val newsList = mutableListOf<NewsItem>()
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val date = document.getString("date") ?: ""
                    val time = document.getString("time") ?: ""
                    val imageUrl = document.getString("fileUrl")

                    val newsItem = NewsItem(
                        title = title,
                        content = content,
                        imageUrl = imageUrl,
                        date =  getCurrentDate(),
                        time =  getCurrentTime()
                    )

                    newsList.add(newsItem)
                }
                val adapter = NewsListAdapter(this, newsList)
                newsListView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil berita", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk mendapatkan tanggal saat ini dalam format "dd MMMM yyyy"
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Fungsi untuk mendapatkan waktu saat ini dalam format "HH:mm"
    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedFileUri = data.data
            Toast.makeText(this, "File berhasil dipilih: ${selectedFileUri?.path}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadFileAndPostNews(newsTitle: String, newsText: String, fileUri: Uri) {
        showLoader()
        val storageRef = storage.reference.child("uploads/${fileUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                postNews(newsTitle, newsText, uri.toString())
            }
        }.addOnFailureListener { e ->
            Log.w("NewsPortalActivity", "Error uploading file", e)
            Toast.makeText(this, "Gagal mengunggah file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun postNews(newsTitle: String, newsText: String, fileUrl: String?) {
        val news = hashMapOf(
            "title" to newsTitle,
            "content" to newsText,
            "fileUrl" to fileUrl
        )

        db.collection("news")
            .add(news)
            .addOnSuccessListener { documentReference ->
                Log.d("NewsPortalActivity", "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(this, "Berita berhasil diposting", Toast.LENGTH_SHORT).show()
                // Refresh the ListView or take any additional actions
                fetchNews()
                hideLoader()
            }
            .addOnFailureListener { e ->
                Log.w("NewsPortalActivity", "Error adding document", e)
                Toast.makeText(this, "Gagal memposting berita", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
    }
    private fun showLoader() {
        progressDialog.show()
    }

    private fun hideLoader() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}