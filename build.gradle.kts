import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-library`
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "it.unibo"
version = "0.3.3"

repositories { mavenCentral() }

dependencies {
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:${extra["paho_version"].toString()}")
    implementation("com.google.code.gson:gson:${extra["gsonVersion"].toString()}")
    implementation(kotlin("stdlib-jdk8"))
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

tasks.shadowJar.configure {
    // removes "-all" from the jar name
    archiveClassifier.set("")
    exclude ("**/*.kotlin_metadata")
    exclude ("**/*.kotlin_module")
    exclude ("**/*.kotlin_builtins")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}