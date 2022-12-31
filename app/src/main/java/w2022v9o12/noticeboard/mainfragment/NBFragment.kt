package w2022v9o12.noticeboard.mainfragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import w2022v9o12.noticeboard.LookActivity
import w2022v9o12.noticeboard.R
import w2022v9o12.noticeboard.WriteActivity
import w2022v9o12.noticeboard.adapter.NoticeBoardLVA
import w2022v9o12.noticeboard.databinding.FragmentNBBinding
import w2022v9o12.noticeboard.datamodel.NoticeBoardDM

class NBFragment : Fragment() {
    private lateinit var binding: FragmentNBBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_n_b, container, false)

        // 파이어베이스 작업
        val database = Firebase.database
        val noticeBoardRef = database.getReference("NoticeBoard")

        // 리스트 뷰 작업
        val noticeBoardDMList = ArrayList<NoticeBoardDM>()

        val lvAdapter = NoticeBoardLVA(noticeBoardDMList, 0)
        binding.listView.adapter = lvAdapter

        noticeBoardRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noticeBoardDMList.clear()
                for(setItem in snapshot.children) { noticeBoardDMList.add(setItem.getValue(NoticeBoardDM::class.java)!!) }
                noticeBoardDMList.reverse()

                lvAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 아이템 이벤트 처리
        binding.listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(requireContext(), LookActivity::class.java)
            intent.putExtra("position", position)

            startActivity(intent)
        }

        // 게시글 작성 버튼
        binding.writeNoticeboard.setOnClickListener { startActivity(Intent(requireContext(), WriteActivity::class.java)) }

        // 네비게이션 작업
        binding.nbUserInfo.setOnClickListener {
            it.findNavController().navigate(R.id.action_NBFragment_to_userInfoFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "계정 정보"
        }
        binding.nbBookmark.setOnClickListener {
            it.findNavController().navigate(R.id.action_NBFragment_to_bookmarkFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "북마크"
        }
        binding.nbNotice.setOnClickListener {
            it.findNavController().navigate(R.id.action_NBFragment_to_noticeFragment)
            requireActivity().findViewById<TextView>(R.id.title).text = "공지 사항"
        }

        return binding.root
    }
}