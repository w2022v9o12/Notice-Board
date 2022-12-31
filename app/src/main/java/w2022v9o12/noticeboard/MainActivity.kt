package w2022v9o12.noticeboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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