name := "Lifeway Homework"
version := "1.0"
scalaVersion := "2.13.8"
val AkkaVersion = "2.6.9"
val AkkaHttpVersion = "10.2.0"
val ScalaTestVersion = "3.2.16"

scalacOptions ++= Seq(
  "-Ywarn-unused:imports"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
)

assembly / assemblyJarName := "star-wars-proxy-api.jar"

run / fork := true