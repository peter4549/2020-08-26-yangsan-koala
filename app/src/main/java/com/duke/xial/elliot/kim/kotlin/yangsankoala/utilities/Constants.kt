package com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities

object Constants {
    const val BILLING_RESPONSE_RESULT_OK = 0
    const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
    const val BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2
    const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE =	3
    const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
    const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
    const val BILLING_RESPONSE_RESULT_ERROR = 6
    const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
    const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8

    val errorCodeMessageMap = mapOf(
        BILLING_RESPONSE_RESULT_OK to "완료되었습니다.",
        BILLING_RESPONSE_RESULT_USER_CANCELED to "취소되었습니다..",
        BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE to "네트워크 연결이 끊겼습니다.",
        BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE to "요청한 유형에 Google Play 결제 AIDL 버전이 지원되지 않습니다.",
        BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE to "요청한 제품을 구매할 수 없습니다.",
        BILLING_RESPONSE_RESULT_DEVELOPER_ERROR to "API에 제공된 인수가 잘못되었습니다.",
        BILLING_RESPONSE_RESULT_ERROR to "API 작업 중 오류가 발생했습니다.",
        BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED to "항목을 이미 소유하고 있기 때문에 구매할 수 없습니다.",
        BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED to "항목을 소유하고 있지 않기 때문에 소비할 수 없습니다.",
    )
}

