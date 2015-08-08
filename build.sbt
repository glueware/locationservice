name := """locationservice"""

version := "1.0"

scalaVersion := Version.scala

compileOrder := CompileOrder.Mixed

libraryDependencies ++= Dependencies.lib

scalacOptions in Test ++= Seq("-Yrangepos", "-language:postfixOps", "-language:implicitConversions", "-language:reflectiveCalls", "-feature")

Revolver.settings

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

fork in run := true