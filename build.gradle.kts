buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.6.0") // Safe Args Plugin
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
