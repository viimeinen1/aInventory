group = "io.github.viimeinen1.ainventory"
version = "3.1.3"
description = "Inventory GUI library"

plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

repositories {
    mavenLocal()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/") 
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

mavenPublishing {
  publishToMavenCentral()
  
  signAllPublications()
}

mavenPublishing {
  coordinates(group.toString(), name, version.toString())

  pom {
    name.set("aInventory")
    description.set("Inventory GUI library")
    inceptionYear.set("2025")
    url.set("https://github.com/viimeinen1/ainventory/")
    licenses {
      license {
        name.set("MIT")
        url.set("https://opensource.org/licenses/MIT")
      }
    }
    developers {
      developer {
        id.set("viimeinen1")
        name.set("viimeinen1")
        url.set("https://github.com/viimeinen1/")
      }
    }
    scm {
      url.set("https://github.com/viimeinen1/ainventory/")
    }
  }
}