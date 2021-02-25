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

            var searchFlag : Boolean = false
            var corp_code = ""
            var result = ""

            val cal = Calendar.getInstance()
            val base_year = cal.get(Calendar.YEAR)
            val base_month = cal.get(Calendar.MONTH)
            val base_day = cal.get(Calendar.DATE)

            val reprt_year = arrayOf(base_year-3,base_year-2)
            val reprt_codes = arrayOf("11013","11012","11014","11011")

            for (i in entries){
                val split_with_comma = i.toString().split(",")
                val split_with_equal = split_with_comma[1].split("=")
                val corp_name = split_with_equal[1].replace(")","")
                if (corp_name == editText.text.toString()){
                    corp_code = split_with_comma[0].replace(("[^0-9]").toRegex(), "")
//                    result_text.text = corp_code
                    searchFlag = true
                    break
                }
            }
            if (searchFlag == false){
                result_text.text = "찾으시는 기업의 코드가 존재하지 않습니다. 다시 입력해주셔요."
            }
            else{
                result_text.setText(API.CRTFC_KEY + "\n")
                result_text.append(editText.text.toString() + "\n")
                RetrofitManager.instance.serachCorpClass(corp_code = corp_code, bgn_de= (base_year-2).toString() + "0101", last_reprt_at ="N", completion = { responseState, responseBody ->

                    when (responseState) {
                        RESPONSE_STATE.OKAY -> {
                            Log.d(TAG, "api 호출 성공 : $responseBody")

                            var a = GetCorpClass(responseBody)
                            a.findcorpclass()

                            result_text.append ("법인 구분 : " + a.corp_cls + "\n")

                        }
                        RESPONSE_STATE.FAIL -> {
                            Toast.makeText(this, "api 호출 에러입니다.", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "api 호출 실패 : $responseBody")
                        }
                    }
                })

                for (year in reprt_year){
                    for (report_code in reprt_codes){
                        RetrofitManager.instance.serachCorpData(corp_code = corp_code, bsns_year= year.toString(), reprt_code =report_code, completion = { responseState, responseBody ->
                            when (responseState) {
                                RESPONSE_STATE.OKAY -> {
                                    Log.d(TAG, "api 호출 성공 : $responseBody")

                                    try{
                                        var a = Preprocessing(corp_code, report_code, responseBody)
                                        a.calcdata()

                                        result_text.append("보고서 연도 : " + year.toString() + "\n")
                                        result_text.append("보고서 코드 : " + report_code + "\n")
                                        result_text.append("매출 : " + a.sales + "\n")
                                        result_text.append("법인세차감전 순이익 : " + a.net_income + "\n")
                                        result_text.append("자본금 : " + a.capital + "\n")
                                        result_text.append("자산총계 : " + a.total_assets + "\n")
                                        result_text.append("자본총계 : "  + a.total_ownership_interest + "\n")
                                        result_text.append("영업이익 : " + a.business_profit + "\n")
                                        result_text.append("\n")

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

        }
    }
}