organization      := "com.bytes32"

version           := "0.1"

scalaVersion      := "2.11.4"

scalacOptions     := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

parallelExecution in ThisBuild := false

resolvers ++= Seq(
  "typesafe-snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= {
  val akkaV = "2.3.5"
  Seq(
    "com.typesafe.play"   %%  "play-iteratees"    % "2.3.5" withSources(),
    "com.typesafe.akka"   %%  "akka-actor"        % akkaV withSources() withJavadoc(),
    "org.scala-lang"      %%  "scala-pickling"    % "0.9.2-SNAPSHOT" withSources(),
    "com.typesafe.akka"   %%  "akka-testkit"      % akkaV   % "test",
    "org.scalatest"       %%  "scalatest"         % "2.2.1" % "test"
  )
}

Revolver.settings
