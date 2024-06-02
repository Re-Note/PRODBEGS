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

class SignUpPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin2)

        val moveButton = findViewById<Button>(R.id.btn_next2)
        val regsterPw = findViewById<EditText>(R.id.regster_pw)

        moveButton.setOnClickListener{
            val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("pw", regsterPw.text.toString())
            editor.apply()
            moveToAnotherPage()
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)	//툴바 사용 설정
    }
    private fun moveToAnotherPage(){
        val intent = Intent(this, SignUpName::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.left_in, R.anim.left_out)
    }
}
