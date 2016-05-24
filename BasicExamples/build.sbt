name := "Basic Examples"
 
version := "1.0"
 
scalaVersion := "2.11.4"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
 
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.3"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.3"

libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

libraryDependencies += "junit" % "junit" % "4.12"
