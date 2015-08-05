import sbt._

object Version {
  val scala = "2.11.7"
  val akka = "2.3.11"
  val logback = "1.0.13"
  val spray = "1.3.3"
  val sprayJson = "1.3.2"
  val scalaTest = "2.2.4"
  val spec2 = "2.3.13"
}

object Library {
  val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % Version.akka
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback
  val sprayCan = "io.spray" %% "spray-can" % Version.spray
  val sprayRouting = "io.spray" %% "spray-routing" % Version.spray
  val sprayClient = "io.spray" %% "spray-client" % Version.spray
  val sprayJson = "io.spray" %% "spray-json" % Version.sprayJson
  val sprayTest = "io.spray" %% "spray-testkit" % Version.spray
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val spec2 = "org.specs2" %% "specs2" % Version.spec2 
}

object Dependencies {

  import Library._

  val lib = List(    
    akkaKernel,
    akkaActor,
    akkaSlf4j,
    logbackClassic,
    sprayCan,
    sprayRouting,
    sprayClient,
    sprayJson,
    akkaTestkit % "test",
    scalaTest % "test",
    sprayTest % "test",
    spec2 % "test")
    
}
