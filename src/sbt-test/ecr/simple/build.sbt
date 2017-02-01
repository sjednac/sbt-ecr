import com.amazonaws.regions.{Region, Regions}

scalaVersion  := "2.11.8"

region         in ecr := Region.getRegion(Regions.US_EAST_1)
repositoryName in ecr := "scripted-test"

enablePlugins(EcrPlugin)
