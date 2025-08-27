pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroup("com.github.pedroSG94.rtmp-rtsp-stream-client-java")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroup("com.github.pedroSG94.rtmp-rtsp-stream-client-java")
            }
        }
    }
}

rootProject.name = "OTG USB CAMERA"
include(":app")
include(":lib:strem_lib")
include(":lib:libuvccamera")
