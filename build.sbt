lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  // New-style macro annotations are under active development.  As a result, in
  // this build we'll be referring to snapshot versions of both scala.meta and
  // macro paradise.
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.bintrayIvyRepo("scalameta", "maven"),
  // A dependency on macro paradise 3.x is required to both write and expand
  // new-style macros.  This is similar to how it works for old-style macro
  // annotations and a dependency on macro paradise 2.x.
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M7" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  // temporary workaround for https://github.com/scalameta/paradise/issues/10
  scalacOptions in (Compile, console) := Seq(), // macroparadise plugin doesn't work in repl yet.
  // temporary workaround for https://github.com/scalameta/paradise/issues/55
  sources in (Compile, doc) := Nil // macroparadise doesn't work with scaladoc yet.
)

lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
  organization := "com.zhranklin",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq(scalaVersion.value, "2.11.8"),
  // Sonatype OSS deployment
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  licenses := ("Apache2", url("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil,
  homepage := Some(url("http://softwaremill.com")),
  scmInfo := Some(ScmInfo(url("https://github.com/zhranklin/easy-ddd"), "scm:git:git@github.com/zhranklin/easy-ddd.git", None)),
  developers := Developer("Zhranklin", "Zhranklin", "chigou79@outlook.com", url("http://www.zhranklin.com")) :: Nil
)

lazy val rootProject = (project in file("."))
  .settings(
    commonSettings,
    publishArtifact := false,
    name := "easy-ddd")
  .aggregate(macros, core, casbah, testcase)

lazy val macros = project.settings(
  commonSettings,
  metaMacroSettings,
  name := "easy-ddd-macros",
  libraryDependencies += "org.scalameta" %% "scalameta" % "1.6.0"
)

lazy val core = project.settings(
  commonSettings,
  name := "easy-ddd-core"
).dependsOn(macros)

lazy val casbah = project.settings(
  commonSettings,
  name := "easy-ddd-casbah",
  libraryDependencies += "org.mongodb" %% "casbah" % "3.1.1" % "provided",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
).dependsOn(core)

lazy val testcase = project
  .settings(
    commonSettings,
    metaMacroSettings,
    publishArtifact := false,
//    scalacOptions := Seq("-Xlog-implicits"),
    libraryDependencies += "org.mongodb" %% "casbah" % "3.1.1" % "test",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ).dependsOn(core, casbah)
