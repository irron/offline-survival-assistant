# 权限与数据安全说明

本文件用于填写应用市场后台的“权限用途说明”“隐私说明”“数据安全表单”。

## 当前权限清单
来自 [AndroidManifest.xml](/E:/dev/ToolApp/app/src/main/AndroidManifest.xml)：
- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.CAMERA`
- `android.permission.FLASHLIGHT`
- `android.permission.ACCESS_FINE_LOCATION`

## 每项权限对应用途
`INTERNET`
- 用途：用户主动下载本地 AI 模型文件。

`ACCESS_NETWORK_STATE`
- 用途：判断网络状态，便于提示模型下载可用性。

`CAMERA`
- 用途：当前工程通过系统相机闪光灯能力实现 SOS 手电筒闪烁。
- 说明：由于实现方式基于相机闪光灯控制，当前版本建议保留该权限；同时已在 `Manifest` 中将相机硬件声明为可选，减少设备分发限制。

`FLASHLIGHT`
- 用途：SOS 求救时控制手电筒闪烁。

`ACCESS_FINE_LOCATION`
- 用途：在工具页显示 GPS 经纬度、海拔和定位精度。

## 数据处理说明
默认不要求用户注册或登录。

本地处理为主：
- 聊天文本：本地处理
- 用户选择图片：本地复制、本地推理
- GPS 信息：本地显示
- 模型文件：本地下载、本地保存

不会主动上传到开发者服务器：
- 聊天内容
- 图片内容
- GPS 坐标

需要你重点确认的合规点：
- 如果继续采用直接从 ModelScope 下载模型的方式，应用在模型下载时会产生外部网络请求。
- 对于 Google Play 的 Data safety 表单，建议你结合是否存在第三方数据接收、日志记录、CDN 统计等情况做最终法务确认。

## 市场后台可直接使用的权限说明文案
定位权限说明：
用于显示当前 GPS 坐标、海拔和定位精度，帮助用户在应急或野外场景中确认位置。本应用不会默认上传定位信息。

网络权限说明：
用于用户主动下载离线 AI 模型，下载完成后核心推理功能可在本地运行。

闪光灯/相机能力说明：
用于 SOS 手电筒求救功能，仅控制闪光灯，不拍照、不录像、不采集画面内容。
