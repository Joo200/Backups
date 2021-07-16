import de.terraconia.gradle.dependency.TerraBukkitDependency.*

plugins {
    java
    jacoco
    id("de.terraconia.terragradle") version "0.1.0-SNAPSHOT"
}

group = "de.terraconia"
version = "SERVER-SNAPSHOT"

dependencies {
    compileOnly("de.terraconia:CoreModule-Bukkit:SERVER-SNAPSHOT:all")
}

terraconia {
    dependencies(
        BUKKIT,
        WORLDGUARD,
        WORLDEDIT,
    //    CORE_MODULE,
        DYNMAP,
        CHESTSHOP,
        VAULT,
        LUCKPERMS,
        LWC
    )
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}