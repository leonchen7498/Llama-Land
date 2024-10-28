import sbt.Keys.libraryDependencies

ThisBuild / organization := "nl.sogyo"
ThisBuild / scalaVersion := "3.3.3"
Compile / mainClass := Some("play.core.server.ProdServerStart")

lazy val root = (project in file(".")).aggregate(api, domain, persistence).dependsOn(api, domain, persistence)

lazy val domain = (project in file("domain"))
  .settings(
    name := "domain",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.1" % Test,
    coverageEnabled := true,
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 80,
    coverageMinimumBranchTotal := 80
  )

lazy val persistence = (project in file("persistence"))
  .settings(
    name := "persistence",
    libraryDependencies += ("org.mongodb.scala" %% "mongo-scala-driver" % "5.1.4").cross(CrossVersion.for3Use2_13)
  ).dependsOn(domain);

lazy val api = (project in file("api"))
  .enablePlugins(PlayScala)
  .settings(
    name := "api",
    version := "1.0-SNAPSHOT",
    libraryDependencies += guice,
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
  ).dependsOn(domain, persistence)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "nl.sogyo.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "nl.sogyo.binders._"
