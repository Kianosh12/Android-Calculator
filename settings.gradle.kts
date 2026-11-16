pluginManagement {
    repositories {
        // Optional mirror for the Google Maven repository, useful on restricted networks.
        // Enable with: ./gradlew -PgoogleMavenMirror=https://<mirror>/google ...
        providers.gradleProperty("googleMavenMirror").orNull?.let { mirror ->
            maven(url = mirror)
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        providers.gradleProperty("googleMavenMirror").orNull?.let { mirror ->
            maven(url = mirror)
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Android-Calculator"
include(":app")
