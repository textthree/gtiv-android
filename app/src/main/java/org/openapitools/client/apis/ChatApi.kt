package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.T3imapiv1AddMeListRes
import org.openapitools.client.models.T3imapiv1ChatRecordRes
import org.openapitools.client.models.T3imapiv1CheckOnlineReq
import org.openapitools.client.models.T3imapiv1CheckOnlineRes
import org.openapitools.client.models.T3imapiv1PushMidReq
import org.openapitools.client.models.T3imapiv1PushMidRes
import org.openapitools.client.models.T3imapiv1PushRoomReq
import org.openapitools.client.models.T3imapiv1PushRoomRes
import org.openapitools.client.models.T3imapiv1RoomMsgRes

interface ChatApi {
    /**
     * 获取申请加我为好友的人
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[T3imapiv1AddMeListRes]>
     */
    @GET("chat/addme-list")
    fun chatAddmeListGet(): Call<T3imapiv1AddMeListRes>

    /**
     * 获取未同步的聊天记录
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 id
     * @param lastMessageTime 客户端保存的该群最后一条聊天记录
     * @return [Call]<[T3imapiv1RoomMsgRes]>
     */
    @GET("chat/room-msg")
    fun chatRoomMsgGet(@Query("RoomId") roomId: kotlin.String, @Query("LastMessageTime") lastMessageTime: java.math.BigDecimal): Call<T3imapiv1RoomMsgRes>

    /**
     * 获取未同步的聊天记录
     * 
     * Responses:
     *  - 200: 
     *
     * @param lastMessageTime 客户端数据库与该用户聊天的最后一条消息时间
     * @return [Call]<[T3imapiv1ChatRecordRes]>
     */
    @GET("chat/sync-private-message")
    fun chatSyncPrivateMessageGet(@Query("LastMessageTime") lastMessageTime: java.math.BigDecimal): Call<T3imapiv1ChatRecordRes>

    /**
     * 判断用户是否在线
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1CheckOnlineReq 
     * @return [Call]<[T3imapiv1CheckOnlineRes]>
     */
    @POST("imlogic/check-online")
    fun imlogicCheckOnlinePost(@Body t3imapiv1CheckOnlineReq: T3imapiv1CheckOnlineReq): Call<T3imapiv1CheckOnlineRes>

    /**
     * 发送私信
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1PushMidReq 
     * @return [Call]<[T3imapiv1PushMidRes]>
     */
    @POST("imlogic/push-mid")
    fun imlogicPushMidPost(@Body t3imapiv1PushMidReq: T3imapiv1PushMidReq): Call<T3imapiv1PushMidRes>

    /**
     * 发送群消息
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1PushRoomReq 
     * @return [Call]<[T3imapiv1PushRoomRes]>
     */
    @POST("imlogic/push-room")
    fun imlogicPushRoomPost(@Body t3imapiv1PushRoomReq: T3imapiv1PushRoomReq): Call<T3imapiv1PushRoomRes>

}
