fun properties(key: String) =
    project.findProperty(key)?.toString() ?: throw IllegalStateException("Property `$key` is undefined")

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.14.1"
}

group = "com.zj"
version = properties("plugin.version")

intellij {
    version.set(properties("intellij.version"))
    type.set(properties("intellij.type"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(properties("since.build"))
        untilBuild.set(properties("until.build"))
        changeNotes.set(parseChangeNotesFromReadme(rootProject.file("CHANGELOG.md").toPath()))
    }

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
dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testImplementation("junit:junit:4.13.1")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.assemble {
    dependsOn(tasks.buildPlugin)
}

tasks.buildPlugin {
    destinationDirectory.set(project.rootProject.file("dist"))
}


tasks.clean {
    delete(tasks.buildPlugin.get().archiveFile)
    delete(project.rootProject.file("dist"))
}

tasks.withType<Test>().configureEach {
    enabled = false
}

tasks.publishPlugin {
    token.set(properties("publish.token"))
}