package com.example.prodiot

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.recaptcha.RecaptchaErrorCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    var max = true;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** KakoSDK init */
        KakaoSdk.init(this, this.getString(R.string.kakao_app_key))

        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val loginButton = findViewById<Button>(R.id.btn_login)
        val kakaoLogin = findViewById<Button>(R.id.kakao_login)
        kakaoLogin.setOnClickListener {
            kakaoLogin()
        }
        loginButton.setOnClickListener {
            login()
        }
        // 페이지 이동 함수
        val moveToAnotherPage = { destination: Class<*> ->
            startActivity(Intent(this, destination))
            overridePendingTransition(R.anim.left_in, R.anim.left_out)
        }
        // 이벤트 핸들러
        findViewById<Button>(R.id.btn_signin).setOnClickListener {
            moveToAnotherPage(SignUpEmail::class.java)
        }
    }

    private fun login() {
        val emailEditText = findViewById<EditText>(R.id.login_id)
        val passwordEditText = findViewById<EditText>(R.id.login_pw)
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (email.isEmpty() || password.isEmpty()) { // 필수 정보가 입력되지 않은 경우 처리
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {   // 로그인 성공
                val user = auth.currentUser
                if (user != null) { // 로그인 성공 후 처리할 작업을 여기에 추가
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    // 예: 다음 화면으로 이동
                    val intent = Intent(this, MainMenu::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            else { // 로그인 실패
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun kakaoLogin() {
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        var isLoggedIn = false
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                //TextMsg(this, "카카오계정으로 로그인 실패 : ${error}")
                Toast.makeText(this@LoginActivity, "${error}" ,Toast.LENGTH_SHORT ).show()
                //setLogin(false)
            } else if (token != null) {
                val user = auth.currentUser
                val uid = user?.uid
                // Firebase 데이터베이스 인스턴스를 초기화합니다.
                val database = FirebaseDatabase.getInstance()
                // "users" 테이블에 대한 참조를 가져옵니다.
                val usersRef = database.getReference("users")
                Log.d("uid", "post: $uid")
                val intent = Intent(this, MainMenu::class.java)

                // 기본 키 값을 가져오기 위한 쿼리를 실행합니다.

                //TODO: 최종적으로 카카오로그인 및 유저정보 가져온 결과
                UserApiClient.instance.me { user, error ->
                    //TextMsg(this, "카카오계정으로 로그인 성공 \n\n " +
                    //        "token: ${token.accessToken} \n\n " +
                    //        "me: ${user}")
                    //TextMsg(this, "닉네임: ${user?.kakaoAccount?.profile?.nickname}")
                    Toast.makeText(this@LoginActivity, "메일 정보 : ${user?.kakaoAccount?.email}" ,Toast.LENGTH_SHORT ).show()

                    val name = user?.kakaoAccount?.profile?.nickname
                    val email = user?.kakaoAccount?.email
                    val password = "abcd1234**"



                    //10.19일 다음화면으로 넘어가기는하나 user가 정보를 못받아옴 이거를 해결해야함
                    //sign부분은 퍼오지말것 user다날라가는수가 있음
                    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            //동기 비동기 방식이 있어서 oncomplete어쩌구대신 addOnSuccessListener 이거를 넣어서
                            //ondatachange에 오류가 안나도록함 해결완료
                            for (snapshot in dataSnapshot.children) {
                                val key = snapshot.key // 기본 키 값을 가져옵니다.
                                // 가져온 기본 키 값을 사용하여 원하는 작업을 수행합니다.
                                // 예: 기본 키 값에 해당하는 사용자 데이터를 가져오거나 수정합니다.
                                val user = snapshot.getValue(User::class.java)
                                val d_user = user?.name;
                                Log.d("key", "K_key: $name")
                                Log.d("key", "D_key: $d_user")
                                if (name == d_user) {
                                    isLoggedIn = true
                                    Log.d("key", "flag: $isLoggedIn")
                                    // 가져온 데이터를 뷰에 설정
                                    if (isLoggedIn) {
                                        Log.d("key", "flag: $isLoggedIn")
                                        if (email != null) {
                                            auth.signInWithEmailAndPassword(email, password)
                                                .addOnSuccessListener { authResult ->
                                                    val user = auth.currentUser
                                                    if (user != null) { // 로그인 성공 후 처리할 작업을 여기에 추가
                                                        // 예: 다음 화면으로 이동
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                }
                                        }
                                    }
                                }
                            }

                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            // 오류 처리를 수행합니다.
                        }
                    })

                    Log.d("key", "flag: $isLoggedIn")
                    if (email != null && name != null) {
                        signUp(email, password, name)
                        //signup이 실행되면안됨
                        //근데 실행되는것으로 보임
                    }

                    /*
                    if(isLoggedIn){
                        //밑에 있는 addOnCompleteListener이게 비동기방식이라 잠깐대기
                        Log.d("key", "flag: $isLoggedIn")
                        if (email != null) {
                            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {   // 로그인 성공
                                    val user = auth.currentUser
                                    if (user != null) { // 로그인 성공 후 처리할 작업을 여기에 추가
                                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                                        // 예: 다음 화면으로 이동
                                        val intent = Intent(this, MainMenu::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else { // 로그인 실패
                                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    else{
                        Log.d("key", "flag: $isLoggedIn")
                        if (email != null) {
                            if (name != null) {
                                signUp(email, password, name)
                                //signup이 실행되면안됨
                                //근데 실행되는것으로 보임
                            }
                        }
                    }*/
                }

            }
        }


        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    //TextMsg(this, "카카오톡으로 로그인 실패 : ${error}")
                    Toast.makeText(this@LoginActivity, "${error}" ,Toast.LENGTH_SHORT ).show()
                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    // Firebase 데이터베이스 인스턴스를 초기화합니다.
                    val database = FirebaseDatabase.getInstance()
                    // "users" 테이블에 대한 참조를 가져옵니다.
                    val usersRef = database.getReference("users")
                    Log.d("uid", "post: $uid")
                    val intent = Intent(this, MainMenu::class.java)

                    // 기본 키 값을 가져오기 위한 쿼리를 실행합니다.



                    //TODO: 최종적으로 카카오로그인 및 유저정보 가져온 결과
                    UserApiClient.instance.me { user, error ->
                        //TextMsg(this, "카카오계정으로 로그인 성공 \n\n " +
                        //        "token: ${token.accessToken} \n\n " +
                        //        "me: ${user}")
                        //TextMsg(this, "닉네임: ${user?.kakaoAccount?.profile?.nickname}")
                        Toast.makeText(this@LoginActivity, "닉네임: ${user?.kakaoAccount?.email}" ,Toast.LENGTH_SHORT ).show()

                        val name = user?.kakaoAccount?.profile?.nickname
                        val email = user?.kakaoAccount?.email
                        val password = "abcd1234**"



                        //10.19일 다음화면으로 넘어가기는하나 user가 정보를 못받아옴 이거를 해결해야함
                        //sign부분은 퍼오지말것 user다날라가는수가 있음
                        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                //동기 비동기 방식이 있어서 oncomplete어쩌구대신 addOnSuccessListener 이거를 넣어서
                                //ondatachange에 오류가 안나도록함 해결완료
                                for (snapshot in dataSnapshot.children) {
                                    val key = snapshot.key // 기본 키 값을 가져옵니다.
                                    // 가져온 기본 키 값을 사용하여 원하는 작업을 수행합니다.
                                    // 예: 기본 키 값에 해당하는 사용자 데이터를 가져오거나 수정합니다.
                                    val user = snapshot.getValue(User::class.java)
                                    val d_user = user?.name;
                                    Log.d("key", "K_key: $name")
                                    Log.d("key", "D_key: $d_user")
                                    if (name == d_user) {
                                        isLoggedIn = true
                                        Log.d("key", "flag: $isLoggedIn")
                                        // 가져온 데이터를 뷰에 설정
                                        if (isLoggedIn) {
                                            Log.d("key", "flag: $isLoggedIn")
                                            if (email != null) {
                                                auth.signInWithEmailAndPassword(email, password)
                                                    .addOnSuccessListener { authResult ->
                                                        val user = auth.currentUser
                                                        if (user != null) { // 로그인 성공 후 처리할 작업을 여기에 추가
                                                            // 예: 다음 화면으로 이동
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                    }
                                                    .addOnFailureListener { exception ->
                                                    }
                                            }
                                        }
                                    }
                                }

                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                // 오류 처리를 수행합니다.
                            }
                        })

                        Log.d("key", "flag: $isLoggedIn")
                        if (email != null && name != null) {
                            signUp(email, password, name)
                            //signup이 실행되면안됨
                            //근데 실행되는것으로 보임
                        }

                        /*
                        if(isLoggedIn){
                            //밑에 있는 addOnCompleteListener이게 비동기방식이라 잠깐대기
                            Log.d("key", "flag: $isLoggedIn")
                            if (email != null) {
                                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {   // 로그인 성공
                                        val user = auth.currentUser
                                        if (user != null) { // 로그인 성공 후 처리할 작업을 여기에 추가
                                            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                                            // 예: 다음 화면으로 이동
                                            val intent = Intent(this, MainMenu::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    } else { // 로그인 실패
                                        Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        else{
                            Log.d("key", "flag: $isLoggedIn")
                            if (email != null) {
                                if (name != null) {
                                    signUp(email, password, name)
                                    //signup이 실행되면안됨
                                    //근데 실행되는것으로 보임
                                }
                            }
                        }*/
                    }

                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun signUp(email: String, password: String, name: String) {

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            // 필수 정보가 입력되지 않은 경우 처리
            Toast.makeText(this, "모든 필수 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) { // 회원가입 성공
                val user: FirebaseUser? = auth.currentUser
                // Update user's display name
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                if (user != null) {
                    // 회원가입 성공 후 처리할 작업을 여기에 추가
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) { // 사용자 이름 저장 성공
                            // Store name in the database
                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("users")
                            val userRef = usersRef.child(user?.uid ?: "")
                            val userData = User()
                            userData.name = name
                            userRef.setValue(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                                    // 예: 다음 화면으로 이동
                                    val intent = Intent(this, MainMenu::class.java)
                                    startActivity(intent)
                                    finish()
                                    Log.d("1","1")
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                                    Log.d("2","2")
                                }
                        } else {
                            // 사용자 이름 저장 실패
                            Toast.makeText(this, "이름 저장 실패", Toast.LENGTH_SHORT).show()
                            Log.d("3","3")
                        }
                    }
                }
            } else {
                // 회원가입 실패 // 실패 이유에 따라 처리
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                Log.d("4","4")
            }
        }
    }
}
