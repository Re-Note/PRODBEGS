package com.example.prodiot

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

fun initializeCategorySpinner(activity: AppCompatActivity, categoryTextCallback: (String) -> Unit) {
    val categorySpinner = activity.findViewById<Spinner>(R.id.category_spinner)

    val adapter = ArrayAdapter.createFromResource(
        activity,
        R.array.categorys,
        android.R.layout.simple_spinner_item
    )
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    categorySpinner.adapter = adapter

    categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parentView: AdapterView<*>?,
            selectedItemView: View?,
            position: Int,
            id: Long
        ) {
            val selectedCategory = parentView?.getItemAtPosition(position) as String
            if (selectedCategory != "선택") {
                when (selectedCategory) {
                    "일반 게시글" -> { categoryTextCallback("일반 게시글") }
                    "환경 설정" -> { categoryTextCallback("환경 설정") }
                    "기본 문법" -> { categoryTextCallback("기본 문법") }
                    "심화 문법" -> { categoryTextCallback("심화 문법") }
                    else -> { categoryTextCallback("") }
                }

                // 선택된 언어 메시지 표시
                Toast.makeText(activity, "$selectedCategory 카테고리 선택", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {
            // 선택되지 않았을 때의 처리
        }
    }
}


