package com.talktalk.hatalkhatalk

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.talktalk.hatalkhatalk.rest.BillingConfig
import com.talktalk.hatalkhatalk.rest.URL
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_fillcash.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class FillCashActivity:AppCompatActivity(), BillingProcessor.IBillingHandler{

    internal lateinit var userData: SharedPreferences
    private var bp: BillingProcessor? = null

    private var uuid = "null"

    private var FLAG = 0
    private var MYCASH = -1

    //Purchase-start
    override fun onBillingInitialized() {
        //처음초기
    }

    override fun onPurchaseHistoryRestored() {
        //구매정보복원
        Log.d("TAG", "FLAG = "+FLAG)

    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        //구매완료시
        when(productId){

            BillingConfig.SKU_100 -> MYCASH += 100
            BillingConfig.SKU_300 -> MYCASH += 300
            BillingConfig.SKU_500 -> MYCASH += 500
            BillingConfig.SKU_1000 -> MYCASH += 1000
            BillingConfig.SKU_5000 -> MYCASH += 5000
            BillingConfig.SKU_10000 -> MYCASH += 10000

        }

        //set to server
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

                if (str != "succeeded") {
                    showErrorToast()
                } else {
                    Toast.makeText(this@FillCashActivity, "결제가 완료됐어요!",
                        Toast.LENGTH_LONG).show()
                }

            }

        })

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        //구매오류시
        showErrorToast()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (!bp!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    override fun onStart() {
        super.onStart()

        //get uuid
        //userdata
        userData = PreferenceManager.getDefaultSharedPreferences(this)
        uuid = userData.getString("uuid", "none")!!

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

            }

        })

    }

    override fun onDestroy() {
        if (bp != null) {
            bp!!.release();
        }
        super.onDestroy();
    }

    //Purchase-end

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fillcash)

        //billing
        bp = BillingProcessor(this, BillingConfig.BILLING_API_KEY, this)
        bp?.initialize()

        //widgets
        setWidgets()

    }

    fun setWidgets(){

        //fonts
        fillcash_title.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_policy.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        fillcash_100.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_300.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_500.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_1000.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_5000.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        //discount
        fillcash_10000.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        fillcash_text1.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_text2.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_text3.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_text4.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_text5.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        fillcash_text6.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        fillcash_100.setOnClickListener {

            if(uuid != "" && uuid != "none")
            FLAG = 1
            bp!!.purchase(this, BillingConfig.SKU_100)

        }
        fillcash_300.setOnClickListener {

            if(uuid != "" && uuid != "none")
            FLAG = 2
            bp!!.purchase(this, BillingConfig.SKU_300)

        }
        fillcash_500.setOnClickListener {

            if(uuid != "" && uuid != "none")
            FLAG = 3
            bp!!.purchase(this, BillingConfig.SKU_500)

        }
        fillcash_1000.setOnClickListener {

            if(uuid != "" && uuid != "none")
            FLAG = 4
            bp!!.purchase(this, BillingConfig.SKU_1000)

        }
        fillcash_5000.setOnClickListener {

            if(uuid != "" && uuid != "none")
            FLAG = 5
            bp!!.purchase(this, BillingConfig.SKU_5000)

        }
        fillcash_10000.setOnClickListener {

            if(uuid != "" && uuid != "none")
            FLAG = 6
            bp!!.purchase(this, BillingConfig.SKU_10000)

        }

        fillcash_policy.setOnClickListener {
            startActivity(Intent(this,RefundPolicyActivity::class.java))
        }

        fillcash_back.setOnClickListener { finish() }

    }

    fun showErrorToast(){
        runOnUiThread {
            Toast.makeText(this, "ERROR, 저희에게 문의해주세요!", Toast.LENGTH_SHORT).show()
        }
    }

}