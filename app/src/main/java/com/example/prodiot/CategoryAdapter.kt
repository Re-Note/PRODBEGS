package com.example.prodiot

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prodiot.FreeBoardView
import com.example.prodiot.Post
import com.google.firebase.database.*
import org.greenrobot.eventbus.EventBus

//data class Post(val title: String, val content: String, val category: String)

class PostAdapter(private val posts: List<Post>, private val categoryFilter: String) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.categoryAuthorTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.normal_post_layout, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                val context = holder.itemView.context
                val intent = Intent(context, FreeBoardView::class.java)
                val selectedKey = post.key
                intent.putExtra("selected_item", selectedKey)
                context.startActivity(intent)

                // Firebase Realtime Database에서 views 값을 1 증가시키기
                val firebaseDatabase = FirebaseDatabase.getInstance()
                val postsRef = firebaseDatabase.reference.child("posts")
                val postKey = post.key.toString()

                postsRef.child(postKey).child("views").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val views = mutableData.getValue(Int::class.java) ?: 0
                        mutableData.value = views + 1
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(
                        databaseError: DatabaseError?,
                        committed: Boolean,
                        dataSnapshot: DataSnapshot?
                    ) {
                        if (databaseError != null) {
                            Log.d("TAG", "onComplete: ${databaseError.message}")
                        }
                    }
                })

            }
        }

        // 카테고리 필터링
        if (post.category == categoryFilter) {
            holder.titleTextView.text = post.title
            holder.contentTextView.text = post.author
        } else {
            // 카테고리에 맞지 않는 게시글은 뷰에서 숨깁니다.
            holder.itemView.visibility = View.GONE
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.height = 0
            params.width = 0
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}

