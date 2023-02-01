pluginManagement {
    val quarkusPluginVersion: String by settings
    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("io.quarkus.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version quarkusPluginVersion
    }
}

rootProject.name="code-with-quarkus"

include(
  "modA",
  "modB"
)