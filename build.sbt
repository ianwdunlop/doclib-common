import com.gilcloud.sbt.gitlab.{GitlabCredentials,GitlabPlugin}

GitlabPlugin.autoImport.gitlabGroupId     :=  Some(73679838)
GitlabPlugin.autoImport.gitlabProjectId   :=  Some(50550924)
GitlabPlugin.autoImport.gitlabCredentials  := {
  sys.env.get("GITLAB_PRIVATE_TOKEN") match {
    case Some(token) =>
      Some(GitlabCredentials("Private-Token", token))
    case None =>
      Some(GitlabCredentials("Job-Token", sys.env.get("CI_JOB_TOKEN").get))
  }
}

lazy val scala_2_13 = "2.13.3"

lazy val IntegrationTest = config("it") extend Test

lazy val root = (project in file(".")).
  configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
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
    resolvers += ("gitlab" at "https://gitlab.com/api/v4/projects/50550924/packages/maven"),
    credentials += {
      sys.env.get("CI_JOB_TOKEN") match {
        case Some(p) =>
          Credentials("GitLab Packages Registry", "gitlab.com", "gitlab-ci-token", p)
        case None =>
          Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= {
      val kleinUtilVersion = "1.2.6"
      val kleinMongoVersion = "2.0.7"
      val kleinQueueVersion = "2.0.0"

      val configVersion = "1.4.2"
      val catsVersion = "2.9.0"
      val playVersion = "2.9.4"
      val tikaVersion = "1.28.5"
      val betterFilesVersion = "3.9.2"
      val akkaVersion = "2.8.1"
      val prometheusClientVersion = "0.9.0"
      val scalacticVersion = "3.2.15"
      val scalaTestVersion = "3.2.17"
      val scalaMockVersion = "5.2.0"
      val scalaCheckVersion = "1.17.0"
      val scoptVersion = "4.1.0"
      val lemonLabsURIVersion = "4.0.3"

      Seq(
        "io.mdcatapult.klein" %% "queue"                % kleinQueueVersion,
        "io.mdcatapult.klein" %% "mongo"                % kleinMongoVersion,
        "io.mdcatapult.klein" %% "util"                 % kleinUtilVersion,

        "org.scalactic" %% "scalactic"                  % scalacticVersion,
        "org.scalatest" %% "scalatest"                  % scalaTestVersion % "it, test",
        "org.scalamock" %% "scalamock"                  % scalaMockVersion % "it, test",
        "org.scalacheck" %% "scalacheck"                % scalaCheckVersion % Test,
        "com.typesafe.akka" %% "akka-slf4j"             % akkaVersion,
        "com.typesafe.akka" %% "akka-testkit"           % akkaVersion % "it, test",
        "com.typesafe.akka" %% "akka-protobuf-v3"       % akkaVersion,
        "com.typesafe.akka" %% "akka-stream"            % akkaVersion,
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