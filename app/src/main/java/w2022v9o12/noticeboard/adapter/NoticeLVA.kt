package w2022v9o12.noticeboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import w2022v9o12.noticeboard.R
import w2022v9o12.noticeboard.datamodel.NoticeDataModel
import com.google.firebase.database.ktx.getValue

class NoticeLVA(val noticeDMList: List<NoticeDataModel>, val noticeDMKeyList: List<String>) : BaseAdapter() {
    private lateinit var auth: FirebaseAuth

    override fun getCount(): Int {
        return noticeDMList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // 파이어베이스 준비
        auth = Firebase.auth
        val database = Firebase.database
        val noticeCheckRef = database.getReference("NoticeCheck").child(auth.currentUser!!.uid)

        var itemView = convertView
        if(itemView == null) { itemView = LayoutInflater.from(parent?.context).inflate(R.layout.lv_item_notice, parent, false) }

        // 아이템의 어떤 뷰가 어떤 데이터를 받을 것인지 작업
        val dateAndTitle = itemView?.findViewById<TextView>(R.id.notice_title)
        dateAndTitle?.text = "(${noticeDMList[position].date}) ${noticeDMList[position].title}"

        val content = itemView?.findViewById<TextView>(R.id.notice_content)
        content?.text = "${noticeDMList[position].content}"

        // 공지를 한번이라도 읽었는지 검사
        noticeCheckRef.child(noticeDMKeyList[position]).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.getValue<Boolean>() == true) { itemView?.findViewById<ImageView>(R.id.new_mark)?.visibility = View.GONE }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return itemView!!
    }

    override fun getItem(position: Int): Any {
        return noticeDMList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}