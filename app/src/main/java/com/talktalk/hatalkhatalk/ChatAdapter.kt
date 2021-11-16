package com.talktalk.hatalkhatalk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.talktalk.hatalkhatalk.rest.Global
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

class ChatAdapter(val context: Context, val arrayList: ArrayList<ChatModel>) :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal lateinit var userData: SharedPreferences
    val TEN_MEGABYTE: Long = 1024 * 1024 * 10 * 10

    init {
        setHasStableIds(true);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        //getItemViewType 에서 뷰타입 1을 리턴받았다면 내채팅레이아웃을 받은 Holder를 리턴
        if(viewType == 1){
            view = LayoutInflater.from(context).inflate(R.layout.chat_my, parent, false)
            return Holder(view)
        }else if(viewType == 2){
            view = LayoutInflater.from(context).inflate(R.layout.chat_my_picture, parent, false)
            return HolderP(view)
        }else if(viewType == 3){
            view = LayoutInflater.from(context).inflate(R.layout.chat_my_present, parent, false)
            return HolderC(view)
        }else if(viewType == 4){
            view = LayoutInflater.from(context).inflate(R.layout.chat_other, parent, false)
            return Holder2(view)
        }else if(viewType == 5){
            view = LayoutInflater.from(context).inflate(R.layout.chat_other_picture, parent, false)
            return HolderP2(view)
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.chat_other_present, parent, false)
            return HolderC2(view)
        }
        //getItemViewType 에서 뷰타입 2을 리턴받았다면 상대채팅레이아웃을 받은 Holder2를 리턴
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return arrayList.size

    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {

        //onCreateViewHolder에서 리턴받은 뷰홀더가 Holder라면 내채팅, item_my_chat의 뷰들을 초기화 해줌
        if (viewHolder is Holder) {

            (viewHolder as Holder).chat_Text.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as Holder).chat_Text.setText(arrayList.get(i).script)
            (viewHolder as Holder).chat_Time.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as Holder).chat_Time.setText(arrayList.get(i).date_time)

        } else if(viewHolder is HolderP) {

            if(Global.pictures.get(i.toString()) != null){

                if((viewHolder as HolderP).chat_Picture.drawable == null){
                    Glide.with(context).load(resizeImage(Global.pictures.get(i.toString())!!))
                        .into((viewHolder as HolderP).chat_Picture)
                }

            }else{

                val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(arrayList.get(i).picture)
                gsReference.getBytes(TEN_MEGABYTE).addOnSuccessListener {

                    Global.pictures.put(i.toString(), Global.byteArrayToBitmap(it))

                    Glide.with(context).load(resizeImage(Global.pictures.get(i.toString())!!))
                        .into((viewHolder as HolderP).chat_Picture)

                }.addOnFailureListener {
                    showErrorToast()
                }

            }


            (viewHolder as HolderP).chat_Time.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as HolderP).chat_Time.setText(arrayList.get(i).date_time)

            (viewHolder as HolderP).chat_Picture.setOnClickListener {
                var intent = Intent(context, PictureViewActivity::class.java)
                intent.putExtra("picture", arrayList.get(i).picture)
                context.startActivity(intent)
            }

        }else if(viewHolder is HolderC) {


            (viewHolder as HolderC).chat_present_text.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as HolderC).chat_present_text.setText("Heart Cash "+arrayList.get(i).charge)
            (viewHolder as HolderC).chat_Time.setText(arrayList.get(i).date_time)


        }else if(viewHolder is Holder2) {

            Glide.with(context).load(Global.o_profile_image).circleCrop()
                .into((viewHolder as Holder2).chat_Profile)


            (viewHolder as Holder2).chat_Text.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as Holder2).chat_Text.setText(arrayList.get(i).script)
            (viewHolder as Holder2).chat_Time.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as Holder2).chat_Time.setText(arrayList.get(i).date_time)

        }else if(viewHolder is HolderP2) {

            Glide.with(context).load(Global.o_profile_image).circleCrop()
                .into((viewHolder as HolderP2).chat_Profile)

            if(Global.pictures.get(i.toString()) != null){

                if((viewHolder as HolderP2).chat_Picture.drawable == null){
                    Log.d("TAG", "LAUNCHED!!!")
                    Glide.with(context).load(resizeImage(Global.pictures.get(i.toString())!!))
                        .into((viewHolder as HolderP2).chat_Picture)

                }

                //else -> 할 필요 X 이미 로딩되었다는 전제 하에

            }else{

                val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(arrayList.get(i).picture)
                gsReference.getBytes(TEN_MEGABYTE).addOnSuccessListener {

                    Global.pictures.put(i.toString(), Global.byteArrayToBitmap(it))

                    Glide.with(context).load(resizeImage(Global.pictures.get(i.toString())!!))
                        .into((viewHolder as HolderP2).chat_Picture)

                }.addOnFailureListener {
                    showErrorToast()
                }

            }


            (viewHolder as HolderP2).chat_Time.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as HolderP2).chat_Time.setText(arrayList.get(i).date_time)

            (viewHolder as HolderP2).chat_Picture.setOnClickListener {
                var intent = Intent(context, PictureViewActivity::class.java)
                intent.putExtra("picture", arrayList.get(i).picture)
                context.startActivity(intent)
            }

        }else if(viewHolder is HolderC2) {

            Glide.with(context).load(Global.o_profile_image).circleCrop()
                .into((viewHolder as HolderC2).chat_Profile)

            (viewHolder as HolderC2).chat_present_text.typeface = ResourcesCompat.getFont(context, R.font.nanumsquareotfb)
            (viewHolder as HolderC2).chat_present_text.setText("Heart Cash "+arrayList.get(i).charge)
            (viewHolder as HolderC2).chat_Time.setText(arrayList.get(i).date_time)

        }
        //onCreateViewHolder에서 리턴받은 뷰홀더가 Holder2라면 상대의 채팅, item_your_chat의 뷰들을 초기화 해줌

    }

    //내가친 채팅 뷰홀더
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //친구목록 모델의 변수들 정의하는부분
        val chat_Text = itemView.findViewById<TextView>(R.id.chat_text)
        val chat_Time = itemView.findViewById<TextView>(R.id.chat_time)

    }

    //내가친 채팅 뷰홀더 (사진)
    inner class HolderP(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //친구목록 모델의 변수들 정의하는부분
        val chat_Picture = itemView.findViewById<ImageView>(R.id.chat_picture)
        val chat_Time = itemView.findViewById<TextView>(R.id.chat_time)

    }

    //내가친 채팅 뷰홀더 (선물)
    inner class HolderC(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //친구목록 모델의 변수들 정의하는부분
        val chat_present_text = itemView.findViewById<TextView>(R.id.chat_present_text)
        val chat_Time = itemView.findViewById<TextView>(R.id.chat_time)

    }

    //상대가친 채팅 뷰홀더
    inner class Holder2(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //친구목록 모델의 변수들 정의하는부분
        val chat_Profile = itemView.findViewById<ImageView>(R.id.chat_profile)
        val chat_Text = itemView.findViewById<TextView>(R.id.chat_text)
        val chat_Time = itemView.findViewById<TextView>(R.id.chat_time)

    }

    //상대가친 채팅 뷰홀더 (사진)
    inner class HolderP2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //친구목록 모델의 변수들 정의하는부분
        val chat_Profile = itemView.findViewById<ImageView>(R.id.chat_profile)
        val chat_Picture = itemView.findViewById<ImageView>(R.id.chat_picture)
        val chat_Time = itemView.findViewById<TextView>(R.id.chat_time)

    }

    //상대가친 채팅 뷰홀더 (선물)
    inner class HolderC2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //친구목록 모델의 변수들 정의하는부분
        val chat_Profile = itemView.findViewById<ImageView>(R.id.chat_profile)
        val chat_present_text = itemView.findViewById<TextView>(R.id.chat_present_text)
        val chat_Time = itemView.findViewById<TextView>(R.id.chat_time)

    }

    override fun getItemViewType(position: Int): Int {//여기서 뷰타입을 1, 2로 바꿔서 지정해줘야 내채팅 너채팅을 바꾸면서 쌓을 수 있음

        userData = PreferenceManager.getDefaultSharedPreferences(context)

        //내 아이디와 arraylist의 uuid 같다면 내꺼 아니면 상대꺼
        return if (arrayList.get(position).uuid == userData.getString("uuid","")) {
            //my

            if(arrayList.get(position).picture != "none"){
                2//picture
            }else if(arrayList.get(position).charge != "none"){
                3//charge
            }else{
                1//normal
            }

        } else {
            //other

            if(arrayList.get(position).picture != "none"){
                5//picture
            }else if(arrayList.get(position).charge != "none"){
                6//charge
            }else{
                4//normal
            }

        }
    }

    fun resizeImage(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 200, 200, false)
    }

    fun showErrorToast(){
        Toast.makeText(context, "ERROR, 저희에게 문의해주세요!", Toast.LENGTH_SHORT).show()
    }

}