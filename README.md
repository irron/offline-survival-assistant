# 末日工具箱

一款面向断网、野外、灾害和极端环境的离线生存工具 App。

## 功能

- `AI 生存助手`：下载模型后可在本地离线推理，支持文字与图片输入。
- `离线知识库`：包含生火、找水、搭建庇护所、急救等常用生存知识。
- `SOS 求救`：支持手电筒闪光、屏幕闪烁和摩尔斯电码发送。
- `生存工具`：提供指南针和 GPS 数值显示。
- `模型管理`：支持下载并自动加载本地模型。

## 技术栈

- Kotlin
- MVVM
- AndroidX + Material
- 本地离线推理
- MNN / Qwen3.5-2B

## 运行要求

- Android 8.0 及以上
- 64 位 ARM 设备
- 首次使用建议连接网络下载模型

## 权限说明

- `INTERNET`：用于下载本地模型
- `ACCESS_NETWORK_STATE`：用于判断网络状态
- `ACCESS_FINE_LOCATION`：用于显示 GPS 信息
- `CAMERA` / `FLASHLIGHT`：用于 SOS 手电筒闪光

## 隐私与安全

- 默认以本地处理为主，不依赖云端 API
- 图片、聊天内容和模型文件主要保存在设备本地
- 本应用仅供参考，不能替代专业救援、医疗或官方指导

## 构建

```powershell
git clone https://github.com/irron/offline-survival-assistant.git
cd offline-survival-assistant
./gradlew assembleRelease
```

Windows 下可以使用：

```powershell
.\gradlew.bat assembleRelease
```

## 发布包

- `AAB`：`app/build/outputs/bundle/release/app-release.aab`
- `APK`：`app/build/outputs/apk/release/app-release.apk`

## 说明

- 模型下载后会自动作为当前模型使用。
- 本项目适合继续扩展为更完整的应急工具套件。
