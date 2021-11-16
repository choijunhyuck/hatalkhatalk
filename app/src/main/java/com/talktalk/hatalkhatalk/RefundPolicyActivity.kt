package com.talktalk.hatalkhatalk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_refundpolicy.*

class RefundPolicyActivity :AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refundpolicy)

        setWidgets()

    }

    fun setWidgets(){

        refundpolicy_title.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        refundpolicy_text.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        refundpolicy_back.setOnClickListener { finish() }

    }



}