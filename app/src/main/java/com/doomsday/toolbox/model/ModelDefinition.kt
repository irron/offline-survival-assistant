package com.doomsday.toolbox.model

data class ModelDefinition(
    val displayName: String,
    val modelScopeId: String,
    val files: List<String>,
    val requiredFiles: Set<String>,
    val approxSizeLabel: String
) {
    val folderName: String get() = modelScopeId.substringAfter("/")
}

object ModelCatalog {
    val qwen35_2b = ModelDefinition(
        displayName = "Qwen3.5-2B-MNN",
        modelScopeId = "MNN/Qwen3.5-2B-MNN",
        files = listOf(
            "config.json",
            "configuration.json",
            "export_args.json",
            "llm.mnn",
            "llm.mnn.json",
            "llm.mnn.weight",
            "llm_config.json",
            "tokenizer.txt",
            "visual.mnn",
            "visual.mnn.weight"
        ),
        requiredFiles = setOf(
            "config.json",
            "llm.mnn",
            "llm.mnn.weight",
            "tokenizer.txt"
        ),
        approxSizeLabel = "约 2.0 GB"
    )
}
