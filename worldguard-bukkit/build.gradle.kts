import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "paper"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "arim-mvn-lgpl3"
        url = uri("https://mvn-repo.arim.space/lesser-gpl3/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    "api"(project(":worldguard-core"))
    "compileOnly"("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    "runtimeOnly"("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }
    "api"("com.sk89q.worldedit:worldedit-bukkit:${Versions.WORLDEDIT}") { isTransitive = false }
    "implementation"("com.google.guava:guava:${Versions.GUAVA}")
    "compileOnly"("com.sk89q:commandbook:2.3") { isTransitive = false }
    "shadeOnly"("io.papermc:paperlib:1.0.8")
    "shadeOnly"("space.arim.morepaperlib:morepaperlib:0.4.2")
    "shadeOnly"("org.bstats:bstats-bukkit:3.0.1")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(dependency(":worldguard-core"))
        relocate("org.bstats", "com.sk89q.worldguard.bukkit.bstats") {
            include(dependency("org.bstats:"))
        }
        relocate ("io.papermc.lib", "com.sk89q.worldguard.bukkit.paperlib") {
            include(dependency("io.papermc:paperlib"))
        }
        relocate ("space.arim.morepaperlib", "com.sk89q.worldguard.bukkit.morepaperlib") {
            include(dependency("space.arim.morepaperlib:morepaperlib"))
        }
        relocate ("co.aikar.timings.lib", "com.sk89q.worldguard.bukkit.timingslib") {
            include(dependency("co.aikar:minecraft-timings"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
