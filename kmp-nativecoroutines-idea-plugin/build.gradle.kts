@file:Suppress("UnstableApiUsage")

plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij)
}

kotlin {
    explicitApi()
    jvmToolchain(17)
}

dependencies {
    implementation(project(":kmp-nativecoroutines-compiler"))
}

intellij {
    version = "233-EAP-SNAPSHOT"
    type = "IC"
    plugins = listOf("org.jetbrains.kotlin", "com.intellij.gradle")
}

tasks {
    patchPluginXml {
        sinceBuild = "233"
        untilBuild = "233.*"
    }

    buildSearchableOptions {
        enabled = false
    }

    signPlugin {
        certificateChain = System.getenv("IDEA_CERTIFICATE_CHAIN")
        privateKey = System.getenv("IDEA_PRIVATE_KEY")
        password = System.getenv("IDEA_PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        token = System.getenv("IDEA_PUBLISH_TOKEN")
        if ((version as String).contains("-kotlin-")) {
            channels = listOf("eap")
        }
    }

    runIde {
        maxHeapSize = "4g"
    }
}
