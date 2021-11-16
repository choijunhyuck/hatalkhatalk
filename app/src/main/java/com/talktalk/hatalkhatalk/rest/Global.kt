package com.talktalk.hatalkhatalk.rest

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.talktalk.hatalkhatalk.R

class Global{

    companion object{

        //for chat_activity, load other profile image
        lateinit var o_profile_image : Bitmap

        //for chat activity, load pictures early
        var pictures:MutableMap<String, Bitmap> = mutableMapOf()

        fun byteArrayToBitmap(byteArray: ByteArray): Bitmap{
            var bitmap: Bitmap? = null
            bitmap = BitmapFactory.decodeByteArray(byteArray,0, byteArray.size)
            return bitmap
        }


        //dialogs
        fun showHeartDialog(context:Context, dialog: ImageView){

            dialog.visibility = View.VISIBLE

            val shake: Animation
            shake = AnimationUtils.loadAnimation(
                context,
                R.anim.pulse
            )

            dialog.startAnimation(shake)

        }

        fun hideHeartDialog(dialog: ImageView){

            dialog.visibility = View.GONE
            dialog.clearAnimation()

        }

        //

    }

}