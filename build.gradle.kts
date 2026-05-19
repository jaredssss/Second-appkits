buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.27")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
