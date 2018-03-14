# Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class * { *; }
-dontwarn javax.**
-dontwarn io.realm.**

# RetroFit2
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# UCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

# PlaceHolderView
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.mindorks.placeholderview.annotations.** <methods>;
}

# CarouselView
-keep class com.synnapps.carouselview.** { *; }