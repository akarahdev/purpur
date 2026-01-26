plugins {
    id("fabric-loom") version "1.15-SNAPSHOT"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

loom {
    accessWidenerPath = file("src/main/resources/purpur.accesswidener")
}

val usingLocal = true

repositories {
    mavenCentral()
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
    }
    if(usingLocal) {
        mavenLocal()
        maven {
            url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
        }
    }
}

dependencies {
    mappings(loom.officialMojangMappings())

    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")

    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    if (usingLocal) {
        modApi("io.github.redvortexdev:Flint:1.0.0")
    } else {
        modApi("maven.modrinth:flint:${project.property("flint_version")}")
    }
    modImplementation("net.kyori:adventure-platform-fabric:${project.property("adventure_fabric_version")}")

    implementation("org.apache.commons:commons-text:1.15.0")
    implementation("com.github.florianingerl.util:regex:1.1.11")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version")!!,
            "loader_version" to project.property("loader_version")!!
        )
    }
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}