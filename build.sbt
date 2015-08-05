name := """locationservice"""

version := "1.0"

scalaVersion := Version.scala

compileOrder := CompileOrder.Mixed

libraryDependencies ++= Dependencies.lib

Revolver.settings

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

fork in run := true