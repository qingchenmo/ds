package com.ds.view

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import com.ds.R


class AllowListUitl {
    companion object {
        fun addAllowInfo(context: Context, listener: DialogInterface.OnClickListener) {
            var view = LayoutInflater.from(context).inflate(R.layout.dia_add_layout, null)
            var builder = AlertDialog.Builder(context)
                    .setTitle("添加白名单")
                    .setView(view)
                    .setPositiveButton("确定", listener)
                    .setNegativeButton("取消") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
            builder.create().show()
        }

        fun addAllowInfo(chepai: String, context: Context, listener: DialogInterface.OnClickListener) {
            var view = LayoutInflater.from(context).inflate(R.layout.dia_add_layout, null)
            var chepaiView = view.findViewById<TextView>(R.id.chepai_et)
            chepaiView.text = chepai
            var builder = AlertDialog.Builder(context)
                    .setTitle("添加白名单")
                    .setView(view)
                    .setPositiveButton("确定", listener)
                    .setNegativeButton("取消") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
            builder.create().show()
        }
    }


}