package com.example.prodiot

import OptionMenuHandler
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*

class CodeStepView : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var optionMenuHandler: OptionMenuHandler
    private lateinit var webViewHelper: StepWebViewHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var TitleView: TextView
    private lateinit var ContentView: TextView
    private lateinit var CodeView: TextView
    private lateinit var InputView: TextView
    private lateinit var OutputView: TextView
    private var selectedStepId: String = ""
    private lateinit var commentEditText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codestepview)

        // 네비게이션 아이템 클릭 리스너 설정
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationHelper = BottomNavigationHelper(this, bottomNavigationView)

        TitleView = findViewById(R.id.title_edittext)
        ContentView = findViewById(R.id.content_edittext)
        CodeView = findViewById(R.id.code_edittext)
        InputView = findViewById(R.id.input_edittext)
        OutputView = findViewById(R.id.output_edittext)

        val webView: WebView = findViewById(R.id.webView)
        val movebutton = findViewById<Button>(R.id.btn_run)
        val key = intent.getStringExtra("selected_item")
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uid = user?.uid
        selectedStepId = key.toString()
        // OptionMenuHandler 초기화
        optionMenuHandler = OptionMenuHandler(this)

        fetchUserFromFirebase(uid)
        // 게시물 조회 함수 호출
        retrievePostFromFirebase(key.toString())
        // 툴바 설정
        setSupportActionBar(findViewById(R.id.toolbar))

        // 페이지 이동 함수
        val moveToAnotherPage = { destination: Class<*> ->
            startActivity(Intent(this, destination))
            overridePendingTransition(R.anim.left_in, R.anim.left_out)
        }


        movebutton.setOnClickListener {
            val sharedPref = getSharedPreferences("step_data", Context.MODE_PRIVATE)
            val progressDialog = CustomProgressDialog(this)
            progressDialog.show()
            val editor = sharedPref.edit()
            editor.putString("CodeString", CodeView.text.toString())
            editor.putString("InputString", InputView.text.toString())
            editor.apply()

            webViewHelper = StepWebViewHelper(this)
            webViewHelper.configureWebView(webView)
            webViewHelper.submitCode(webView, progressDialog) {
                // `submitCode`가 완료되고 나서 이 코드가 실행됩니다
                compareTextWithFirebase(key.toString(), OutputView.text.toString())
            }
        }
    }

    // 게시물 조회 함수
    private fun retrievePostFromFirebase(stepId: String) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference = firebaseDatabase.reference.child("steps")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 데이터베이스에서 해당 게시물에 대한 데이터 가져오기
                val stepSnapshot = dataSnapshot.child(stepId)
                val stepId = stepSnapshot.key.toString() // 게시물 아이디 가져오기
                val sharedPref = getSharedPreferences("my_stepId", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("my_stepId", stepId)
                editor.apply()
                Log.d("FreeboardView", "step: $stepSnapshot")
                val step = stepSnapshot.getValue(Step::class.java)
                // 가져온 데이터를 뷰에 설정
                step?.let {
                    TitleView.text = step.title
                    ContentView.text = step.content
                    CodeView.text = step.inputcode
                    InputView.text = step.input
                    findViewById<TextView>(R.id.title).text = step.title
                }
                val sharedPref2 = getSharedPreferences("auth_id", Context.MODE_PRIVATE)
                val editor2 = sharedPref2.edit()
                if (step != null) { //이게 그 물음표 구조
                    editor2.putString("auth_id", step.author)
                }
                editor2.apply()

                val sharedPref5 = getSharedPreferences("step_data", Context.MODE_PRIVATE)
                val editor5 = sharedPref5.edit()
                editor5.putString("LangString", step?.language)
                Log.d("MainActivity", "Output Text: ${step?.language}")
                editor5.apply()
            }
            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
                Log.e("CodeStepView", "Failed to read step data.", error.toException())
            }
        })
    }

    // 옵션 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return optionMenuHandler.handleItemSelected(item) || super.onOptionsItemSelected(item)
    }

    private fun fetchUserFromFirebase(uid: String?) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val key2 = snapshot.key
                    if (uid == key2) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            val sharedPref3 = getSharedPreferences("user_name", Context.MODE_PRIVATE)
                            val editor3 = sharedPref3.edit()
                            editor3.putString("user_name", user.name)
                            editor3.apply()
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리를 수행합니다.
            }
        })
    }

    private fun compareTextWithFirebase(stepId: String, output: String) {
            val firebaseDatabase = FirebaseDatabase.getInstance()
            val databaseReference = firebaseDatabase.reference.child("steps")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // 데이터베이스에서 해당 게시물에 대한 데이터 가져오기
                    val stepSnapshot = dataSnapshot.child(stepId)
                    val stepId = stepSnapshot.key.toString() // 게시물 아이디 가져오기
                    val sharedPref = getSharedPreferences("my_stepId", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("my_stepId", stepId)
                    editor.apply()
                    Log.d("동작됨", "step:")
                    val step = stepSnapshot.getValue(Step::class.java)
                    // 가져온 데이터를 뷰에 설정
                    step?.let {
                        Log.d("step.output","${step.output}")
                        Log.d("output","$output")
                        if (step.output == output) {
                            val answerProgress = answerProgressDialog(this@CodeStepView) // answerProgressDialog 생성
                            answerProgress.show() // answerProgressDialog 표시
                            showToast("정답 입니다. : true")
                        } else {
                            val wrongAnswerProgress = wrong_answerProgressDialog(this@CodeStepView) // wrong_answerProgressDialog 생성
                            wrongAnswerProgress.show() // wrong_answerProgressDialog 표시
                            showToast("오답 입니다. : false")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // 오류 처리
                    Log.e("CodeStepView", "Failed to read step data.", error.toException())
                }

            })

        }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
