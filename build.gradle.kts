plugins {
    kotlin("jvm") version "1.5.20"
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

val pluginStorage = "C:/MC-Development/PluginStorage"
val nmsBukkitPath = "C:/MC-Development/마인즈서버/paper-1.12.2-R0.1-SNAPSHOT-shaded.jar"
val copyPluginDestination = "C:/MC-Development/마인즈서버/plugins"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(files(nmsBukkitPath))

    compileOnly("com.zaxxer", "HikariCP", "4.0.3")
    compileOnly("com.comphenix.protocol", "ProtocolLib", "4.6.0")

    compileOnly(files("$pluginStorage/ServerCore_S.jar"))
    compileOnly(files("$pluginStorage/CrackShot-2_S.jar"))
    compileOnly(files("$pluginStorage/GlowAPI_v1.4.6_S.jar"))
}

spigot {
    authors = listOf("SuL")

    apiVersion = "1.12"
    version = project.version.toString()
    depends = listOf("ServerCore")
    softDepends = listOf("CrackShot-2")
    commands {
        create("ItemFarming") {
            permission = "op.op"
        }
        create("itemdb") {
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

//    val copyPlugin = register<Copy>("copyPlugin") {
//        from(files("$pluginStorage/${project.name}_S.jar"))
//        into(file("C:/Users/PHR/Desktop/SERVER2/plugins"))
//    }
    val copyPlugin_2 = register<Copy>("copyPlugin_2") {
        from(files("$pluginStorage/${project.name}_S.jar"))
        into(file(copyPluginDestination))
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
        finalizedBy(copyPlugin_2)
    }
}
