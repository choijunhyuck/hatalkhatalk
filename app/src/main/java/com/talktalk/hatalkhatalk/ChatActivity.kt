package com.talktalk.hatalkhatalk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.firebase.storage.FirebaseStorage
import com.talktalk.hatalkhatalk.rest.Global
import com.talktalk.hatalkhatalk.rest.URL
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {

    var BUTTON_STATUS = 0 //menu
    //= 1 //send

    var GALLERY = 1

    var MYCASH = -1

    var TEN_MEGABYTE: Long = 1024 * 1024 * 10 * 10

    internal lateinit var userData: SharedPreferences

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

            }

        })

    }

    fun widgetSettings() {

        //font
        chat_status.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        chat_input.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        chat_m_normal.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        chat_m_signal.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        chat_m_myinfo.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        chat_m_report.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        chat_present_text1.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)
        chat_present_input.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        chat_snackbar_text.typeface = ResourcesCompat.getFont(this, R.font.nanumsquareotfb)

        //widgets
        chat_input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                //
                if (!DISCONNECTED) {

                    if (s.length != 0) {
                        BUTTON_STATUS = 1
                        chat_alternative_button.setImageResource(R.drawable.chat_asset_send)
                    } else {
                        BUTTON_STATUS = 0
                        chat_alternative_button.setImageResource(R.drawable.chat_asset_menu)
                    }

                } else {
                    BUTTON_STATUS = 0
                    chat_alternative_button.setImageResource(R.drawable.chat_asset_menu)
                }

            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                //
            }

            override fun afterTextChanged(s: Editable) {
                //
            }
        })

        //setonclick
        chat_alternative_button.setOnClickListener {

            if (!DISCONNECTED) {

                if (BUTTON_STATUS == 0) {
                    //menu
                    chat_menu_layout.visibility = View.VISIBLE

                } else {
                    //send

                    //keyboard hide
                    var imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(chat_input.windowToken, 0)

                    sendMessage()

                }

            } else {

                chat_menu_layout.visibility = View.VISIBLE

            }
        }

        chat_menu_layout.setOnClickListener {
            chat_menu_layout.visibility = View.GONE
        }

        chat_m_normal.setOnClickListener {

            if (WAITING) {
                WAITING = false
                deleteQueue()
            }

            //clear list
            chatModel.clear()
            mAdapter.notifyDataSetChanged()

            chat_menu_layout.visibility = View.GONE

            //disconnect
            disconnect()

            Handler().postDelayed({

                newConnection()

            }, DELAY)

        }

        chat_m_signal.setOnClickListener {

            if (WAITING) {
                WAITING = false
                deleteQueue()
            }

            //clear list
            chatModel.clear()
            mAdapter.notifyDataSetChanged()

            chat_menu_layout.visibility = View.GONE

            //disconnect
            disconnect()

            signalConnect()

        }

        chat_m_myinfo.setOnClickListener {
            chat_menu_layout.callOnClick()
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        chat_m_report.setOnClickListener {

            if (o_uuid != "none") {

                if (!REPORTED) {
                    report(o_uuid)
                } else {
                    Toast.makeText(this, "이미 신고한 계정이에요!", Toast.LENGTH_LONG).show()
                }

            } else {
                showErrorToast()
            }

        }

        chat_plus.setOnClickListener { if(!DISCONNECTED){chat_menu_layout2.visibility = View.VISIBLE} }



        chat_menu_layout2.setOnClickListener {
            chat_menu_layout2.visibility = View.GONE
        }


        chat_m_picture.setOnClickListener { choosePhotoFromGallary()
                                            chat_menu_layout2.visibility = View.GONE
        }

        chat_m_present.setOnClickListener {

            chat_present_input.requestFocus()
            var imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(chat_present_input, InputMethodManager.SHOW_IMPLICIT);

            chat_menu_layout2.visibility = View.GONE
            chat_present_layout.visibility = View.VISIBLE

        }

        chat_present_layout.setOnClickListener {
            chat_present_input.setText("")
            chat_present_layout.visibility = View.GONE
        }

        chat_send_present.setOnClickListener {

            if(chat_present_input.text.length > 0){

                var charge = chat_present_input.text.toString()

                if(MYCASH >= charge.toInt()){

                    MYCASH -= charge.toInt()
                    chargeCash()

                    //sendPresent
                    sendPresent(charge.toInt())


                }else{

                    Toast.makeText(applicationContext, "하트캐시를 충전해주세요!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(applicationContext, FillCashActivity::class.java))

                }

            }else{
                Toast.makeText(applicationContext, "개수를 입력해 주세요!", Toast.LENGTH_LONG).show()
            }

        }

    }

    fun sendPresent(charge:Int){

        //claer input
        chat_input.setText("")

        //soket
        val jsonObject = JSONObject()
        try {
            jsonObject.put("uuid", uuid)
            jsonObject.put("profile_image", profile_image)
            jsonObject.put("gender", gender)
            jsonObject.put("script", "none")
            jsonObject.put("picture", "none")
            jsonObject.put("charge", charge.toString())
            jsonObject.put("date_time", getDate())
            jsonObject.put("roomName", roomName)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //screen down
        chat_list.scrollToPosition(chatModel.size - 1);

        Log.e("TAG", "sendMessage: 1" + mSocket.emit("chat message", jsonObject))

        chat_present_layout.visibility = View.GONE

        //keyboard hide
        var imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(chat_input.windowToken, 0)

    }

    fun fillCash(){

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
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "선물을 성공적으로 받았어요!",
                            Toast.LENGTH_LONG).show()
                    }
                }

            }

        })

    }

    fun chargeCash(){

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

    fun report(o_uuid: String) {

        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("uuid", o_uuid)
            .build()

        val request = Request.Builder()
            .url(URL.REPORT_URL)
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
                if (str != "succeeded") {
                    showErrorToast()
                } else {
                    runOnUiThread {

                        REPORTED = true
                        chat_menu_layout.visibility = View.GONE
                        Toast.makeText(this@ChatActivity, "신고완료!", Toast.LENGTH_LONG).show()
                    }
                }

            }

        })
    }

    fun choosePhotoFromGallary() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        //결과로 실행시킬 함수
        startActivityForResult(intent, GALLERY)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/

        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                chat_snackbar_text.setText("사진을 보내고 있어요..")
                chat_snackbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this@ChatActivity,
                        R.color.colorPrimaryPrepare
                    )
                )
                chat_snackbar.visibility = View.VISIBLE

                val contentURI = data.data

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

                        uploadToFirebase(dstBmp)

                    } else {

                        var dstBmp = Bitmap.createBitmap(
                            bitmap,
                            0,
                            bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                            bitmap.getWidth(),
                            bitmap.getWidth()
                        )

                        uploadToFirebase(dstBmp)

                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    showErrorToast()
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //upload
    fun uploadToFirebase(bitmap: Bitmap) {

        var rnds = (0..20000).random()

        var baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 30, baos)
        var data = baos.toByteArray()

        val location = "gs://hatalkhatalk.appspot.com/upload_image/"
        val url = location + uuid + "_" + rnds + ".png"

        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url).putBytes(data)
            .addOnCompleteListener {

                if (it.isSuccessful) {

                    sendPictureMessage(url)

                }

            }.addOnFailureListener {
                showErrorToast()
            }

    }

    //rest functions
    override fun onBackPressed() {

        if (mSocket.connected()) {

            disconnect()

        } else if (WAITING) {

            deleteQueue()

        }

        Handler().postDelayed({

            //show dialog?
            finish()

        }, DELAY)

    }

    override fun onDestroy() {
        super.onDestroy()

        if (mSocket.connected()) {

            disconnect()

        } else if (WAITING) {

            deleteQueue()

        }

    }

    fun getProfilePicture(url: String) {

        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        gsReference.getBytes(TEN_MEGABYTE).addOnSuccessListener {

            Global.o_profile_image = Global.byteArrayToBitmap(it)

        }.addOnFailureListener {
            showErrorToast()
        }

    }

    fun getDate(): String {

        var hFormat = SimpleDateFormat("HH", Locale.KOREA)
        var hour = hFormat.format(Date())

        var mFormat = SimpleDateFormat("mm", Locale.KOREA)
        var minute = mFormat.format(Date())

        var string = hour + ":" + minute

        /*
        var calendar = Calendar.getInstance();
        var hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        */

        return string

    }

    fun showErrorToast() {
        runOnUiThread {
            Toast.makeText(this@ChatActivity, "ERROR, 저희에게 문의해주세요!", Toast.LENGTH_SHORT).show()
        }
    }

    fun shake(textView: TextView) {

        val shake: Animation
        shake = AnimationUtils.loadAnimation(
            this,
            R.anim.shake
        )

        textView.startAnimation(shake)

    }

    //***********위는 레이아웃 등 중요하지 않은 것
    //***********아래는 통신부분

    var chatModel = ArrayList<ChatModel>()
    val mAdapter = ChatAdapter(this, chatModel)

    lateinit var uuid: String
    lateinit var gender: String
    lateinit var profile_image: String
    lateinit var roomName: String

    lateinit var o_uuid: String
    lateinit var o_gender: String
    lateinit var o_profile_image: String

    private var hasConnection: Boolean = false

    private var REPORTED: Boolean = false

    private var DISCONNECTED: Boolean = true
    private var WAITING: Boolean = false

    private var DELAY: Long = 2000

    private var mSocket: Socket = IO.socket("http://3.34.2.142:5000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //userdata
        userData = PreferenceManager.getDefaultSharedPreferences(this)
        uuid = userData.getString("uuid", "none")!!
        gender = userData.getString("gender", "none")!!
        profile_image = userData.getString("profile_image", "none")!!

        //list setting
        var lim = LinearLayoutManager(this)

        chat_list.adapter = mAdapter
        chat_list.layoutManager = lim
        chat_list.setHasFixedSize(true)

        //get Cash DATA
        getCashData()

        widgetSettings()

        //soket setting
        if (savedInstanceState != null) {
            hasConnection = savedInstanceState.getBoolean("hasConnection")
        }

        //initial connect
        if (!hasConnection) {

            newConnection()

        }

        hasConnection = true

    }

    fun newConnection() {

        DISCONNECTED = true

        chat_status.setText("연결중..")
        shake(chat_status)

        var rnds = (0..20000).random()
        roomName = "room_" + rnds

        checkQueue()

    }

    fun checkQueue() {

        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("uuid", uuid)
            .add("gender", gender)
            .add("roomName", roomName)
            .build()

        val request = Request.Builder()
            .url(URL.MATCH)
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

                if (str == "waiting") {
                    //waiting... -> 연결중으로 바꿈면
                    //추후 상대방이 들어오면 onConnect, onNewUser 발동됨
                    waiting()

                } else if (str == "error") {

                    showErrorToast()

                } else {

                    //처음 들어왔을 때 상대방이 존재하면
                    //connected, uuid와 gender, roomName을 받아옴
                    //불러온 uuid와 gender은 각각 o_uuid, o_gender에 저장함
                    //roomName(방제목)을 socket과 연결함
                    //상단 연결중 타이틀을 연결됨 타이틀 등으로 바꿔야함

                    val obj = JSONObject(str)

                    if (uuid != obj.getString("uuid")) {

                        DISCONNECTED = false

                        //get other information
                        o_uuid = obj.getString("uuid")
                        o_gender = obj.getString("gender")

                        o_profile_image =
                            "gs://hatalkhatalk.appspot.com/profile_image/" + o_uuid + ".png"
                        getProfilePicture(o_profile_image)

                        roomName = obj.getString("roomName")

                        Log.d("TAG", "상대방과 연결됨")
                        connect(roomName)

                        runOnUiThread {
                            chat_status.setText("연결됨")
                            shake(chat_status)
                        }

                    } else {

                        Log.d("TAG", "대기함")
                        waiting()

                    }

                }

            }

        })

    }

    private fun connect(room: String) {

        //connect
        mSocket.connect()

        //callbacks
        mSocket.on("connect user", onNewUser)
        mSocket.on("chat message", onNewMessage)
        mSocket.on("user left", onUserLeft)
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)

        val jsonObject = JSONObject()
        try {
            jsonObject.put("uuid", uuid)
            jsonObject.put("gender", gender)
            jsonObject.put("roomName", room)

            //socket.emit은 메세지 전송임
            mSocket.emit("connect user", jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    private fun signalConnect() {

        //DB에서 젠더가 반대인 사람을 찾아 uuid, gender, roomName을 받아옴
        //그 후 아래 connect 실시

        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("uuid", uuid)
            .add("gender", gender)
            .build()

        val request = Request.Builder()
            .url(URL.SIGNAL_MATCH)
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

                if (str.substring(1, 6) == "empty") {

                    //snackbar
                    runOnUiThread {
                        chat_status.setText("연결끊김")
                        shake(chat_status)

                        chat_snackbar_text.setText("이성이 모두 바빠요.. 다시 시도해보세요!")
                        chat_snackbar.setBackgroundColor(
                            ContextCompat.getColor(
                                this@ChatActivity,
                                R.color.colorPrimary
                            )
                        )
                        chat_snackbar.visibility = View.VISIBLE

                        Handler().postDelayed({

                            chat_snackbar.visibility = View.GONE

                        }, DELAY)

                    }

                } else if(str.substring(1, 4) == "not"){

                    runOnUiThread {
                        Toast.makeText(applicationContext, "하트캐시를 충전해주세요!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(applicationContext, FillCashActivity::class.java))
                    }

                }else{

                    chat_status.setText("연결중..")
                    shake(chat_status)

                    runOnUiThread {
                        Handler().postDelayed({

                        }, DELAY)
                    }

                    DISCONNECTED = false

                    //charge heart cash
                    //

                    //처음 들어왔을 때 상대방이 존재하면
                    //connected, uuid와 gender, roomName을 받아옴
                    //불러온 uuid와 gender은 각각 o_uuid, o_gender에 저장함
                    //roomName(방제목)을 socket과 연결함
                    //상단 연결중 타이틀을 연결됨 타이틀 등으로 바꿔야함

                    val obj = JSONObject(str)

                    o_uuid = obj.getString("uuid")
                    o_gender = obj.getString("gender")

                    o_profile_image =
                        "gs://hatalkhatalk.appspot.com/profile_image/" + o_uuid + ".png"
                    getProfilePicture(o_profile_image)

                    roomName = obj.getString("roomName")

                    connect(roomName)

                    runOnUiThread {
                        chat_status.setText("연결됨")
                        shake(chat_status)
                    }

                }

            }

        })
    }

    private fun waiting() {

        WAITING = true
        connect(roomName)

    }z
    //callbacks
    internal var onNewUser: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread(Runnable {

            val length = args.size

            if (length == 0) {

                showErrorToast()
                return@Runnable
            }

            val data = args[0] as JSONObject

            try {

                var uuid = data.getString("uuid")
                var gender = data.getString("gender")

                if (uuid == this.uuid) {
                    Log.d("TAG", "내가 들어왔습니다")
                } else {

                    chat_status.setText("연결됨")
                    shake(chat_status)

                    if (DISCONNECTED == true) {
                        DISCONNECTED = false
                    }

                    if (WAITING == true) {
                        WAITING = false
                    }

                    o_uuid = uuid
                    o_gender = gender

                    o_profile_image =
                        "gs://hatalkhatalk.appspot.com/profile_image/" + o_uuid + ".png"
                    getProfilePicture(o_profile_image)

                    Log.d("TAG", "상대방이 들어왔습니다")

                }

            } catch (e: Exception) {
                showErrorToast()
                return@Runnable
            }

        })
    }

    internal var onNewMessage: Emitter.Listener = Emitter.Listener { args ->

        runOnUiThread(Runnable {

            val data = args[0] as JSONObject
            val uuid: String
            val profile_image: String
            val gender: String
            val script: String
            val picture: String
            var charge: String
            val date_time: String

            try {

                uuid = data.getString("uuid")
                profile_image = data.getString("profile_image")
                gender = data.getString("gender")
                script = data.getString("script")
                picture = data.getString("picture")
                charge = data.getString("charge")
                date_time = data.getString("date_time")

                if(this.uuid != uuid && charge != "none"){
                    MYCASH += charge.toInt()
                    fillCash()
                }

                chatModel.add(ChatModel(uuid, profile_image, gender, script, picture, charge, date_time))
                mAdapter.notifyDataSetChanged()

                Log.e("TAG", "Message Arrived : " + uuid)

                //recyclerview always bottom state
                chat_list.scrollToPosition(chatModel.size - 1)

            } catch (e: Exception) {
                return@Runnable
            }
        })
    }

    internal val onUserLeft =
        Emitter.Listener {

            if (DISCONNECTED == false) {
                DISCONNECTED = true
            }

            disconnect()

        }

    internal val onConnect =
        Emitter.Listener { Log.d("TAG", "connected") }

    internal val onConnectError =
        Emitter.Listener { Log.d("TAG", "Error connecting") }

    internal val onDisconnect =
        Emitter.Listener { disconnect() }

    fun sendMessage() {

        var script = chat_input.getText().toString().trim({ it <= ' ' })

        //claer input
        chat_input.setText("")

        //soketage
        val jsonObject = JSONObject()
        try {
            jsonObject.put("uuid", uuid)
            jsonObject.put("profile_image", profile_image)
            jsonObject.put("gender", gender)
            jsonObject.put("script", script)
            jsonObject.put("picture", "none")
            jsonObject.put("charge", "none")
            jsonObject.put("date_time", getDate())
            jsonObject.put("roomName", roomName)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //screen down
        chat_list.scrollToPosition(chatModel.size - 1);

        Log.e("TAG", "sendMessage: 1" + mSocket.emit("chat message", jsonObject))

    }

    fun sendPictureMessage(url: String) {

        //claer input
        chat_input.setText("")

        //soket
        val jsonObject = JSONObject()
        try {
            jsonObject.put("uuid", uuid)
            jsonObject.put("profile_image", profile_image)
            jsonObject.put("gender", gender)
            jsonObject.put("script", "none")
            jsonObject.put("picture", url)
            jsonObject.put("charge", "none")
            jsonObject.put("date_time", getDate())
            jsonObject.put("roomName", roomName)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //screen down
        //chat_list.scrollToPosition(chatModel.size - 1)

        chat_snackbar.visibility = View.GONE
        chat_list.smoothScrollToPosition(chatModel.size)
        Log.e("TAG", "sendMessage: 1" + mSocket.emit("chat message", jsonObject))

    }

    fun deleteQueue() {

        val client = OkHttpClient()

        val RequestFormBody = FormBody.Builder()
            .add("uuid", uuid)
            .build()

        val request = Request.Builder()
            .url(URL.DELETE_QUEUE)
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
                if (str != "success") {
                    Log.d("TAG", "DQ_ERROR")
                } else {
                    Log.d("TAG", "DQ_SUCCESS")
                }

            }

        })

    }

}
