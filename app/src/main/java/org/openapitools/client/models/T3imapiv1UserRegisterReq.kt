/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package org.openapitools.client.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param tel 电话
 * @param telCode 国际电话区号
 * @param verifyCode 验证码
 * @param password 密码，md5 值
 * @param inviteCode 邀请码
 */


data class T3imapiv1UserRegisterReq (

    /* 电话 */
    @Json(name = "Tel")
    val tel: kotlin.String,

    /* 国际电话区号 */
    @Json(name = "TelCode")
    val telCode: java.math.BigDecimal,

    /* 验证码 */
    @Json(name = "VerifyCode")
    val verifyCode: java.math.BigDecimal,

    /* 密码，md5 值 */
    @Json(name = "Password")
    val password: kotlin.String,

    /* 邀请码 */
    @Json(name = "InviteCode")
    val inviteCode: kotlin.String? = null

)
