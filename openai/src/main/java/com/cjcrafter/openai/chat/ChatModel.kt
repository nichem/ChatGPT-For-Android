package com.cjcrafter.openai.chat

enum class ChatModel(val string: String) {
    /**
     * gpt-3.5-turbo
     */
    GPT_3_5_TURBO("gpt-3.5-turbo"),

    /**
     * gpt-3.5-turbo-0613 支持函数
     */
    GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613"),

    /**
     * gpt-3.5-turbo-16k 超长上下文
     */
    GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k"),

    /**
     * gpt-3.5-turbo-16k-0613 超长上下文 支持函数
     */
    GPT_3_5_TURBO_16K_0613("gpt-3.5-turbo-16k-0613"),

    /**
     * GPT4.0
     */
    GPT_4("gpt-4"),

    /**
     * GPT4.0 超长上下文
     */
    GPT_4_32K("gpt-4-32k"),

    /**
     * gpt-4-0613，支持函数
     */
    GPT_4_0613("gpt-4-0613"),

    /**
     * gpt-4-0613，支持函数
     */
    GPT_4_32K_0613("gpt-4-32k-0613"),
}
