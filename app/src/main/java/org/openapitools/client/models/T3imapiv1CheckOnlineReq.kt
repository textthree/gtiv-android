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
 * @param user 目标用户，不传则检测自己是否在线
 */


data class T3imapiv1CheckOnlineReq (

    /* 目标用户，不传则检测自己是否在线 */
    @Json(name = "User")
    val user: kotlin.String? = null

)
