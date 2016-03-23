#!/usr/bin/env Rscript
setwd("~/Research/datastructures-for-metaprogramming/hamt-heterogeneous/data")

library(reshape2)

# benchmark_fileName <- "map_sizes_heterogeneous_exponential"
benchmark_fileName <- "map_sizes_heterogeneous_exponential_32bit_primitive_latest"
benchmark_data <- read.csv(paste0(benchmark_fileName, ".csv"), sep=",", header=TRUE)

# benchmark_data <- subset(benchmark_data, className != "TrieMapIntInt")

benchmark_data.m <- melt(benchmark_data, id.vars=c('elementCount', 'className'), measure.vars=c('footprintInBytes'))
benchmark_data.c <- dcast(benchmark_data.m,  className ~ elementCount, median, fill=0)

# http://r.789695.n4.nabble.com/Is-there-any-R-function-for-data-normalization-td4644768.html
# http://stackoverflow.com/questions/5294955/how-to-scale-down-a-range-of-numbers-with-a-known-min-and-max-value
# http://math.stackexchange.com/questions/362918/value-range-of-normalization-methods-min-max-z-score-decimal-scaling
normalize <- function(x) { 
  x <- sweep(x, 2, apply(x, 2, min)) 
  sweep(x, 2, apply(x, 2, max), "/") 
} 

ratio <- function(x) { 
  x_min <- apply(x, 2, min)
  sweep(x, 2, x_min, "/") 
}

result_fileName <- paste0(benchmark_fileName, "_result.csv")
write.csv2(benchmark_data.c, file = result_fileName, quote = FALSE)

benchmark_data.c.transposed <- data.frame(t(benchmark_data.c[-1]))
colnames(benchmark_data.c.transposed) <-  benchmark_data.c[,1]

resultTransposed_fileName <- paste0(benchmark_fileName, "_result.transposed.csv")
write.csv2(benchmark_data.c.transposed, file = resultTransposed_fileName, quote = FALSE)

benchmark_data.c.ratio <- benchmark_data.c
benchmark_data.c.ratio[-1] <- ratio(benchmark_data.c[-1]) 

resultRatio_fileName <- paste0(benchmark_fileName, "_result.ratio.csv")
write.csv2(benchmark_data.c.ratio, file = resultRatio_fileName, quote = FALSE)

benchmark_data.c.ratio.transposed <- data.frame(t(benchmark_data.c.ratio[-1]))
colnames(benchmark_data.c.ratio.transposed) <-  benchmark_data.c[,1]

resultRatioTransposed_fileName <- paste0(benchmark_fileName, "_result.ratio.transposed.csv")
write.csv2(benchmark_data.c.ratio.transposed, file = resultRatioTransposed_fileName, quote = FALSE)

benchmark_data.c.norm <- benchmark_data.c
benchmark_data.c.norm[-1] <- normalize(benchmark_data.c[-1]) 

resultNorm_fileName <- paste0(benchmark_fileName, "_result.norm.csv")
write.csv2(benchmark_data.c.norm, file = resultNorm_fileName, quote = FALSE)

tmp <- data.frame(t(benchmark_data.c.norm[-1]))
colnames(tmp) <- benchmark_data.c.norm[,1]

resultNormTransposed_fileName <- paste0(benchmark_fileName, "_result.norm.transposed.csv")
write.csv2(tmp, file = resultNormTransposed_fileName, quote = FALSE)
