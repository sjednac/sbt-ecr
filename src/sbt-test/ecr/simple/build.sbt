import com.amazonaws.regions.{Region, Regions}

scalaVersion  := "2.11.8"

enablePlugins(EcrPlugin)

region         in Ecr := Region.getRegion(Regions.US_EAST_1)
repositoryName in Ecr := "scripted-test"
