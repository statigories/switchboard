javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

val commonDeps = Seq(
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.10.4",
  "org.mockito" % "mockito-core" % "2.0.28-beta" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scribe" % "scribe" % "1.3.6"
)

lazy val commonSettings = Seq(
  organization := "com.statigories",
  version := "0.0.0",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "switchboard",
    libraryDependencies ++= commonDeps
  )
