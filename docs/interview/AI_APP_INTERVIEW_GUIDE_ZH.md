# 末日工具箱面试讲稿（AI 应用工程方向）

这份材料面向 AI 应用工程岗位，重点讲“端侧本地大模型落地”。建议你按 `30 秒 -> 2 分钟 -> 5 分钟 -> 追问问答` 的顺序准备。

---

## 一、项目一句话定位

《末日工具箱》是一个面向断网、野外、灾害等场景的 Android 离线生存工具 App，核心亮点是把 Qwen3.5-2B 通过 MNN 部署到端侧，实现了本地文字与图片输入、本地推理、本地安全约束和离线应急体验。

---

## 二、30 秒项目介绍

我做了一个 Android 端的离线生存工具 App，叫《末日工具箱》。它面向野外迷路、灾害断网、极端环境这类场景，核心功能是一个本地 AI 生存助手，模型下载后可以在手机上做离线推理，不依赖云端 API。这个项目的技术重点不是普通聊天，而是把端侧模型下载、Kotlin 会话封装、JNI 桥接、MNN 本地推理、多模态图片输入和安全输出约束串成了一条完整链路，同时还配了知识库、SOS、指南针和 GPS 等离线能力。

---

## 三、2 分钟完整项目讲稿

这个项目的背景是，在灾害、户外或断网环境下，用户往往无法依赖在线搜索或云端助手，所以我希望做一个“真正能离线使用”的生存工具应用。  

从产品设计上，我把它拆成几个模块：AI 生存助手、离线知识库、SOS 求救、指南针和 GPS。其中最核心的是本地大模型能力，因为它决定了 App 能不能在无网条件下给用户提供动态建议。  

技术实现上，我选的是 Qwen3.5-2B 的 MNN 版本。用户首次联网时先下载模型文件，模型下载完成后保存在应用私有目录，并自动设置为当前可用模型，这部分在 [ModelDownloadRepository.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/model/ModelDownloadRepository.kt)。  

在 Kotlin 层，我用 [OfflineSurvivalAssistant.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/OfflineSurvivalAssistant.kt) 统一管理 prompt、会话生命周期和输出清洗；真正的 native 会话通过 [NativeLlmSession.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/NativeLlmSession.kt) 暴露给 Kotlin。  

在 JNI 层，我定义了 `nativeCreate / nativeGenerate / nativeReset / nativeRelease` 这几个接口，对应实现放在 [llm_mnn_jni.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_mnn_jni.cpp)。再往下，C++ 层在 [llm_session.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_session.cpp) 里调用 `MNN::Transformer::Llm::createLLM(configPath)` 创建模型实例，加载配置后执行生成。  

为了让输出更适合生存场景，我在 prompt 里明确限制“最多 5 条、每条一句、强调可执行、避免危险建议”，并在客户端再做一次 normalize，包括去除思考标签、截断长度和格式统一。这让结果更稳定，也更适合真实用户场景。  

另外，这个项目还支持图片输入。图片会先复制到应用私有目录，然后通过 `<img>本地路径</img>` 的方式拼进 prompt，让模型直接走本地多模态理解能力。整体上，这个项目的价值在于我把端侧模型集成做成了一个真正可运行、可演示、可扩展的 Android 产品原型。

---

## 四、5 分钟技术深讲版

### 1. 业务需求为什么决定必须做端侧

这个项目不是普通聊天 App，而是应急工具。  
如果我走云端 API，会有几个明显问题：

- 第一，断网场景下核心功能直接失效，不符合产品目标。
- 第二，灾害和应急场景对隐私更敏感，用户可能不希望图片和文本上传云端。
- 第三，产品的核心卖点就是“离线可用”，所以端侧推理不是锦上添花，而是产品成立的基础。

所以我从一开始就把技术路线定成了：联网只用于“下载模型”，推理必须“本地完成”。

### 2. 模型管理怎么做

模型相关逻辑集中在 [ModelDownloadRepository.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/model/ModelDownloadRepository.kt)。  
我做的事情有三层：

- 定义模型文件集合，比如 `config.json`、`llm.mnn`、`llm.mnn.weight`、`tokenizer.txt` 等。
- 下载到应用专属目录，避免和用户普通文件混在一起。
- 用 `AppPrefs.activeModelConfigPath` 记录当前激活模型，下载成功后自动设置，避免用户再点一次“设为当前模型”。

这块在面试里可以强调两个点：
- 我不仅下载文件，还做了“是否已下载”的校验逻辑。
- 我把模型状态抽象成 `ModelDownloadState`，UI 只消费状态，不直接感知下载细节。

### 3. Kotlin 会话层怎么设计

Kotlin 层的核心是 [OfflineSurvivalAssistant.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/OfflineSurvivalAssistant.kt)。

它负责三件事：

- `ensureLoaded()`：检查当前模型是否已配置并完成 native session 初始化。
- `buildPrompt()`：把用户输入和业务规则转成适合模型理解的 prompt。
- `normalize()`：对模型结果做二次规整，提升稳定性和可读性。

这里的设计思路是：  
UI 层只负责“收集输入”和“展示结果”，而真正的 AI 业务逻辑收口在 assistant 层，这样以后换模型、换 prompt 策略、增加多轮历史都不会污染页面代码。

### 4. Kotlin 和 C++ 的边界怎么划分

我没有让 Kotlin 直接承担推理细节，而是通过 [NativeLlmSession.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/NativeLlmSession.kt) 做了一层稳定接口。

接口非常克制，只有：

- `load()`
- `generate(prompt)`
- `reset()`
- `release()`
- `isReady()`

这样做的好处是：

- Kotlin 只关心业务和生命周期。
- C++ 只关心模型推理和性能。
- JNI 层足够薄，方便排查问题。

面试里可以把这句话讲出来：  
“我刻意把 JNI 层做薄，把它当成稳定桥接层，而不是让业务逻辑散落在 native 里。”

### 5. JNI 和 MNN 本地推理链路

JNI 入口在 [llm_mnn_jni.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_mnn_jni.cpp)。

工作流程是：

1. Kotlin 把 `configPath`、`mergedConfigJson` 和 `runtimeConfigJson` 传进 native。
2. JNI 把这些 Java 字符串转成 C++ 字符串和 JSON。
3. 创建 `doom::LlmSession`。
4. 如果加载失败，直接抛 `IllegalStateException` 回 Kotlin。
5. 成功后返回 native 指针给 Kotlin 持有。

真正推理发生在 [llm_session.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_session.cpp)：

- 通过 `MNN::Transformer::Llm::createLLM(configPath_)` 创建模型对象。
- 把运行时配置合并进去，比如 `use_mmap`、`tmp_path`。
- 调用 `llm_->load()` 完成模型加载。
- 生成时先 `response(history_, &outputStream, "<eop>", 1)`，再循环 `generate(1)`。
- 遇到 `<eop>` 停止。
- 去掉 `<think>` 内容后再返回给上层。

这部分可以突出你的理解不是“会用 SDK”，而是“知道生成过程和截断机制是怎么跑起来的”。

### 6. 多模态图片输入怎么接

图片能力有两个关键点：

- 页面选图后，先通过本地文件层落到应用私有目录。
- 再由 assistant 把图片路径拼成 `<img>本地路径</img>` 放进 prompt。

这个设计非常适合面试讲，因为它体现了你理解的不是 UI 选图，而是“多模态模型需要什么输入格式，以及如何从 Android 输入管道转成模型可消费的数据”。

### 7. 为什么还要做 prompt 约束和客户端二次清洗

这个项目不是开放式聊天，而是应急建议。  
如果完全放任模型自由输出，会出现两个问题：

- 输出太长，不适合用户在紧急场景下阅读。
- 输出可能包含模糊、危险或不确定建议。

所以我做了双层控制：

- 第一层：prompt 约束模型输出风格。
- 第二层：客户端对结果做 `normalize()`，统一成最多 5 条、格式稳定、可执行的建议。

这个点在面试里非常加分，因为它说明你知道“模型能力”不等于“产品可用性”，中间必须有工程化约束层。

### 8. Android 工程化怎么支撑这条链路

除了 AI 主链路，我在 Android 侧还做了这些工程化控制：

- 用 MVVM 拆分 `AI / Model / Knowledge / SOS / Sensor / UI`，减少模块耦合。
- 会话生命周期跟页面生命周期对齐，在 `ViewModel.onCleared()` 或退出时释放 native 资源。
- 模型和图片都放在私有目录，避免路径混乱和权限问题。
- 聊天页只负责输入输出，下载、推理、状态管理分别下沉到 repository / assistant / native session。

一句总结可以这么讲：  
“这个项目的重点不是把模型跑起来，而是把模型能力放进一个可维护、可交付、可继续扩展的 Android 应用结构里。”

---

## 五、本地大模型调用链专项拆解

面试官如果问“你把本地大模型接进 Android 的完整链路是什么”，你可以按下面回答：

### 1. 模型下载

用户在模型管理页触发下载，应用通过 [ModelDownloadRepository.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/model/ModelDownloadRepository.kt) 下载 `config.json`、`llm.mnn`、`llm.mnn.weight`、`tokenizer.txt` 等模型文件，并保存到应用专属目录。

### 2. 模型激活

下载完成后把 `config.json` 的路径写入 [AppPrefs.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/common/AppPrefs.kt) 中的 `activeModelConfigPath`，作为当前模型入口。

### 3. Kotlin 会话初始化

聊天时先走 [OfflineSurvivalAssistant.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/OfflineSurvivalAssistant.kt) 的 `ensureLoaded()`，读取 `configPath`，拼装 `runtimeConfigJson`，尤其是 `keep_history` 和 `mmap_dir`。

### 4. JNI 建桥

Kotlin 调用 [NativeLlmSession.kt](/E:/dev/ToolApp/app/src/main/java/com/doomsday/toolbox/ai/NativeLlmSession.kt) 的 `load()`，对应 native 侧的 [llm_mnn_jni.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_mnn_jni.cpp) `nativeCreate()`。

### 5. C++ 模型加载

native 层创建 `LlmSession`，在 [llm_session.cpp](/E:/dev/ToolApp/app/src/main/cpp/llm_session.cpp) 中调用 MNN 的 `createLLM()`、`set_config()` 和 `load()` 完成模型初始化。

### 6. Prompt 组织

Kotlin 根据业务规则构造 prompt。  
纯文本场景直接拼接用户问题。  
图片场景则把 `<img>图片路径</img>` 和用户问题一起拼接，交给多模态模型理解。

### 7. 本地生成

Kotlin 调用 `generate(prompt)`。  
JNI 进入 native，最终调用 `llm_->response(...)` 和循环 `llm_->generate(1)`。  
生成过程中通过流式 buffer 收集 token，碰到 `<eop>` 就结束。

### 8. 输出清洗

native 层先去掉 `<think>` 段。  
Kotlin 层再做 normalize，统一输出成适合生存场景的短步骤建议。

### 9. 生命周期回收

页面销毁或 ViewModel 清理时调用 `release()`，释放 native 指针和模型会话，避免内存泄漏。

---

## 六、10 个高频追问与回答

### 1. 为什么不用 OpenAI 或其他云端接口？

因为这个项目的场景本身就是断网、野外、灾害和应急环境，核心目标是“无网可用”。如果依赖云端 API，最关键的价值就没了。同时端侧方案对图片和文本隐私更友好。

### 2. 端侧大模型最难的点是什么？

不是把模型“跑起来”，而是把它变成一个可稳定交付的移动端能力。难点主要在模型资源管理、JNI 桥接、内存与加载策略、输出稳定性控制，以及和 Android 生命周期的正确配合。

### 3. Kotlin 和 C++ 的边界怎么划分？

Kotlin 负责业务逻辑、状态管理、prompt 组织和生命周期；C++ 负责模型加载和生成；JNI 只是桥接层。我尽量保持 JNI 很薄，避免业务逻辑下沉到 native，保证可维护性。

### 4. 模型下载后怎么判断是否可用？

我不是只看文件夹是否存在，而是会校验关键文件，比如 `config.json` 和 required files 是否齐全。只有关键文件齐了，才认为模型已下载，并允许后续加载。

### 5. 多模态图片是怎么传给模型的？

图片先落到应用私有目录，然后在 prompt 中用 `<img>本地路径</img>` 的格式传给模型。这是把 Android 文件输入转换为模型多模态输入的一种轻量方式。

### 6. 如果模型输出不稳定怎么办？

我做了双层约束：第一层是 prompt 约束输出格式和安全边界；第二层是客户端 normalize，包括截断条数、统一编号、去除多余内容。这样就算模型输出有波动，最终用户看到的结果仍然相对稳定。

### 7. 为什么要做客户端二次清洗？

因为模型输出不一定天然适合产品消费。尤其在应急场景里，用户需要的是短、清晰、可执行的结果，所以客户端要承担最后一道“产品化格式控制”。

### 8. 端侧推理的性能和内存怎么考虑？

这个项目用的是较小规模的端侧模型，并通过 `mmap_dir` 配置支持更合理的加载策略，尽量减少直接整块加载压力。另外会话对象是按需加载、按生命周期释放，避免长时间占用资源。

### 9. 这个项目的工程亮点是什么？

我觉得亮点不是功能多，而是把 AI 能力做成了完整闭环：模型下载、模型激活、Kotlin 封装、JNI 桥接、MNN 推理、多模态输入、安全输出、UI 状态管理，整条链路是通的。

### 10. 如果继续迭代，你会怎么优化？

我会优先做三件事：  
第一，优化流式输出体验，让生成过程边出边显示；  
第二，引入更细粒度的模型状态校验和下载恢复；  
第三，做性能监控和埋点，观察不同机型上的首轮加载耗时、内存占用和回复时延。

---

## 七、你在面试里最该强调的关键词

- 离线可用
- 端侧大模型
- 本地推理
- 模型下载与激活
- Kotlin + JNI + C++
- MNN 集成
- 多模态图片输入
- Prompt 工程
- 输出规整
- Android 工程化

---

## 八、推荐你的讲法节奏

### 第一轮：先讲价值

“这是一个离线 AI 生存助手，不依赖云端 API，主要面向断网和应急场景。”

### 第二轮：再讲技术主线

“它的核心是把模型下载、Kotlin 封装、JNI 桥接、MNN 推理和多模态输入做成一条闭环。”

### 第三轮：最后讲工程理解

“我最关注的不是把模型跑起来，而是把它变成移动端上可维护、可扩展、可交付的产品能力。”

这三句话一出来，面试官基本就知道你不是在背概念，而是真的做过落地。
