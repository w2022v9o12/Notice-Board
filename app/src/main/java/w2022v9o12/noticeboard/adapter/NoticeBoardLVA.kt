package w2022v9o12.noticeboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import w2022v9o12.noticeboard.R
import w2022v9o12.noticeboard.datamodel.NoticeBoardDM

class NoticeBoardLVA(val NoticeBoardDMList: List<NoticeBoardDM>, val purpose: Int) : BaseAdapter() {
    private lateinit var auth: FirebaseAuth

    override fun getCount(): Int {
        return NoticeBoardDMList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // 파이어베이스 준비
        auth = Firebase.auth

        var itemView = convertView
        if(itemView == null) {
            itemView = when(purpose) {
                0 -> LayoutInflater.from(parent?.context).inflate(R.layout.lv_item_nb, parent, false)    // 게시판에서 보여줄 레이아웃
                1 -> LayoutInflater.from(parent?.context).inflate(R.layout.lv_item_bookmark, parent, false)    // 북마크에서 보여줄 레이아웃
                else -> null
            }
        }

        // 아이템의 어떤 뷰가 어떤 데이터를 받을 것인지 작업
        itemView?.findViewById<TextView>(R.id.title)?.text = NoticeBoardDMList[position].title

        if(NoticeBoardDMList[position].uid == auth.currentUser?.uid) { itemView?.findViewById<ImageView>(R.id.mine)?.visibility = View.VISIBLE }

        return itemView!!
    }

    override fun getItem(position: Int): Any {
        return NoticeBoardDMList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}