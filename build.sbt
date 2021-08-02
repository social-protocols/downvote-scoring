ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"

Global / onChangedBuildSource := IgnoreSourceChanges

lazy val commonSettings = Seq(
  addCompilerPlugin(
    "org.typelevel" % "kind-projector" % "0.13.0" cross CrossVersion.full
  ),
  resolvers ++=
    ("jitpack" at "https://jitpack.io") ::
      Nil,
  scalacOptions --= Seq("-Xfatal-warnings")
)

lazy val webSettings = Seq(
  useYarn := true,
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  webpack / version := "4.46.0",
  Compile / npmDevDependencies += "fun-pack" -> "git://github.com/fun-stack-org/fun-pack#c51221a",
  Compile / npmDevDependencies ++= Seq(
    "autoprefixer" -> "10.2.5",
    "postcss" -> "8.2.9",
    "postcss-loader" -> "4.2.0",
    "postcss-import" -> "14.0.1",
    "postcss-nesting" -> "7.0.1",
    "postcss-extend-rule" -> "3.0.0",
    "tailwindcss" -> "2.1.1"
  ),
  scalaJSUseMainModuleInitializer := true,
  Test / requireJsDomEnv := true,
  startWebpackDevServer / version := "3.11.2",
  webpackDevServerExtraArgs := Seq("--color"),
  webpackDevServerPort := 12345,
  fastOptJS / webpackConfigFile := Some(
    baseDirectory.value / "webpack.config.dev.js"
  ),
  fullOptJS / webpackConfigFile := Some(
    baseDirectory.value / "webpack.config.prod.js"
  ),
  fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
  libraryDependencies += "org.portable-scala" %%% "portable-scala-reflect" % "1.1.1"
)

val outwatchVersion = "d9b5d516"
lazy val root = (project in file("."))
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin
  )
  .settings(commonSettings, webSettings)
  .settings(
    name := "simulation-v2",
    resolvers += ("jitpack" at "https://jitpack.io"),
    libraryDependencies ++= Seq(
      "com.github.cornerman.outwatch" %%% "outwatch" % outwatchVersion,
      "com.github.fdietze.probability-monad" %%% "probability-monad" % "837a419257883"
    )
  )

addCommandAlias("dev", "devInit; devWatchAll; devDestroy") // watch all
addCommandAlias("devInit", "fastOptJS::startWebpackDevServer")
addCommandAlias("devWatchAll", "~; fastOptJS::webpack")
addCommandAlias("devDestroy", "fastOptJS::stopWebpackDevServer")
