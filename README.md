## 简介

GTIV 是一个短视频社交项目。包含短视频、直播、即时通讯、音视频通话、单聊、群聊等功能。

此项目源于作者在技术学习探索过程中写的一些试验代码，在空闲之际将其整理开源，并写了一些简单的业务 Demo
形成一个可运行的产品。
包含后 [GTIV后端](https://github.com/text3cn/gtiv)
加 [GTIV Android](https://github.com/text3cn/gtiv-android) 端，有这方面技术需求的朋友可以将其作为一个参考。

由于作者精力有限，一些 Demo 怎么快怎么写，并没有严格的编码规约，如果你有自己的编码风格，请自行改造。
同时也希望您踊跃参与开源贡献或测试 bug 提出 Issue，愿您在技术的道路上突飞猛进，祝您工作顺利！

## 技术栈

Kotlin 为主，但早期写的一些代码是 Java 的。用到的一些第三方库清单:

- 使用 [Openapi-generator](https://github.com/OpenAPITools/openapi-generator)
- 视频播放器基于 [Ijkplayer](https://github.com/bilibili/ijkplayer) 实现
- 基于 [WebRTC](https://webrtc.org/?hl=zh-cn) 实现音视频通话
  生成 [Retrofit](https://github.com/square/retrofit) 代码
- 图片加载框架使用 [Glide](https://github.com/bumptech/glide)
  和 [Fresco](https://github.com/facebook/fresco)
- 权限请求库使用 [Easypermissions](https://github.com/googlesamples/easypermissions)
- 长连接通信使用 [Socket.io](https://github.com/socketio/socket.io-client-java)
- KV 存储使用 [MMKV](https://github.com/Tencent/MMKV)
- SQLite 使用了 ORM 框架 [LitePal](https://github.com/guolindev/LitePal)
- 组件间通信使用 [EventBus](https://github.com/greenrobot/EventBus)
- 使用 [Luban](https://github.com/Curzibn/Luban) 进行图片压缩

因为对 Luban 压缩进行了一些定制，将源码 Copy 到了工程 `app/src/main/java/kit/luban` 中。

## Openapi

### 生成代码

使用 [openapi-generator](https://github.com/OpenAPITools/openapi-generator#13---download-jar) 生成
retrofit 代码。运行 Makefile 命令:

```bash
make genapi
```

生成的代码位于 `/app/src/main/java/org.openapitools.client` 包中。
> 注意：<br >
> 后端编写文档时分组 tags 使用中文无法生成接口，因为 openapi 生成 retrofit 时根据 tags 来生成接口文件名称。
> 后端编写文档时如果接口没有 tags 则默认在 DefualtApi.kt 中

### 使用

#### 1、添加依赖

```groovy
implementation "com.squareup.moshi:moshi-kotlin:1.14.0"
implementation "com.squareup.moshi:moshi-adapters:1.14.0"
implementation "com.squareup.retrofit2:converter-moshi:2.9.0" // 对应 retrofit2:retrofit:2.9.0 版本
implementation "com.squareup.retrofit2:converter-scalars:2.9.0" // 对应 retrofit2:retrofit:2.9.0 版本
implementation "org.jetbrains.kotlin:kotlin-reflect:1.9.0"
```

#### 2、整合到自己的工程

复制生成的 org.openapitools.client 到工程 `src/main/java` (执行 `make genapi` 命令时会自动复制)。

使用以下两个文件发起请求：

- 请求构造器：com.dqd2022.api.RetrofitClient.kt
- 所有接口分组索引: com.dqd2022.api.API.kt

RetrofitClient.kt 是参考生成的 org.openapitools.client.infrastructure/ApiClient.kt 编写的，
API.kt 是所有接口的索引类，这个类是通过 Go 程序 GenIndex 生成的。

#### 3、业务代码中调用

```kt
import com.dqd2022.api.API

API().User.userInfoGet("33").enqueue(object : Callback<T3imapiv1UserinfoRes?> {
    override fun onResponse(
        call: Call<T3imapiv1UserinfoRes?>,
        response: Response<T3imapiv1UserinfoRes?>
    ) {
        if (!response.isSuccessful) return;
        var res = response.body()
    }

    override fun onFailure(call: Call<T3imapiv1UserinfoRes?>, t: Throwable) {
    }
})
```

### App 截图

<style>
  img{border:1px solid #e8e6e6}
</style>
<img src="http://gtiv.text3.cn/github/1.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/2.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/3.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/4.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/5.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/6.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/7.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/8.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/9.jpg" width="240" >
<img src="http://gtiv.text3.cn/github/10.jpg" width="240" >

### 作者本地环境

- MacOS 12.3.1
- Huawei nova 8 (鸿蒙 4.0)
- SAMSUNG Galaxy A7

除以上两款真机外，还没有在其他机型上测试过，可能存在不兼容情况。ijkplayer 只编译了 arm64-v8a，如需支持其他
cpu 构架需自行编译相关 jniLibs

如果你需要此项目相关的技术咨询，可以给我来杯咖啡。微信号: text3cn

### 声明与支持

此项目属于个人开源作品，仅做学习交流使用，因为个人精力有限，并未做大量测试。
如果您需要将其作为商业用途，需考虑此项目可能存在的 Bug，以及相关第三方开源库的开源许可证条款。