#pragma once

#include <functional>
#include <streambuf>

class LlmStreamBuffer : public std::streambuf {
public:
    using Callback = std::function<void(const char* text, size_t length)>;

    explicit LlmStreamBuffer(Callback callback) : callback_(std::move(callback)) {}

protected:
    std::streamsize xsputn(const char* s, std::streamsize n) override {
        if (callback_) {
            callback_(s, static_cast<size_t>(n));
        }
        return n;
    }

private:
    Callback callback_;
};
