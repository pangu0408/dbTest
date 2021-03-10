package com.example.dbtest

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room

import org.json.JSONArray
import org.json.JSONObject

import com.example.dbtest.utils.Constants.TAG
import com.example.dbtest.retrofit.RetrofitManager
import com.example.dbtest.utils.API
import com.example.dbtest.utils.RESPONSE_STATE
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity - onCreate() called")

        title = "JOSIM"
        //dart api key 선택을 위한 라디오 그룹
        val key_radio: RadioGroup = findViewById(R.id.radio_group)
        //결과 출력
        val result_text: TextView = findViewById(R.id.result_text)
        result_text.movementMethod = ScrollingMovementMethod()
        // 기업 명 입력 후
        val search_button: Button = findViewById(R.id.searchButton)
        val editText: EditText = findViewById(R.id.editText)

        val assetManager = resources.assets
        val inputStream= assetManager.open("CORPCODE_RE.xml")

        val entries: List<*> = StackOverflowXmlParser().parse(inputStream)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()

        key_radio.setOnCheckedChangeListener{radioGroup, checkedId ->
            when(checkedId){
                R.id.yh_radioButton -> {API.CRTFC_KEY = API.YH_CRTFC_KEY}
                R.id.sh_radioButton -> {API.CRTFC_KEY = API.SH_CRTFC_KEY}
                R.id.dw_radioButton -> {API.CRTFC_KEY = API.DW_CRTFC_KEY}
            }
        }

        search_button.setOnClickListener {
            Log.d(TAG, "MainActivity - 검색 버튼이 클릭되었다.")
            
//            DB에 검색 기록 저장
            db.todoDao().insert(Todo(editText.text.toString()))

            val corp_name = editText.text.toString()

            result_text.append(editText.text.toString() + "\n")

            RetrofitManager.instance.searchCorpData(corp_name = corp_name, completion = { responseState, responseBody ->
                when (responseState) {
                    RESPONSE_STATE.OKAY -> {
                        Log.d(TAG, "api 호출 성공 : $responseBody")

                        try{
                            result_text.text = responseBody

                        }catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    RESPONSE_STATE.FAIL -> {
                        Toast.makeText(this, "api 호출 에러입니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "api 호출 실패 : $responseBody")
                    }
                }
            })
        }
    }
}