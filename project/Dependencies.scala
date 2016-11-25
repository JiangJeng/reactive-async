import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  lazy val opal = "de.opal-project" % "abstract-interpretation-framework_2.11" % "0.9.0-SNAPSHOT"
  lazy val opalFixpoint = "de.opal-project" % "fixpoint-computations-framework-analyses_2.11" % "0.9.0-SNAPSHOT"
  lazy val scalaMeter = "com.storm-enroute" %% "scalameter" % "0.7"
  lazy val sparkcore = "org.apache.spark" %% "spark-core" % "2.0.2"
  lazy val sparksql = "org.apache.spark" %% "spark-sql" % "2.0.2"
  lazy val sparkgraphx = "org.apache.spark" %% "spark-graphx" % "2.0.2"
  lazy val log4j= "org.apache.logging.log4j" % "log4j-core" % "2.7"

}
