package com.example.prodiot

import OptionMenuHandler
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Suppress("DEPRECATION")
class FreeBoardWrite : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var codeEditText: EditText
    private lateinit var inputEditText: EditText
    private lateinit var createButton: Button
    private lateinit var optionMenuHandler: OptionMenuHandler
    private lateinit var auth: FirebaseAuth
    private lateinit var webViewHelper: PostWebViewHelper
    private val selectedImageUris: ArrayList<Uri> = ArrayList()
    private val PICK_IMAGE_REQUEST = 123

    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    // 외부에서 사용할 변수를 클래스 수준에 정의합니다.
    private var categorystring = ""
    private var langstring = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_freeboardwrite)

        // 네비게이션 아이템 클릭 리스너 설정
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationHelper = BottomNavigationHelper(this, bottomNavigationView)

        imageRecyclerView = findViewById(R.id.image_recyclerView)
        imageAdapter = ImageAdapter()
        imageRecyclerView.layoutManager = LinearLayoutManager(this)
        imageRecyclerView.adapter = imageAdapter

        // Firebase 인증 객체 초기화
        auth = FirebaseAuth.getInstance()

        titleEditText = findViewById(R.id.title_edittext)
        contentEditText = findViewById(R.id.content_edittext)
        codeEditText = findViewById(R.id.code_edittext)
        inputEditText = findViewById(R.id.input_edittext)
        createButton = findViewById(R.id.btn_write_test)

// 선택된 카테고리에 대한 작업을 수행할 함수 호출
        initializeCategorySpinner(this) { categoryText ->
            // 콜백 함수 내에서 선택된 카테고리에 대한 작업을 수행할 수 있습니다.
            this.categorystring = categoryText
        }

        // 코드 언어 선택 함수 호출
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
            val content = contentEditText.text.toString().trim()
            val code = codeEditText.text.toString().trim()
            val input = inputEditText.text.toString().trim()
            val user = auth.currentUser
            val author = user_name
            val timestamp = Date(System.currentTimeMillis())
            val langstring = langstring
            val categorystring = categorystring
            Log.d("MainActivity", "Output Text: $categorystring")

            if (title.isNotEmpty() && content.isNotEmpty() && author.isNotEmpty()) {
                savePost(title, content, author, code, input, timestamp, selectedImageUris, langstring, categorystring)
                val intent = Intent(this, FreeBoardList::class.java)
                startActivity(intent)
                finish()
            }

        }

        val setupButton: Button = findViewById(R.id.setupButton) // setupButton을 찾음

        setupButton.setOnClickListener {
            // 이미지 리스트 초기화
            selectedImageUris.clear()
            imageAdapter.setImageUrls(emptyList())
            imageAdapter.notifyDataSetChanged()
        }

        val webView: WebView = findViewById(R.id.webView)
        val movebutton = findViewById<Button>(R.id.btn_run)
        val sharedPref = getSharedPreferences("post_data", Context.MODE_PRIVATE)

        movebutton.setOnClickListener {
            val progressDialog = CustomProgressDialog(this)
            progressDialog.show()
            val editor = sharedPref.edit()
            editor.putString("CodeString", codeEditText.text.toString())
            editor.putString("InputString", inputEditText.text.toString())
            editor.apply()
            webViewHelper = PostWebViewHelper(this)
            webViewHelper.configureWebView(webView)
            webViewHelper.submitCode(webView, progressDialog)
        }
        // 툴바 설정
        setSupportActionBar(findViewById(R.id.toolbar))

        // 페이지 이동 함수
        val moveToAnotherPage = { destination: Class<*> ->
            startActivity(Intent(this, destination))
            overridePendingTransition(R.anim.left_in, R.anim.left_out)
        }
        // 이벤트 핸들러
        findViewById<Button>(R.id.uploadButton).setOnClickListener {
            openGallery()
        }
    }

    // 외부에서 사용할 함수를 정의하여 categoryText 값을 반환합니다.
    fun getCategoryText(): String {
        return categorystring
    }

    private fun savePost(
        title: String,
        content: String,
        author: String,
        code: String,
        input: String,
        timestamp: Date,
        imageUriList: ArrayList<Uri>,
        langstring: String,
        categorystring: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val postsRef = database.reference.child("posts")
        val newPostRef = postsRef.push()
        val post = HashMap<String, Any>()
        post["title"] = title
        post["content"] = content
        post["author"] = author
        post["code"] = code
        post["input"] = input
        post["timestamp"] = timestamp
        post["language"] = langstring
        post["category"] = categorystring
        Log.d("MainActivity", "Output Text: $categorystring")

        newPostRef.setValue(post).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 게시글 저장 성공
                titleEditText.text.clear()
                contentEditText.text.clear()
                codeEditText.text.clear()
                inputEditText.text.clear()
                // 이미지 업로드
                if (imageUriList.isNotEmpty()) {
                    uploadImages(newPostRef.key, imageUriList)
                }
            } else {
                // 게시글 저장 실패
            }
        }

    }

    private fun uploadImages(postId: String?, imageUriList: ArrayList<Uri>) {
        val storageRef = FirebaseStorage.getInstance().reference
        val uploadTasks: ArrayList<UploadTask> = ArrayList()
        val imageUrlList: ArrayList<String> = ArrayList()
        for (imageUri in imageUriList) {
            val originalFileName = UUID.randomUUID().toString() // 랜덤 UID 생성
            val imageRef = storageRef.child("images/$originalFileName")
            val uploadTask = imageRef.putFile(imageUri)
            uploadTasks.add(uploadTask)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result?.toString()
                    downloadUrl?.let { imageUrlList.add(it) }
                    // 모든 이미지 다운로드 URL을 얻었는지 확인
                    if (imageUrlList.size == imageUriList.size) {
                        // 이미지 업로드가 완료되면 게시글에 이미지 URL들을 저장
                        postId?.let { saveImageUrls(it, imageUrlList) }
                    }
                } else {
                    // 이미지 다운로드 URL 가져오기 실패
                }
            }
        }
    }



    // 게시글에 이미지 URL들 저장
    private fun saveImageUrls(postId: String, imageUrlList: ArrayList<String>) {
        val database = FirebaseDatabase.getInstance()
        val postsRef = database.reference.child("posts").child(postId)
        val post = HashMap<String, Any>()
        post["imageUrls"] = imageUrlList
        postsRef.updateChildren(post).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 이미지 URL 저장 성공
            } else {
                // 이미지 URL 저장 실패
            }
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
                            val sharedPref3 =
                                getSharedPreferences("user_name", Context.MODE_PRIVATE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    selectedImageUris.add(imageUri)
                }
            } else if (data.data != null) {
                val imageUri: Uri = data.data!!
                selectedImageUris.add(imageUri)
            }
            imageAdapter.setImageUrls(selectedImageUris.map { it.toString() })
            imageAdapter.notifyDataSetChanged()
        }
    }

    // 이미지 선택 버튼 클릭 이벤트 핸들러
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_REQUEST
        )
    }
}
