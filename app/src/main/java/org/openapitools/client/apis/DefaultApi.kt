package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.T3imapiv1AzureChattingBlobReq
import org.openapitools.client.models.T3imapiv1AzureChattingBlobRes
import org.openapitools.client.models.T3imapiv1ClientTokenRes
import org.openapitools.client.models.T3imapiv1CountryRes
import org.openapitools.client.models.T3imapiv1InternationSmsRes
import org.openapitools.client.models.T3imapiv1UserAppsRes
import org.openapitools.client.models.T3imapiv1VersionRes

interface DefaultApi {
    /**
     * 获取国家区号列表
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[T3imapiv1CountryRes]>
     */
    @GET("app/country")
    fun appCountryGet(): Call<T3imapiv1CountryRes>

    /**
     * 获取国家区号列表
     * 
     * Responses:
     *  - 200: 
     *
     * @param type 短信类型：1.注册
     * @param telCode 国际区号
     * @param tel 电话
     * @return [Call]<[T3imapiv1InternationSmsRes]>
     */
    @GET("app/global-sms")
    fun appGlobalSmsGet(@Query("Type") type: java.math.BigDecimal, @Query("TelCode") telCode: kotlin.String, @Query("Tel") tel: kotlin.String): Call<T3imapiv1InternationSmsRes>

    /**
     * 版本检查
     * 
     * Responses:
     *  - 200: 
     *
     * @param type 上传类型：1.安卓 2.ios
     * @return [Call]<[T3imapiv1VersionRes]>
     */
    @GET("app/version")
    fun appVersionGet(@Query("Type") type: java.math.BigDecimal): Call<T3imapiv1VersionRes>

    /**
     * 获取用户广告应用
     * 
     * Responses:
     *  - 200: 
     *
     * @param userId 用户 id
     * @return [Call]<[T3imapiv1UserAppsRes]>
     */
    @GET("apps/user-apps")
    fun appsUserAppsGet(@Query("UserId") userId: kotlin.String): Call<T3imapiv1UserAppsRes>

    /**
     * 
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1AzureChattingBlobReq 
     * @return [Call]<[T3imapiv1AzureChattingBlobRes]>
     */
    @POST("obstore/azure_blob")
    fun obstoreAzureBlobPost(@Body t3imapiv1AzureChattingBlobReq: T3imapiv1AzureChattingBlobReq): Call<T3imapiv1AzureChattingBlobRes>

    /**
     * 获取上传 token
     * 
     * Responses:
     *  - 200: 
     *
     * @param type 上传类型：1.聊天资源（7 天过期） 2.非聊天资源（不过期）
     * @param objectKey 对象key (optional)
     * @return [Call]<[T3imapiv1ClientTokenRes]>
     */
    @GET("obstore/client_token")
    fun obstoreClientTokenGet(@Query("Type") type: java.math.BigDecimal, @Query("ObjectKey") objectKey: kotlin.String? = null): Call<T3imapiv1ClientTokenRes>

}
