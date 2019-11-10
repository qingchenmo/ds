package com.ds.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.ds.R

class AddAllowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_allow)
        var name = findViewById<TextView>(R.id.name_et)
        var chepaiView = findViewById<TextView>(R.id.chepai_et)

        findViewById<View>(R.id.commit).setOnClickListener {
            if (TextUtils.isEmpty(name.text)) {
                Toast.makeText(this, "请输入名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(chepaiView.text)) {
                Toast.makeText(this, "请输入车牌", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var intent = Intent()
            intent.putExtra("name", name.text.toString())
            intent.putExtra("chepai", chepaiView.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        findViewById<View>(R.id.cancel).setOnClickListener {
            finish()
        }
    }
}