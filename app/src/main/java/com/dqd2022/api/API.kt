package com.dqd2022.api

import com.dqd2022.Config
import org.openapitools.client.apis.ChatApi

import org.openapitools.client.apis.ContactsApi

import org.openapitools.client.apis.DefaultApi

import org.openapitools.client.apis.RoomApi

import org.openapitools.client.apis.UserApi

class API {
	val baseUrl = Config.IMBIZ

	val Chat = ChatApi()
	val Contacts = ContactsApi()
	val Default = DefaultApi()
	val Room = RoomApi()
	val User = UserApi()

	private fun ChatApi(): ChatApi{
		val retrofit = RetrofitClient(baseUrl).builder()
		val service: ChatApi = retrofit.create(ChatApi::class.java)
		return service
	}

	private fun ContactsApi(): ContactsApi{
		val retrofit = RetrofitClient(baseUrl).builder()
		val service: ContactsApi = retrofit.create(ContactsApi::class.java)
		return service
	}

	private fun DefaultApi(): DefaultApi{
		val retrofit = RetrofitClient(baseUrl).builder()
		val service: DefaultApi = retrofit.create(DefaultApi::class.java)
		return service
	}

	private fun RoomApi(): RoomApi{
		val retrofit = RetrofitClient(baseUrl).builder()
		val service: RoomApi = retrofit.create(RoomApi::class.java)
		return service
	}

	private fun UserApi(): UserApi{
		val retrofit = RetrofitClient(baseUrl).builder()
		val service: UserApi = retrofit.create(UserApi::class.java)
		return service
	}

}