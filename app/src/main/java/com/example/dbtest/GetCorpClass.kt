package com.example.dbtest

import org.json.JSONObject

data class CorpClass(val corp_class: String?)

class GetCorpClass(_response : String) {

    lateinit var corp_cls: String

    val jsonObject = JSONObject(_response)
    val status = jsonObject.getString("status")
    val jsonArray = jsonObject.getJSONArray("list")
    val jsonObject2 = jsonArray.getJSONObject(0)

    fun findcorpclass():CorpClass{
        if (status == "000")
        {
            val account_nm = jsonObject2.getString("corp_cls")

            if (account_nm == "Y") {
                corp_cls = "유가"
            }
            else if (account_nm == "K") {
                corp_cls = "코스닥"
            }
        }
        else{
            corp_cls= jsonObject.getString("message")
        }
        return CorpClass(corp_cls)
    }
}