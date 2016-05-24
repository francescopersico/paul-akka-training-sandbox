name := """Go Reactive Common"""

organization := "goreactivewithakka"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test,
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  // Scala Async
  "org.scala-lang.modules" % "scala-async_2.11" % "0.9.6-RC2",
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.2",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.3",
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.3",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.2.12",
  // Gson
  "com.google.code.gson" % "gson" % "2.6.2"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
