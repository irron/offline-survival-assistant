# ProGuard rules for Survival Toolbox

# Keep ViewModel and LiveData
-keep class androidx.lifecycle.** { *; }

# Keep Room entities
-keep class com.survival.toolbox.knowledge.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
