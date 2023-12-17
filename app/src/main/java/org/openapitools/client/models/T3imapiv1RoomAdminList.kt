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
 * @param userId 用户 id
 * @param nickname 昵称
 * @param avatar 头像
 * @param role 角色：10.普通用户 20.群普通管理员 30.群主
 */


data class T3imapiv1RoomAdminList (

    /* 用户 id */
    @Json(name = "UserId")
    val userId: kotlin.String,

    /* 昵称 */
    @Json(name = "Nickname")
    val nickname: kotlin.String,

    /* 头像 */
    @Json(name = "Avatar")
    val avatar: kotlin.String,

    /* 角色：10.普通用户 20.群普通管理员 30.群主 */
    @Json(name = "Role")
    val role: java.math.BigDecimal

)
