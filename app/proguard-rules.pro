# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keepattributes Signature
-keepattributes *Annotation*

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
-keepclassmembers class tikfans.tikplus.model.** {
  *;
}

-keep class tikfans.tikplus.model.** {
  *;
}

-keep class com.android.vending.** { *; }
-dontnote com.google.**
-dontwarn com.google.gson.Gson$6
-ignorewarnings

-keep class com.google.**
-keep interface com.google.** { *;}
-dontwarn com.google.**

-keep class com.google.api.** {
    *;
}
#-keep public class android.suport.design.** { *; }
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
#-keepclassmembers class * {
#  @com.google.api.client.util.Key <fields>;
#}

# Needed by google-http-client-android when linking against an older platform version
-dontwarn com.google.api.client.extensions.android.**

# Needed by google-api-client-android when linking against an older platform version
-dontwarn com.google.api.client.googleapis.extensions.android.**

# Needed by google-play-services when linking against an older platform version
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontnote com.google.android.gms.**
# com.google.client.util.IOUtils references java.nio.file.Files when on Java 7+
-dontnote java.nio.file.Files, java.nio.file.Path

# Suppress notes on LicensingServices
-dontnote **.ILicensingService

# Suppress warnings on sun.misc.Unsafe
-dontnote sun.misc.Unsafe
-dontwarn sun.misc.Unsafe