plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
}

javafx {
    version = "19"
    modules("javafx.controls")
}


repositories {
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}

application {
    mainClass.set("ass2.App")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
