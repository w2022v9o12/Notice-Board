package w2022v9o12.noticeboard.mainfragment

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import w2022v9o12.noticeboard.AuthActivity
import w2022v9o12.noticeboard.R
import w2022v9o12.noticeboard.databinding.FragmentUserInfoBinding

class UserInfoFragment : Fragment() {
    private lateinit var binding: FragmentUserInfoBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_info, container, false)

        // 파이어베이스 사전 준비
        auth = Firebase.auth

        val database = Firebase.database
        val bookmarkRef = database.getReference("Bookmark").child(auth.currentUser!!.uid)
        val countRef = database.getReference("Count").child(auth.currentUser!!.uid)
        val noticeCheckRef = database.getReference("NoticeCheck").child(auth.currentUser!!.uid)
        val signUpDateRef = database.getReference("SignUpDate").child(auth.currentUser!!.uid)

        // 이메일과 가입 날짜 정보 알려주기
        signUpDateRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(auth.currentUser?.email == "" || auth.currentUser?.email == null) {
                    binding.emailSignUpDate.text = "비회원 (로그인 날짜: ${snapshot.value})"
                } else {
                    binding.emailSignUpDate.text = "이메일: ${auth.currentUser?.email} (가입일: ${snapshot.value})"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 게시글 작성 개수 알려주기
        countRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { binding.noticeBoardCount.text = "작성한 게시글 개수: ${snapshot.value}개" }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 로그아웃 버튼
        binding.logout.setOnClickListener {
            Firebase.auth.signOut()

            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        // 회원 탈퇴 버튼
        binding.withdrawal.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("[ 회원 탈퇴 ]")
                .setMessage("탈퇴 후 같은 이메일로 가입할 수 없으며 \n직접 작성한 게시글은 지워지지 않습니다. \n\n회원 탈퇴를 진행하시겠습니까?")
                .setPositiveButton("네", object : OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val currentUser = auth.currentUser

                        Firebase.auth.signOut()
                        currentUser?.delete()?.addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                bookmarkRef.removeValue()
                                countRef.removeValue()
                                noticeCheckRef.removeValue()
                                signUpDateRef.removeValue()

                                requireActivity().finishAffinity()
                                requireActivity().startActivity(Intent(requireContext(), AuthActivity::class.java))
                            }
                        }
                    }
                })
                .setNegativeButton("아니오", null)
                .create()
                .show()
        }

        // 네비게이션 작업
        binding.uiNoticeBoard.setOnClickListener {
            it.findNavController().navigate(R.id.action_userInfoFragment_to_NBFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "게시판"
        }
        binding.uiBookmark.setOnClickListener {
            it.findNavController().navigate(R.id.action_userInfoFragment_to_bookmarkFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "북마크"
        }
        binding.uiNotice.setOnClickListener {
            it.findNavController().navigate(R.id.action_userInfoFragment_to_noticeFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "공지 사항"
        }

        return binding.root
    }
}