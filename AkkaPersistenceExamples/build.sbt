name := """akka-scala-seed"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  // scalatest
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.2",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.3",
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.3",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.2.12"
)
