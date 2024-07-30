package com.example.uts_mobile_programming

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Button
import android.widget.Toast

class NewsListAdapter(context: Context, private val newsList: MutableList<NewsItem>) :
    ArrayAdapter<NewsItem>(context, 0, newsList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater = LayoutInflater.from(context)
            itemView = inflater.inflate(R.layout.news_item, parent, false)
        }

        val currentNews = newsList[position]

        val titleTextView = itemView?.findViewById<TextView>(R.id.title_text_view)
        val contentTextView = itemView?.findViewById<TextView>(R.id.content_text_view)
        val dateTextView = itemView?.findViewById<TextView>(R.id.date_text_view)
        val timeTextView = itemView?.findViewById<TextView>(R.id.time_text_view)
        val imageView = itemView?.findViewById<ImageView>(R.id.news_image)
        val deleteButton = itemView?.findViewById<ImageView>(R.id.delete_button)
        val editButton = itemView?.findViewById<ImageView>(R.id.edit_button)

        deleteButton?.setOnClickListener {
            // Remove item from list
            newsList.removeAt(position)
            notifyDataSetChanged()

            // Optionally, remove item from database
            removeFromDatabase(currentNews)
        }

        editButton?.setOnClickListener {
            showEditDialog(position, currentNews)
        }

        titleTextView?.text = currentNews.title
        contentTextView?.text = currentNews.content
        dateTextView?.text = currentNews.date
        timeTextView?.text = currentNews.time

        if (currentNews.imageUrl != null) {
            // Load image using Glide or another image loading library
            Glide.with(context)
                .load(currentNews.imageUrl)
                .into(imageView!!)
            imageView.visibility = View.VISIBLE
        } else {
            imageView?.visibility = View.GONE
        }

        return itemView!!
    }

    private fun showEditDialog(position: Int, newsItem: NewsItem) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_news, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.edit_title)
        val contentEditText = dialogView.findViewById<EditText>(R.id.edit_content)
        val saveButton = dialogView.findViewById<Button>(R.id.save_button)

        titleEditText.setText(newsItem.title)
        contentEditText.setText(newsItem.content)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit News")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString().trim()
            val newContent = contentEditText.text.toString().trim()

            if (newTitle.isNotEmpty() && newContent.isNotEmpty()) {
                updateNewsItem(position, newsItem.copy(title = newTitle, content = newContent))
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateNewsItem(position: Int, updatedNewsItem: NewsItem) {
        val db = FirebaseFirestore.getInstance()

        // Assuming you have a reference to Firestore
        val newsRef = db.collection("news")

        // Query to find the document to update
        newsRef
            .whereEqualTo("title", updatedNewsItem.title) // Adjust if needed
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "News item not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    // Use document ID to update
                    newsRef.document(document.id)
                        .set(updatedNewsItem.toMap()) // Convert NewsItem to a Map
                        .addOnSuccessListener {
                            Log.d("NewsListAdapter", "DocumentSnapshot successfully updated!")
                            // Update the local list and refresh the view
                            newsList[position] = updatedNewsItem
                            notifyDataSetChanged()
                            Toast.makeText(context, "News item updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w("NewsListAdapter", "Error updating document", e)
                            Toast.makeText(context, "Failed to update news item", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("NewsListAdapter", "Error getting documents", e)
                Toast.makeText(context, "Failed to fetch news item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromDatabase(newsItem: NewsItem) {
        // Assuming you have a reference to Firestore
        val db = FirebaseFirestore.getInstance()

        // Example collection name, adjust to your actual Firestore collection
        db.collection("news")
            .whereEqualTo("title", newsItem.title)  // Use appropriate query to identify the item
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("news").document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("NewsListAdapter", "DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("NewsListAdapter", "Error deleting document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("NewsListAdapter", "Error getting documents", e)
            }
    }
}
