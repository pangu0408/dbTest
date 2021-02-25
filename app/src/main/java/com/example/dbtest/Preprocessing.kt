package com.example.dbtest

import org.json.JSONObject
import org.json.JSONException

data class ReportData(
    val corp_code: String?,
    val rept_code: String?,
    val sales: String?,
    val sales_last: String?,
    val net_income: String?,
    val net_income_last: String?,
    val capital: String?,
    val capital_last: String?,
    val total_assets: String?,
    val total_assets_last: String?,
    val total_ownership_interest: String?,
    val total_ownership_interest_last: String?,
    val business_profit: String?,
    val business_profit_last: String?)

class Preprocessing (_corp_code: String, _rept_code: String, _response : String) {

    val corp_code = _corp_code
    val rept_code = _rept_code
    val response = _response

    var sales_index = 0
    var net_income_index = 0
    var capital_index = 0
    var total_assets_index = 0
    var total_ownership_interest_index = 0
    var business_profit_index = 0

    var sales = ""
    var net_income = ""
    var capital = ""
    var total_assets = ""
    var total_ownership_interest = ""
    var business_profit = ""

    var sales_last = ""
    var net_income_last = ""
    var capital_last = ""
    var total_assets_last = ""
    var total_ownership_interest_last = ""
    var business_profit_last = ""

    fun calcdata(): ReportData {

        val jsonObject = JSONObject(response)
        val jsonArray = jsonObject.getJSONArray("list")
        val status = jsonObject.getString("status")

        var i = 0
        var tempStr = ""

        if (status == "000"){
            try {
                while (i < jsonArray.length()) {
                    val jsonObject2 = jsonArray.getJSONObject(i)

                    val account_nm = jsonObject2.getString("account_nm")

                    if (account_nm == "매출액") {
                        sales_index = i
                        sales = jsonObject2.getString("thstrm_amount")
                        sales_last = jsonObject2.getString("frmtrm_amount")
                    } else if (account_nm == "법인세차감전 순이익") {
                        net_income_index = i
                        net_income = jsonObject2.getString("thstrm_amount")
                        net_income_last = jsonObject2.getString("frmtrm_amount")
                    } else if (account_nm == "자본금") {
                        capital_index = i
                        capital = jsonObject2.getString("thstrm_amount")
                        capital_last = jsonObject2.getString("frmtrm_amount")
                    } else if (account_nm == "자산총계") {
                        total_assets_index = i
                        total_assets = jsonObject2.getString("thstrm_amount")
                        total_assets_last = jsonObject2.getString("frmtrm_amount")
                    } else if (account_nm == "자본총계") {
                        total_ownership_interest_index = i
                        total_ownership_interest = jsonObject2.getString("thstrm_amount")
                        total_ownership_interest_last = jsonObject2.getString("frmtrm_amount")
                    } else if (account_nm == "영업이익") {
                        business_profit_index = i
                        business_profit = jsonObject2.getString("thstrm_amount")
                        business_profit_last = jsonObject2.getString("frmtrm_amount")
                    }
                    i++
                }
            }catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return ReportData(
            corp_code,
            rept_code,
            sales,
            sales_last,
            net_income,
            net_income_last,
            capital,
            capital_last,
            total_assets,
            total_assets_last,
            total_ownership_interest,
            total_ownership_interest_last ,
            business_profit,
            business_profit_last)
    }
}