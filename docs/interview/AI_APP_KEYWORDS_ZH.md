# 末日工具箱面试速记版

## 30 秒版本

这是一个面向断网和应急场景的 Android 离线生存工具 App。  
核心亮点是把 Qwen3.5-2B 通过 MNN 部署到端侧，实现了模型下载、本地推理、图片输入、JNI 桥接和安全输出约束。  
我想强调的不是普通聊天，而是“端侧本地大模型如何在 Android 产品中真正落地”。

## 主讲链路

`业务需求 -> 为什么选端侧 -> 模型下载 -> Kotlin 封装 -> JNI -> MNN 推理 -> 输出控制 -> 用户体验`

## 关键模块

- 模型下载与激活：[ModelDownloadRepository.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/model/ModelDownloadRepository.kt)
- Kotlin AI 封装：[OfflineSurvivalAssistant.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/OfflineSurvivalAssistant.kt)
- JNI 会话接口：[NativeLlmSession.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/NativeLlmSession.kt)
- JNI 入口：[llm_mnn_jni.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_mnn_jni.cpp)
- MNN 推理主逻辑：[llm_session.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_session.cpp)

## 高频关键词

- 离线可用
- 端侧推理
- MNN
- JNI
- 多模态
- prompt 约束
- 输出 normalize
- 生命周期管理
- 应用私有目录
- 模型状态校验

## 高频回答模板

### 为什么选端侧？

因为产品核心场景是断网和应急，云端方案在最关键的场景里不可用。

### 为什么做 JNI？

因为模型推理核心在 C++ / MNN，Kotlin 适合业务层；JNI 是二者之间的稳定桥梁。

### 为什么还要做输出清洗？

因为模型输出不等于产品可用输出，尤其在应急场景里，必须短、稳、可执行。

### 多模态怎么接？

图片先存本地，再用 `<img>路径</img>` 拼入 prompt，交给模型本地理解。

### 工程亮点是什么？

我把模型下载、会话管理、JNI 桥接、MNN 推理和 UI 状态做成了完整闭环，而不是只做了一个 Demo。

## 面试最后一句总结

这个项目最能体现我的地方，是我不仅会调用模型，还能把端侧模型能力真正做进 Android 产品，并处理好资源、生命周期、输出稳定性和用户体验。
