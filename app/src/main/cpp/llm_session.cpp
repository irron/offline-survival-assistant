#include "llm_session.h"

#include <algorithm>
#include <cctype>
#include <ostream>
#include <sstream>

#include "llm_stream_buffer.hpp"
#include "mls_log.h"
#include "utf8_stream_processor.hpp"

namespace doom {

namespace {

std::string ltrim(std::string value) {
    value.erase(value.begin(), std::find_if(value.begin(), value.end(), [](unsigned char ch) {
        return !std::isspace(ch);
    }));
    return value;
}

}  // namespace

LlmSession::LlmSession(
        std::string configPath,
        nlohmann::json mergedConfig,
        nlohmann::json runtimeConfig)
    : configPath_(std::move(configPath)),
      mergedConfig_(std::move(mergedConfig)),
      runtimeConfig_(std::move(runtimeConfig)) {
    maxNewTokens_ = mergedConfig_.value("max_new_tokens", 512);
    keepHistory_ = runtimeConfig_.value("keep_history", false);
    systemPrompt_ = mergedConfig_.value("system_prompt", std::string("You are a helpful assistant."));
    history_.emplace_back("system", systemPrompt_);
}

LlmSession::~LlmSession() {
    delete llm_;
}

bool LlmSession::load() {
    lastError_.clear();
    llm_ = MNN::Transformer::Llm::createLLM(configPath_);
    if (llm_ == nullptr) {
        lastError_ = "Failed to create MNN LLM instance.";
        return false;
    }

    auto config = mergedConfig_;
    const std::string mmapDir = runtimeConfig_.value("mmap_dir", std::string());
    if (!mmapDir.empty()) {
        config["use_mmap"] = true;
        config["tmp_path"] = mmapDir;
    }

    llm_->set_config(config.dump());
    if (!llm_->load()) {
        lastError_ = "Failed to load model from downloaded config.";
        return false;
    }

    return true;
}

bool LlmSession::isReady() const {
    return llm_ != nullptr;
}

std::string LlmSession::generate(const std::string& prompt) {
    if (llm_ == nullptr) {
        return "模型尚未加载";
    }

    if (!keepHistory_) {
        reset();
    }

    history_.emplace_back("user", prompt);

    std::stringstream responseBuffer;
    bool reachedEop = false;

    mls::Utf8StreamProcessor processor([&](const std::string& chunk) {
        if (chunk.find("<eop>") != std::string::npos) {
            reachedEop = true;
            return;
        }
        responseBuffer << chunk;
    });

    LlmStreamBuffer streamBuffer([&](const char* text, size_t len) {
        processor.processStream(text, len);
    });
    std::ostream outputStream(&streamBuffer);

    llm_->response(history_, &outputStream, "<eop>", 1);
    int generated = 0;
    while (!reachedEop && generated < maxNewTokens_) {
        llm_->generate(1);
        generated++;
    }

    std::string response = trimResponse(responseBuffer.str());
    if (keepHistory_) {
        history_.emplace_back("assistant", response);
    }
    return response;
}

void LlmSession::reset() {
    history_.clear();
    history_.emplace_back("system", systemPrompt_);
}

const std::string& LlmSession::getLastError() const {
    return lastError_;
}

std::string LlmSession::trimResponse(const std::string& text) const {
    std::string value = text;
    const std::string thinkStart = "<think>";
    const std::string thinkEnd = "</think>";
    const auto start = value.find(thinkStart);
    const auto end = value.find(thinkEnd);
    if (start != std::string::npos && end != std::string::npos && end > start) {
        value.erase(start, end + thinkEnd.size() - start);
    }
    return ltrim(value);
}

}  // namespace doom
