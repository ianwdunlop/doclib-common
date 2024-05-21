lazy val scala_2_13 = "2.13.14"

val pekkoVersion = "1.0.2"
val kleinUtilVersion = "1.2.6"
val kleinMongoVersion = "2.0.8"
val kleinQueueVersion = "3.0.1"

val configVersion = "1.4.3"
val catsVersion = "2.10.0"
val playVersion = "2.10.5"
val tikaVersion = "2.9.2"
val betterFilesVersion = "3.9.2"
val prometheusClientVersion = "0.9.0"
val scalacticVersion = "3.2.18"
val scalaTestVersion = "3.2.18"
val scalaMockVersion = "5.2.0"
val scalaCheckVersion = "1.18.0"
val scoptVersion = "4.1.0"
val lemonLabsURIVersion = "4.0.3"

lazy val creds = {
  sys.env.get("CI_JOB_TOKEN") match {
    case Some(token) =>
      Credentials("GitLab Packages Registry", "gitlab.com", "gitlab-ci-token", token)
    case _ =>
      Credentials(Path.userHome / ".sbt" / ".credentials")
  }
}

// Registry ID is the project ID of the project where the package is published, this should be set in the CI/CD environment
val registryId = sys.env.get("REGISTRY_HOST_PROJECT_ID").getOrElse("")

lazy val publishSettings = Seq(
  publishTo := {
    Some("gitlab" at s"https://gitlab.com/api/v4/projects/$registryId/packages/maven")
  },
  credentials += creds
)

lazy val root = (project in file("."))
  .settings(
    name := "common",
    organization := "io.mdcatapult.doclib",
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
    resolvers += ("gitlab" at s"https://gitlab.com/api/v4/projects/$registryId/packages/maven"),
    credentials += creds,
    libraryDependencies ++= {
      Seq(
        "io.mdcatapult.klein" %% "queue"                % kleinQueueVersion,
        "io.mdcatapult.klein" %% "mongo"                % kleinMongoVersion,
        "io.mdcatapult.klein" %% "util"                 % kleinUtilVersion,

        "org.scalactic" %% "scalactic"                  % scalacticVersion,
        "org.scalatest" %% "scalatest"                  % scalaTestVersion % "test",
        "org.scalamock" %% "scalamock"                  % scalaMockVersion % "test",
        "org.scalacheck" %% "scalacheck"                % scalaCheckVersion % "test",
        "org.apache.pekko" %% "pekko-testkit"           % pekkoVersion % "test",
        "org.apache.pekko" %% "pekko-protobuf-v3"       % pekkoVersion,
        "org.apache.pekko" %% "pekko-stream"            % pekkoVersion,
        "com.typesafe.play" %% "play-json"              % playVersion,
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