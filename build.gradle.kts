import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.31"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compile("org.apache.commons:commons-csv:1.5")
    compile("no.tornado:tornadofx:1.7.17")
    compile("de.jensd:fontawesomefx:8.9")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xallow-result-return-type")
    }
}

tasks.named<Jar>("jar") {
    manifest.attributes["Main-Class"] = "com.zelkatani.gui.app.Editor"
    from(configurations.runtimeClasspath.map { if (it.isDirectory) it as Any else zipTree(it) })
    from(sourceSets["main"].output)
    archiveName = "localization-editor.jar"
}