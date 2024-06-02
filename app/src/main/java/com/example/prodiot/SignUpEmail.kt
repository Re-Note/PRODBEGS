package com.example.prodiot

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class SignUpEmail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin1)

        val moveButton = findViewById<Button>(R.id.btn_next)
        val regsterId = findViewById<EditText>(R.id.regster_id)

        moveButton.setOnClickListener{
            // 데이터 저장
            val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("id", regsterId.text.toString())
            editor.apply()
            moveToAnotherPage()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)	//툴바 사용 설정
    }
    private fun moveToAnotherPage(){
        val intent = Intent(this, SignUpPassword::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.left_in, R.anim.left_out)
    }
}
