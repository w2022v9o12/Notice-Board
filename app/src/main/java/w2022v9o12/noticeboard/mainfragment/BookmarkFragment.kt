package w2022v9o12.noticeboard.mainfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.w3c.dom.Text
import w2022v9o12.noticeboard.R
import w2022v9o12.noticeboard.adapter.NoticeBoardLVA
import w2022v9o12.noticeboard.databinding.FragmentBookmarkBinding
import w2022v9o12.noticeboard.datamodel.NoticeBoardDM

class BookmarkFragment : Fragment() {
    private lateinit var binding: FragmentBookmarkBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bookmark, container, false)

        // 파이어베이스 사전 준비
        auth = Firebase.auth

        val database = Firebase.database
        val bookmarkRef = database.getReference("Bookmark").child(auth.currentUser!!.uid)
        val noticeBoardDMRef = database.getReference("NoticeBoard")

        val storage = Firebase.storage
        val storageRef = storage.reference

        // 리스트 뷰 작업
        val noticeBoardDMList = ArrayList<NoticeBoardDM>()
        val noticeBoardDMKeyList = ArrayList<String>()

        val listViewAdapter = NoticeBoardLVA(noticeBoardDMList, 1)
        binding.listView.adapter = listViewAdapter

        bookmarkRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookmarkKLKeyList = ArrayList<String>()
                val bookmarkKeyList = ArrayList<String>()
                for(setItem in snapshot.children) {
                    bookmarkKLKeyList.add(setItem.key!!)
                    bookmarkKeyList.add(setItem.getValue<String>()!!)
                }

                noticeBoardDMRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        noticeBoardDMKeyList.clear()
                        noticeBoardDMList.clear()
                        for(setItem in snapshot.children) {
                            if(bookmarkKeyList.contains(setItem.key)) {
                                noticeBoardDMKeyList.add(setItem.key!!)
                                noticeBoardDMList.add(setItem.getValue<NoticeBoardDM>()!!)
                            }
                        }
                        noticeBoardDMKeyList.reverse()
                        noticeBoardDMList.reverse()

                        listViewAdapter.notifyDataSetChanged()

                        // 아이템 클릭 이벤트 처리
                        binding.listView.setOnItemClickListener { parent, view, position, id ->
                            if(noticeBoardDMList[position].isClick) {
                                if(noticeBoardDMList[position].isImageExit) {
                                    if(noticeBoardDMList[position].content != "") {
                                        view.findViewById<TextView>(R.id.content).visibility = View.VISIBLE
                                        view.findViewById<TextView>(R.id.content).text = noticeBoardDMList[position].content
                                    }

                                    view.findViewById<ImageView>(R.id.image).visibility = View.VISIBLE
                                    storageRef.child(noticeBoardDMKeyList[position]).downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                                        if(task.isSuccessful) { Glide.with(requireContext()).load(task.result).into(view.findViewById<ImageView>(R.id.image)) }
                                    })
                                } else {
                                    if(noticeBoardDMList[position].content != "") {
                                        view.findViewById<TextView>(R.id.content).visibility = View.VISIBLE
                                        view.findViewById<TextView>(R.id.content).text = noticeBoardDMList[position].content
                                    }
                                }
                            } else {
                                view.findViewById<TextView>(R.id.content).visibility = View.GONE
                                view.findViewById<ImageView>(R.id.image).visibility = View.GONE
                            }

                            noticeBoardDMList[position].isClick = if(noticeBoardDMList[position].isClick == true) false else true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 네비게이션 작업
        binding.bUserInfo.setOnClickListener {
            it.findNavController().navigate(R.id.action_bookmarkFragment_to_userInfoFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "계정 정보"
        }
        binding.bNoticeBoard.setOnClickListener {
            it.findNavController().navigate(R.id.action_bookmarkFragment_to_NBFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "게시판"
        }
        binding.bNotice.setOnClickListener {
            it.findNavController().navigate(R.id.action_bookmarkFragment_to_noticeFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "공지 사항"
        }

        return binding.root
    }
}