package com.talktalk.hatalkhatalk

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.talktalk.hatalkhatalk.rest.Global
import com.talktalk.hatalkhatalk.rest.URL
import kotlinx.android.synthetic.main.activity_profile.*
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private val GALLERY = 1
    val TEN_MEGABYTE: Long = 1024 * 1024 * 10 * 10

    lateinit var userData:SharedPreferences

    lateinit var uuid:String
    lateinit var gender:String
    lateinit var profile_image_string:String
    var MYCASH = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //onstart에서 Init이 늦게됨
        userData = PreferenceManager.getDefaultSharedPreferences(this)
        uuid = userData.getString("uuid", "none")!!
        gender = userData.getString("gender", "none")!!
        profile_image_string = userData.getString("profile_image", "none")!!

        //showDialog
        Global.showHeartDialog(applicationContext, profile_heart_dialog)

        getCashData()
        widgetSettings()

    }

    fun getCashData(){

        //initializing MYCASH
        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("uuid", uuid)
            .build()

        val request = Request.Builder()
            .url(URL.EXPORT_CASH)
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

                val obj = JSONObject(str)
                MYCASH = obj.getString("cash").toInt()
                Log.d("TAG", "MYCASH = "+MYCASH.toString())

                runOnUiThread {
                    profile_cash.text = "Heart Cash 보유 : "+MYCASH.toString()+"개"
                }

            }

        })

    }

    fun widgetSettings(){

        profile_title.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        profile_cash.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        profile_fill_cash.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        profile_customer_service.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        if(profile_image_string != "none"){

            //have user profile
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(profile_image_string)
            gsReference.getBytes(TEN_MEGABYTE).addOnSuccessListener {

                runOnUiThread {

                    Glide.with(applicationContext).load(resizeImage(Global.byteArrayToBitmap(it))).circleCrop()
                        .into(profile_image)
                    Global.hideHeartDialog(profile_heart_dialog)

                }

            }.addOnFailureListener {
                showErrorToast()
            }

        }else{
            //haven't user profile
            setProfilePictureInitial()
        }

        profile_back.setOnClickListener {
            finish()
        }

        profile_image.setOnClickListener {
            showPictureDialog()
        }

        profile_customer_service.setOnClickListener {
            startActivity(Intent(this,CustomerServiceActivity::class.java))
        }

        profile_fill_cash.setOnClickListener {
            //startActivity(Intent(this,FillCashActivity::class.java))
            Toast.makeText(this, "서비스를 준비 중입니다!", Toast.LENGTH_LONG).show()
        }

    }

    //setprofile
    fun showPictureDialog(){

            val pictureDialog = AlertDialog.Builder(this)
            val pictureDialogItems = arrayOf("갤러리에서 사진 선택", "기본이미지로 변경")
            pictureDialog.setItems(
                pictureDialogItems
            ) { dialog, which ->
                when (which) {
                    0 -> {

                        if(MYCASH >= 20) {

                            choosePhotoFromGallary()

                        } else {
                            Toast.makeText(applicationContext, "하트캐시를 충전해주세요!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(applicationContext, FillCashActivity::class.java))
                        }

                    }
                    1 -> {
                        userData.edit().putString("profile_image", "none").apply()
                        setProfilePictureInitial()
                    }
                }
            }
            pictureDialog.show()

    }

    fun choosePhotoFromGallary() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        //결과로 실행시킬 함수
        startActivityForResult(intent, GALLERY)

        /*
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
        */

    }

    fun setProfilePictureInitial(){

        if(gender == "1"){

            Glide.with(applicationContext).load(R.drawable.global_asset_woman_profile).circleCrop()
                .into(profile_image)

        }else{
            Glide.with(applicationContext).load(R.drawable.global_asset_man_profile).circleCrop()
                .into(profile_image)
        }

        Global.hideHeartDialog(profile_heart_dialog)

    }

    //album result
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/

        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK)
        {
            if (data != null)
            {

                val contentURI = data!!.data

                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                    if (bitmap.getWidth() >= bitmap.getHeight()) {

                        var dstBmp = Bitmap.createBitmap(
                            bitmap,
                            bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                            0,
                            bitmap.getHeight(),
                            bitmap.getHeight()
                        )

                        profile_image.loadBitmap(resizeImage(dstBmp))
                        uploadToFirebase(dstBmp)

                    } else {

                        var dstBmp = Bitmap.createBitmap(
                            bitmap,
                            0,
                            bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                            bitmap.getWidth(),
                            bitmap.getWidth()
                        )

                        profile_image.loadBitmap(resizeImage(dstBmp))
                        uploadToFirebase(dstBmp)

                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    showErrorToast()
                }
            }

        }else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun resizeImage(bitmap: Bitmap): Bitmap{
        return Bitmap.createScaledBitmap(bitmap, 200, 200, false)
    }

    //upload
    fun uploadToFirebase(bitmap: Bitmap){

        MYCASH -= 20
        Log.d("TAG", "MYCASH=="+MYCASH.toString())
        profile_cash.text = "Heart Cash 보유 : "+MYCASH.toString()+"개"

        var baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,30, baos)
        var data = baos.toByteArray()

        val location = "gs://hatalkhatalk.appspot.com/profile_image/"
        val url = location + uuid + ".png"

        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url).putBytes(data).addOnCompleteListener {

            if(it.isSuccessful){

                userData.edit().putString("profile_image", url).apply()
                Log.d("TAG", "profile upload succeeded")

                chargeCash()

            }

        }.addOnFailureListener {
            showErrorToast()
        }

    }

    fun chargeCash(){

        //MYCASH -= 20
        //profile_cash.text = "Heart Cash 보유 : "+MYCASH.toString()+"개"

        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("uuid", uuid)
            .add("cash", MYCASH.toString())
            .build()

        val request = Request.Builder()
            .url(URL.CHANGE_CASH)
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

                if (str.substring(1, 7) == "failed") {
                    Log.d("TAG", "CHARGE FAILED")
                }else{
                 Log.d("TAG", "CHARGE SUCCESS")
                }

            }

        })

    }

    fun ImageView.loadBitmap(image: Bitmap?) {

        Glide.with(applicationContext).load(image).circleCrop().into(this)
    }

    //setprofile-end

    fun showErrorToast(){
        runOnUiThread {
            Toast.makeText(this@ProfileActivity, "ERROR, 저희에게 문의해주세요!", Toast.LENGTH_SHORT).show()
        }
    }


}
