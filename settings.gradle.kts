pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/wildberries-tech/wbmap-sdk-android")
            credentials {
                val githubUser: String by settings
                val githubToken: String by settings

                username = githubUser
                password = githubToken
            }
        }
    }
}

rootProject.name = "wbmapSdk"
include(":app")
