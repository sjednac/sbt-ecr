import com.amazonaws.regions.Regions

name := "sbt-ecr-simple"

scalaVersion  := "2.12.6"

enablePlugins(JavaAppPackaging, EcrPlugin)

Ecr / region := Regions.US_EAST_1
