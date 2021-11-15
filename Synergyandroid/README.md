# Synergy-Android-client

## 1.简介

基于https://github.com/symless/synergy-android-7项目改动进行APK调试，实现了windows作为server，android为client的鼠标（有延迟）、键盘共享。

通信方式：局域网socket

synergy-core version：1.14.2

## 2.windows端设置

主要参考：

- https://github.com/symless/synergy-core/wiki/Compiling     windows部分
- https://segmentfault.com/a/1190000023512582   
- https://www.bilibili.com/read/cv11942391      直接打包成安装包然后GUI设置
- https://zhuanlan.zhihu.com/p/55169623     msbuild命令行编译可能有问题，直接利用前面搭建环境工具编译

tips：可能最后运行synergys.exe或sysnergyc.exe 缺少OpenSSL  dll库（我的是libssl-3-x64.dll和libcrypto-3-x64.dll），在安装目录找到对应的dll复制到synergy-core/build/bin/Debug

## 3.Android APK （仅client端）

https://github.com/symless/synergy-android-7该项目直接运行出错，调试修改后在本人本地环境下可以实现windows端鼠标、键盘共享到app（鼠标移动有延迟）。

主要原因：server端定制的message(src/lib/synergy/protocol_types.cpp )与app端(java/io/msgs) 读写不一致，导致socket无法握手成功。

tips：调试或运行时，app涉及到selinux权限问题，最好

```shell
 adb root && adb shell setenforce 0
 adb shell && chmod 777 dev/uinput
```

## 4.使用示例

windows server ：关闭防火墙

```shell
cd filepath/synergy-core/build/bin/debug 
[synergys.exe -h  server命令帮助   synergyc.exe -h client 命令帮助]
synergys.exe -c mysynergy.conf --address [your server ip,e.g. 192.168.137.1] --name [your server screen alias ,e.g. windows] --no-daemon --debug DEBUG2
```

app client :   3的tips

