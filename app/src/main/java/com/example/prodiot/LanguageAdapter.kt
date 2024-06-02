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
import com.example.prodiot.Step
import com.google.firebase.database.*
import org.greenrobot.eventbus.EventBus

//data class Step(val title: String, val content: String, val language: String)

class StepAdapter(private val steps: List<Step>, private val languageFilter: String) : RecyclerView.Adapter<StepAdapter.StepViewHolder>() {

    inner class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.categoryAuthorTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.normal_post_layout, parent, false)
        return StepViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = steps[steps.size - position -1]

        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                val context = holder.itemView.context
                val intent = Intent(context, CodeStepView::class.java)
                val selectedKey = step.key
                intent.putExtra("selected_item", selectedKey)
                context.startActivity(intent)

                // Firebase Realtime Database에서 views 값을 1 증가시키기
                val firebaseDatabase = FirebaseDatabase.getInstance()
                val stepsRef = firebaseDatabase.reference.child("steps")
                val stepKey = step.key.toString()

                stepsRef.child(stepKey).child("views").runTransaction(object : Transaction.Handler {
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
        if (step.language == languageFilter) {
            holder.titleTextView.text = step.title
            holder.contentTextView.text = step.author
        } else {
            // 카테고리에 맞지 않는 게시글은 뷰에서 숨깁니다.
            holder.itemView.visibility = View.GONE
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.height = 0
            params.width = 0
        }
    }

    override fun getItemCount(): Int {
        return steps.size
    }
}

