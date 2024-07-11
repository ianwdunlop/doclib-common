lazy val scala_2_13 = "2.13.14"

ThisBuild / versionScheme := Some("early-semver")

val pekkoVersion = "1.0.3"
val doclibUtilVersion = "2.0.0"
val doclibVersion = "3.0.1"
val doclibQueueVersion = "4.0.0"

val configVersion = "1.4.3"
val catsVersion = "2.12.0"
val playVersion = "3.0.4"
val tikaVersion = "2.9.2"
val betterFilesVersion = "3.9.2"
val prometheusClientVersion = "0.16.0"
val scalacticVersion = "3.2.19"
val scalaTestVersion = "3.2.19"
val scalaMockVersion = "6.0.0"
val scalaCheckVersion = "1.18.0"

val scoptVersion = "4.1.0"
val lemonLabsURIVersion = "4.0.3"

lazy val packageRepoOwner = sys.env.getOrElse("GITHUB_USERNAME", "")

lazy val root = (project in file("."))
  .settings(
    name := "common",
    organization := "io.doclib",
    scalaVersion := scala_2_13,
    useCoursier := false,
    crossScalaVersions := scala_2_13 :: Nil,
    scalacOptions ++= Seq(
      "-encoding", "utf-8",
      "-unchecked",
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-Xlint",
      "-Xfatal-warnings",
    ),
    githubOwner := packageRepoOwner,
    githubRepository := sys.env.getOrElse("GITHUB_PACKAGE_REPO", "scala-packages"),
    resolvers += Resolver.githubPackages(packageRepoOwner),
    releaseIgnoreUntrackedFiles := true,
    libraryDependencies ++= {
      Seq(
        "io.doclib" %% "queue"                          % doclibQueueVersion,
        "io.doclib" %% "mongo"                          % doclibVersion,
        "io.doclib" %% "common-util"                    % doclibUtilVersion,

        "org.scalactic" %% "scalactic"                  % scalacticVersion,
        "org.scalatest" %% "scalatest"                  % scalaTestVersion % "test",
        "org.scalamock" %% "scalamock"                  % scalaMockVersion % "test",
        "org.scalacheck" %% "scalacheck"                % scalaCheckVersion % "test",
        "org.apache.pekko" %% "pekko-testkit"           % pekkoVersion % "test",
        "org.apache.pekko" %% "pekko-protobuf-v3"       % pekkoVersion,
        "org.apache.pekko" %% "pekko-stream"            % pekkoVersion,
        "org.playframework" %% "play-json"              % playVersion,
        "com.typesafe" % "config"                       % configVersion,
        "org.typelevel" %% "cats-kernel"                % catsVersion,
        "org.typelevel" %% "cats-core"                  % catsVersion,
        "io.lemonlabs" %% "scala-uri"                   % lemonLabsURIVersion,
        "com.github.scopt" %% "scopt"                   % scoptVersion,
        "org.apache.tika" % "tika-core"                 % tikaVersion,
        "org.apache.tika" % "tika-parsers"              % tikaVersion,
        "org.apache.tika" % "tika-langdetect"           % tikaVersion,
        "com.github.pathikrit"  %% "better-files"       % betterFilesVersion,
        "io.prometheus" % "simpleclient"                % prometheusClientVersion,
        "io.prometheus" % "simpleclient_hotspot"        % prometheusClientVersion,
        "io.prometheus" % "simpleclient_httpserver"     % prometheusClientVersion
      )
    }
  )

Global / excludeLintKeys += Test / sourceDirectories

lazy val it = project
  .in(file("it"))  //it test located in a directory named "it"
  .settings(
    name := "common-it",
    scalaVersion := "2.13.14",
    Test / sourceDirectories ++= (root / Test / sourceDirectories).value,
    libraryDependencies ++= {
      Seq(
        "org.scalatest" %% "scalatest"         % scalaTestVersion,
        "org.scalamock" %% "scalamock"         % scalaMockVersion,
        "org.apache.pekko" %% "pekko-testkit"  % pekkoVersion,
        "org.apache.pekko" %% "pekko-actor"    % pekkoVersion,
        "org.scalatest" %% "scalatest"         % scalaTestVersion,
        "org.apache.pekko" %% "pekko-slf4j"    % pekkoVersion
      )
    }
  )
  .dependsOn(root % "test->test;compile->compile")