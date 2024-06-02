package com.example.prodiot

import OptionMenuHandler
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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


class CategoryList : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var recyclerView1: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var recyclerView3: RecyclerView
    private lateinit var recyclerView4: RecyclerView
    private val categoryFilter1 = "일반 게시글"
    private val categoryFilter2 = "환경 설정"
    private val categoryFilter3 = "기본 문법"
    private val categoryFilter4 = "심화 문법"
    private lateinit var optionMenuHandler: OptionMenuHandler
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var buttonList: Button
    private lateinit var originalItems: List<Post>
    private lateinit var filteredItems: MutableList<Post>
    private lateinit var adapter1: PostAdapter
    private lateinit var adapter2: PostAdapter
    private lateinit var adapter3: PostAdapter
    private lateinit var adapter4: PostAdapter
    private lateinit var fold_button1: Button
    private lateinit var fold_button2: Button
    private lateinit var fold_button3: Button
    private lateinit var fold_button4: Button
    // flag 변수를 클래스 멤버 변수로 선언
    private var flag1 = false
    private var flag2 = false
    private var flag3 = false
    private var flag4 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorylist)

        // 네비게이션 아이템 클릭 리스너 설정
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationHelper = BottomNavigationHelper(this, bottomNavigationView)

        recyclerView1 = findViewById(R.id.recyclerView1)
        recyclerView2 = findViewById(R.id.recyclerView2)
        recyclerView3 = findViewById(R.id.recyclerView3)
        recyclerView4 = findViewById(R.id.recyclerView4)

        recyclerView1.layoutManager = LinearLayoutManager(this)
        recyclerView2.layoutManager = LinearLayoutManager(this)
        recyclerView3.layoutManager = LinearLayoutManager(this)
        recyclerView4.layoutManager = LinearLayoutManager(this)

        // 검색 기능을 위한 UI 요소 초기화
        editTextSearch = findViewById(R.id.editTextSearch)
        buttonSearch = findViewById(R.id.buttonSearch)
        buttonList = findViewById(R.id.buttonList)

        // 파이어베이스 데이터베이스 초기화 및 데이터 가져오기
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference = firebaseDatabase.reference.child("posts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val allItems = mutableListOf<Post>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let {
                        val key = postSnapshot.key // 아이템의 키값 가져오기
                        it.key = key // Post 객체에 키값 저장
                        allItems.add(it)
                    }
                }
                originalItems = allItems
                filteredItems = allItems.toMutableList()

                adapter1 = PostAdapter(filteredItems, categoryFilter1)
                adapter2 = PostAdapter(filteredItems, categoryFilter2)
                adapter3 = PostAdapter(filteredItems, categoryFilter3)
                adapter4 = PostAdapter(filteredItems, categoryFilter4)

                recyclerView1.adapter = adapter1
                recyclerView2.adapter = adapter2
                recyclerView3.adapter = adapter3
                recyclerView4.adapter = adapter4
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리 (선택사항)
            }
        })

        fold_button1 = findViewById(R.id.btn_Unfold1)
        fold_button2 = findViewById(R.id.btn_Unfold2)
        fold_button3 = findViewById(R.id.btn_Unfold3)
        fold_button4 = findViewById(R.id.btn_Unfold4)

        fold_button1.setOnClickListener {
            toggleRecyclerViewVisibility(fold_button1, recyclerView1, flag1)
            flag1 = !flag1
        }

        fold_button2.setOnClickListener {
            toggleRecyclerViewVisibility(fold_button2, recyclerView2, flag2)
            flag2 = !flag2
        }

        fold_button3.setOnClickListener {
            toggleRecyclerViewVisibility(fold_button3, recyclerView3, flag3)
            flag3 = !flag3
        }

        fold_button4.setOnClickListener {
            toggleRecyclerViewVisibility(fold_button4, recyclerView4, flag4)
            flag4 = !flag4
        }



        // 검색 버튼 클릭 시 검색 기능 수행
        buttonSearch.setOnClickListener {
            val query = editTextSearch.text.toString().trim()
            searchPosts(query)
        }

        // 검색 버튼 클릭 시 검색 기능 수행
        buttonList.setOnClickListener {
            buttonList.setOnClickListener {
                // 전체 게시물 목록을 filteredItems에 대입
                filteredItems.clear()
                filteredItems.addAll(originalItems)

                // 어댑터에 변경된 목록을 알려줌
                adapter1.notifyDataSetChanged()
                adapter2.notifyDataSetChanged()
                adapter3.notifyDataSetChanged()
                adapter4.notifyDataSetChanged()

            }
        }


        // 툴바 설정
        setSupportActionBar(findViewById(R.id.toolbar))

        // 페이지 이동 함수
        val moveToAnotherPage = { destination: Class<*> ->
            startActivity(Intent(this, destination))
            overridePendingTransition(R.anim.left_in, R.anim.left_out)
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

    //검색부분
    private fun searchPosts(query: String) {
        filteredItems.clear() // 리스트 초기화
        if (query.isEmpty()) { // 검색어 부분이 비어있는지 확인
            filteredItems.addAll(originalItems) //비어있다면 모든 리스트값들 불러오기
        } else {// 아니라면
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault()) // 대소문 구분없이 전부다 소문자처리
            //LOCALE.GETDEFAULT는 쿼리의 데이터형식을 기본값을 읽어오기
            for (post in originalItems) { // 이후 반복하여 해당 쿼리에 맞는것들을 리스트에 담아줌
                if (post.title.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) || //제목이 같거나
                    post.author.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) // 작성자 이름이 같거나
                ) {
                    filteredItems.add(post) // 해당한다면 리스트에 추가
                }
            }
        }
    }

    fun toggleRecyclerViewVisibility(button: Button, recyclerView: RecyclerView, flag: Boolean) {
        if (!flag) {
            button.setBackgroundResource(R.drawable.fold_icon)
            recyclerView.visibility = View.VISIBLE
        } else {
            button.setBackgroundResource(R.drawable.unfold_icon)
            recyclerView.visibility = View.GONE
        }
    }
}
