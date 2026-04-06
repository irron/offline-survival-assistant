#pragma once

#include <string>
#include <utility>
#include <vector>

#include "llm/llm.hpp"
#include "nlohmann/json.hpp"

namespace doom {

class LlmSession {
public:
    LlmSession(std::string configPath, nlohmann::json mergedConfig, nlohmann::json runtimeConfig);
    ~LlmSession();

    bool load();
    bool isReady() const;
    std::string generate(const std::string& prompt);
    void reset();
    const std::string& getLastError() const;

private:
    std::string trimResponse(const std::string& text) const;

    std::string configPath_;
    nlohmann::json mergedConfig_;
    nlohmann::json runtimeConfig_;
    std::vector<std::pair<std::string, std::string>> history_;
    MNN::Transformer::Llm* llm_ = nullptr;
    int maxNewTokens_ = 512;
    bool keepHistory_ = false;
    std::string systemPrompt_;
    std::string lastError_;
};

}  // namespace doom
