package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.T3imapiv1CheckRigiterReq
import org.openapitools.client.models.T3imapiv1CheckRigiterRes
import org.openapitools.client.models.T3imapiv1DeleteAccountReq
import org.openapitools.client.models.T3imapiv1DeleteAccountRes
import org.openapitools.client.models.T3imapiv1SearchUserReq
import org.openapitools.client.models.T3imapiv1SearchUserRes
import org.openapitools.client.models.T3imapiv1SubscribeUserListRes
import org.openapitools.client.models.T3imapiv1SubscribeUserReq
import org.openapitools.client.models.T3imapiv1SubscribeUserRes
import org.openapitools.client.models.T3imapiv1UpdateUserinfoReq
import org.openapitools.client.models.T3imapiv1UpdateUserinfoRes
import org.openapitools.client.models.T3imapiv1UserLoginReq
import org.openapitools.client.models.T3imapiv1UserLoginRes
import org.openapitools.client.models.T3imapiv1UserRegisterReq
import org.openapitools.client.models.T3imapiv1UserRegisterRes
import org.openapitools.client.models.T3imapiv1UserinfoRes

interface UserApi {
    /**
     * 注册信息检查
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1CheckRigiterReq 
     * @return [Call]<[T3imapiv1CheckRigiterRes]>
     */
    @POST("user/check-register")
    fun userCheckRegisterPost(@Body t3imapiv1CheckRigiterReq: T3imapiv1CheckRigiterReq): Call<T3imapiv1CheckRigiterRes>

    /**
     * 注销账号
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1DeleteAccountReq 
     * @return [Call]<[T3imapiv1DeleteAccountRes]>
     */
    @POST("user/delete")
    fun userDeletePost(@Body t3imapiv1DeleteAccountReq: T3imapiv1DeleteAccountReq): Call<T3imapiv1DeleteAccountRes>

    /**
     * 获取用户信息
     * 
     * Responses:
     *  - 200: 
     *
     * @param userId  (optional)
     * @return [Call]<[T3imapiv1UserinfoRes]>
     */
    @GET("user/info")
    fun userInfoGet(@Query("UserId") userId: kotlin.String? = null): Call<T3imapiv1UserinfoRes>

    /**
     * 修改用户信息
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1UpdateUserinfoReq 
     * @return [Call]<[T3imapiv1UpdateUserinfoRes]>
     */
    @POST("user/info")
    fun userInfoPost(@Body t3imapiv1UpdateUserinfoReq: T3imapiv1UpdateUserinfoReq): Call<T3imapiv1UpdateUserinfoRes>

    /**
     * 登录
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1UserLoginReq 
     * @return [Call]<[T3imapiv1UserLoginRes]>
     */
    @POST("user/login")
    fun userLoginPost(@Body t3imapiv1UserLoginReq: T3imapiv1UserLoginReq): Call<T3imapiv1UserLoginRes>

    /**
     * 用户注册
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1UserRegisterReq 
     * @return [Call]<[T3imapiv1UserRegisterRes]>
     */
    @POST("user/register")
    fun userRegisterPost(@Body t3imapiv1UserRegisterReq: T3imapiv1UserRegisterReq): Call<T3imapiv1UserRegisterRes>

    /**
     * 查找用户
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1SearchUserReq 
     * @return [Call]<[T3imapiv1SearchUserRes]>
     */
    @POST("user/search")
    fun userSearchPost(@Body t3imapiv1SearchUserReq: T3imapiv1SearchUserReq): Call<T3imapiv1SearchUserRes>

    /**
     * 我关注的用户列表
     * 
     * Responses:
     *  - 200: 
     *
     * @param page 第几页 (optional)
     * @param rows 每页显示多少条 (optional)
     * @return [Call]<[T3imapiv1SubscribeUserListRes]>
     */
    @GET("user/subscribe_list")
    fun userSubscribeListGet(@Query("Page") page: java.math.BigDecimal? = null, @Query("Rows") rows: java.math.BigDecimal? = null): Call<T3imapiv1SubscribeUserListRes>

    /**
     * 关注他人
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1SubscribeUserReq 
     * @return [Call]<[T3imapiv1SubscribeUserRes]>
     */
    @POST("user/subscribe")
    fun userSubscribePost(@Body t3imapiv1SubscribeUserReq: T3imapiv1SubscribeUserReq): Call<T3imapiv1SubscribeUserRes>

}
