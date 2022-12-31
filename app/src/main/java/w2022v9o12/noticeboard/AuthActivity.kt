package w2022v9o12.noticeboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import w2022v9o12.noticeboard.databinding.ActivityAuthBinding
import java.util.Calendar
import java.util.GregorianCalendar

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)

        // 파이어베이스 사전 준비
        auth = Firebase.auth

        val database = Firebase.database
        val emailListRef = database.getReference("EmailList")
        val signUpDateRef = database.getReference("SignUpDate")
        val countRef = database.getReference("Count")

        // 가입 날짜를 위한 사전 준비
        val calendar = GregorianCalendar()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)

        // 이미 가입되어 있는 이메일 가져오기
        val emailList = ArrayList<String>()
        emailListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(setItem in snapshot.children) { emailList.add(setItem.getValue(String::class.java)!!) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 회원가입 클릭
        binding.signUp.setOnClickListener {
            val signUpView = LayoutInflater.from(this).inflate(R.layout.dialog_sign_up, null)
            val signUpDialog = AlertDialog.Builder(this).setView(signUpView)
            val showingSUD = signUpDialog.show()

            // 다이얼로그: 확인 버튼
            val submitBtn = showingSUD.findViewById<Button>(R.id.submit)
            submitBtn?.setOnClickListener {
                val email = showingSUD.findViewById<EditText>(R.id.email)?.text.toString()
                val password = showingSUD.findViewById<EditText>(R.id.password)?.text.toString()
                val checkPassword = showingSUD.findViewById<EditText>(R.id.passwordCheck)?.text.toString()

                var allRight = true    // 입력한 이메일, 비밀번호 검사 결과

                if(email.isEmpty()) {
                    Toast.makeText(this, "이메일을 입력해야 합니다.", Toast.LENGTH_SHORT).show()

                    allRight = false
                }

                // 이메일 특수문자 검사
                if(!email.isEmpty()) {
                    // 이메일 형식에 맞지 않는 특수문자 검사
                    val prohibitionList = listOf<String>(
                        "`", "~", "!", "#", "$", "%", "^", "&", "*", "(",
                        ")", "-", "_", "=", "+", "[", "{", "]", "}", ";",
                        ":", ",", "<", ">", "/", "?", "|",
                        "\'", "\"", "\\"
                    )
                    for(item in prohibitionList) {
                        if(email.indexOf(item) != -1) { allRight = false }
                    }

                    // @, . 검사
                    if(email.count { it == '@' } < 1 || 1 < email.count { it == '@' }) {
                        allRight = false
                    } else if(email.count { it == '.' } < 1 || 1 < email.count { it == '.' }) {
                        allRight = false
                    }
                    if(email.indexOf('@') > email.indexOf('.')) { allRight = false }
                    if(email.indexOf('@') == 0 || email.indexOf('@') - email.indexOf('.') == -1 || email.indexOf('.') == email.lastIndex) { allRight = false }

                    if(!allRight) { Toast.makeText(this, "옳바른 이메일 형식으로 입력해 주세요.", Toast.LENGTH_SHORT).show() }
                }

                // 이미 회원가입 되어있는 이메일인지 검사
                if(!email.isEmpty()) {
                    for(item in emailList) {
                        if(email == item) { allRight = false }
                    }

                    if(!allRight) { Toast.makeText(this, "이미 가입 되어있는 이메일입니다.", Toast.LENGTH_SHORT).show() }
                }

                // 비밀번호 검사
                if(password.isEmpty()) {
                    Toast.makeText(this, "비밀번호를 입력해야 합니다.", Toast.LENGTH_SHORT).show()

                    allRight = false
                }

                // 파이어베이스 비밀번호 조건에 부합되는지 검사 (6자리 이상)
                else if(password.length < 6) {
                    Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()

                    allRight = false
                }

                // 비밀번호를 정확히 입력했는지 확인
                if(checkPassword.isEmpty()) {
                    Toast.makeText(this, "\"비밀번호 확인\"도 입력해 주세요.", Toast.LENGTH_SHORT).show()

                    allRight = false
                } else {
                    if(checkPassword != password) {
                        Toast.makeText(this, "비밀번호가 서로 다릅니다.", Toast.LENGTH_SHORT).show()

                        allRight = false
                    }
                }

                // 회원가입 시도
                if(allRight) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if(task.isSuccessful) {
                                // 파이어베이스 작업
                                emailListRef.child(auth.currentUser!!.uid).setValue(email)
                                signUpDateRef.child(auth.currentUser!!.uid).setValue("${year}년 ${month + 1}월 ${date}일")
                                countRef.child(auth.currentUser!!.uid).setValue(0)

                                // 액티비티 작업
                                showingSUD.dismiss()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                }
            }

            // 다이얼로그: 취소 버튼
            val cancelBtn = showingSUD.findViewById<Button>(R.id.cancel)
            cancelBtn?.setOnClickListener { showingSUD.dismiss() }
        }

        // 로그인 버튼
        binding.login.setOnClickListener {
            val email = binding.authEmail.text.toString()
            val password = binding.authPassword.text.toString()

            // 검사 및 로그인 오류 띄워주기
            var allRight = true

            if(email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()

                allRight = false
            }

            // 이메일 특수문자 검사
            if(!email.isEmpty()) {
                // 이메일 형식에 맞지 않는 특수문자 검사
                val prohibitionList = listOf<String>(
                    "`", "~", "!", "#", "$", "%", "^", "&", "*", "(",
                    ")", "-", "_", "=", "+", "[", "{", "]", "}", ";",
                    ":", ",", "<", ">", "/", "?", "|",
                    "\'", "\"", "\\"
                )
                for(item in prohibitionList) {
                    if(email.indexOf(item) != -1) { allRight = false }
                }

                // @, . 검사
                if(email.count { it == '@' } < 1 || 1 < email.count { it == '@' }) {
                    allRight = false
                } else if(email.count { it == '.' } < 1 || 1 < email.count { it == '.' }) {
                    allRight = false
                }
                if(email.indexOf('@') > email.indexOf('.')) { allRight = false }
                if(email.indexOf('@') == 0 || email.indexOf('@') - email.indexOf('.') == -1 || email.indexOf('.') == email.lastIndex) { allRight = false }

                if(!allRight) { Toast.makeText(this, "옳바른 이메일 형식으로 입력해 주세요.", Toast.LENGTH_SHORT).show() }
            }

            // 비밀번호 검사
            if(password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()

                allRight = false
            }

            // 파이어베이스 비밀번호 조건에 부합되는지 검사 (6자리 이상)
            else if(password.length < 6) {
                Toast.makeText(this, "비밀번호는 6자리 이상입니다.", Toast.LENGTH_SHORT).show()

                allRight = false
            }

            // 로그인 시도
            if(allRight) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "이메일 혹은 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // 비회원 로그인 버튼
        binding.loginAnonymous.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        signUpDateRef.child(auth.currentUser!!.uid).setValue("${year}년 ${month + 1}월 ${date}일")
                        countRef.child(auth.currentUser!!.uid).setValue(0)

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
        }
    }

    // 뒤로가기 버튼 두 번 눌러야 종료되도록 작업
    private var isExit = false
    override fun onBackPressed() {
        if(!isExit) {
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()

            isExit = true
            Handler().postDelayed({ isExit = false }, 1500)
        } else {
            finish()
        }
    }
}