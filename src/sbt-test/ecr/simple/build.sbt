import com.amazonaws.regions.{Region, Regions}

scalaVersion  := "2.11.8"

accountId in ecr := "123456789000"

region in ecr := Region.getRegion(Regions.US_EAST_1)

repositoryName in ecr := "scripted-test"
