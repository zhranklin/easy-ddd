import scala.languageFeature.experimental.macros

organization  := "com.zhranklin.easy-ddd"

version       := "0.1"

scalaVersion in ThisBuild := "2.12.1"


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

lazy val macros = project.settings(
  metaMacroSettings,
  libraryDependencies += "org.scalameta" %% "scalameta" % "1.6.0" withSources()
)

lazy val model = project.settings().dependsOn(macros)

lazy val infra = project.settings().dependsOn(model)

lazy val testcase = project
  .settings(metaMacroSettings,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    scalacOptions := Seq("-Xlog-implicits")
  ).dependsOn(infra)
