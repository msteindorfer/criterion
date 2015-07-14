#!/usr/bin/env Rscript

# http://stackoverflow.com/questions/14167178/passing-command-line-arguments-to-r-cmd-batch
args <- commandArgs(TRUE)

# setwd(args[1])
# dataDirectory <- args[2]
# timestamp <- args[3]

setwd("~/Development/rascal-devel/jmh-dscg-benchmarks/resources/r")
dataDirectory <- "~/Development/rascal-devel/jmh-dscg-benchmarks"
timestamp <- "20150714_0455"

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


loadMemoryFootprintData <- function() {
  ###
  # Load 32-bit and 64-bit data and combine them.
  ##
  dss32_fileName <- paste(paste(dataDirectory, paste("map-sizes-and-statistics", "32bit", timestamp, sep="-"), sep="/"), "csv", sep=".")
  dss32_stats <- read.csv(dss32_fileName, sep=",", header=TRUE)
  dss32_stats <- within(dss32_stats, arch <- factor(32))
  #
  dss64_fileName <- paste(paste(dataDirectory, paste("map-sizes-and-statistics", "64bit", timestamp, sep="-"), sep="/"), "csv", sep=".")
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
colnames(benchmarks) <- c("Benchmark", "Mode", "Threads", "Samples", "Score", "ScoreError", "Unit", "Param_dataType", "Param_producer", "Param_run", "Param_sampleDataSelection", "Param_size", "Param_valueFactoryFactory")

benchmarks$Annotation <- getBenchmarkAnnotationName(benchmarks$Benchmark)
benchmarks$Benchmark <- getBenchmarkMethodName(benchmarks$Benchmark)


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
  candidates <- c("ContainsKey", "ContainsKeyNotContained", "Insert", "InsertContained", "RemoveKey", "RemoveKeyNotContained", "Iteration", "EntryIteration", "EqualsRealDuplicate", "EqualsDeltaDuplicate", "EqualsRealDuplicateModified", "Footprint32", "Footprint64")
  
  if (dataType == "MAP") {
    candidates
  } else {
    candidates[candidates != "EntryIteration"]
  }
}

orderedBenchmarkNamesForBoxplot <- function(dataType) {
  candidates <- c("Lookup\n", "Lookup\n(Fail)", "Insert\n", "Insert\n(Fail)", "Delete\n", "Delete\n(Fail)", "Iteration\n(Key)", "Iteration\n(Entry)", "Equality\n(Distinct)", "Equality\n(Derived)", "Equality\n(Different)", "Footprint\n(32-bit)", "Footprint\n(64-bit)")
  
  if (dataType == "MAP") {
    candidates
  } else {
    candidates[candidates != "Iteration\n(Entry)"]
  }
}

createTable <- function(input, dataType, dataStructureCandidate, dataStructureBaseline, measureVars, dataFormatter, compare, includeMemory = F) {
  lowerBoundExclusive <- 1
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
  colnames(tableAll_fmt) <- colnames(tableAll)
  
  tableAll_summary <- selectComparisionColumnsSummary(benchmarksCast, baselineAndOtherPairName, orderingByName)
  
  if (includeMemory == T) {
    tableAll_summary <- data.frame(tableAll_summary, calculateMemoryFootprintSummary(memFootprint))
  }
  
  tableAll_summary_fmt <- data.frame(sapply(1:NCOL(tableAll_summary), function(col_idx) { tableAll_summary[,c(col_idx)] <- dataFormatter(tableAll_summary[,c(col_idx)])}))
  rownames(tableAll_summary_fmt) <- rownames(tableAll_summary)

  fileNameSummary <- paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), "summary", sep="-"), "tex", sep=".")
  write.table(tableAll_summary_fmt, file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  #write.table(t(tableAll_summary_fmt), file = fileNameSummary, sep = " & ", row.names = TRUE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  
  fileName <- paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), sep="-"), "tex", sep=".")
  write.table(tableAll_fmt, file = fileName, sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")
  #write.table(t(tableAll_fmt), file = fileName, sep = " & ", row.names = FALSE, col.names = FALSE, append = FALSE, quote = FALSE, eol = " \\\\ \n")  

  createBoxplot(tableAll, dataType, baselineAndOtherPairName);
}

createBoxplot <- function(tableAll, dataType, baselineAndOtherPairName) {
  ###
  # Create boxplots as well
  ##
  outFileName <-paste(paste("all", "benchmarks", tolower(baselineAndOtherPairName), tolower(dataType), "boxplot", sep="-"), "pdf", sep=".")
  fontScalingFactor <- 1.2
  pdf(outFileName, family = "Times", width = 15, height = 4)
  
  selection <- tableAll[2:NCOL(tableAll)]
  names(selection) <- orderedBenchmarkNamesForBoxplot(dataType)
  
  par(mar = c(3.5,4.75,0,0) + 0.1)
  par(mgp=c(3.5, 1.75, 0)) # c(axis.title.position, axis.label.position, axis.line.position)
  
  boxplot(selection, ylim=range(-0.5, 1.0), yaxt="n", las=0, ylab="savings (in %)", 
          cex.lab=fontScalingFactor, cex.axis=fontScalingFactor, cex.main=fontScalingFactor, cex.sub=fontScalingFactor)
  
  z  <- c(-0.4, -0.2, 0.0, 0.2, 0.4, 0.6, 0.8, 1.0)
  zz <- c("-40%", "-20%", "0%", "20%", "40%", "60%", "80%", "100%")
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





benchmarksByNameOutput <- data.frame(benchmarksByName)
benchmarksByNameOutput$Param_out_sizeLog2 <- latexMath(paste("2^{", log2(benchmarksByName$Param_size), "}", sep = ""))

###
# Comparising functions
##
compareSpeedup <- Vectorize(function(candidate, baseline) {
  (baseline / candidate)
})

compareSaving <- Vectorize(function(candidate, baseline) {
  1 - (candidate / baseline)
})

###
# Results as saving percentages
##
measureVars_Scala <- c('VF_PDB_PERSISTENT_CURRENT_BY_VF_SCALA_ScoreSavings')
measureVars_Clojure <- c('VF_PDB_PERSISTENT_CURRENT_BY_VF_CLOJURE_ScoreSavings')
dataFormatter <- latexMathPercent
compareFunction = compareSaving

createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_SCALA", measureVars_Scala, dataFormatter, compareFunction)
createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_CLOJURE", measureVars_Scala, dataFormatter, compareFunction)
createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_SCALA", measureVars_Scala, dataFormatter, compareFunction)
createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_CLOJURE", measureVars_Scala, dataFormatter, compareFunction)

# createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_MEMOIZED_LAZY", "VF_PDB_PERSISTENT_CURRENT", measureVars_Scala, dataFormatter, compareFunction)

createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_CURRENT", "VF_SCALA", measureVars_Scala, dataFormatter, compareFunction)
createTable(benchmarksByNameOutput, "MAP", "VF_PDB_PERSISTENT_CURRENT", "VF_CLOJURE", measureVars_Clojure, dataFormatter, compareFunction)
createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_CURRENT", "VF_SCALA", measureVars_Scala, dataFormatter, compareFunction)
createTable(benchmarksByNameOutput, "SET", "VF_PDB_PERSISTENT_CURRENT", "VF_CLOJURE", measureVars_Clojure, dataFormatter, compareFunction)

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
