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
 * @param topicId 动态 id
 * @param commentId 评论 id
 * @param userId 评论者 id
 * @param avatar 评论者头像
 * @param nickname 评论者昵称
 * @param content 评论内容
 * @param createTime 评论时间
 */


data class T3imapiv1TopicCommentList (

    /* 动态 id */
    @Json(name = "TopicId")
    val topicId: java.math.BigDecimal,

    /* 评论 id */
    @Json(name = "CommentId")
    val commentId: java.math.BigDecimal,

    /* 评论者 id */
    @Json(name = "UserId")
    val userId: java.math.BigDecimal,

    /* 评论者头像 */
    @Json(name = "Avatar")
    val avatar: kotlin.String,

    /* 评论者昵称 */
    @Json(name = "Nickname")
    val nickname: kotlin.String,

    /* 评论内容 */
    @Json(name = "Content")
    val content: kotlin.String,

    /* 评论时间 */
    @Json(name = "CreateTime")
    val createTime: java.math.BigDecimal

)
