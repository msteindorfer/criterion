#!/usr/bin/env Rscript
setwd("~/tmp/jmh-dscg-benchmarks-results")

timestamp <- "20150109_1718"

# install.packages("vioplot")
# install.packages("beanplot")
# install.packages("ggplot2")
# install.packages("reshape2")
# install.packages("functional")
# install.packages("plyr")
# install.packages("extrafont")
# install.packages("scales")
library(vioplot)
library(beanplot)
library(ggplot2)
library(reshape2)
library(functional)
library(plyr) # needed to access . function
library(extrafont)
library(scales)
loadfonts()


capwords <- function(s, strict = FALSE) {
  cap <- function(s) paste(toupper(substring(s, 1, 1)),
{s <- substring(s, 2); if(strict) tolower(s) else s},
sep = "", collapse = " " )
sapply(strsplit(s, split = " "), cap, USE.NAMES = !is.null(names(s)))
}


calculateMemoryFootprintOverhead <- function(requestedDataType, dataStructureOrigin) {
  ###
  # Load 32-bit and 64-bit data and combine them.
  ##
  dss32_fileName <- paste(paste("/Users/Michael/Dropbox/Research/hamt-improved-results/map-sizes-and-statistics", "32bit", timestamp, sep="-"), "csv", sep=".")
  dss32_stats <- read.csv(dss32_fileName, sep=",", header=TRUE)
  dss32_stats <- within(dss32_stats, arch <- factor(32))
  #
  dss64_fileName <- paste(paste("/Users/Michael/Dropbox/Research/hamt-improved-results/map-sizes-and-statistics", "64bit", timestamp, sep="-"), "csv", sep=".")
  dss64_stats <- read.csv(dss64_fileName, sep=",", header=TRUE)
  dss64_stats <- within(dss64_stats, arch <- factor(64))
  #
  dss_stats <- rbind(dss32_stats, dss64_stats)
  
  
  classNameTheOther <- switch(dataStructureOrigin, 
                              Scala = paste("scala.collection.immutable.Hash", capwords(tolower(requestedDataType)), sep = ""),
                              Clojure = paste("clojure.lang.PersistentHash", capwords(tolower(requestedDataType)), sep = ""))  

  classNameOurs <-  paste("io.usethesource.capsule.Trie", capwords(tolower(requestedDataType)), "_5Bits", sep = "")
  
  ###
  # If there are more measurements for one size, calculate the median.
  # Currently we only have one measurment.
  ##
  dss_stats_meltByElementCount <- melt(dss_stats, id.vars=c('elementCount', 'className', 'dataType', 'arch'), measure.vars=c('footprintInBytes')) # measure.vars=c('footprintInBytes')
  dss_stats_castByMedian <- dcast(dss_stats_meltByElementCount, elementCount + className + dataType + arch ~ "footprintInBytes_median", median, fill=0)
  
  mapClassName <- "io.usethesource.capsule.TrieMap_5Bits"
  setClassName <- "io.usethesource.capsule.TrieSet_5Bits"

#   mapClassName <- "io.usethesource.capsule.TrieMap_BleedingEdge"
#   setClassName <- "io.usethesource.capsule.TrieSet_BleedingEdge"
  
  ###
  # Calculate different baselines for comparison.
  ##
  dss_stats_castByBaselinePDBDynamic <- aggregate(footprintInBytes_median ~ elementCount + dataType + arch, dss_stats_castByMedian[dss_stats_castByMedian$className == mapClassName | dss_stats_castByMedian$className == setClassName,], min)
  names(dss_stats_castByBaselinePDBDynamic) <- c('elementCount', 'dataType', 'arch', 'footprintInBytes_baselinePDBDynamic')
  
  # dss_stats_castByBaselinePDB0To4 <- aggregate(footprintInBytes_median ~ elementCount + dataType + arch, dss_stats_castByMedian[dss_stats_castByMedian$className == "io.usethesource.capsule.TrieMap" | dss_stats_castByMedian$className == "io.usethesource.capsule.TrieSet",], min)
  # names(dss_stats_castByBaselinePDB0To4) <- c('elementCount', 'dataType', 'arch', 'footprintInBytes_baselinePDB0To4')
  # 
  # dss_stats_castByBaselinePDB0To8 <- aggregate(footprintInBytes_median ~ elementCount + dataType + arch, dss_stats_castByMedian[dss_stats_castByMedian$className == "io.usethesource.capsule.TrieMap0To8" | dss_stats_castByMedian$className == "io.usethesource.capsule.TrieSet0To8",], min)
  # names(dss_stats_castByBaselinePDB0To8) <- c('elementCount', 'dataType', 'arch', 'footprintInBytes_baselinePDB0To8')
  # 
  # dss_stats_castByBaselinePDB0To12 <- aggregate(footprintInBytes_median ~ elementCount + dataType + arch, dss_stats_castByMedian[dss_stats_castByMedian$className == "io.usethesource.capsule.TrieMap0To12" | dss_stats_castByMedian$className == "io.usethesource.capsule.TrieSet0To12",], min)
  # names(dss_stats_castByBaselinePDB0To12) <- c('elementCount', 'dataType', 'arch', 'footprintInBytes_baselinePDB0To12')
  
  ###
  # Merges baselines.
  ##
  dss_stats_with_min <- merge(dss_stats_castByMedian, dss_stats_castByBaselinePDBDynamic)
  # dss_stats_with_min <- merge(dss_stats_with_min, dss_stats_castByBaselinePDB0To4)
  # dss_stats_with_min <- merge(dss_stats_with_min, dss_stats_castByBaselinePDB0To8)
  # dss_stats_with_min <- merge(dss_stats_with_min, dss_stats_castByBaselinePDB0To12)
  
  # http://www.dummies.com/how-to/content/how-to-add-calculated-fields-to-data-in-r.navId-812016.html
  dss_stats_with_min <- within(dss_stats_with_min, memoryOverheadFactorComparedToPDBDynamic <- dss_stats_with_min$footprintInBytes_median / footprintInBytes_baselinePDBDynamic)
  dss_stats_with_min <- within(dss_stats_with_min, memorySavingComparedToPDBDynamic <- 1 - (dss_stats_with_min$footprintInBytes_baselinePDBDynamic / dss_stats_with_min$footprintInBytes_median))
  #
  # dss_stats_with_min <- within(dss_stats_with_min, memoryOverheadFactorComparedToPDB0To8 <- dss_stats_with_min$footprintInBytes_median / footprintInBytes_baselinePDB0To8)
  # dss_stats_with_min <- within(dss_stats_with_min, memorySavingComparedToPDB0To8 <- 1 - (dss_stats_with_min$footprintInBytes_baselinePDB0To8 / dss_stats_with_min$footprintInBytes_median))
  
  ###
  # How good score our specializations [map]?
  ##
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMapDynamic",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap0To8",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap0To12",]$memorySavingComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMapDynamic" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap0To8" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap0To12" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMapDynamic" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap0To8" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieMap0To12" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  
  
  ###
  # How good score our specializations [set]?
  ##
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSetDynamic",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet0To8",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet0To12",]$memorySavingComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSetDynamic" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet0To8" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet0To12" & dss_stats_with_min$arch == "32",]$memorySavingComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSetDynamic" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet0To8" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "io.usethesource.capsule.TrieSet0To12" & dss_stats_with_min$arch == "64",]$memorySavingComparedToPDBDynamic)
  
  
  ###
  # Compare generic data structure to competition.
  ##
  # median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashMap",]$memorySavingComparedToPDBDynamic)
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashMap",]$memorySavingComparedToPDBDynamic)
  #
  # median(dss_stats_with_min[dss_stats_with_min$className == "com.gs.collections.impl.map.mutable.UnifiedMap",]$memorySavingComparedToPDBDynamic)
  # median(dss_stats_with_min[dss_stats_with_min$className == "java.util.HashMap",]$memorySavingComparedToPDBDynamic)
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.mutable.HashMap",]$memorySavingComparedToPDBDynamic)
  # median(dss_stats_with_min[dss_stats_with_min$className == "com.google.common.collect.ImmutableMap",]$memorySavingComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashMap",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashMap" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashMap" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashMap",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashMap" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashMap" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashSet",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashSet" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashSet" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDBDynamic)
  
  median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashSet",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashSet" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDBDynamic)
  median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashSet" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDBDynamic)
  
  
  # ###
  # # Compare specialization to competition.
  # ##
  # median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashMap",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashMap" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashMap" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDB0To8)
  # 
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashMap",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashMap" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashMap" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDB0To8)
  # 
  # median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashSet",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashSet" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "clojure.lang.PersistentHashSet" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDB0To8)
  # 
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashSet",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashSet" & dss_stats_with_min$arch == "32",]$memoryOverheadFactorComparedToPDB0To8)
  # median(dss_stats_with_min[dss_stats_with_min$className == "scala.collection.immutable.HashSet" & dss_stats_with_min$arch == "64",]$memoryOverheadFactorComparedToPDB0To8)
  
#   sel.tmp <- dss_stats_with_min[dss_stats_with_min$className != mapClassName & dss_stats_with_min$className != setClassName,]
#   dss.tmp <- melt(sel.tmp, id.vars=c('elementCount', 'arch', 'dataType', 'className'), measure.vars = c('memoryOverheadFactorComparedToPDBDynamic'))
#   
#   # dss.tmp.cast_Map <- dcast(dss.tmp[dss.tmp$dataType == "MAP",], elementCount ~ className + dataType + arch + variable)
#   # dss.tmp.cast_Set <- dcast(dss.tmp[dss.tmp$dataType == "SET",], elementCount ~ className + dataType + arch + variable)

#   sel.tmp <- dss_stats_with_min[dss_stats_with_min$className == classNameTheOther,]
#   dss.tmp <- melt(sel.tmp, id.vars=c('elementCount', 'arch', 'dataType', 'className'), measure.vars = c('memoryOverheadFactorComparedToPDBDynamic'))
#   
#   res <- dcast(dss.tmp[dss.tmp$dataType == requestedDataType,], elementCount ~ className + arch + dataType + variable)
#   
#   # sort: first 32 then 64 bit, inside first Scala, then Clojure
#   # res[,c(1,4,2,5,3)]
#   
#   res

  theOther <- dss_stats_castByMedian[dss_stats_castByMedian$className == classNameTheOther & dss_stats_castByMedian$dataType == requestedDataType,]
  ours <- dss_stats_castByMedian[dss_stats_castByMedian$className == classNameOurs & dss_stats_castByMedian$dataType == requestedDataType,]

  ###
  # BE AWARE: hard-coded switching from 'memory savings in %' to 'speedup factor'.
  ##
  # memorySavingComparedToTheOther <- 1 - (ours$footprintInBytes_median / theOther$footprintInBytes_median)
  memorySavingComparedToTheOther <- (theOther$footprintInBytes_median / ours$footprintInBytes_median)

  sel.tmp = data.frame(ours$elementCount, ours$arch, memorySavingComparedToTheOther)
  colnames(sel.tmp) <- c('elementCount', 'arch', 'memorySavingComparedToTheOther')
  dss.tmp <- melt(sel.tmp, id.vars=c('elementCount', 'arch'), measure.vars = c('memorySavingComparedToTheOther'))

  res <- dcast(dss.tmp, elementCount ~ arch + variable)
  # print(res)
  res
}

# http://stackoverflow.com/questions/11340444/is-there-an-r-function-to-format-number-using-unit-prefix
formatCsUnits__ <- function (number,rounding=T) 
{
  lut <- c(1e-24, 1e-21, 1e-18, 1e-15, 1e-12, 1e-09, 1e-06, 
           0.001, 1, 1000, 1e+06, 1e+09, 1e+12, 1e+15, 1e+18, 1e+21, 
           1e+24)
  pre <- c("y", "z", "a", "f", "p", "n", "u", "m", "", "K", 
           "M", "G", "T", "P", "E", "Z", "Y")
  ix <- findInterval(number, lut)
  if (lut[ix]!=1) {
    if (rounding==T) {
      sistring <- paste(formatC(number/lut[ix], digits=0, format="f"),pre[ix], sep="")
    }
    else {
      sistring <- paste(formatC(number/lut[ix], digits=0, format="f"), pre[ix], sep="")
    } 
  }
  else {
    sistring <- paste(round(number, digits=0))
  }
  return(sistring)
}

formatCsUnits <- Vectorize(formatCsUnits__)

formatFactor__ <- function(arg,rounding=F) {
  if (is.nan(arg)) {
    x <- "0"
  } else {
    digits = 2
    
    if (rounding==T) {
      x <- format(round(arg, digits), nsmall=digits, digits=digits, scientific=FALSE)            
    } else {
      x <- format(arg, nsmall=digits, digits=digits, scientific=FALSE)      
    }      
  }
  
  # paste(x, "\\%", sep = "")
  x
}

formatFactor <- Vectorize(formatFactor__)

formatPercent__ <- function(arg,rounding=F) {
  if (is.nan(arg)) {
    x <- "0"
  } else {
    argTimes100 <- as.numeric(arg) * 100
    digits = 0
    
    if (rounding==T) {
      x <- format(round(argTimes100, digits), nsmall=digits, digits=digits, scientific=FALSE)            
    } else {
      x <- format(argTimes100, nsmall=digits, digits=digits, scientific=FALSE)      
    }      
  }
  
  # paste(x, "\\%", sep = "")
  x
}

formatPercent <- Vectorize(formatPercent__)

formatNsmall2__ <- function(arg,rounding=T) {
  if (is.nan(arg)) {
    x <- "0"
  } else {
    if (rounding==T) {
      x <- format(round(as.numeric(arg), 2), nsmall=2, digits=2, scientific=FALSE)            
    } else {
      x <- format(round(as.numeric(arg), 2), nsmall=2, digits=2, scientific=FALSE)
    }      
  }
}

formatNsmall2 <- Vectorize(formatNsmall2__)


latexMath__ <- function(arg) {
  paste("$", arg, "$", sep = "")
}

latexMath <- Vectorize(latexMath__)


# latexMathFactor__ <- function(arg) {
#   if (as.numeric(arg) < 1) {
#     paste("${\\color{red}", arg, "\\times}$", sep = "")
#   } else {
#     paste("$", arg, "\\times$", sep = "")
#   }
# }

latexMathFactor__ <- function(arg) {  
  arg_fmt <- formatFactor(arg, rounding=T)
  
  if (as.numeric(arg) < 1) {
    paste("${\\color{red}", arg_fmt, "}$", sep = "")
  } else {
    paste("$", arg_fmt, "$", sep = "")
  }
}

latexMathFactor <- Vectorize(latexMathFactor__)


latexMathPercent__ <- function(arg) {
  arg_fmt <- formatPercent(arg)
  
  postfix <- "\\%"
  
  if (is.na(arg) | is.nan(arg)) { #  | !is.numeric(arg)
    paste("$", "--", "$", sep = "")
  } else {
    if (as.numeric(arg) < 0) {
      paste("${\\color{red}", arg_fmt, postfix, "}$", sep = "")
    } else {
      paste("$", arg_fmt, postfix, "$", sep = "")
    }
  }
}

latexMathPercent <- Vectorize(latexMathPercent__)


getBenchmarkMethodName__ <- function(arg) {
  strsplit(as.character(arg), split = "[.]time")[[1]][2]
}

getBenchmarkMethodName <- Vectorize(getBenchmarkMethodName__)


benchmarksFileName <- paste(paste("/Users/Michael/Dropbox/Research/hamt-improved-results/results.all", timestamp, sep="-"), "log", sep=".")
benchmarks <- read.csv(benchmarksFileName, sep=",", header=TRUE, stringsAsFactors=FALSE)
colnames(benchmarks) <- c("Benchmark", "Mode", "Threads", "Samples", "Score", "ScoreError", "Unit", "Param_dataType", "Param_run", "Param_sampleDataSelection", "Param_size", "Param_valueFactoryFactory")

benchmarks$Benchmark <- getBenchmarkMethodName(benchmarks$Benchmark)

benchmarksCleaned <- benchmarks[benchmarks$Param_sampleDataSelection == "MATCH" & !grepl("@", benchmarks$Benchmark),c(-2,-3,-4,-7,-10)]
# benchmarksCleaned[benchmarksCleaned$Param_valueFactoryFactory == "VF_PDB_PERSISTENT_BLEEDING_EDGE", ]$Param_valueFactoryFactory <- "VF_PDB_PERSISTENT_CURRENT"

###
# If there are more measurements for one size, calculate the median.
# Currently we only have one measurment.
##
benchmarksCleaned = ddply(benchmarksCleaned, c("Benchmark", "Param_dataType", "Param_size", "Param_valueFactoryFactory"), function(x) c(Score = median(x$Score), ScoreError = median(x$ScoreError)))

#benchmarksByName <- melt(benchmarksCleaned, id.vars=c('Benchmark', 'Param_size', 'Param_dataType', 'Param_valueFactoryFactory')) # 'Param_valueFactoryFactory'

#ggplot(data=benchmarksByName, aes(x=variable, y=value, fill=as.factor(Param_valueFactoryFactory))) + geom_histogram(position="dodge", stat="identity")  + xlab("node branching factor") + ylab("value") + scale_x_discrete(labels=as.character(seq(1, 64)))

ggplot(benchmarks[benchmarks$Param_size == 1000000,], aes(x=Param_valueFactoryFactory, y=Score, group=Benchmark, fill=Param_valueFactoryFactory)) + geom_bar(position="dodge", stat="identity") + facet_grid(Benchmark ~ Param_size, scales = "free")

#benchmarksCast <- dcast(benchmarksByName, Benchmark + Param_size ~ Param_valueFactoryFactory + Param_dataType + variable)

### 
# Cache Statistics
##
benchmarksPerfStatFileName <- paste(paste("/Users/Michael/Dropbox/Research/hamt-improved-results/results.all", timestamp, sep="-"), "perf-stat", "log.xz", sep=".")
benchmarksPerfStat.all <- read.csv(benchmarksPerfStatFileName, sep=",", header=TRUE, stringsAsFactors=FALSE)
colnames(benchmarksPerfStat.all) <- c("Benchmark", "Param_size", "Param_valueFactoryFactory", "Param_dataType", "Param_run", "Param_sampleDataSelection", "L1_REF", "L1_MISSES", "L2_REF", "L2_HIT", "L3_REF", "L3_MISSES", "L3_MISSES_ALT")
#
benchmarksPerfStat <- subset(benchmarksPerfStat.all, select=-c(Param_sampleDataSelection))

benchmarksPerfStat$L2_MISSES <- benchmarksPerfStat$L2_REF - benchmarksPerfStat$L2_HIT

benchmarksPerfStat$L1_HIT <- benchmarksPerfStat$L1_REF - benchmarksPerfStat$L1_MISSES
benchmarksPerfStat$L3_HIT <- benchmarksPerfStat$L3_REF - benchmarksPerfStat$L3_MISSES

benchmarksPerfStat$L1_HIT_RATE <- benchmarksPerfStat$L1_HIT / benchmarksPerfStat$L1_REF
benchmarksPerfStat$L2_HIT_RATE <- benchmarksPerfStat$L2_HIT / benchmarksPerfStat$L2_REF
benchmarksPerfStat$L3_HIT_RATE <- benchmarksPerfStat$L3_HIT / benchmarksPerfStat$L3_REF

benchmarksPerfStat$L1_MISS_RATE <- 1 - benchmarksPerfStat$L1_HIT_RATE
benchmarksPerfStat$L2_MISS_RATE <- 1 - benchmarksPerfStat$L2_HIT_RATE
benchmarksPerfStat$L3_MISS_RATE <- 1 - benchmarksPerfStat$L3_HIT_RATE

benchmarksPerfStat.xxx = ddply(benchmarksPerfStat, c("Benchmark", "Param_dataType", "Param_size", "Param_valueFactoryFactory"), function(x) c(L1_REF = median(x$L1_REF), L1_HIT = median(x$L1_HIT), L1_MISSES = median(x$L1_MISSES), L1_HIT_RATE = median(x$L1_HIT_RATE), L1_MISS_RATE = median(x$L1_MISS_RATE), L2_REF = median(x$L2_REF), L2_HIT = median(x$L2_HIT), L2_MISSES = median(x$L2_MISSES), L2_HIT_RATE = median(x$L2_HIT_RATE), L2_MISS_RATE = median(x$L2_MISS_RATE), L3_REF = median(x$L3_REF), L3_HIT = median(x$L3_HIT), L3_MISSES = median(x$L3_MISSES), L3_HIT_RATE = median(x$L3_HIT_RATE), L3_MISS_RATE = median(x$L3_MISS_RATE), L3_MISSES_ALT = median(x$L3_MISSES_ALT)))
#ggplot(benchmarksPerfStat, aes(x=benchmarksPerfStat$benchmark, y=benchmarksPerfStat$L1.DCACHE.LOADS)) + geom_boxplot()
#ggplot(benchmarksPerfStat, aes(x=Param_valueFactoryFactory, y=Score, group=Benchmark, fill=Param_valueFactoryFactory)) + geom_bar(position="dodge", stat="identity") + facet_grid(Benchmark ~ Param_size, scales = "free")

benchmarksPerfStat.xxx.sub <- subset(benchmarksPerfStat.xxx, Param_dataType == "SET" & (Benchmark == "Iteration" | Benchmark == "EqualsRealDuplicate") & Param_size >= 1048576) #  & (Param_size == 2097152 | Param_size == 8388608)

ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_valueFactoryFactory, y=L3_REF, group=Benchmark, fill=Param_valueFactoryFactory)) + geom_bar(position="dodge", stat="identity") + facet_grid(Benchmark + Param_dataType ~ Param_size, scales = "free")
ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_valueFactoryFactory, y=L3_REF, group=Benchmark, fill=Param_valueFactoryFactory)) + geom_bar(position="dodge", stat="identity") + facet_grid(Benchmark + Param_dataType ~ ., scales = "free")

ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_size, y=L3_REF, colour=Param_valueFactoryFactory)) + geom_line() + facet_grid(Benchmark + Param_dataType ~ ., scales = "free") + scale_x_log10()
ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_size, y=L3_MISSES, colour=Param_valueFactoryFactory)) + geom_line() + facet_grid(Benchmark + Param_dataType ~ ., scales = "free") + scale_x_log10()
ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_size, y=L3_MISSES_ALT, colour=Param_valueFactoryFactory)) + geom_line() + facet_grid(Benchmark + Param_dataType ~ ., scales = "free") + scale_x_log10()

ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_valueFactoryFactory, y=L3_REF, group=Benchmark, fill=Param_valueFactoryFactory)) + geom_bar(position="dodge", stat="identity") + facet_grid(Benchmark + Param_dataType ~ Param_size, scales = "free") # + scale_y_log10()
ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_valueFactoryFactory, y=L3_MISSES, group=Benchmark, fill=Param_valueFactoryFactory)) + geom_bar(position="dodge", stat="identity") + facet_grid(Benchmark + Param_dataType ~ Param_size, scales = "free") # + scale_y_log10()
ggplot(benchmarksPerfStat.xxx.sub, aes(x=Param_valueFactoryFactory, y=L3_MISSES_ALT, group=Benchmark, fill=Param_valueFactoryFactory)) + geom_bar(position="dodge", stat="identity") + facet_grid(Benchmark + Param_dataType ~ Param_size, scales = "free") # + scale_y_log10()

#data.frame(benchmarksCleaned, benchmarksPerfStat)


#benchmarksByName <- melt(benchmarksCleaned[benchmarksCleaned$Param_dataType == "MAP",], id.vars=c('Benchmark', 'Param_size', 'Param_dataType', 'Param_valueFactoryFactory'))
benchmarksByName <- melt(data.frame(benchmarksCleaned, benchmarksPerfStat), id.vars=c('Benchmark', 'Param_size', 'Param_dataType', 'Param_valueFactoryFactory'))

# benchmarksTmpCast <- dcast(benchmarksByName, Benchmark + Param_size + Param_dataType ~ Param_valueFactoryFactory + variable)
# benchmarksTmpCast$VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score <- benchmarksTmpCast$VF_CLOJURE_Score / benchmarksTmpCast$VF_PDB_PERSISTENT_CURRENT_Score
# benchmarksTmpCast$VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score <- benchmarksTmpCast$VF_SCALA_Score / benchmarksTmpCast$VF_PDB_PERSISTENT_CURRENT_Score

# benchmarksByName$value <- formatPercent(benchmarksByName$value, rounding=F)
# benchmarksByName$value <- format(benchmarksByName$value, nsmall=2, digits=3, scientific=TRUE)

# benchmarksByName$Param_sizeLog2 <- paste("2^", log2(benchmarksByName$Param_size), sep = "")
# benchmarksByName$Param_sizeLog2 <- latexMath(paste("2^", log2(benchmarksByName$Param_size), sep = ""))

benchmarksByNameOutput <- data.frame(benchmarksByName)
# benchmarksByNameOutput$value <- formatPercent(benchmarksByName$value, rounding=F)
benchmarksByNameOutput$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksByName$Param_size), "}", sep = ""))
# benchmarksByNameOutput$Param_size <- latexMath(benchmarksByName$Param_size)
# benchmarksByNameOutput$value <- latexMath(benchmarksByName$value)



###
# OLD CODE
##

# # TODO: ensure that Param_dataType is always the same for each invocation
# benchmarksCast_Map <- dcast(benchmarksByNameOutput[benchmarksByNameOutput$Param_dataType == "MAP",], Benchmark + Param_size ~ Param_valueFactoryFactory + variable)
# benchmarksCast_Set <- dcast(benchmarksByNameOutput[benchmarksByNameOutput$Param_dataType == "SET",], Benchmark + Param_size ~ Param_valueFactoryFactory + variable)
# 
# benchmarksCast_Map$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksCast_Map$Param_size), "}", sep = ""))
# benchmarksCast_Map$VF_CLOJURE_Interval <- latexMath(paste(benchmarksCast_Map$VF_CLOJURE_Score, "\\pm", benchmarksCast_Map$VF_CLOJURE_ScoreError))
# benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Interval <- latexMath(paste(benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Score, "\\pm", benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_ScoreError))
# benchmarksCast_Map$VF_SCALA_Interval <- latexMath(paste(benchmarksCast_Map$VF_SCALA_Score, "\\pm", benchmarksCast_Map$VF_SCALA_ScoreError))
# ###
# benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Score)
# benchmarksCast_Map$VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast_Map$VF_SCALA_Score / benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Score)
# benchmarksCast_Map$VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast_Map$VF_CLOJURE_Score / benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Score)
# benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score <- (benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast_Map$VF_SCALA_Score)
# benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score <- (benchmarksCast_Map$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast_Map$VF_CLOJURE_Score)
# 
# benchmarksCast_Set$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksCast_Set$Param_size), "}", sep = ""))
# benchmarksCast_Set$VF_CLOJURE_Interval <- latexMath(paste(benchmarksCast_Set$VF_CLOJURE_Score, "\\pm", benchmarksCast_Set$VF_CLOJURE_ScoreError))
# benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Interval <- latexMath(paste(benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Score, "\\pm", benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_ScoreError))
# benchmarksCast_Set$VF_SCALA_Interval <- latexMath(paste(benchmarksCast_Set$VF_SCALA_Score, "\\pm", benchmarksCast_Set$VF_SCALA_ScoreError))
# ###
# benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Score)
# benchmarksCast_Set$VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast_Set$VF_SCALA_Score / benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Score)
# benchmarksCast_Set$VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast_Set$VF_CLOJURE_Score / benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Score)
# benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score <- (benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast_Set$VF_SCALA_Score)
# benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score <- (benchmarksCast_Set$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast_Set$VF_CLOJURE_Score)

#benchmarksCast <- data.frame(benchmarksCast_Map, benchmarksCast_Set)

# formatPercent(benchmarksCast$VF_CLOJURE_Score, rounding=F)
# 
# format(benchmarksCast$VF_CLOJURE_Score, nsmall=2, digits=3, scientific=TRUE)
# format(benchmarksCast$VF_CLOJURE_ScoreError, nsmall=2, digits=3, scientific=TRUE)

# write.table(benchmarksCast_Map[,c(1,9,13,14,15)], file = "results_latex_map.tex", sep = " & ", row.names = FALSE, col.names = TRUE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
# write.table(benchmarksCast_Set[,c(1,9,13,14,15)], file = "results_latex_set.tex", sep = " & ", row.names = FALSE, col.names = TRUE, append = FALSE, quote = FALSE, eol = " \\\\ \n")

# orderedBenchmarkNames <- c("ContainsKey", "Insert", "RemoveKey", "Iteration", "EntryIteration", "EqualsRealDuplicate", "EqualsDeltaDuplicate")
# orderedBenchmarkIDs <- seq(1:length(orderedBenchmarkNames))
# 
# orderingByName <- data.frame(orderedBenchmarkIDs, orderedBenchmarkNames)
# colnames(orderingByName) <- c("BenchmarkSortingID", "Benchmark")

# selectComparisionColumns <- Vectorize(function(castedData, benchmarkName) {
#   data.frame(castedData[castedData$Benchmark == benchmarkName,])[,c(13,14,15)]
# })

selectComparisionColumns <- function(inputData, measureVars, orderingByName) {
  tmp.m <- melt(data=join(inputData, orderingByName), id.vars=c('BenchmarkSortingID', 'Benchmark', 'Param_size'), measure.vars=measureVars)

  #tmp.m$value <- formatNsmall2(tmp.m$value, rounding=T)

  tmp.c <- dcast(tmp.m, Param_size ~ BenchmarkSortingID + Benchmark + variable)
  # tmp.c$Param_size <- latexMath(paste("2^{", log2(tmp.c$Param_size), "}", sep = ""))
  tmp.c
}

selectComparisionColumnsSummary <- function(inputData, measureVars, orderingByName) {
  tmp.m <- melt(data=join(inputData, orderingByName), id.vars=c('BenchmarkSortingID', 'Benchmark', 'Param_size'), measure.vars=measureVars)
  
  tmp.c <- dcast(tmp.m, Param_size ~ BenchmarkSortingID + Benchmark + variable)
  
  mins.c <- apply(tmp.c, c(2), min) # as.numeric(formatNsmall2(apply(tmp.c, c(2), min), rounding=T))
  maxs.c <- apply(tmp.c, c(2), max) # as.numeric(formatNsmall2(apply(tmp.c, c(2), max), rounding=T))
  #mean.c <- apply(tmp.c, c(2), mean)
  medians.c <- apply(tmp.c, c(2), median) # as.numeric(formatNsmall2(apply(tmp.c, c(2), median), rounding=T))

  res <- data.frame(rbind(mins.c, maxs.c, medians.c))[-1]
  rownames(res) <- c('minimum', 'maximum', 'median')
  res
}

calculateMemoryFootprintSummary <- function(inputData) {
  mins.c <- apply(inputData, c(2), min) # as.numeric(formatNsmall2(apply(inputData, c(2), min), rounding=T))
  maxs.c <- apply(inputData, c(2), max) # as.numeric(formatNsmall2(apply(inputData, c(2), max), rounding=T))
  #mean.c <- apply(inputData, c(2), mean)
  medians.c <- apply(inputData, c(2), median) # as.numeric(formatNsmall2(apply(inputData, c(2), median), rounding=T))
  
  res <- data.frame(rbind(mins.c, maxs.c, medians.c))[-1]
  rownames(res) <- c('minimum', 'maximum', 'median')
  res
}

# calculateMemoryFootprintSummary <- function(inputData) {
#   mins.c <- as.numeric(formatNsmall2(apply(inputData, c(2), min), rounding=T))
#   maxs.c <- as.numeric(formatNsmall2(apply(inputData, c(2), max), rounding=T))
#   medians.c <- as.numeric(formatNsmall2(apply(inputData, c(2), median), rounding=T))
#   
#   res <- data.frame(rbind(mins.c, maxs.c, medians.c))[-1]
#   rownames(res) <- c('minimum', 'maximum', 'median')
#   res
# }


###
# OLD CODE
##

# tableMapAll_summary <- selectComparisionColumnsSummary(benchmarksCast_Map, c('VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score', 'VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score'))
# tableSetAll_summary <- selectComparisionColumnsSummary(benchmarksCast_Set, c('VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score', 'VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score'))
# 
# tableMapAll <- selectComparisionColumns(benchmarksCast_Map, c('VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score', 'VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score'))
# tableSetAll <- selectComparisionColumns(benchmarksCast_Set, c('VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score', 'VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score'))
# 
# memFootprintMap <- calculateMemoryFootprintOverhead("MAP") 
# memFootprintMap_fmt <- data.frame(sapply(1:NCOL(memFootprintMap), function(col_idx) { memFootprintMap[,c(col_idx)] <- latexMathFactor(formatNsmall2(memFootprintMap[,c(col_idx)], rounding=T))}))
# colnames(memFootprintMap_fmt) <- colnames(memFootprintMap)
# #
# memFootprintSet <- calculateMemoryFootprintOverhead("SET") 
# memFootprintSet_fmt <- data.frame(sapply(1:NCOL(memFootprintSet), function(col_idx) { memFootprintSet[,c(col_idx)] <- latexMathFactor(formatNsmall2(memFootprintSet[,c(col_idx)], rounding=T))}))
# colnames(memFootprintSet_fmt) <- colnames(memFootprintSet)
# 
# tableMapAll <- data.frame(tableMapAll, memFootprintMap_fmt[,c(2,3,4,5)])
# tableSetAll <- data.frame(tableSetAll, memFootprintSet_fmt[,c(2,3,4,5)])
# 
# 
# 
# tableMapAll_summary <- data.frame(tableMapAll_summary, calculateMemoryFootprintSummary(memFootprintMap))
# tableSetAll_summary <- data.frame(tableSetAll_summary, calculateMemoryFootprintSummary(memFootprintSet))
# 
# tableMapAll_summary_fmt <- data.frame(sapply(1:NCOL(tableMapAll_summary), function(col_idx) { tableMapAll_summary[,c(col_idx)] <- latexMathFactor(tableMapAll_summary[,c(col_idx)]) }))
# rownames(tableMapAll_summary_fmt) <- rownames(tableMapAll_summary)
# tableSetAll_summary_fmt <- data.frame(sapply(1:NCOL(tableSetAll_summary), function(col_idx) { tableSetAll_summary[,c(col_idx)] <- latexMathFactor(tableSetAll_summary[,c(col_idx)]) }))
# rownames(tableSetAll_summary_fmt) <- rownames(tableSetAll_summary)
# 
# write.table(tableMapAll_summary_fmt, file = "all-benchmarks-map-summary.tex", sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
# write.table(tableSetAll_summary_fmt, file = "all-benchmarks-set-summary.tex", sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
# 
# # tableMapAll <- data.frame(sapply(1:NCOL(tableMapAll), function(col_idx) { tableMapAll[,c(col_idx)] <- paste("\\tableMapAll_c", col_idx, "{", tableMapAll[,c(col_idx)], "}", sep = "") })) # colnames(tableMapAll)[col_idx]
# # tableSetAll <- data.frame(sapply(1:NCOL(tableSetAll), function(col_idx) { tableSetAll[,c(col_idx)] <- paste("\\tableSetAll_c", col_idx, "{", tableSetAll[,c(col_idx)], "}", sep = "") })) # colnames(tableSetAll)[col_idx]
# 
# write.table(tableMapAll, file = "all-benchmarks-map.tex", sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
# write.table(tableSetAll, file = "all-benchmarks-set.tex", sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")

orderedBenchmarkNames <- function(dataType) {
  candidates <- c("ContainsKey", "Insert", "RemoveKey", "Iteration", "EntryIteration", "EqualsRealDuplicate", "EqualsDeltaDuplicate")
  
  if (dataType == "MAP") {
    candidates
  } else {
    candidates[candidates != "EntryIteration"]
  }
}

orderedBenchmarkNamesForBoxplot <- function(dataType) {
  candidates <- c("Lookup\n", "Insert\n", "Delete\n", "Iteration\n(Key)", "Iteration\n(Entry)", "Equality\n(Distinct)", "Equality\n(Derived)", "Footprint\n(32-bit)", "Footprint\n(64-bit)")
  
  if (dataType == "MAP") {
    candidates
  } else {
    candidates[candidates != "Iteration\n(Entry)"]
  }
}

createTable <- function(input, dataType, dataStructureOrigin, measureVars, dataFormatter) {
  lowerBoundExclusive <- 1
  
  benchmarksCast <- dcast(input[input$Param_dataType == dataType & input$Param_size > lowerBoundExclusive,], Benchmark + Param_size ~ Param_valueFactoryFactory + variable)
    
  benchmarksCast$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksCast$Param_size), "}", sep = ""))
  benchmarksCast$VF_CLOJURE_Interval <- latexMath(paste(benchmarksCast$VF_CLOJURE_Score, "\\pm", benchmarksCast$VF_CLOJURE_ScoreError))
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Interval <- latexMath(paste(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score, "\\pm", benchmarksCast$VF_PDB_PERSISTENT_CURRENT_ScoreError))
  benchmarksCast$VF_SCALA_Interval <- latexMath(paste(benchmarksCast$VF_SCALA_Score, "\\pm", benchmarksCast$VF_SCALA_ScoreError))
  ###
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
  benchmarksCast$VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_SCALA_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
  benchmarksCast$VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_CLOJURE_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
  ###
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_SCALA_Score)
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_CLOJURE_Score)
  ###
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_ScoreSavings <- (1 - benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score)
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_ScoreSavings <- (1 - benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score)
  
  orderedBenchmarkNames <- orderedBenchmarkNames(dataType)
  orderedBenchmarkIDs <- seq(1:length(orderedBenchmarkNames))
  
  orderingByName <- data.frame(orderedBenchmarkIDs, orderedBenchmarkNames)
  colnames(orderingByName) <- c("BenchmarkSortingID", "Benchmark")
  
  # selectComparisionColumns <- Vectorize(function(castedData, benchmarkName) {
  #   data.frame(castedData[castedData$Benchmark == benchmarkName,])[,c(13,14,15)]
  # })
    
  tableAll_summary <- selectComparisionColumnsSummary(benchmarksCast, measureVars, orderingByName)
  
  memFootprint <- calculateMemoryFootprintOverhead(dataType, dataStructureOrigin) 
  memFootprint <- memFootprint[memFootprint$elementCount > lowerBoundExclusive,]
  memFootprint_fmt <- data.frame(sapply(1:NCOL(memFootprint), function(col_idx) { memFootprint[,c(col_idx)] <- dataFormatter(memFootprint[,c(col_idx)])}))
  colnames(memFootprint_fmt) <- colnames(memFootprint)
    
  tableAll <- selectComparisionColumns(benchmarksCast, measureVars, orderingByName)
  tableAll <- tableAll[tableAll$Param_size > lowerBoundExclusive,]
  tableAll <- data.frame(tableAll, memFootprint[,c(2,3)])      
  
  tableAll_fmt <- data.frame(
    latexMath(paste("2^{", log2(tableAll$Param_size), "}", sep = "")),
    sapply(2:NCOL(tableAll), function(col_idx) { tableAll[,c(col_idx)] <- dataFormatter(tableAll[,c(col_idx)])}))
  colnames(tableAll_fmt) <- colnames(tableAll)
  
  tableAll_summary <- data.frame(tableAll_summary, calculateMemoryFootprintSummary(memFootprint))
  tableAll_summary_fmt <- data.frame(sapply(1:NCOL(tableAll_summary), function(col_idx) { tableAll_summary[,c(col_idx)] <- dataFormatter(tableAll_summary[,c(col_idx)])}))
  rownames(tableAll_summary_fmt) <- rownames(tableAll_summary)

  fileNameSummary <- paste(paste("all", "benchmarks", tolower(dataStructureOrigin), tolower(dataType), "summary", sep="-"), "tex", sep=".")
  write.table(tableAll_summary_fmt, file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  #write.table(t(tableAll_summary_fmt), file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  
  fileName <- paste(paste("all", "benchmarks", tolower(dataStructureOrigin), tolower(dataType), sep="-"), "tex", sep=".")
  write.table(tableAll_fmt, file = fileName, sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  #write.table(t(tableAll_fmt), file = fileName, sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")  


  ###
  # Create boxplots as well
  ##
  outFileName <-paste(paste("all", "benchmarks", tolower(dataStructureOrigin), tolower(dataType), "boxplot", sep="-"), "pdf", sep=".")
  fontScalingFactor <- 1.2
  pdf(outFileName, family = "Times", width = 10, height = 3)
  
  selection <- tableAll[2:NCOL(tableAll)]
  names(selection) <- orderedBenchmarkNamesForBoxplot(dataType)
  
  par(mar = c(3.5,4.75,0,0) + 0.1)
  par(mgp=c(3.5, 1.75, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  
  boxplot(selection, ylim=range(-0.1, 1.0), yaxt="n", las=0, ylab="savings (in %)", 
          cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  
  z  <- c(0.0, 0.2, 0.4, 0.6, 0.8, 1.0)
  zz <- c("0%", "20%", "40%", "60%", "80%", "100%")
  par(mgp=c(0, 0.75, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  axis(2, at=z, labels=zz, las=2,
       cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  
#   abline(v =  5.5)
  
  #abline(h =  0.75, lty=3)
  #abline(h =  0.5, lty=3)
  #abline(h =  0.25, lty=3)
  abline(h =  0)
  abline(h = -0.5, lty=3)
  dev.off()
  embed_fonts(outFileName)
  
}

# ###
# # Results as saving percentags
# ##
# measureVars_Scala <- c('VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_ScoreSavings')
# measureVars_Clojure <- c('VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_ScoreSavings')
# dataFormatter <- latexMathPercent
# 
# createTable(benchmarksByNameOutput, "SET", "Scala", measureVars_Scala, dataFormatter)
# createTable(benchmarksByNameOutput, "SET", "Clojure", measureVars_Clojure, dataFormatter)
# createTable(benchmarksByNameOutput, "MAP", "Scala", measureVars_Scala, dataFormatter)
# createTable(benchmarksByNameOutput, "MAP", "Clojure", measureVars_Clojure, dataFormatter)

# ###
# # Results as speedup factors
# ##
# measureVars_Scala <- c('VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score')
# measureVars_Clojure <- c('VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score')
# dataFormatter <- latexMathFactor
# 
# createTable(benchmarksByNameOutput, "SET", "Scala", measureVars_Scala, dataFormatter)
# createTable(benchmarksByNameOutput, "SET", "Clojure", measureVars_Clojure, dataFormatter)
# createTable(benchmarksByNameOutput, "MAP", "Scala", measureVars_Scala, dataFormatter)
# createTable(benchmarksByNameOutput, "MAP", "Clojure", measureVars_Clojure, dataFormatter)


createCacheStatTable <- function(input, dataType, dataStructureOrigin, measureVars, benchmarkName, dataFormatter) {
  lowerBoundExclusive <- 1
  
  benchmarksCast <- dcast(input[input$Benchmark == benchmarkName & input$Param_dataType == dataType & input$Param_size > lowerBoundExclusive,], Benchmark + Param_size ~ Param_valueFactoryFactory + variable)
  
  my_ylim <- range(0, 1.0)
  
  boxplot(ylim=my_ylim, benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L1_HIT_RATE, benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L2_HIT_RATE, benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L3_HIT_RATE)
  boxplot(ylim=my_ylim, benchmarksCast$VF_SCALA_L1_HIT_RATE, benchmarksCast$VF_SCALA_L2_HIT_RATE, benchmarksCast$VF_SCALA_L3_HIT_RATE)
  boxplot(ylim=my_ylim, benchmarksCast$VF_CLOJURE_L1_HIT_RATE, benchmarksCast$VF_CLOJURE_L2_HIT_RATE, benchmarksCast$VF_CLOJURE_L3_HIT_RATE)
  
  boxplot(ylim=my_ylim, benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L1_HIT_RATE, benchmarksCast$VF_SCALA_L1_HIT_RATE, benchmarksCast$VF_CLOJURE_L1_HIT_RATE)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L1_HIT, benchmarksCast$VF_SCALA_L1_HIT, benchmarksCast$VF_CLOJURE_L1_HIT)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L1_REF, benchmarksCast$VF_SCALA_L1_REF, benchmarksCast$VF_CLOJURE_L1_REF)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L1_MISSES, benchmarksCast$VF_SCALA_L1_MISSES, benchmarksCast$VF_CLOJURE_L1_MISSES)
  
  boxplot(ylim=my_ylim, benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L2_HIT_RATE, benchmarksCast$VF_SCALA_L2_HIT_RATE, benchmarksCast$VF_CLOJURE_L2_HIT_RATE)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L2_HIT, benchmarksCast$VF_SCALA_L2_HIT, benchmarksCast$VF_CLOJURE_L2_HIT)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L2_REF, benchmarksCast$VF_SCALA_L2_REF, benchmarksCast$VF_CLOJURE_L2_REF)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L2_MISSES, benchmarksCast$VF_SCALA_L2_MISSES, benchmarksCast$VF_CLOJURE_L2_MISSES)
  
  boxplot(ylim=my_ylim, benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L3_HIT_RATE, benchmarksCast$VF_SCALA_L3_HIT_RATE, benchmarksCast$VF_CLOJURE_L3_HIT_RATE)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L3_HIT, benchmarksCast$VF_SCALA_L3_HIT, benchmarksCast$VF_CLOJURE_L3_HIT)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L3_REF, benchmarksCast$VF_SCALA_L3_REF, benchmarksCast$VF_CLOJURE_L3_REF)
  boxplot(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_L3_MISSES, benchmarksCast$VF_SCALA_L3_MISSES, benchmarksCast$VF_CLOJURE_L3_MISSES)
  
  sel <- c("Param_size", "Param_valueFactoryFactory", "L3_MISSES")
  tmp <- dcast(input[input$Benchmark == benchmarkName & input$Param_dataType == dataType & input$Param_size > lowerBoundExclusive,], Benchmark + Param_size + Param_valueFactoryFactory ~ variable)[sel]  
  # tmp[,3:NCOL(tmp)] <- round(tmp[,3:NCOL(tmp)], 2)
  tmp[,3:NCOL(tmp)] <- formatCsUnits(tmp[,3:NCOL(tmp)])
  
  
  benchmarksCast$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksCast$Param_size), "}", sep = ""))
  benchmarksCast$VF_CLOJURE_Interval <- latexMath(paste(benchmarksCast$VF_CLOJURE_Score, "\\pm", benchmarksCast$VF_CLOJURE_ScoreError))
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Interval <- latexMath(paste(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score, "\\pm", benchmarksCast$VF_PDB_PERSISTENT_CURRENT_ScoreError))
  benchmarksCast$VF_SCALA_Interval <- latexMath(paste(benchmarksCast$VF_SCALA_Score, "\\pm", benchmarksCast$VF_SCALA_ScoreError))
  ###
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
  benchmarksCast$VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_SCALA_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
  benchmarksCast$VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_CLOJURE_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
  ###
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_SCALA_Score)
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_CLOJURE_Score)
  ###
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_ScoreSavings <- (1 - benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score)
  benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_ScoreSavings <- (1 - benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score)
  
  orderedBenchmarkNames <- orderedBenchmarkNames(dataType)
  orderedBenchmarkIDs <- seq(1:length(orderedBenchmarkNames))
  
  orderingByName <- data.frame(orderedBenchmarkIDs, orderedBenchmarkNames)
  colnames(orderingByName) <- c("BenchmarkSortingID", "Benchmark")
  
  # selectComparisionColumns <- Vectorize(function(castedData, benchmarkName) {
  #   data.frame(castedData[castedData$Benchmark == benchmarkName,])[,c(13,14,15)]
  # })
  
  tableAll_summary <- selectComparisionColumnsSummary(benchmarksCast, measureVars, orderingByName)
  
  tableAll <- selectComparisionColumns(benchmarksCast, measureVars, orderingByName)
  tableAll <- tableAll[tableAll$Param_size > lowerBoundExclusive,]
#   tableAll <- data.frame(tableAll, memFootprint[,c(2,3)])      
  
  tableAll_fmt <- data.frame(
    latexMath(paste("2^{", log2(tableAll$Param_size), "}", sep = "")),
    sapply(2:NCOL(tableAll), function(col_idx) { tableAll[,c(col_idx)] <- dataFormatter(tableAll[,c(col_idx)])}))
  colnames(tableAll_fmt) <- colnames(tableAll)
  
#   tableAll_summary <- data.frame(tableAll_summary, calculateMemoryFootprintSummary(memFootprint))
  tableAll_summary_fmt <- data.frame(sapply(1:NCOL(tableAll_summary), function(col_idx) { tableAll_summary[,c(col_idx)] <- dataFormatter(tableAll_summary[,c(col_idx)])}))
  rownames(tableAll_summary_fmt) <- rownames(tableAll_summary)
  
  fileNameSummary <- paste(paste("all", "benchmarks", tolower(dataStructureOrigin), tolower(dataType), "summary", sep="-"), "tex", sep=".")
  write.table(tableAll_summary_fmt, file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  #write.table(t(tableAll_summary_fmt), file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  
  fileName <- paste(paste("all", "benchmarks", tolower(dataStructureOrigin), tolower(dataType), sep="-"), "tex", sep=".")
  write.table(tableAll_fmt, file = fileName, sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  #write.table(t(tableAll_fmt), file = fileName, sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")  
  
  
#   ###
#   # Create boxplots as well
#   ##
#   outFileName <-paste(paste("all", "benchmarks", tolower(dataStructureOrigin), tolower(dataType), "boxplot", sep="-"), "pdf", sep=".")
#   fontScalingFactor <- 1.2
#   pdf(outFileName, family = "Times", width = 10, height = 3)
#   
#   selection <- tableAll[2:NCOL(tableAll)]
#   names(selection) <- orderedBenchmarkNamesForBoxplot(dataType)
#   
#   par(mar = c(3.5,4.75,0,0) + 0.1)
#   par(mgp=c(3.5, 1.75, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
#   
#   boxplot(selection, ylim=range(-0.1, 1.0), yaxt="n", las=0, ylab="savings (in %)", 
#           cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
#   
#   z  <- c(0.0, 0.2, 0.4, 0.6, 0.8, 1.0)
#   zz <- c("0%", "20%", "40%", "60%", "80%", "100%")
#   par(mgp=c(0, 0.75, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
#   axis(2, at=z, labels=zz, las=2,
#        cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
#   
#   #   abline(v =  5.5)
#   
#   #abline(h =  0.75, lty=3)
#   #abline(h =  0.5, lty=3)
#   #abline(h =  0.25, lty=3)
#   abline(h =  0)
#   abline(h = -0.5, lty=3)
#   dev.off()
#   embed_fonts(outFileName)
  
}

###
# Results as speedup factors
##
measureVars_Scala <- c('VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score')
measureVars_Clojure <- c('VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score')
dataFormatter <- latexMathFactor

# createCacheStatTable(benchmarksByNameOutput, "MAP", "Scala", measureVars_Scala, "EntryIteration", dataFormatter)
createCacheStatTable(benchmarksByNameOutput, "MAP", "Scala", measureVars_Scala, "Iteration", dataFormatter)
createCacheStatTable(benchmarksByNameOutput, "MAP", "Scala", measureVars_Scala, "EqualsRealDuplicate", dataFormatter)

# createCacheStatTable(benchmarksByNameOutput, "SET", "Clojure", measureVars_Clojure, dataFormatter)
# createCacheStatTable(benchmarksByNameOutput, "MAP", "Scala", measureVars_Scala, dataFormatter)
# createCacheStatTable(benchmarksByNameOutput, "MAP", "Clojure", measureVars_Clojure, dataFormatter)
