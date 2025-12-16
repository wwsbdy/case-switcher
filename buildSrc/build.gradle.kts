plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://maven.aliyun.com/nexus/content/groups/public/")
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://maven.aliyun.com/repository/google/")
    maven("https://maven.aliyun.com/repository/jcenter/")
    maven("https://maven.aliyun.com/repository/central/")
    google()
    mavenCentral()
    gradlePluginPortal()
}