import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    id("java")
    id("eclipse")
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
}

group = "github.moriyoshi"
version = "2.0.0"

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.codemc.org/repository/maven-public/")
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://maven.maxhenkel.de/repository/public")
}

val anvilGUI = "net.wesjd:anvilgui:1.9.4-SNAPSHOT"
val commandAPI = "dev.jorel:commandapi-bukkit-shade:9.4.2"
val nbtAPI = "de.tr7zw:item-nbt-api:2.12.4"
val reflections = "org.reflections:reflections:0.10.2"
val fastBoard = "fr.mrmicky:fastboard:2.1.2"

val relocates = mapOf(
    "dev.jorel.commandapi" to "github.moriyoshi.comminiplugin.dependencies.commandapi",
    "net.wesjd.anvilgui" to "github.moriyoshi.comminiplugin.dependencies.anvilgui",
    "de.tr7zw.changeme.nbtapi" to "github.moriyoshi.comminiplugin.dependencies.nbtapi",
    "org.reflections" to "github.moriyoshi.comminiplugin.dependencies.reflections",
    "fr.mrmicky.fastboard" to "github.moriyoshi.comminiplugin.dependencies.reflections"
)

val lombok = "org.projectlombok:lombok:1.18.32"

// paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")

    compileOnly(lombok)
    annotationProcessor(lombok)

    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)

    shadow(reflections)
    shadow(anvilGUI)
    shadow(commandAPI)
    shadow(nbtAPI)
    shadow(fastBoard)
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.shadow.get())

        relocates.forEach { (originalPackage, relocatedPackage) ->
            relocate(
                originalPackage,
                relocatedPackage
            )
        }
    }
}

tasks.register("copyPlugin") {
    dependsOn("reobfJar")
    doLast {
        if (file("./build/libs/ComMiniPlugin-${version}.jar").exists()) {
            exec {
                workingDir(".")
                executable("./run.sh")
                args("$version")
            }
        } else {
            println("Once again, please.")
        }
    }
}


tasks.register("runPlugin") {
    dependsOn("reobfJar")
    doLast {
        if (file("./build/libs/ComMiniPlugin-${version}.jar").exists()) {
            exec {
                workingDir(".")
                executable("./run.sh")
                args("$version", "run")
            }
        } else {
            println("Once again, please.")
        }
    }
}

bukkitPluginYaml {
    main = "github.moriyoshi.comminiplugin.ComMiniPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.POSTWORLD
    name = "ComMiniPlugin"
    description = "ComMiniPlugin"
    authors.add("moriyoshi-kasuga")
    apiVersion = "1.20.6"
    // TODO: add psql
    libraries = listOf("org.postgresql:postgresql:42.7.3", "com.zaxxer:HikariCP:5.1.0")
}
