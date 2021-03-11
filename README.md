# scrcpy_plat

这是一个编译过apk包的版本，apk包已经按照网上别人给出的一个解决方案修改并编译完成。能用的话可以直接拉取，无需自己搭建构建环境并修改编译出包等工作。

项目中的内容，是对应scrcpy的client部分，只是我们代替了exe文件执行，直接以启动服务的方式，就可以去连websocket获取数据。

这里主要介绍两个内容：环境搭建和接入设备。

## 环境搭建

### 材料准备

1. java1.8
2. adb
3. mysql数据库
4. IDEA专业版
5. vpn(可选)

java版本保持统一比较好，其他的相对没有版本要求，当然原则上越新越好。vpn是因为使用过程中会有插件下载，当然，下载源可以切到国内镜像，但内容比较多，直接挂个梯子会很方便。

### 构建过程

#### 关于IDEA

1. 下载项目，用idea打开，这个就不多做赘述了。

2. 下载Maven依赖，这一步IDEA会自动帮你完成，而因为依赖比较多，建议这里就要开vpn了，项目的构建和运行，也需要在這一步完成的前提下进行。

3. File-Settings-Plugins,搜索Lombok，查看是否安装，没装就装上

4. 打开pom.xml文件，按图示按钮进行下载

   [![6twUaT.png](https://s3.ax1x.com/2021/03/11/6twUaT.png)](https://imgtu.com/i/6twUaT)

5. 配置mysql数据库，建一个新库就行，记住他的名字

6. 打开resources下的application.yml文件，修改数据库的地址

   [![6t0Gfe.png](https://s3.ax1x.com/2021/03/11/6t0Gfe.png)](https://imgtu.com/i/6t0Gfe) [![6t0rtS.png](https://s3.ax1x.com/2021/03/11/6t0rtS.png)](https://imgtu.com/i/6t0rtS)

7. 打开src-main-java-com.example.devices-DevicesApplication.java文件，右击运行

   [![6tBijA.png](https://s3.ax1x.com/2021/03/11/6tBijA.png)](https://imgtu.com/i/6tBijA) 运行过程中需稍等，右下方有进度条会提示当前启动进度[![6tBa34.png](https://s3.ax1x.com/2021/03/11/6tBa34.png)](https://imgtu.com/i/6tBa34) 

8. 启动成功，附上一个成功截图供参考：

   [![6tB5DI.png](https://s3.ax1x.com/2021/03/11/6tB5DI.png)](https://imgtu.com/i/6tB5DI) 最后在浏览器中输入localhost，就可以进行查看，默认端口是80

#### 关于安卓设备

1. 先确定开发者选项、USB那些已经连上，adb devices能够检测到设备。

2. 将项目目录—kilo-device\lib\apk下面的server-debug.apk，用adb命令push到设备的/data/local/tmp目录下，输入:adb shell CLASSPATH=/data/local/tmp/server-debug.apk app_process / com.genymobile.scrcpy.Server -L 如果看到类似:/system/lib64:/system/product/lib64，这样的输出结果，就说明安装成功了，同时，还需要把lib\对应手机芯片cpu框架下的三个文件push到手机相同的目录

   [![6tgfF1.png](https://s3.ax1x.com/2021/03/11/6tgfF1.png)](https://imgtu.com/i/6tgfF1) 

3. 在服务启动的情况下打开浏览器，输入localhost，随便输入一个账号进入到平台中，选择添加设备（其实服务启动数据库中就已经有相关的表被自动建立出来了）。按照提示项，依次输入对应的值（有 * 号的是必填项）最后提交，就会在数据库中生成数据。他的这个新建设备是有问题的，可能会提示失败，但实际是成功的，还有可能会引起系统报错。所以在对项目熟悉后，我更推荐大家使用自己去写数据库的方式替代这个过程。

4. 回到localhost界面，选择一个自己添加的设备，点击使用，如果画面没有显示，尝试刷新一下屏幕，就会有了。

