# Add project specific ProGuard rules for FriendHub Release Obfuscation
-keep class com.mycompany.friendhub.** { *; }
-keep class com.example.** { *; }
-keepnames class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
