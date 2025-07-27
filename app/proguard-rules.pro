#############################################
# PROGUARD RULES FOR WanderTrack APP
#############################################

########## GENERAL APP RULES ##########

# Keep all classes in your app's main package (modelos, managers, repositorios, etc.)
-keep class com.carlosjimz87.wandertrack.** { *; }

########## FIREBASE ##########

# General Firebase SDK
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Firebase Auth & Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Google Maps core
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.maps.**

# GeoJsonLayer / KML / utilidades de Maps
-keep class com.google.maps.android.data.** { *; }
-keep class com.google.maps.android.ktx.** { *; }
-dontwarn com.google.maps.android.**

# Para cargar estilos desde JSON
-keep class com.google.android.gms.maps.model.MapStyleOptions { *; }

# Firestore: keep annotated fields
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}
-keepattributes Signature
-keepattributes *Annotation*

########## GSON ##########

# Keep fields with @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

########## KOIN ##########

# Allow Koin to find modules and inject dependencies
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}
-keep class org.koin.** { *; }
-dontwarn org.koin.**

########## JETPACK COMPOSE ##########

# Keep Compose UI elements
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Navigation 3 (optional if you're using androidx.navigation3)
-keep class androidx.navigation3.** { *; }
-dontwarn androidx.navigation3.**

########## ANDROIDX (GENERAL) ##########

# Prevent stripping of base classes used reflectively
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-dontwarn androidx.lifecycle.**
-dontwarn androidx.navigation.**

########## KEEP MAIN ENTRY POINTS ##########

# Keep Application and MainActivity entry points
-keep class com.carlosjimz87.wandertrack.WanderTrackApp { *; }
-keep class com.carlosjimz87.wandertrack.MainActivity { *; }

########## OPTIONAL DEBUGGING ##########

# To help troubleshoot ProGuard-related crashes, remove this in production
# -printmapping mapping.txt

########## JSON GOOGLE MAPS FILES IN RAW ##########
-keep class com.carlosjimz87.wandertrack.R$raw {
    *;
}
-keepclassmembers class ** {
    public static final int *;
}