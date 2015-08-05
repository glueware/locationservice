import sbt._

object Version {
  val scala = "2.11.7"
  val akka = "2.3.11"
  val logback = "1.0.13"
  val spray = "1.3.3"
  val sprayJson = "1.3.2"
  val scalaTest = "2.2.4"
}

object Library {
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback
  val sprayCan = "io.spray" %% "spray-can" % Version.spray
  val sprayRouting = "io.spray" %% "spray-routing" % Version.spray
  val sprayJson = "io.spray" %% "spray-json" % Version.sprayJson
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
}

object Dependencies {

  import Library._

  val lib = List(
    akkaActor,
    akkaSlf4j,
    logbackClassic,
    sprayCan,
    sprayRouting,
    sprayJson,
    akkaTestkit % "test",
    scalaTest % "test")
}
