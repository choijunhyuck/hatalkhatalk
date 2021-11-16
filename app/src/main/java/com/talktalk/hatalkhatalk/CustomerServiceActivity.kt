package com.talktalk.hatalkhatalk

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.talktalk.hatalkhatalk.rest.URL
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_customer_service.*
import okhttp3.*
import java.io.IOException

class CustomerServiceActivity : AppCompatActivity() {

    lateinit var data:String
    lateinit var email:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_service)

        widgetSettings()
    }

    fun widgetSettings(){

        //font
        customer_title.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        customer_submit.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        customer_input.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        customer_text_1.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        customer_input_email.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        customer_submit.setOnClickListener {

            data = customer_input.text.toString()
            email = customer_input_email.text.toString()

            if(data.length != 0){

                if(email.length != 0){
                    //submit
                    submit(data,email)
                }else{
                    Toast.makeText(this, "이메일을 입력해주세요!", Toast.LENGTH_LONG).show()
                }

            }else{
                Toast.makeText(this, "내용을 입력해주세요!", Toast.LENGTH_LONG).show()
            }

        }

        customer_back.setOnClickListener {
            finish()
        }

    }

    fun submit(data:String, email:String){

        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("data", data)
            .add("email", email)
            .build()

        val request = Request.Builder()
            .url(URL.SUBMIT_QUESTION)
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
                    //success
                    runOnUiThread {

                        Toast.makeText(this@CustomerServiceActivity,
                            "빠른 시일 내에 답장해드릴게요!", Toast.LENGTH_LONG).show()
                        finish()

                    }

                }

            }

        })

    }

    fun showErrorToast(){
        runOnUiThread {
            Toast.makeText(this, "ERROR, 저희에게 문의해주세요!", Toast.LENGTH_SHORT).show()
        }
    }

}