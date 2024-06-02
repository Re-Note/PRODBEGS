package com.example.prodiot

import OptionMenuHandler
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class CodeStepMenu : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var optionMenuHandler: OptionMenuHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codestepmenu)
        // 툴바 설정
        setSupportActionBar(findViewById(R.id.toolbar))

        // 네비게이션 아이템 클릭 리스너 설정
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationHelper = BottomNavigationHelper(this, bottomNavigationView)

        // 페이지 이동 함수
        val moveToAnotherPage = { destination: Class<*> ->
            startActivity(Intent(this, destination))
            overridePendingTransition(R.anim.left_in, R.anim.left_out)
        }
        // 버튼2를 누르면 다른 페이지로 이동
        findViewById<Button>(R.id.c_button).setOnClickListener {
            moveToAnotherPage(CodeStepList_C::class.java)
        }

        // 버튼3를 누르면 다른 페이지로 이동
        findViewById<Button>(R.id.java_button).setOnClickListener {
            moveToAnotherPage(CodeStepList_JAVA::class.java)
        }

        // 버튼4를 누르면 다른 페이지로 이동
        findViewById<Button>(R.id.python_button).setOnClickListener {
            moveToAnotherPage(CodeStepList_PYTHON::class.java)
        }

        findViewById<Button>(R.id.cs_button).setOnClickListener {
            moveToAnotherPage(CodeStepList_CS::class.java)
        }

        // OptionMenuHandler 초기화
        optionMenuHandler = OptionMenuHandler(this)
    }
    // 옵션 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return optionMenuHandler.handleItemSelected(item) || super.onOptionsItemSelected(item)
    }
}
