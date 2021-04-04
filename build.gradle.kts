plugins {
    kotlin("jvm") version "1.4.10"
    id("kr.entree.spigradle") version "2.2.3"
}

group = "kr.sul"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io/")
    mavenLocal()
}

val pluginStorage = "C:/Users/PHR/Desktop/PluginStorage"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.destroystokyo.paper", "paper-api", "1.12.2-R0.1-SNAPSHOT")
    implementation("org.spigotmc", "spigot", "1.12.2-R0.1-SNAPSHOT")

    compileOnly("com.zaxxer", "HikariCP", "4.0.3")
//    compileOnly("com.github.simplix-softworks", "SimplixStorage", "3.2.2")
    runtimeOnly("com.google.code.gson", "gson", "2.8.6")

    compileOnly(files("C:/Users/PHR/Desktop/PluginStorage/ServerCore_S.jar"))
}

spigot {
    authors = listOf("SuL")

    apiVersion = "1.12"
    version = project.version.toString()
    depends = listOf("ServerCore")
    commands {
        create("ItemFarming") {
            permission = "op.op"
        }
    }
}

val shade = configurations.create("shade")
shade.extendsFrom(configurations.compileOnly.get())

tasks {
    compileJava.get().options.encoding = "UTF-8"
    compileKotlin.get().kotlinOptions.jvmTarget = "1.8"
    compileTestKotlin.get().kotlinOptions.jvmTarget = "1.8"

    val copyPlugin = register<Copy>("copyPlugin") {
        from(files("$pluginStorage/${project.name}_S.jar"))
        into(file("C:/Users/PHR/Desktop/SERVER2/plugins"))
    }

    jar {
        archiveFileName.set("${project.name}_S.jar")
        destinationDirectory.set(file(pluginStorage))

//        from(
//            shade.filter { it.name.startsWith("SimplixStorage") }  // compileOnly 파일 중에 anvilgui만!
//                .map {
//                    if (it.isDirectory)
//                        it
//                    else
//                        zipTree(it)
//                }
//        )
        finalizedBy(copyPlugin)
    }
}
