package com.talktalk.hatalkhatalk

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.talktalk.hatalkhatalk.rest.Global
import kotlinx.android.synthetic.main.activity_picture_view.*

class PictureViewActivity : AppCompatActivity() {

    val TEN_MEGABYTE:Long = 1024 * 1024 * 10 * 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_view)

        var picture = intent.getStringExtra("picture")

        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(picture)
        gsReference.getBytes(TEN_MEGABYTE).addOnSuccessListener {

            Glide.with(applicationContext).load(Global.byteArrayToBitmap(it))
                .into(pictureview_screen)

        }.addOnFailureListener {
            showErrorToast()
        }

        pictureview_back.setOnClickListener {
            finish()
        }

    }

    override fun onBackPressed() {

        super.onBackPressed()

    }

    fun showErrorToast(){

        Toast.makeText(this, "ERROR, 저희에게 문의해주세요!", Toast.LENGTH_SHORT).show()

    }

}