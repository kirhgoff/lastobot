enablePlugins(JavaAppPackaging)

name := "lastobot"

organization := "org.kirhgoff"

//version := "1.0"

scalaVersion := "2.11.8"

crossScalaVersions := Seq(scalaVersion.value)

//resolvers += "Local Maven Repository" at "http://eif-repository.moex.com/nexus/content/repositories/releases"
resolvers += Resolver.sonatypeRepo("snapshots")

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

//Define dependencies. These ones are only required for Test and Integration Test scopes.
libraryDependencies ++= Seq(
  //Test
  "org.scalatest"   %% "scalatest"    % "2.2.4"   % "test",
  "org.scalacheck"  %% "scalacheck"   % "1.12.5"      % "test",

  // Utils
  "org.apache.commons" % "commons-lang3" % "3.4",

  //Akka
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.6",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.6",

  //Db
  "org.mongodb" %% "casbah" % "3.1.1",

  //Logging
  "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",

  // Charts
  "org.knowm.xchart" % "xchart" % "3.0.4",

//  "info.mukel" %% "telegrambot4s" % "1.0.3-SNAPSHOT",// excludeAll ExclusionRule(organization="org.json4s"),
//  "org.json4s" % "json4s-native_2.11" % "3.2.10"
  "info.mukel" %% "telegrambot4s" % "1.1.4-SNAPSHOT"
)

parallelExecution in Test := false

// Compiler settings. Use scalac -X for other options and their description.
// See Here for more info http://www.scala-lang.org/files/archive/nightly/docs/manual/html/scalac.html 
scalacOptions ++= List("-feature","-deprecation", "-unchecked", "-Xlint")

javaOptions += "-Xmx4G"

mainClass in Compile := Some("org.kirhgoff.lastobot.LastobotApp")

sbtPlugin := true

// Release
import ReleaseTransformations._

val buildReleaseTarball = (ref: ProjectRef) => ReleaseStep(
  action = releaseStepTaskAggregated(packageZipTarball in Universal in ref)
)

releaseProcess := Seq[ReleaseStep](
    inquireVersions,                        // : ReleaseStep
    setReleaseVersion,                      // : ReleaseStep
    commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
    tagRelease,                             // : ReleaseStep
    //buildReleaseTarball,
    ReleaseStep(releaseStepTask(packageZipTarball in Universal)),
    setNextVersion,                         // : ReleaseStep
    commitNextVersion,                      // : ReleaseStep
    pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
)
