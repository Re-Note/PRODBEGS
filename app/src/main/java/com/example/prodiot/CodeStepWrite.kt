package com.example.prodiot

import OptionMenuHandler
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
class CodeStepWrite : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var titleEditText: EditText
    private lateinit var codeEditText: EditText
    private lateinit var inputEditText: EditText
    private lateinit var createButton: Button
    private lateinit var optionMenuHandler: OptionMenuHandler
    private lateinit var auth: FirebaseAuth
    private lateinit var webViewHelper: StepWebViewHelper
    private lateinit var contentEditText: EditText
    private lateinit var inputcodeEditText: EditText
    private lateinit var outputEditText: EditText
    private lateinit var inputoutputEditText: EditText

    private var langstring = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codestepwrite)



        // 네비게이션 아이템 클릭 리스너 설정
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationHelper = BottomNavigationHelper(this, bottomNavigationView)

        // Firebase 인증 객체 초기화
        auth = FirebaseAuth.getInstance()

        titleEditText = findViewById(R.id.title_edittext)
        codeEditText = findViewById(R.id.code_edittext)
        inputEditText = findViewById(R.id.input_edittext)
        createButton = findViewById(R.id.btn_write_test)
        contentEditText = findViewById(R.id.content_edittext)
        inputcodeEditText = findViewById(R.id.inputcode_edittext)
        outputEditText = findViewById(R.id.output_edittext)
        inputoutputEditText = findViewById(R.id.inputoutput_edittext)

// 선택된 카테고리에 대한 작업을 수행할 함수 호출
        initializeLanguageSpinner(this, codeEditText) { langText ->
            // 콜백 함수 내에서 선택된 카테고리에 대한 작업을 수행할 수 있습니다.
            this.langstring = langText
        }

        var user_name = ""
        val user = auth.currentUser
        val uid = user?.uid
        // Firebase 데이터베이스 인스턴스를 초기화합니다.
        val database = FirebaseDatabase.getInstance()
        // "users" 테이블에 대한 참조를 가져옵니다.
        val usersRef = database.getReference("users")
        Log.d("uid", "post: $uid")
        // 기본 키 값을 가져오기 위한 쿼리를 실행합니다.
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val key = snapshot.key // 기본 키 값을 가져옵니다.
                    // 가져온 기본 키 값을 사용하여 원하는 작업을 수행합니다.
                    // 예: 기본 키 값에 해당하는 사용자 데이터를 가져오거나 수정합니다.

                    Log.d("key", "post: $key")
                    if (uid == key) {
                        val user = snapshot.getValue(User::class.java)

                        Log.d("user", "post: $user")
                        // 가져온 데이터를 뷰에 설정
                        user?.let {
                            user_name = user.name.toString()
                            Log.d("user.name", "post: ${user.name}")
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리를 수행합니다.
            }
        })

        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val code = codeEditText.text.toString().trim()
            val user = auth.currentUser
            val author = user_name
            val input = inputEditText.text.toString().trim()
            val timestamp = Date(System.currentTimeMillis())
            val langstring = langstring.toString().trim()
            val content = contentEditText.text.toString().trim()
            val inputcode = inputcodeEditText.text.toString().trim()
            val output = outputEditText.text.toString().trim()
            val inputoutput = inputoutputEditText.text.toString().trim()

            if (title.isNotEmpty() && code.isNotEmpty() && author.isNotEmpty()) {
                saveStep(title, code, author, input, timestamp, langstring, content, inputcode, output, inputoutput)
                val intent = Intent(this, CodeStepList::class.java)
                startActivity(intent)
                finish()
            }
        }

        val webView: WebView = findViewById(R.id.webView)
        val movebutton = findViewById<Button>(R.id.btn_run)
        movebutton.setOnClickListener {
            val sharedPref = getSharedPreferences("step_data", Context.MODE_PRIVATE)
            val progressDialog = CustomProgressDialog(this)
            progressDialog.show()
            val editor = sharedPref.edit()
            editor.putString("CodeString", codeEditText.text.toString())
            Log.d("MainActivity", "Output Text: ${codeEditText.toString()}")
            editor.putString("InputString", inputEditText.text.toString())
            editor.apply()

            webViewHelper = StepWebViewHelper(this)
            webViewHelper.configureWebView(webView)
            webViewHelper.submitCode(webView, progressDialog){

            }
        }
    }

    private fun saveStep(
        title: String,
        code: String,
        author: String,
        input: String,
        timestamp: Date,
        langstring: String,
        content: String,
        inputcode: String,
        output: String,
        inputoutput: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val stepsRef = database.reference.child("steps")
        val newstepRef = stepsRef.push()

        val step = HashMap<String, Any>()
        step["title"] = title
        step["code"] = code
        step["author"] = author
        step["input"] = input
        step["timestamp"] = timestamp
        step["language"] = langstring
        step["content"] = content
        step["inputcode"] = inputcode
        step["output"] = output
        step["inputoutput"] = inputoutput

        newstepRef.setValue(step).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 게시글 저장 성공
                titleEditText.text.clear()
                codeEditText.text.clear()
                inputEditText.text.clear()
                contentEditText.text.clear()
                inputcodeEditText.text.clear()
                outputEditText.text.clear()
                inputoutputEditText.text.clear()
            } else {
                // 게시글 저장 실패
            }
        }

        // 툴바 설정
        setSupportActionBar(findViewById(R.id.toolbar))

        // 페이지 이동 함수
        val moveToAnotherPage = { destination: Class<*> ->
            startActivity(Intent(this, destination))
            overridePendingTransition(R.anim.left_in, R.anim.left_out)
        }
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


}
