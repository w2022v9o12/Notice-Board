package w2022v9o12.noticeboard.mainfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import w2022v9o12.noticeboard.R
import w2022v9o12.noticeboard.adapter.NoticeLVA
import w2022v9o12.noticeboard.databinding.FragmentNoticeBinding
import w2022v9o12.noticeboard.datamodel.NoticeDataModel
import com.google.firebase.database.ktx.getValue

class NoticeFragment : Fragment() {
    private lateinit var binding: FragmentNoticeBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notice, container, false)

        // 파이어베이스 준비
        auth = Firebase.auth
        val database = Firebase.database
        val noticeRef = database.getReference("Notice")
        val noticeCheckRef = database.getReference("NoticeCheck").child(auth.currentUser!!.uid)

        /* 공지를 실시간 데이터베이스에 넣는 영역 */
        /* noticeRef.push().setValue(NoticeDataModel(날짜, 제목, 내용)) */

        // 리스트 뷰 작업
        val noticeDMList = ArrayList<NoticeDataModel>()
        val noticeDMKeyList = ArrayList<String>()

        val listViewAdapter = NoticeLVA(noticeDMList, noticeDMKeyList)
        binding.listView.adapter = listViewAdapter

        noticeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noticeDMList.clear()
                for(setItem in snapshot.children) {
                    noticeDMList.add(setItem.getValue(NoticeDataModel::class.java)!!)
                    noticeDMKeyList.add(setItem.key!!)
                }

                noticeDMList.reverse()
                noticeDMKeyList.reverse()
                listViewAdapter.notifyDataSetChanged()

                // 리스트 뷰 아이템 이벤트 처리
                binding.listView.setOnItemClickListener { parent, view, position, id ->
                    noticeDMList[position].isClick = if(noticeDMList[position].isClick == false) true else false

                    view.findViewById<ImageView>(R.id.new_mark).visibility = View.GONE

                    if(noticeDMList[position].isClick == true) {
                        view.findViewById<TextView>(R.id.notice_line).visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.notice_content).visibility = View.VISIBLE
                    } else {
                        view.findViewById<TextView>(R.id.notice_line).visibility = View.GONE
                        view.findViewById<TextView>(R.id.notice_content).visibility = View.GONE
                    }

                    noticeCheckRef.child(noticeDMKeyList[position]).setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 네비게이션 작업
        binding.nUserInfo.setOnClickListener {
            it.findNavController().navigate(R.id.action_noticeFragment_to_userInfoFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "계정 정보"
        }
        binding.nNoticeBoard.setOnClickListener {
            it.findNavController().navigate(R.id.action_noticeFragment_to_NBFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "게시판"
        }
        binding.nBookmark.setOnClickListener {
            it.findNavController().navigate(R.id.action_noticeFragment_to_bookmarkFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "북마크"
        }

        return binding.root
    }
}