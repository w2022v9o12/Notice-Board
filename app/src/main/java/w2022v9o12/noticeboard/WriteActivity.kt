package w2022v9o12.noticeboard

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import w2022v9o12.noticeboard.databinding.ActivityWriteBinding
import w2022v9o12.noticeboard.datamodel.NoticeBoardDM
import java.text.SimpleDateFormat
import java.util.*

class WriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteBinding

    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_write)

        // 파이어베이스 사전 준비
        auth = Firebase.auth

        val database = Firebase.database
        val noticeBoardRef = database.getReference("NoticeBoard")
        val noticeBoardKey = noticeBoardRef.push().key!!
        val countRef = database.getReference("Count").child(auth.currentUser!!.uid)

        val storage = Firebase.storage
        val storageRef = storage.reference
        lateinit var imageRef: StorageReference

        // 게시글 작성 개수 사전 작업
        var count = 0
        countRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                count = snapshot.getValue(Int::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 뒤로가기 아이콘 클릭
        binding.cancel.setOnClickListener { finish() }

        // 이미지 업로드 버튼
        binding.imageUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intent, 2022)
        }

       // 업로드 버튼
        binding.upload.setOnClickListener {
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()

            val calendar = Calendar.getInstance().time
            val date = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(calendar)

            var emailOrAnonymous = if(auth.currentUser?.email == "" || auth.currentUser?.email == null) { "작성자: 익명" } else { "작성자: ${auth.currentUser?.email!!}" }

            if(intent.getIntExtra("rewrite", 0) != 1) {
                if(imageUri == null) {
                    noticeBoardRef.child(noticeBoardKey).setValue(NoticeBoardDM(auth.currentUser!!.uid, title, content, date, emailOrAnonymous))
                }

                if(imageUri != null) {
                    noticeBoardRef.child(noticeBoardKey).setValue(NoticeBoardDM(auth.currentUser!!.uid, title, content, date, emailOrAnonymous, true))
                    imageRef = storageRef.child(noticeBoardKey)
                    imageRef.putFile(imageUri!!)
                }

                countRef.setValue(count + 1)
            } else {
                if(imageUri == null) {
                    noticeBoardRef.child(intent.getStringExtra("key")!!).setValue(NoticeBoardDM(auth.currentUser!!.uid, title, content, date + " (수정됨)", emailOrAnonymous))
                }

                if(imageUri != null) {
                    noticeBoardRef.child(intent.getStringExtra("key")!!).setValue(NoticeBoardDM(auth.currentUser!!.uid, title, content, date + " (수정됨)", emailOrAnonymous, true))
                    imageRef = storageRef.child(intent.getStringExtra("key")!!)
                    imageRef.putFile(imageUri!!)
                }
            }

            finish()
        }

        // 수정하기 버튼으로 WriteActivity 실행한 경우
        if(intent.getIntExtra("rewrite", 0) == 1) {
            binding.title.setText(intent.getStringExtra("title"))
            binding.content.setText(intent.getStringExtra("content"))

            /* 이미지 재 업로드 버튼 구현하면 이미지를 이미지 뷰에 업로드 하는 것을 구현할 것 */
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK) {
            binding.imageUpload.visibility = View.GONE
            binding.image.visibility = View.VISIBLE

            binding.image.setImageURI(data!!.data)

            imageUri = data!!.data
        }
    }
}