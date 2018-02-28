#!/usr/bin/env Rscript

# http://stackoverflow.com/questions/14167178/passing-command-line-arguments-to-r-cmd-batch
args <- commandArgs(TRUE)

timestamp <- args[2]
timestampMemoryMeasurement <- timestamp

dataDirectory <- paste(args[1], timestamp, sep = "/")
setwd(dataDirectory)

# http://stackoverflow.com/questions/17705133/package-error-when-running-r-code-on-command-line
cran_rstudio_repo="http://cran.rstudio.com/"

# install.packages("vioplot", repos = cran_rstudio_repo)
# install.packages("beanplot", repos = cran_rstudio_repo)
# install.packages("ggplot2", repos = cran_rstudio_repo)
# install.packages("reshape2", repos = cran_rstudio_repo)
# install.packages("functional", repos = cran_rstudio_repo)
# install.packages("plyr", repos = cran_rstudio_repo)
# install.packages("extrafont", repos = cran_rstudio_repo)
# install.packages("scales", repos = cran_rstudio_repo)
# # install.packages("tikzDevice", repos = cran_rstudio_repo)
library(vioplot)
library(beanplot)
library(ggplot2)
library(reshape2)
library(functional)
library(plyr) # needed to access . function
library(extrafont)
library(scales)
# require(tikzDevice)
loadfonts()


capwords <- function(s, strict = FALSE) {
  cap <- function(s) paste(toupper(substring(s, 1, 1)),
{s <- substring(s, 2); if(strict) tolower(s) else s},
sep = "", collapse = " " )
sapply(strsplit(s, split = " "), cap, USE.NAMES = !is.null(names(s)))
}


loadMemoryFootprintData <- function() {
  ###
  # Load 32-bit and 64-bit data and combine them.
  ##
  dss32_fileName <- paste(paste(dataDirectory, paste("map_sizes_heterogeneous_exponential", "32bit", timestampMemoryMeasurement, sep="_"), sep="/"), "csv", sep=".")
  dss32_stats <- read.csv(dss32_fileName, sep=",", header=TRUE)
  dss32_stats <- within(dss32_stats, arch <- factor(32))
  #
  dss64_fileName <- paste(paste(dataDirectory, paste("map_sizes_heterogeneous_exponential", "64bit", timestampMemoryMeasurement, sep="_"), sep="/"), "csv", sep=".")
  dss64_stats <- read.csv(dss64_fileName, sep=",", header=TRUE)
  dss64_stats <- within(dss64_stats, arch <- factor(64))
  #
  dss_stats <- rbind(dss32_stats, dss64_stats)
  colnames(dss_stats) <- c("Param_size", "Param_run", "Param_valueFactoryFactory", "Param_dataType", "archetype", "supportsStagedMutability", "footprintInBytes", "footprintInObjects", "footprintInReferences", "arch")
  #
  dss_stats
}

simplifyMemoryFootprintData <- function(dss_stats) {
  ###
  # If there are more measurements for one size, calculate the median.
  # Currently we only have one measurment.
  ##
  dss_stats_meltByElementCount <- melt(dss_stats, id.vars=c('Param_size', 'Param_valueFactoryFactory', 'Param_dataType', 'arch'), measure.vars=c('footprintInBytes'))
  dss_stats_castByMedian <- dcast(dss_stats_meltByElementCount, Param_size + Param_valueFactoryFactory + Param_dataType ~ paste("footprintInBytes", arch, "median", sep = "_"), median, fill=0)
}

memoryFootprintDataAsBenchmark <- function(dss_stats) {
  ###
  # If there are more measurements for one size, calculate the median.
  # Currently we only have one measurment.
  ##
  dss_stats_meltByElementCount <- melt(dss_stats, id.vars=c('Param_size', 'Param_valueFactoryFactory', 'Param_dataType', 'arch'), measure.vars=c('footprintInBytes'), value.name = "Score")
  dss_stats_meltByElementCount$Benchmark <- paste("Footprint", dss_stats_meltByElementCount$arch, sep = "")
  
  dss_stats_meltByElementCount <- subset(dss_stats_meltByElementCount, select = -c(arch, variable))
  
  aggregate(. ~ Param_dataType + Param_valueFactoryFactory + Param_size + Benchmark, dss_stats_meltByElementCount, median)
}

calculateMemoryFootprintOverhead <- function(requestedDataType, dataStructureBaseline) {
  
  dss_stats <- loadMemoryFootprintData()
  dss_stats_castByMedian <- simplifyMemoryFootprintData(dss_stats)
  
  classNameTheOther <- dataStructureBaseline
  classNameOurs <- "VF_PDB_PERSISTENT_CURRENT"
  
  ###
  # Calculate different baselines for comparison.
  ##
  dss_stats_castByBaselinePDBDynamic <- aggregate(footprintInBytes_median ~ Param_size + Param_dataType + arch, dss_stats_castByMedian[dss_stats_castByMedian$Param_valueFactoryFactory == classNameOurs & dss_stats_castByMedian$dataType == requestedDataType,], min)
  names(dss_stats_castByBaselinePDBDynamic) <- c('Param_size', 'Param_dataType', 'arch', 'footprintInBytes_baselinePDBDynamic')
  
  ###
  # Merges baselines.
  ##
  dss_stats_with_min <- merge(dss_stats_castByMedian, dss_stats_castByBaselinePDBDynamic)

  # http://www.dummies.com/how-to/content/how-to-add-calculated-fields-to-data-in-r.navId-812016.html
  dss_stats_with_min <- within(dss_stats_with_min, memoryOverheadFactorComparedToPDBDynamic <- dss_stats_with_min$footprintInBytes_median / footprintInBytes_baselinePDBDynamic)
  dss_stats_with_min <- within(dss_stats_with_min, memorySavingComparedToPDBDynamic <- 1 - (dss_stats_with_min$footprintInBytes_baselinePDBDynamic / dss_stats_with_min$footprintInBytes_median))

  ###
  # selecting data subsets  
  ##
  theOther <- dss_stats_castByMedian[dss_stats_castByMedian$className == classNameTheOther & dss_stats_castByMedian$dataType == requestedDataType,]
  ours <- dss_stats_castByMedian[dss_stats_castByMedian$className == classNameOurs & dss_stats_castByMedian$dataType == requestedDataType,]

  ###
  # BE AWARE: hard-coded switching from 'memory savings in %' to 'speedup factor'.
  ##
  # 'memory savings in %':
  memorySavingComparedToTheOther <- 1 - (ours$footprintInBytes_median / theOther$footprintInBytes_median)
  # 'speedup factor':
  # memorySavingComparedToTheOther <- (theOther$footprintInBytes_median / ours$footprintInBytes_median)

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
  
  if (is.na(arg) || as.numeric(arg) < 1) {
    paste("${\\color{red}", arg_fmt, "}$", sep = "")
  } else {
    paste("$", arg_fmt, "$", sep = "")
  }
}

latexMathFactor <- Vectorize(latexMathFactor__)


latexMathPercent__ <- function(arg) {
  arg_fmt <- formatPercent(arg)
  
  postfix <- "\\%"
  
  if (is.na(arg) || is.nan(arg)) { #  | !is.numeric(arg)
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
  argWithoutAnnotation <- strsplit(as.character(arg), split = ":")[[1]][1]
  strsplit(argWithoutAnnotation, split = "[.]time")[[1]][2]
}

getBenchmarkMethodName <- Vectorize(getBenchmarkMethodName__)


getBenchmarkAnnotationName__ <- function(arg) {
  strsplit(as.character(arg), split = ":")[[1]][2]
}

getBenchmarkAnnotationName <- Vectorize(getBenchmarkAnnotationName__)


benchmarksFileName <- paste(paste(dataDirectory, paste("results.all", timestamp, sep="-"), sep="/"), "log", sep=".")
benchmarks <- read.csv(benchmarksFileName, sep=",", header=TRUE, stringsAsFactors=FALSE)

# TODO: switch between MEAN and MEDIAN
# colnames(benchmarks) <- c("Benchmark", "Mode", "Threads", "Samples", "Score", "ScoreError", "Unit", "Param_dataType", "Param_multimapValueSize", "Param_producer", "Param_run", "Param_sampleDataSelection", "Param_size", "Param_stepSizeOneToOneSelector", "Param_valueFactoryFactory")
colnames(benchmarks) <- c("Benchmark", "Mode", "Threads", "Samples", "Score", "ScoreError_90_0", "RelativeScoreError_90_0", "ScoreError_95_0", "RelativeScoreError_95_0", "ScoreError_99_9", "RelativeScoreError_99_9", "MedianScore", "MedianAbsoluteDeviation", "RelativeMedianAbsoluteDeviation", "Unit", "Param_dataType", "Param_multimapValueSize", "Param_producer", "Param_run", "Param_sampleDataSelection", "Param_size", "Param_stepSizeOneToOneSelector", "Param_valueFactoryFactory")

# "Benchmark","Mode","Threads","Samples","Score","Score Error (90.0%)","Relative Score Error (90.0%)","Score Error (95.0%)","Relative Score Error (95.0%)","Score Error (99.9%)","Relative Score Error (99.9%)","Median Score","Median Absolute Deviation","Relative Median Absolute Deviation","Unit","Param: dataType","Param: multimapValueSize","Param: producer","Param: run","Param: sampleDataSelection","Param: size","Param: stepSizeOneToOneSelector","Param: valueFactoryFactory"

# View(benchmarks$Score - benchmarks$MedianScore)

# TODO: switch between MEAN and MEDIAN
# benchmarks$Score <- benchmarks$Score # identity
benchmarks$Score <- benchmarks$MedianScore

benchmarks$Annotation <- getBenchmarkAnnotationName(benchmarks$Benchmark)
benchmarks$Benchmark <- getBenchmarkMethodName(benchmarks$Benchmark)

lowerBoundExclusive <- 1

# ###
# # Filter before grouping.
# ##
benchmarksCleaned <- subset(benchmarks, 
                            Param_sampleDataSelection == "MATCH" & benchmarks$Annotation == "hashCode") # is.na(benchmarks$Annotation)
# benchmarksCleaned <- subset(benchmarks, 
#                             Param_sampleDataSelection == "MATCH" & is.na(benchmarks$Annotation), 
#                             select = c(Benchmark, Score, Param_dataType, Param_run, Param_size, Param_valueFactoryFactory, footprintInBytes_32_median, footprintInBytes_64_median))
# # benchmarksCleaned <- subset(benchmarks, Param_sampleDataSelection == "MATCH" & benchmarks$Annotation == "hashCode", select = c(Benchmark, Score, Param_dataType, Param_run, Param_size, Param_valueFactoryFactory))

###
# If there are more measurements for one size, calculate the median.
# Currently we only have one measurment.
#
# Optionally use subset selection: .(Benchmark, Param_dataType, Param_size, Param_valueFactoryFactory)
# Optionally select all: colnames(benchmarks)
##
benchmarksCleaned <- ddply(benchmarks, .(Benchmark, Param_dataType, Param_size, Param_valueFactoryFactory), function(x) c(Score = median(x$Score))) # ScoreError = median(x$ScoreError)

###
# Load memory footprints and join with other benchmarks.
##
# benchmarksCleaned <- join(benchmarksCleaned, simplifyMemoryFootprintData(loadMemoryFootprintData()))
benchmarksCleaned <- rbind(benchmarksCleaned, memoryFootprintDataAsBenchmark(loadMemoryFootprintData()))

benchmarksByName <- melt(benchmarksCleaned, id.vars=c('Benchmark', 'Param_size', 'Param_dataType', 'Param_valueFactoryFactory'))





selectComparisionColumns <- function(inputData, measureVars, orderingByName) {
  tmp.m <- melt(data=join(inputData, orderingByName, type = "inner"), id.vars=c('BenchmarkSortingID', 'Benchmark', 'Param_size'), measure.vars=measureVars)

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

summarizeTableData <- function(inputData) {
  mins.c <- apply(inputData, c(2), min) # as.numeric(formatNsmall2(apply(tmp.c, c(2), min), rounding=T))
  maxs.c <- apply(inputData, c(2), max) # as.numeric(formatNsmall2(apply(tmp.c, c(2), max), rounding=T))
  #mean.c <- apply(inputData, c(2), mean)
  medians.c <- apply(inputData, c(2), median) # as.numeric(formatNsmall2(apply(tmp.c, c(2), median), rounding=T))
  
  res <- data.frame(rbind(mins.c, medians.c, maxs.c))[-1]
  rownames(res) <- c('minimum', 'median', 'maximum')
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

orderedBenchmarkNames <- function(dataType) {
  candidates <- c("ContainsKey", "ContainsKeyNotContained", "Insert", "InsertContained", "RemoveKey", "RemoveKeyNotContained", "Iteration", "EntryIteration", "EqualsRealDuplicate", "EqualsDeltaDuplicate", "Footprint32", "Footprint64")
  
  if (dataType == "SET_MULTIMAP") {
    c("MultimapLikeContainsTuple" 
      , "MultimapLikeContainsTupleNotContained"
      , "MultimapLikeInsertTuple"
      # , "MultimapLikeInsertTupleContained"
      , "MultimapLikeRemoveTuple"
      # , "MultimapLikeRemoveTupleNotContained"
      # , "MultimapLikeIterationKey"
      # , "MultimapLikeIterationFlattenedEntry"
      # , "MultimapLikeEqualsRealDuplicate" 
      # , "MultimapLikeEqualsDeltaDuplicate"
      , "Footprint32"
      , "Footprint64"
      )
  } else if (dataType == "MAP") {
    c("MapLikeContainsKey"
      , "MapLikeContainsKeyNotContained"
      , "MapLikePut"
      , "MapLikePutContained"
      , "MapLikeRemove"
      , "MapLikeRemoveNotContained"
      , "MapLikeIterationKey"
      , "MapLikeIterationNativeEntry"
      # , "MapLikeEqualsRealDuplicate"
      # , "MapLikeEqualsDeltaDuplicate"
#       , "Footprint32"
#       , "Footprint64"
      )
  } else {
    candidates[candidates != "EntryIteration"]
  }
}

orderedBenchmarkNamesForBoxplot <- function(dataType) {
  candidates <- c("Lookup\n", "Lookup\n(Fail)", "Insert\n", "Insert\n(Fail)", "Delete\n", "Delete\n(Fail)", "Iteration\n(Key)", "Iteration\n(Entry)", "Equality\n(Distinct)", "Equality\n(Derived)", "Footprint\n(32-bit)", "Footprint\n(64-bit)")
  
  candidates <- c("Iter. (Key)", "Iter. (Entry)")
  candidates <- c("Eq. (Distinct)", "Eq. (Derived)")
    
  if (dataType == "SET_MULTIMAP") {
    c("Lookup\n" 
      , "Lookup\n(Fail)" 
      , "Insert\n" 
      # , "Insert\n(Fail)" 
      , "Delete\n" 
      # , "Delete\n(Fail)" 
      # , "Iteration\n(Key)" 
      # , "Iteration\n(Entry)"
      # , "Equality\n(Distinct)" 
      # , "Equality\n(Derived)" 
      , "Footprint\n(32-bit)" 
      , "Footprint\n(64-bit)"
      )
  } else if (dataType == "MAP") {
    c("Lookup\n"
      , "Lookup\n(Fail)"
      , "Insert\n"
#       , "Insert\n(Fail)"
      , "Delete\n"
#       , "Delete\n(Fail)"
      , "Iteration\n(Key)"
      , "Iteration\n(Entry)"
      # , "Equality\n(Distinct)"
      # , "Equality\n(Derived)"
#       , "Footprint\n(32-bit)" 
#       , "Footprint\n(64-bit)"
      )  
    } else {
    candidates[candidates != "Iteration\n(Entry)"]
  }
}

createTable <- function(input, dataType, dataStructureCandidate, dataStructureBaseline, dataFormatter, compare, boxplotFunction, nameAppendix, includeMemory = F) {
  filteredInput <- input[input$Param_dataType == dataType & input$variable == "Score" & input$Param_size > lowerBoundExclusive,]

  # assumes: nlevels(factor(input$variable)) == 1
  benchmarksCast <- dcast(filteredInput, Benchmark + Param_size ~ Param_valueFactoryFactory)

  baselineAndOtherPairName <- paste(dataStructureCandidate, "BY", dataStructureBaseline, sep = "_")
  benchmarksCast[baselineAndOtherPairName] <- compare(benchmarksCast[dataStructureCandidate], benchmarksCast[dataStructureBaseline])
      
#   benchmarksCast$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksCast$Param_size), "}", sep = ""))
#   benchmarksCast$VF_CLOJURE_Interval <- latexMath(paste(benchmarksCast$VF_CLOJURE_Score, "\\pm", benchmarksCast$VF_CLOJURE_ScoreError))
#   benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Interval <- latexMath(paste(benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score, "\\pm", benchmarksCast$VF_PDB_PERSISTENT_CURRENT_ScoreError))
#   benchmarksCast$VF_SCALA_Interval <- latexMath(paste(benchmarksCast$VF_SCALA_Score, "\\pm", benchmarksCast$VF_SCALA_ScoreError))
#   ###
#   benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
#   benchmarksCast$VF_SCALA_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_SCALA_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
#   benchmarksCast$VF_CLOJURE_BY_VF_PDB_PERSISTENT_CURRENT_Score <- (benchmarksCast$VF_CLOJURE_Score / benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score)
#   ###
#   benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_SCALA_Score)
#   benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score <- (benchmarksCast$VF_PDB_PERSISTENT_CURRENT_Score / benchmarksCast$VF_CLOJURE_Score)
#   ###
#   benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_ScoreSavings <- (1 - benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_Score)
#   benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_ScoreSavings <- (1 - benchmarksCast$VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_Score)
  
  orderedBenchmarkNames <- orderedBenchmarkNames(dataType)
  orderedBenchmarkIDs <- seq(1:length(orderedBenchmarkNames))
  
  orderingByName <- data.frame(orderedBenchmarkIDs, orderedBenchmarkNames)
  colnames(orderingByName) <- c("BenchmarkSortingID", "Benchmark")
  
  tableAll <- selectComparisionColumns(benchmarksCast, baselineAndOtherPairName, orderingByName)
  tableAll <- tableAll[tableAll$Param_size > lowerBoundExclusive,]

  if (includeMemory == T) {
    memFootprint <- calculateMemoryFootprintOverhead(dataType, dataStructureBaseline) 
    memFootprint <- memFootprint[memFootprint$elementCount > lowerBoundExclusive,]
    memFootprint_fmt <- data.frame(sapply(1:NCOL(memFootprint), function(col_idx) { memFootprint[,c(col_idx)] <- dataFormatter(memFootprint[,c(col_idx)])}))
    colnames(memFootprint_fmt) <- colnames(memFootprint)
    
    tableAll <- data.frame(tableAll, memFootprint[,c(2,3)]) 
  }
  
  tableAll_fmt <- data.frame(
    latexMath(paste("2^{", log2(tableAll$Param_size), "}", sep = "")),
    sapply(2:NCOL(tableAll), function(col_idx) { tableAll[,c(col_idx)] <- dataFormatter(tableAll[,c(col_idx)])}))
  colnames(tableAll_fmt)[ 1] <- "Size"
  colnames(tableAll_fmt)[-1] <- orderedBenchmarkNamesForBoxplot(dataType)
  
  # tableAll_summary <- selectComparisionColumnsSummary(benchmarksCast, baselineAndOtherPairName, orderingByName)
  tableAll_summary <- summarizeTableData(tableAll)
  
  if (includeMemory == T) {
    tableAll_summary <- data.frame(tableAll_summary, calculateMemoryFootprintSummary(memFootprint))
  }
  
  tableAll_summary_fmt <- data.frame(sapply(1:NCOL(tableAll_summary), function(col_idx) { tableAll_summary[,c(col_idx)] <- dataFormatter(tableAll_summary[,c(col_idx)])}))
  rownames(tableAll_summary_fmt) <- rownames(tableAll_summary)

  # outputFolder <- "./tables-latex/tables"
  # 
  # fileNameSummary <- paste(outputFolder, paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), nameAppendix, "summary", sep="-"), "tex", sep="."), sep="/")
  # write.table(tableAll_summary_fmt, file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  # #write.table(t(tableAll_summary_fmt), file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  # 
  # fileName <- paste(outputFolder, paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), nameAppendix, sep="-"), "tex", sep="."), sep="/")
  # write.table(tableAll_fmt, file = fileName, sep = " & ", row.names = FALSE, col.names = TRUE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  # #write.table(t(tableAll_fmt), file = fileName, sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")  

  # createBoxplot(tableAll, dataType, baselineAndOtherPairName, nameAppendix);
  boxplotFunction(tableAll, dataType, baselineAndOtherPairName, nameAppendix);
}

createBoxplotPercentages <- function(tableAll, dataType, baselineAndOtherPairName, nameAppendix) {

#   options( tikzLatexPackages = c(
#     "\\usepackage{tikz}",
#     "\\usepackage[active,tightpage]{preview}",
#     "\\PreviewEnvironment{pgfpicture}",
#     "\\setlength\\PreviewBorder{0pt}", 
#     "\\usepackage{siunitx}",
#     "\\usepackage{xspace}",
#     "\\newcommand{\\Contained}{Contained\\xspace}",
#     "\\newcommand{\\NotContained}{$\\neg$Contained\\xspace}"),
#     
#     tikzXelatexPackages = c(
#       "\\usepackage{tikz}\n", 
#       "\\usepackage[active,tightpage,xetex]{preview}\n", 
#       "\\usepackage{fontspec,xunicode}\n", 
#       "\\PreviewEnvironment{pgfpicture}\n", 
#       "\\setlength\\PreviewBorder{0pt}\n",
#       "\\usepackage{siunitx}",
#       "\\usepackage{xspace}",
#       "\\newcommand{\\Contained}{Contained\\xspace}",
#       "\\newcommand{\\NotContained}{$\\neg$Contained\\xspace}")
#   )
  
  ###
  # Create boxplots as well
  ##
  outFileName <-paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), "boxplot", nameAppendix, sep="-"), "pdf", sep=".")
  fontScalingFactor <- 0.6

  pdf(outFileName, family = "Times", width = 7, height = 1.65)
  #tikz(outFileName, standAlone = FALSE, width = 15, height = 3.5, engine = "pdftex")
  
  selection <- tableAll[2:NCOL(tableAll)]
  names(selection) <- orderedBenchmarkNamesForBoxplot(dataType)
  
  par(mar = c(1.6,2.3,0,0) + 0.15) # c(bottom, left, top, right)
  par(mgp=c(1.8, 0.425, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  
  par(tck = -0.025)
  boxplot(selection, ylim=range(-0.4, 1.0), yaxt="n", las=0, ylab="savings (in %)", lwd = 0.5, boxlwd = 0.5, outcex = 0.5,
          cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  
  z  <- c(-0.8, -0.6, -0.4, -0.2, 0.0, 0.2, 0.4, 0.6, 0.8, 1.0)
  zz <- c("-80%", "-60%", "-40%", "-20%", "0%", "20%", "40%", "60%", "80%", "100%")
  #zz <- c("\\SI{-80}{\\percent}", "\\SI{-60}{\\percent}", "\\SI{-40}{\\percent}", "\\SI{-20}{\\percent}", "\\SI{0}{\\percent}", "\\SI{20}{\\percent}", "\\SI{40}{\\percent}", "\\SI{60}{\\percent}", "\\SI{80}{\\percent}", "\\SI{100}{\\percent}")
  par(mgp=c(0, 0.25, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  axis(2, at=z, labels=zz, las=2, tck = -0.0225,  
       cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
#  axis(1, labels = NA, tck = -0.025)
  
#   abline(v =  5.5)
  
  #abline(h =  0.75, lty=3)
  #abline(h =  0.5, lty=3)
  #abline(h =  0.25, lty=3)
  abline(h =  0)
  abline(h = -0.5, lty=3)
  dev.off()
  embed_fonts(outFileName)
}

createBoxplotSpeedups <- function(tableAll, dataType, baselineAndOtherPairName, nameAppendix) {
  
  ###
  # Create boxplots as well
  ##
  outFileName <-paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), "boxplot", nameAppendix, sep="-"), "pdf", sep=".")
  fontScalingFactor <- 0.6
  
  pdf(outFileName, family = "Times", width = 7, height = 1.75)
  #tikz(outFileName, standAlone = FALSE, width = 15, height = 3.5, engine = "pdftex")
  
  selection <- tableAll[2:NCOL(tableAll)]
  names(selection) <- orderedBenchmarkNamesForBoxplot(dataType)
  
  par(mar = c(1.6,2.3,0,0) + 0.15) # c(bottom, left, top, right)
  par(mgp=c(1.8, 0.425, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  
  par(tck = -0.025)
  boxplot(selection, ylim=range(-1.5, 4.5), yaxt="n", las=0, ylab="Regression or Improvement (Factor)", lwd = 0.5, boxlwd = 0.5, outcex = 0.5,
          cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  
  z  <- c(-1, 0, 1, 2, 3, 4)
  zz <- c("2x", "neutral", "2x", "3x", "4x", "5x")

  #zz <- c("\\SI{-80}{\\percent}", "\\SI{-60}{\\percent}", "\\SI{-40}{\\percent}", "\\SI{-20}{\\percent}", "\\SI{0}{\\percent}", "\\SI{20}{\\percent}", "\\SI{40}{\\percent}", "\\SI{60}{\\percent}", "\\SI{80}{\\percent}", "\\SI{100}{\\percent}")
  par(mgp=c(0, 0.25, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  axis(2, at=z, labels=zz, las=2, tck = -0.0225,  
       cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  #  axis(1, labels = NA, tck = -0.025)
  
  #   abline(v =  5.5)
  
  # abline(h =  0.75, lty=3)
  # abline(h =  0.5,  lty=3)
  # abline(h =  0.25, lty=3)
  abline(h =  0)
  # abline(h = -0.25, lty=3)
  # abline(h = -0.5,  lty=3)  
  # abline(h = -0.75, lty=3)
  dev.off()
  embed_fonts(outFileName)
}

createBoxplotSetMultimapSpeedups <- function(tableAll, dataType, baselineAndOtherPairName, nameAppendix) {
  
  ###
  # Create boxplots as well
  ##
  outFileName <-paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), "boxplot", nameAppendix, sep="-"), "pdf", sep=".")
  fontScalingFactor <- 1.6
  
  pdf(outFileName, family = "Times", width = 10, height = 3.75)
  #tikz(outFileName, standAlone = FALSE, width = 15, height = 3.5, engine = "pdftex")
  
  selection <- tableAll[2:NCOL(tableAll)]
  names(selection) <- orderedBenchmarkNamesForBoxplot(dataType)
  
  par(mar = c(3.2,5.8,0.3,0) + 0.15) # c(bottom, left, top, right)
  par(mgp=c(4.8, 2.25, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  
  #par(tck = -0.025)
  boxplot(selection, ylim=range(-0.55, 3.5), yaxt="n", las=0, ylab="Regression or Improvement", lwd = 0.5, boxlwd = 0.5, outcex = 0.5,
          cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  
  z  <- c(-1, 0, 1, 2, 3, 4)
  zz <- c("2x", "neutral", "2x", "3x", "4x", "5x")
  
  # z  <- c(-2, -1.75, -1.50, -1.25, -1, -0.75, -0.50, -0.25, 0, 0.25, 0.50, 0.75, 1)
  # zz <- c("3x", "2.75x", "2.50x", "2.25x", "2x", "1.75x", "1.50x", "1.25x", "neutral", "1.25x", "1.50x", "1.75x", "2x")  
  
  #zz <- c("\\SI{-80}{\\percent}", "\\SI{-60}{\\percent}", "\\SI{-40}{\\percent}", "\\SI{-20}{\\percent}", "\\SI{0}{\\percent}", "\\SI{20}{\\percent}", "\\SI{40}{\\percent}", "\\SI{60}{\\percent}", "\\SI{80}{\\percent}", "\\SI{100}{\\percent}")
  par(mgp=c(0, 0.25, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  axis(2, at=z, labels=zz, las=2, tck = -0.0225,  
       cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  #  axis(1, labels = NA, tck = -0.025)
  
  #   abline(v =  5.5)
  
  # abline(h =  0.75, lty=3)
  # abline(h =  0.5,  lty=3)
  # abline(h =  0.25, lty=3)
  abline(h =  0)
  # abline(h = -0.25, lty=3)
  # abline(h = -0.5,  lty=3)  
  # abline(h = -0.75, lty=3)
  dev.off()
  embed_fonts(outFileName)
}

createBoxplotMapVsSetMultimapSpeedups <- function(tableAll, dataType, baselineAndOtherPairName, nameAppendix) {
  
  ###
  # Create boxplots as well
  ##
  outFileName <-paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), "boxplot", nameAppendix, sep="-"), "pdf", sep=".")
  fontScalingFactor <- 1.6
  
  pdf(outFileName, family = "Times", width = 10, height = 3.75)
  #tikz(outFileName, standAlone = FALSE, width = 15, height = 3.5, engine = "pdftex")
  
  selection <- tableAll[2:NCOL(tableAll)]
  names(selection) <- orderedBenchmarkNamesForBoxplot(dataType)
  
  par(mar = c(3.2,5.8,0.3,0) + 0.15) # c(bottom, left, top, right)
  par(mgp=c(4.8, 2.25, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  
  par(tck = -0.025)
  boxplot(selection, ylim=range(-0.55, 1.0), yaxt="n", las=0, ylab="Regression or Improvement", lwd = 0.5, boxlwd = 0.5, outcex = 0.5,
          cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  
  z  <- c(-2, -1.75, -1.50, -1.25, -1, -0.75, -0.50, -0.25, 0, 0.25, 0.50, 0.75, 1)
  zz <- c("3x", "2.75x", "2.50x", "2.25x", "2x", "1.75x", "1.50x", "1.25x", "neutral", "1.25x", "1.50x", "1.75x", "2x")

  #zz <- c("\\SI{-80}{\\percent}", "\\SI{-60}{\\percent}", "\\SI{-40}{\\percent}", "\\SI{-20}{\\percent}", "\\SI{0}{\\percent}", "\\SI{20}{\\percent}", "\\SI{40}{\\percent}", "\\SI{60}{\\percent}", "\\SI{80}{\\percent}", "\\SI{100}{\\percent}")
  par(mgp=c(0, 0.25, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  axis(2, at=z, labels=zz, las=2, tck = -0.0225,  
       cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  #  axis(1, labels = NA, tck = -0.025)
  
  #   abline(v =  5.5)
  
  # abline(h =  0.75, lty=3)
  # abline(h =  0.5,  lty=3)
  # abline(h =  0.25, lty=3)
  abline(h =  0)
  # abline(h = -0.25, lty=3)
  # abline(h = -0.5,  lty=3)  
  # abline(h = -0.75, lty=3)

  dev.off()
  embed_fonts(outFileName)
}



benchmarksByNameOutput <- data.frame(benchmarksByName)
benchmarksByNameOutput$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksByName$Param_size), "}", sep = ""))

###
# Comparising functions
##
compareSpeedup <- Vectorize(function(candidate, baseline) {
  speedup <- baseline/candidate
  slowdown <- candidate/baseline

  ifelse(speedup >= 1, speedup-1, -(slowdown-1))
})

compareSaving <- Vectorize(function(candidate, baseline) {
  1 - (candidate / baseline)
})

createAllTables <- function(dataFormatter, compareFunction, boxplotFunctionMapVsMultimap, boxplotFunctionMultimap, nameAppendix) {
#   createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_SCALA", dataFormatter, compareFunction, nameAppendix)
#   createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_CLOJURE", dataFormatter, compareFunction, nameAppendix)
#   createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_SCALA", dataFormatter, compareFunction, nameAppendix)
#   createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_CLOJURE", dataFormatter, compareFunction, nameAppendix)
# 
#   # createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_PDB_PERSISTENT_CURRENT", dataFormatter, compareFunction, nameAppendix)
# 
#   createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_CURRENT", "VF_SCALA", dataFormatter, compareFunction, nameAppendix)
#   createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_CURRENT", "VF_CLOJURE", dataFormatter, compareFunction, nameAppendix)
#   createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_CURRENT", "VF_SCALA", dataFormatter, compareFunction, nameAppendix)
#   createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_CURRENT", "VF_CLOJURE", dataFormatter, compareFunction, nameAppendix)

#   createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_SCALA", dataFormatter, compareFunction, boxplotFunction, nameAppendix)
#   createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_CLOJURE", dataFormatter, compareFunction, boxplotFunction, nameAppendix)

  ### MAP VS MULTIMAP ###
  #createTable(benchmarksByNameOutput, "MAP", "VF_CHAMP_MULTIMAP_HCHAMP", "VF_CHAMP_MAP_AS_MULTIMAP", dataFormatter, compareFunction, boxplotFunctionMapVsMultimap, nameAppendix)
  createTable(benchmarksByNameOutput, "MAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_CHAMP_MAP_AS_MULTIMAP", dataFormatter, compareFunction, boxplotFunctionMapVsMultimap, nameAppendix)
  #createTable(benchmarksByNameOutput, "MAP", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED", "VF_CHAMP_MAP_AS_MULTIMAP", dataFormatter, compareFunction, boxplotFunctionMapVsMultimap, nameAppendix)
  #createTable(benchmarksByNameOutput, "MAP", "VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED", "VF_CHAMP_MAP_AS_MULTIMAP", dataFormatter, compareFunction, boxplotFunctionMapVsMultimap, nameAppendix)
  #createTable(benchmarksByNameOutput, "MAP", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED", "VF_CHAMP_MULTIMAP_HHAMT", dataFormatter, compareFunction, boxplotFunctionMapVsMultimap, nameAppendix)
  #
  #createTable(benchmarksByNameOutput, "MAP", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_NO_COPYMEMORY", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED", dataFormatter, compareFunction, boxplotFunctionMapVsMultimap, nameAppendix)
  ###
  
  ### MULTIMAP ###
  createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_SCALA", dataFormatter, compareFunction, boxplotFunctionMultimap, nameAppendix)
  createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_CLOJURE", dataFormatter, compareFunction, boxplotFunctionMultimap, nameAppendix)
  #  
  #createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED", "VF_SCALA", dataFormatter, compareFunction, boxplotFunctionMultimap, nameAppendix)
  #createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED", "VF_CLOJURE", dataFormatter, compareFunction, boxplotFunctionMultimap, nameAppendix)
  #
  #createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED", "VF_SCALA", dataFormatter, compareFunction, boxplotFunctionMultimap, nameAppendix)
  #createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED", "VF_CLOJURE", dataFormatter, compareFunction, boxplotFunctionMultimap, nameAppendix)  
  ###
}



###
# Results as speedup factors
##
dataFormatter <- latexMathFactor
compareFunction <- compareSpeedup
nameAppendix <- "speedup"
# boxplotFunction <- createBoxplotSpeedups
# createBoxplotMapVsSetMultimapSpeedups, createBoxplotSetMultimapSpeedups
# createAllTables(dataFormatter, compareFunction, createBoxplotMapVsSetMultimapSpeedups, createBoxplotMapVsSetMultimapSpeedups, nameAppendix)

createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_SCALA", dataFormatter, compareFunction, createBoxplotMapVsSetMultimapSpeedups, nameAppendix)
createTable(benchmarksByNameOutput, "SET_MULTIMAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_CLOJURE", dataFormatter, compareFunction, createBoxplotSetMultimapSpeedups, nameAppendix)
createTable(benchmarksByNameOutput, "MAP", "VF_CHAMP_MULTIMAP_HHAMT", "VF_CHAMP_MAP_AS_MULTIMAP", dataFormatter, compareFunction, createBoxplotMapVsSetMultimapSpeedups, nameAppendix)


###
# Results as saving percentages
##
# dataFormatter <- latexMathPercent
# compareFunction <- compareSaving
# nameAppendix <- "savings"
# boxplotFunction <- createBoxplotPercentages
# 
# createAllTables(dataFormatter, compareFunction, boxplotFunction, nameAppendix)



###
# Report on accuracy of measurements.
##

statScoreError <- function(scoreError) {
  print(c(min(scoreError), median(scoreError), max(scoreError)))
  print(quantile(sort(scoreError), c(.50, .90, .95, .99)))
}

statScoreError(scoreError = benchmarks$RelativeScoreError_90_0)
statScoreError(scoreError = benchmarks$RelativeScoreError_95_0)
statScoreError(scoreError = benchmarks$RelativeScoreError_99_9)

# sub <- benchmarks # benchmarks[benchmarks$Param_size > 1,]
# sub$ScoreErrorRelative <- (sub$ScoreError * 100) / sub$Score
# 
# median(sub$ScoreErrorRelative)
# c(min(sub$ScoreErrorRelative), median(sub$ScoreErrorRelative), max(sub$ScoreErrorRelative))
# # mad((sub$ScoreError * 100) / sub$Score)
# 
# # median(sub$ScoreError)
# # c(min(sub$ScoreError), median(sub$ScoreError), max(sub$ScoreError))
# # # mad(sub$ScoreError)
# 
# # View(sub[order(-sub$ScoreErrorRelative),])
# # View(sub[order(-sub$ScoreErrorRelative) & sub$ScoreErrorRelative > 10,])
# View(sub[order(-sub$ScoreErrorRelative) & sub$ScoreErrorRelative > 10, c('Benchmark','Param_dataType','Param_run','Param_size','Param_valueFactoryFactory')])
# 
# quantile(sub[order(sub$ScoreErrorRelative),]$ScoreErrorRelative, c(.50, .90, .95, .99))


###
# Report on accuracy with new JMH data points.
##
sub <- benchmarks

c(min(sub$RelativeScoreError_90_0), median(sub$RelativeScoreError_90_0), max(sub$RelativeScoreError_90_0))
c(min(sub$RelativeScoreError_95_0), median(sub$RelativeScoreError_95_0), max(sub$RelativeScoreError_95_0))
c(min(sub$RelativeScoreError_99_9), median(sub$RelativeScoreError_99_9), max(sub$RelativeScoreError_99_9))
####
# View(sub[order(-sub$RelativeScoreError_99_9) & sub$RelativeScoreError_99_9 > 0.10, c('Benchmark','Param_dataType','Param_run','Param_size','Param_valueFactoryFactory','RelativeScoreError_99_9')])
####

# c(min(sub$MedianAbsoluteDeviation), median(sub$MedianAbsoluteDeviation), max(sub$MedianAbsoluteDeviation))
c(min(sub$RelativeMedianAbsoluteDeviation), median(sub$RelativeMedianAbsoluteDeviation), max(sub$RelativeMedianAbsoluteDeviation))

quantile(sub[order(sub$RelativeMedianAbsoluteDeviation),]$RelativeMedianAbsoluteDeviation, c(.50, .90, .95, .99))

###
# View(sub[order(-sub$RelativeMedianAbsoluteDeviation) & sub$RelativeMedianAbsoluteDeviation > 0.05, c('Benchmark','Param_dataType','Param_run','Param_size','Param_valueFactoryFactory')])
###


df <- benchmarksCleaned

df$overheadPerTuple <- df$Score / (df$Param_size * 1.5) - 8
dfSubOur <- df[df$Benchmark == "Footprint32" & df$Param_dataType == "SET_MULTIMAP" & df$Param_valueFactoryFactory == "VF_CHAMP_MULTIMAP_HHAMT" & df$Param_size > lowerBoundExclusive,]
dfSubOurInterlinked <- df[df$Benchmark == "Footprint32" & df$Param_dataType == "SET_MULTIMAP" & df$Param_valueFactoryFactory == "VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED" & df$Param_size > lowerBoundExclusive,]
dfSubOurSpecialized <- df[df$Benchmark == "Footprint32" & df$Param_dataType == "SET_MULTIMAP" & df$Param_valueFactoryFactory == "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_PATH_INTERLINKED" & df$Param_size > lowerBoundExclusive,]
dfSubOther <- df[df$Benchmark == "Footprint32" & df$Param_dataType == "SET_MULTIMAP" & (df$Param_valueFactoryFactory == "VF_SCALA" | df$Param_valueFactoryFactory == "VF_CLOJURE") & df$Param_size > lowerBoundExclusive,]

round(c(median(dfSubOur$overheadPerTuple), median(dfSubOther$overheadPerTuple) / median(dfSubOur$overheadPerTuple)), digits=2)
round(c(median(dfSubOurInterlinked$overheadPerTuple),  median(median(dfSubOther$overheadPerTuple) / dfSubOurInterlinked$overheadPerTuple)), digits=2)
round(c(median(dfSubOurSpecialized$overheadPerTuple),  median(median(dfSubOther$overheadPerTuple) / dfSubOurSpecialized$overheadPerTuple)), digits=2)
round(median(dfSubOther$overheadPerTuple), digits=2)
