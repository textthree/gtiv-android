package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.T3imapiv1AddContactsReq
import org.openapitools.client.models.T3imapiv1AddContactsRes
import org.openapitools.client.models.T3imapiv1ContactsListRes
import org.openapitools.client.models.T3imapiv1DeleteContactsRes

interface ContactsApi {
    /**
     * 添加联系人（同意或拒绝）
     * 
     * Responses:
     *  - 200: 
     *
     * @param t3imapiv1AddContactsReq 
     * @return [Call]<[T3imapiv1AddContactsRes]>
     */
    @POST("contacts/add")
    fun contactsAddPost(@Body t3imapiv1AddContactsReq: T3imapiv1AddContactsReq): Call<T3imapiv1AddContactsRes>

    /**
     * 删除联系人
     * 
     * Responses:
     *  - 200: 
     *
     * @param userId 用户 id
     * @return [Call]<[T3imapiv1DeleteContactsRes]>
     */
    @DELETE("contacts")
    fun contactsDelete(@Query("UserId") userId: kotlin.String): Call<T3imapiv1DeleteContactsRes>

    /**
     * 联系人列表
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[T3imapiv1ContactsListRes]>
     */
    @GET("contacts/list")
    fun contactsListGet(): Call<T3imapiv1ContactsListRes>

}
