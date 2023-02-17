plugins {
    kotlin("jvm") version "1.8.0"
    id("kr.entree.spigradle") version "2.2.3"
}

group = "kr.sul"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io/")
    maven("https://nexus-repo.jordanosterberg.com/repository/maven-releases/")
}

val pluginDestination = "C:/MC-Development/마인즈서버/plugins"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC")
    compileOnly("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("com.destroystokyo.paper:paper:1.12.2-R0.1-SNAPSHOT")

    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
//    compileOnly("dev.jcsoftware:JScoreboards","2.1.2-RELEASE")
//    implementation("org.yaml:snakeyaml:1.29")  snakeyaml은 bukkit의 lib에 포함되있을텐데?
    compileOnly("kr.sul.servercore:bukkit:1.0-SNAPSHOT")
    compileOnly("kr.sul:CrackShot-2:1.0-SNAPSHOT")
    testImplementation("junit:junit:4.13.2")
}

spigot {
    authors = listOf("SuL")

    apiVersion = "1.12"
    version = project.version.toString()
    depends = listOf("ServerCore")
    softDepends = listOf("CrackShot-2")
    commands {
        create("itemdb") {
            permission = "op.op"
        }
    }
}


// NOTE synchronized 는 "객체 전체"에 Lock을 검 (-> saveData()로 락이 걸린 상황에서 loadData()는 수행될 수 없음. 또한 PlayerData가 가지고 있는 파라미터도 접근 못 함)
// synchronized() 함수를 사용 시에는 블락할 단위를 설정할 수 있음. 자세한 내용은 아래 링크 참조
// https://tourspace.tistory.com/54 - from AAClans

val shade = configurations.create("shade")
shade.extendsFrom(configurations.compileOnly.get())

tasks {
    compileJava.get().options.encoding = "UTF-8"
    compileKotlin.get().kotlinOptions.jvmTarget = "1.8"
    compileTestKotlin.get().kotlinOptions.jvmTarget = "1.8"

//    val copyPlugin_2 = register<Copy>("copyPlugin_2") {
//        from(jar)
//        into(file(copyPluginDestination))
//    }
//    val movePlugin = register("movePlugin") {
//        doLast {
//            ant.withGroovyBuilder {
//                "move"("file" to jar.get().archiveFile.get(), "todir" to copyPluginDestination)
//            }
//        }
//    }

    jar {
        archiveFileName.set("${project.name}_S.jar")
        destinationDirectory.set(file(pluginDestination))  // clean 영향 안 받음. (영향받는 건 project.buildDir으로 설정했을 때)
    }
}