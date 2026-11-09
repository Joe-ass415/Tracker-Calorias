# Mobile proguard rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep class com.google.ai.client.generativeai.** { *; }
-keep class com.calorietracker.shared.model.** { *; }
-keep class com.calorietracker.mobile.data.local.entity.** { *; }

