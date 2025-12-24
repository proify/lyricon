pluginManagement {
    repositories {
        mavenLocal()
        //maven { url = uri("https://repo1.maven.org/maven2/") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/google/") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://api.xposed.info/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        //maven { url = uri("https://repo1.maven.org/maven2/") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/google/") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
        maven { url = uri("https://api.xposed.info/") }
    }
}

rootProject.name = "LyriconProject"

include(
    ":lyricon",
    ":app",
    ":app:bridge",
    ":xposed",
    ":common",
)

include(":lyric:bridge:core")
include(":lyric:bridge:central")
include(":lyric:bridge:provider")
include(":lyric:bridge:subscriber")

include(":lyric:model")
include(":lyric:view")
include(":lyric:style")