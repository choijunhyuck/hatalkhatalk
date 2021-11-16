package com.talktalk.hatalkhatalk

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings.Secure
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.firebase.storage.FirebaseStorage
import com.talktalk.hatalkhatalk.rest.Global
import com.talktalk.hatalkhatalk.rest.URL
import kotlinx.android.synthetic.main.activity_gender.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class GenderActivity : AppCompatActivity() {

    var GENDER:String = "1" //woman
    lateinit var userData:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gender)

        //user data
        userData = PreferenceManager.getDefaultSharedPreferences(this)

        //settings
        widgetSettings()

    }

    fun widgetSettings(){

        gender_text1.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        gender_confirmation.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        gender_warning.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        gender_consent.setOnClickListener {

            var transition = Slide(Gravity.BOTTOM)
            transition.setDuration(200)
            transition.addTarget(gender_layout_1)

            TransitionManager.beginDelayedTransition(gender_layout_1, transition)

            gender_layout_1.visibility = View.GONE

            Handler().postDelayed({
                gender_layout_2.visibility = View.VISIBLE
            },150)

        }

        gender_reject.setOnClickListener {
            Toast.makeText(this, "BYE!", Toast.LENGTH_LONG).show()
            finish()
        }

        gender_select.setOnClickListener {

            if(GENDER == "1"){
                GENDER = "2"
                gender_select.setImageResource(R.drawable.gender_asset_man)
            }else{
                GENDER = "1"
                gender_select.setImageResource(R.drawable.gender_asset_woman)
            }

        }

        gender_confirmation.setOnClickListener {

            gender_layout_2.visibility = View.GONE
            //show dialog
            Global.showHeartDialog(this, gender_heart_dialog)

            gender_confirmation.isClickable = false

            val androidId = Secure.getString(
                this.getContentResolver(),
                Secure.ANDROID_ID
            )

            uploadMy(androidId)

        }

    }

    fun uploadMy(uuid:String){

        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("uuid", uuid)
            .add("gender", GENDER)
            .build()

        val request = Request.Builder()
            .url(URL.REGISTER_URL)
            .post(RequestFormBody)
            .build()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

                var str = e.localizedMessage.toString()
                Log.d("TAG: ", str)
                showErrorToast()
            }

            override fun onResponse(call: Call, response: Response) {

                var str = response.body()!!.string()
                Log.d("Result: ", str)
                if(str != "succeeded") {
                    showErrorToast()
                }else{

                    userData.edit().putString("uuid",uuid).apply()
                    userData.edit().putString("profile_image","gs://hatalkhatalk.appspot.com/profile_image/"+uuid+".png").apply()
                    userData.edit().putString("gender",GENDER).apply()
                    userData.edit().putString("cash","10").apply()

                    if(GENDER == "1"){
                        val bitmap = BitmapFactory.decodeResource(
                            this@GenderActivity.getResources(),
                            R.drawable.global_asset_woman_profile
                        )
                        uploadToFirebase(uuid,bitmap)
                    }else{
                        val bitmap = BitmapFactory.decodeResource(
                            this@GenderActivity.getResources(),
                            R.drawable.global_asset_man_profile
                        )
                        uploadToFirebase(uuid,bitmap)

                    }

                }

            }

        })
    }

    fun uploadToFirebase(uuid: String, bitmap: Bitmap){

        var baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,30, baos)
        var data = baos.toByteArray()

        val location = "gs://hatalkhatalk.appspot.com/profile_image/"
        val url = location + uuid + ".png"

        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url).putBytes(data).addOnCompleteListener {

            if(it.isSuccessful){

                userData.edit().putString("profile_image", url).apply()
                Log.d("TAG", "profile upload succeeded")

                startActivity(Intent(this@GenderActivity,ChatActivity::class.java))
                finish()

            }

        }.addOnFailureListener {
            showErrorToast()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        Global.hideHeartDialog(gender_heart_dialog)
    }

    fun showErrorToast(){
        runOnUiThread {
            Toast.makeText(this@GenderActivity, "ERROR, 저희에게 문의해주세요!", Toast.LENGTH_SHORT).show()
        }
    }

}
