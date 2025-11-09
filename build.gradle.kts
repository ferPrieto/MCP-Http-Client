plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "io.mcp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    
    // Dependency Injection - Koin
    implementation("io.insert-koin:koin-core:3.5.3")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.insert-koin:koin-test:3.5.3") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
        exclude(group = "junit", module = "junit")
    }
    testImplementation("io.insert-koin:koin-test-junit5:3.5.3") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
}

application {
    mainClass.set("io.mcp.httpclient.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

tasks.shadowJar {
    archiveBaseName.set("mcp-http-client")
    archiveClassifier.set("all")
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "io.mcp.httpclient.MainKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.register<JavaExec>("runBenchmark") {
    group = "verification"
    description = "Run cache performance benchmark"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("io.mcp.httpclient.benchmark.RunBenchmarkKt")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.mcp"
            artifactId = "httpclient"
            version = "1.0.0"
            
            from(components["java"])
            
            artifact(tasks["shadowJar"])
            
            pom {
                name.set("MCP HTTP Client Server")
                description.set("Model Context Protocol server for HTTP/HTTPS, GraphQL, and TCP/Telnet connections")
                url.set("https://github.com/ferPrieto/MCP-Http-Client")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("ferPrieto")
                        name.set("Fernando Prieto")
                        email.set("f.prieto.moyano@gmail.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/ferPrieto/MCP-Http-Client.git")
                    developerConnection.set("scm:git:ssh://github.com/ferPrieto/MCP-Http-Client.git")
                    url.set("https://github.com/ferPrieto/MCP-Http-Client")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ferPrieto/MCP-Http-Client")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME") ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}








