package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.T3imapiv1DeleteVideoRes
import org.openapitools.client.models.T3imapiv1DissolveRoomReq
import org.openapitools.client.models.T3imapiv1DissolveRoomRes
import org.openapitools.client.models.T3imapiv1InviteMemberReq
import org.openapitools.client.models.T3imapiv1InviteMemberRes
import org.openapitools.client.models.T3imapiv1QuitRoomReq
import org.openapitools.client.models.T3imapiv1QuitRoomRes
import org.openapitools.client.models.T3imapiv1RemoveMemberRes
import org.openapitools.client.models.T3imapiv1RoomAdminListReq
import org.openapitools.client.models.T3imapiv1RoomAdminListRes
import org.openapitools.client.models.T3imapiv1RoomBannedListReq
import org.openapitools.client.models.T3imapiv1RoomBannedListRes
import org.openapitools.client.models.T3imapiv1RoomBannedToPostReq
import org.openapitools.client.models.T3imapiv1RoomBannedToPostRes
import org.openapitools.client.models.T3imapiv1RoomFaqListRes
import org.openapitools.client.models.T3imapiv1RoomGetNoticeRes
import org.openapitools.client.models.T3imapiv1RoomInfoRes
import org.openapitools.client.models.T3imapiv1RoomListRes
import org.openapitools.client.models.T3imapiv1RoomMemberIdsRes
import org.openapitools.client.models.T3imapiv1RoomMemberInfoRes
import org.openapitools.client.models.T3imapiv1RoomMemberListRes
import org.openapitools.client.models.T3imapiv1RoomMessageListRes
import org.openapitools.client.models.T3imapiv1RoomModifyAvatarReq
import org.openapitools.client.models.T3imapiv1RoomModifyAvatarRes
import org.openapitools.client.models.T3imapiv1RoomModifyNameReq
import org.openapitools.client.models.T3imapiv1RoomModifyNameRes
import org.openapitools.client.models.T3imapiv1RoomModifyNoticeReq
import org.openapitools.client.models.T3imapiv1RoomModifyNoticeRes
import org.openapitools.client.models.T3imapiv1RoomRelieveBannedToPostReq
import org.openapitools.client.models.T3imapiv1RoomRelieveBannedToPostRes
import org.openapitools.client.models.T3imapiv1RoomSetAdminReq
import org.openapitools.client.models.T3imapiv1RoomSetAdminRes
import org.openapitools.client.models.T3imapiv1RoomUserInfoRes
import org.openapitools.client.models.T3imapiv1SetVideoPlayListReq
import org.openapitools.client.models.T3imapiv1SetVideoPlayListRes
import org.openapitools.client.models.T3imapiv1ShowVideoReq
import org.openapitools.client.models.T3imapiv1ShowVideoRes
import org.openapitools.client.models.T3imapiv1UploadVideoReq
import org.openapitools.client.models.T3imapiv1UploadVideoRes
import org.openapitools.client.models.T3imapiv1VideoListRes

interface RoomApi {
    /**
     * 管理员列表
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomAdminListReq 
     * @return [Call]<[T3imapiv1RoomAdminListRes]>
     */
    @POST("room/admin_list")
    fun roomAdminListPost(@Body t3imapiv1RoomAdminListReq: T3imapiv1RoomAdminListReq): Call<T3imapiv1RoomAdminListRes>

    /**
     * 禁言列表
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomBannedListReq 
     * @return [Call]<[T3imapiv1RoomBannedListRes]>
     */
    @POST("room/banned_list")
    fun roomBannedListPost(@Body t3imapiv1RoomBannedListReq: T3imapiv1RoomBannedListReq): Call<T3imapiv1RoomBannedListRes>

    /**
     * 用户禁言
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomBannedToPostReq 
     * @return [Call]<[T3imapiv1RoomBannedToPostRes]>
     */
    @POST("room/banned_to_post")
    fun roomBannedToPostPost(@Body t3imapiv1RoomBannedToPostReq: T3imapiv1RoomBannedToPostReq): Call<T3imapiv1RoomBannedToPostRes>

    /**
     * 删除上传的群视频
     * 
     * Responses:
     *  - 200: 
     *
     * @param videoId 视频 ID
     * @param roomId 群 ID
     * @return [Call]<[T3imapiv1DeleteVideoRes]>
     */
    @DELETE("room/delete_video")
    fun roomDeleteVideoDelete(@Query("VideoId") videoId: kotlin.String, @Query("RoomId") roomId: kotlin.String): Call<T3imapiv1DeleteVideoRes>

    /**
     * 解散群
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1DissolveRoomReq 
     * @return [Call]<[T3imapiv1DissolveRoomRes]>
     */
    @POST("room/dissolve")
    fun roomDissolvePost(@Body t3imapiv1DissolveRoomReq: T3imapiv1DissolveRoomReq): Call<T3imapiv1DissolveRoomRes>

    /**
     * 聊天室常见问答
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[T3imapiv1RoomFaqListRes]>
     */
    @GET("room/faq")
    fun roomFaqGet(): Call<T3imapiv1RoomFaqListRes>

    /**
     * 查看群公告
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID
     * @return [Call]<[T3imapiv1RoomGetNoticeRes]>
     */
    @GET("room/get_notice")
    fun roomGetNoticeGet(@Query("RoomId") roomId: kotlin.String): Call<T3imapiv1RoomGetNoticeRes>

    /**
     * 进群获取群信息
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID
     * @return [Call]<[T3imapiv1RoomInfoRes]>
     */
    @GET("room/info")
    fun roomInfoGet(@Query("RoomId") roomId: kotlin.String): Call<T3imapiv1RoomInfoRes>

    /**
     * 创建聊天室/邀请成员
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1InviteMemberReq 
     * @return [Call]<[T3imapiv1InviteMemberRes]>
     */
    @POST("room/invite")
    fun roomInvitePost(@Body t3imapiv1InviteMemberReq: T3imapiv1InviteMemberReq): Call<T3imapiv1InviteMemberRes>

    /**
     * 我的群列表
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[T3imapiv1RoomListRes]>
     */
    @GET("room/list")
    fun roomListGet(): Call<T3imapiv1RoomListRes>

    /**
     * 群成员信息
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID
     * @param userId 用户 id (optional)
     * @return [Call]<[T3imapiv1RoomMemberInfoRes]>
     */
    @GET("room/member_info")
    fun roomMemberInfoGet(@Query("RoomId") roomId: kotlin.String, @Query("UserId") userId: kotlin.String? = null): Call<T3imapiv1RoomMemberInfoRes>

    /**
     * 群成员列表
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID
     * @param nickname 按名称搜索 (optional)
     * @param page 第几页 (optional)
     * @param rows 每页显示多少条 (optional)
     * @return [Call]<[T3imapiv1RoomMemberListRes]>
     */
    @GET("room/member_list")
    fun roomMemberListGet(@Query("RoomId") roomId: kotlin.String, @Query("Nickname") nickname: kotlin.String? = null, @Query("Page") page: kotlin.Int? = null, @Query("Rows") rows: kotlin.Int? = null): Call<T3imapiv1RoomMemberListRes>

    /**
     * 拉取所有在群中的用户 id
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群id (optional)
     * @return [Call]<[T3imapiv1RoomMemberIdsRes]>
     */
    @GET("room/memberids")
    fun roomMemberidsGet(@Query("RoomId") roomId: kotlin.String? = null): Call<T3imapiv1RoomMemberIdsRes>

    /**
     * 最近群消息
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID (optional)
     * @param page 第几页 (optional)
     * @return [Call]<[T3imapiv1RoomMessageListRes]>
     */
    @GET("room/message_list")
    fun roomMessageListGet(@Query("Room_id") roomId: kotlin.String? = null, @Query("Page") page: java.math.BigDecimal? = null): Call<T3imapiv1RoomMessageListRes>

    /**
     * 修改群头像
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomModifyAvatarReq 
     * @return [Call]<[T3imapiv1RoomModifyAvatarRes]>
     */
    @POST("room/modify_avatar")
    fun roomModifyAvatarPost(@Body t3imapiv1RoomModifyAvatarReq: T3imapiv1RoomModifyAvatarReq): Call<T3imapiv1RoomModifyAvatarRes>

    /**
     * 修改群名称
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomModifyNameReq 
     * @return [Call]<[T3imapiv1RoomModifyNameRes]>
     */
    @POST("room/modify_name")
    fun roomModifyNamePost(@Body t3imapiv1RoomModifyNameReq: T3imapiv1RoomModifyNameReq): Call<T3imapiv1RoomModifyNameRes>

    /**
     * 修改群公告
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomModifyNoticeReq 
     * @return [Call]<[T3imapiv1RoomModifyNoticeRes]>
     */
    @POST("room/modify_notice")
    fun roomModifyNoticePost(@Body t3imapiv1RoomModifyNoticeReq: T3imapiv1RoomModifyNoticeReq): Call<T3imapiv1RoomModifyNoticeRes>

    /**
     * 退出群
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1QuitRoomReq 
     * @return [Call]<[T3imapiv1QuitRoomRes]>
     */
    @POST("room/quit")
    fun roomQuitPost(@Body t3imapiv1QuitRoomReq: T3imapiv1QuitRoomReq): Call<T3imapiv1QuitRoomRes>

    /**
     * 解除用户禁言
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomRelieveBannedToPostReq 
     * @return [Call]<[T3imapiv1RoomRelieveBannedToPostRes]>
     */
    @POST("room/relieve_banned_to_post")
    fun roomRelieveBannedToPostPost(@Body t3imapiv1RoomRelieveBannedToPostReq: T3imapiv1RoomRelieveBannedToPostReq): Call<T3imapiv1RoomRelieveBannedToPostRes>

    /**
     * 移除群成员
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID
     * @param userId 用户 id
     * @return [Call]<[T3imapiv1RemoveMemberRes]>
     */
    @DELETE("room/remove_member")
    fun roomRemoveMemberDelete(@Query("RoomId") roomId: kotlin.String, @Query("UserId") userId: kotlin.String): Call<T3imapiv1RemoveMemberRes>

    /**
     * 设置管理员
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1RoomSetAdminReq 
     * @return [Call]<[T3imapiv1RoomSetAdminRes]>
     */
    @POST("room/set_admin")
    fun roomSetAdminPost(@Body t3imapiv1RoomSetAdminReq: T3imapiv1RoomSetAdminReq): Call<T3imapiv1RoomSetAdminRes>

    /**
     * 设置视频播放列表
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1SetVideoPlayListReq 
     * @return [Call]<[T3imapiv1SetVideoPlayListRes]>
     */
    @POST("room/set_video_play_list")
    fun roomSetVideoPlayListPost(@Body t3imapiv1SetVideoPlayListReq: T3imapiv1SetVideoPlayListReq): Call<T3imapiv1SetVideoPlayListRes>

    /**
     * 设置是否展示群视频
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1ShowVideoReq 
     * @return [Call]<[T3imapiv1ShowVideoRes]>
     */
    @POST("room/show_video")
    fun roomShowVideoPost(@Body t3imapiv1ShowVideoReq: T3imapiv1ShowVideoReq): Call<T3imapiv1ShowVideoRes>

    /**
     * 上传群视频
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1UploadVideoReq 
     * @return [Call]<[T3imapiv1UploadVideoRes]>
     */
    @POST("room/upload_video")
    fun roomUploadVideoPost(@Body t3imapiv1UploadVideoReq: T3imapiv1UploadVideoReq): Call<T3imapiv1UploadVideoRes>

    /**
     * 获取指定用户在某个群中的信息
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID
     * @param userId 用户 ID
     * @return [Call]<[T3imapiv1RoomUserInfoRes]>
     */
    @GET("room/userinfo")
    fun roomUserinfoGet(@Query("RoomId") roomId: kotlin.String, @Query("UserId") userId: kotlin.String): Call<T3imapiv1RoomUserInfoRes>

    /**
     * 
     * 
     * Responses:
     *  - 200: 
     *
     * @param roomId 群 ID
     * @param page 第几页 (optional)
     * @param rows 每页显示多少条 (optional)
     * @return [Call]<[T3imapiv1VideoListRes]>
     */
    @GET("room/video_list")
    fun roomVideoListGet(@Query("RoomId") roomId: kotlin.String, @Query("Page") page: java.math.BigDecimal? = null, @Query("Rows") rows: java.math.BigDecimal? = null): Call<T3imapiv1VideoListRes>

}
