plugins {
  kotlin("multiplatform") version "1.7.21"
  id("org.jetbrains.dokka") version "1.7.20"
  `maven-publish`
  signing
}

group = "io.k-libs"
version = "0.1.0"
description = "Some library description."

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "1.8"
    }
    withJava()
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }

  js(IR) {
    browser()
    nodejs()
    binaries.executable()

    compilations.all {
      packageJson {
        customField("description", project.description!!)
      }
    }
  }

  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux"    -> linuxX64("native")
    isMingwX64           -> mingwX64("native")
    else                 -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  sourceSets {
    val commonMain by getting
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val jvmMain by getting
    val jvmTest by getting
    val jsMain by getting
    val jsTest by getting
    val nativeMain by getting
    val nativeTest by getting
  }
}

tasks.dokkaHtml {
  outputDirectory.set(file("docs/dokka/${project.version}"))
}

val javadocJar = tasks.register<Jar>("javadocJar") {
  dependsOn(tasks.dokkaHtml)
  archiveClassifier.set("javadoc")
  from(file("docs/dokka/${project.version}"))
}

tasks.withType<Jar> {
  enabled = true
}

publishing {
  repositories {
    maven {
      name = "Sonatype"
      url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = project.findProperty("nexus.user") as String? ?: System.getenv("NEXUS_USER")
        password = project.findProperty("nexus.pass") as String? ?: System.getenv("NEXUS_PASS")
      }
    }
  }

  publications {
    withType<MavenPublication> {
      artifact(javadocJar)

      pom {
        name.set("K-Sample")
        description.set(project.description)
        url.set("https://github.com/k-libs/k-lib-template")

        licenses {
          license {
            name.set("MIT")
          }
        }

        developers {
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("foxcapades.io@gmail.com")
            url.set("https://github.com/foxcapades")
          }
        }

        scm {
          connection.set("scm:git:git://github.com/k-libs/k-lib-template.git")
          developerConnection.set("scm:git:ssh://github.com/k-libs/k-lib-template.git")
          url.set("https://github.com/k-libs/k-lib-template")
        }
      }
    }
  }
}

signing {
  useGpgCmd()

  sign(configurations.archives.get())
  publishing.publications.withType<MavenPublication> { sign(this) }
}
