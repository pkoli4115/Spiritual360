# --- KEEP DATA CLASSES FOR JSON ---
-keep class com.hindu.pooja.model.** { *; }
-keep class com.hindu.pooja.data.** { *; }

# --- GSON ---
-keep class com.google.gson.** { *; }
-keepattributes Signature,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- KOTLINX SERIALIZATION ---
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*

# --- KEEP CONSTRUCTORS ---
-keepclassmembers class * {
    public <init>(...);
}
