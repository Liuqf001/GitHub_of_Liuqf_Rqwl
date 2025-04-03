1. Android Studio 出现 unable to access android sdk add-on list 的解决方法

   - 解决方法
   - 如果是 windows 系统，在 Android Studio 的安装目录下，找到 \bin\idea.properties 文件。
   - 如果是 macOS 系统，右键应用程序中的 Android Studio.app ，选择 显示包内容 ，接着找到 Contents/bin/idea.properties 文件。
   - 打开 idea.properties 文件，在末尾加入以下配置，这个配置是设置初次打开 Android Studio 时，不检测 SDK 。  
     disable.android.first.run = true

2. Android Studio 不能下载 Gradle 的教程 Could not install Gradle distribution from  
   'https://services.gradle.org/distributions/gradle-5.4.1-all.zip'

国内镜像站点提供的解决方案

- 为了解决 Gradle 下载过慢的问题，我们可以利用国内优秀的镜像站点来加速下载。以下是一些国内镜像站点提供的 Gradle 下载地址：
- 官网地址：https://services.gradle.org/distributions/
- 腾讯云镜像 Gradle 下载地址：https://mirrors.cloud.tencent.com/gradle/ -阿里云镜像 Gradle 下载地址：https://mirrors.aliyun.com/macports/distfiles/gradle/
- 阿里云镜像 Gradle 下载地址：https://mirrors.aliyun.com/gradle/
- 借助这些国内的镜像站点，我们能够以更快的速度获取到 Gradle 的安装包。如此一来，便可以有效避免因官方下载地址而产生的下载速度缓慢的问题，进而极大地提高我们的开发效率。

3. startActivityForResult 使用方法

- startActivityForResult 是 Android 开发中用于启动一个 Activity 并获取其返回结果的方法。当你在一个 Activity 中启动另一个 Activity 进行某些操作，并希望在操作完成后获取返回的数据时，可以使用 startActivityForResult 方法。
- 启动 Activity 并获取返回结果：在调用 startActivityForResult 时，需要传递一个 Intent 对象和一个请求码（requestCode）。请求码用于区分不同的启动请求，以便在回调中区分不同的结果。
- 处理返回结果：在启动 Activity 的父级 Activity 中重写 onActivityResult 方法，当被启动的 Activity 执行完毕并通过 setResult 和 finish 方法返回数据时，父级 Activity 会接收到回调，从而获取返回的数据。

4. 几个标志变量的设置考虑

5. 几个标志变量的设置考虑

```java
compileSdkVersion 29
defaultConfig {
   applicationId "com.example.RQWL001"
   minSdkVersion 26
   targetSdkVersion 29
   versionCode 1
   versionName "1.0"
   testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}
```

6. Github 登录方法
   加速器： Watt_Steam++\_v3.0.0-rc.15_win_x64.exe

[deprecation] Handler е Handler()
