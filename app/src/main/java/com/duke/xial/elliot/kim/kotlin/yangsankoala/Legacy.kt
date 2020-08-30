package com.duke.xial.elliot.kim.kotlin.yangsankoala

/*
private suspend fun getConfirmedPatients() = withContext(Dispatchers.IO) {
    Jsoup.connect(BASE_URL).get().let { document ->
        val tableBodies = document.body().getElementsByClass("cov_03")[0]
            .getElementsByClass("route_box")[0]
            .select("li").select("tbody")

        // Key: Date
        // Pair: (Time, Place)
        val timePlaceArrayList: ArrayList<Pair<String, Pair<String, String>>> = arrayListOf()
        val timePlaceMap =
            mutableMapOf<String, ArrayList<Pair<String, Pair<String, String>>>>()

        for ((index, table) in tableBodies.withIndex()) {

            var date = ""
            timePlaceArrayList.clear()

            for (item in table.select("tr")) {
                val th = item.select("th")
                if (th != null && th.isNotEmpty())
                    date = th[0].text()

                if (item.getElementsByClass("bo-left").isEmpty())
                    continue
                val time = item.getElementsByClass("bo-left")[0]?.text()!!
                val place = item.getElementsByClass("taL")[0].text()
                if (place == null || place.toString().isEmpty())
                    continue
                val pair = Pair(time, place)
                timePlaceArrayList.add(Pair(date, pair))

            }

            timePlaceMap[index.toString()] = timePlaceArrayList.map { it.copy() } as ArrayList
        }
    }
}
 */

/*
val confirmedPatients = arrayListOf(
    ConfirmedPatientModel(id = 18,
        dateTimeLocationArrayList = arrayListOf(
            "8.19.(수) 09:30\n샤브20 양산북정점",
            "8.21.(금) 14:39\n신일약국",
            "8.21.(금) 15:17\nNH농협은행 양산시지부",
        ), positions = arrayListOf(
            Pair(35.3557908077193, 129.042159977208),
            Pair(35.3527785507631, 129.043157658651),
            Pair(35.3430367497135, 129.038169884089)
        ),
        disinfection = arrayListOf(
            true,
            true,
            true
        )
    ),
    ConfirmedPatientModel(id = 17,
        dateTimeLocationArrayList = arrayListOf(
            "8.18.(화) 13:30~14:30\n물금 시장(노점)",
            "8.19.(수) 11:29~12:15\n농협 하나로마트 양산점",
            "8.19.(수) 14:30~15:30\n물금 시장(노점)"
        ), positions = arrayListOf(
            Pair(35.3097668544253, 128.985299492559),
            Pair(35.3091501994391, 129.009096564711),
            Pair(35.3097668544253, 128.985299492559)
        ),
        disinfection = arrayListOf(
            true,
            true,
            true
        )
    ),
    ConfirmedPatientModel(id = 16,
        dateTimeLocationArrayList = arrayListOf(
            "8.17.(월) 14:17~16:17\n쿠우쿠우 양산점(동면 남양산1길 46)"
        ), positions = arrayListOf(
            Pair(35.32565251428232, 129.030745486839)
        ),
        disinfection = arrayListOf(
            true
        )
    ),
    ConfirmedPatientModel(id = 15,
        dateTimeLocationArrayList = arrayListOf(
            "8.13.(목) 18:00~20:00\n수헤어 (고향의봄 1길 18)"
        ), positions = arrayListOf(
            Pair(35.3516286973451, 129.047735176833)
        ),
        disinfection = arrayListOf(
            true
        )
    )
)
 */
