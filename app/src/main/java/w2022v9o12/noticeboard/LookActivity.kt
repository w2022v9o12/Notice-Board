package w2022v9o12.noticeboard

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import w2022v9o12.noticeboard.databinding.ActivityLookBinding
import w2022v9o12.noticeboard.datamodel.NoticeBoardDM

class LookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLookBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_look)

        // 파이어베이스 작업
        auth = Firebase.auth

        val database = Firebase.database
        val noticeBoardRef = database.getReference("NoticeBoard")
        lateinit var clickedRef: DatabaseReference
        lateinit var clickedDM: NoticeBoardDM
        val bookmarkRef = database.getReference("Bookmark").child(auth.currentUser!!.uid)

        val storage = Firebase.storage
        val storageRef = storage.reference

        // X 클릭
        binding.close.setOnClickListener { finish() }

        // 게시글 작업
        val position = intent.getIntExtra("position", 0)
        val keyList = ArrayList<String>()

        noticeBoardRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                keyList.clear()
                for(setItem in snapshot.children) { keyList.add(setItem.key!!) }

                keyList.reverse()

                try {    // 메인 스레드와 파이어베이스의 처리 속도 차이 때문에 생기는 예외
                    clickedRef = database.getReference("NoticeBoard").child(keyList[position])
                } catch (e: Exception) {}
                clickedRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                try {    // 메인 스레드와 파이어베이스의 처리 속도 차이 때문에 생기는 예외
                                    clickedDM = snapshot.getValue(NoticeBoardDM::class.java)!!

                                    // 뷰에 값 배치
                                    binding.title.text = clickedDM.title
                                    binding.date.text = clickedDM.date
                                    binding.author.text = clickedDM.author
                                    binding.content.text = clickedDM.content

                                    // 이미지 작업
                                    if (clickedDM.isImageExit) {
                                        storageRef.child(keyList[position]).downloadUrl.addOnCompleteListener(
                                            OnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    binding.image.visibility = View.VISIBLE
                                                    Glide.with(baseContext).load(task.result).into(binding.image)
                                                }
                                            })
                                    }

                                    // 수정하기 아이콘 클릭
                                    if(auth.currentUser?.uid == clickedDM.uid) {
                                        binding.rewrite.visibility = View.VISIBLE

                                        binding.rewrite.setOnClickListener {
                                            val intent = Intent(baseContext, WriteActivity::class.java)
                                            intent.putExtra("rewrite", 1)
                                            intent.putExtra("title", clickedDM.title)
                                            intent.putExtra("content", clickedDM.content)
                                            intent.putExtra("key", keyList[position])

                                            startActivity(intent)
                                        }
                                    }

                                    // 삭제 아이콘 클릭
                                    // 이미지가 없는 경우
                                    if(auth.currentUser?.uid == clickedDM.uid && !clickedDM.isImageExit) {
                                        binding.delete.visibility = View.VISIBLE
                                        binding.delete.setOnClickListener {
                                            clickedRef.removeValue()
                                            finish()
                                        }
                                    }

                                    // 이미지가 있는 경우
                                    if(auth.currentUser?.uid == clickedDM.uid && clickedDM.isImageExit) {
                                        binding.delete.visibility = View.VISIBLE
                                        binding.delete.setOnClickListener {
                                            clickedRef.removeValue()
                                            storageRef.child(keyList[position]).delete()
                                            finish()
                                        }
                                    }
                                } catch (e: Exception) {}
                            }

                            override fun onCancelled(error: DatabaseError) {}
                })

                // 북마크 작업
                bookmarkRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {    // 게시글이 삭제 되면 일어나는 예외
                            val bookmarkKLKeyList = ArrayList<String>()
                            val bookmarkKeyList = ArrayList<String>()
                            for (setItem in snapshot.children) {
                                bookmarkKLKeyList.add(setItem.key!!)
                                bookmarkKeyList.add(setItem.getValue<String>()!!)
                            }

                            if (bookmarkKeyList.contains(keyList[position])) {
                                binding.bookmark.setImageResource(R.drawable.bookmark_icon_by_icons8_02)    // 저작권 표시 (Bookmark icon by Icons8)

                                val clickedBookmarkIndex =
                                    bookmarkKeyList.indexOf(keyList[position])
                                binding.bookmark.setOnClickListener {
                                    bookmarkRef.child(
                                        bookmarkKLKeyList[clickedBookmarkIndex]
                                    ).removeValue()
                                }
                            } else {
                                binding.bookmark.setImageResource(R.drawable.bookmark_outline_icon_by_icons8_01)    // 저작권 표시: Bookmark Outline icon by Icons8

                                binding.bookmark.setOnClickListener {
                                    bookmarkRef.push().setValue(keyList[position])
                                }
                            }
                        } catch (e: Exception) {}
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}