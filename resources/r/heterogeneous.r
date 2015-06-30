#!/usr/bin/env Rscript

library(reshape2)

benchmark_fileName <- "/Users/Michael/Development/jmh-dscg-benchmarks/map-sizes-and-statistics.csv"
benchmark_data <- read.csv(benchmark_fileName, sep=",", header=TRUE)

benchmark_data.m <- melt(benchmark_data, id.vars=c('elementCount', 'className'), measure.vars=c('footprintInBytes'))
benchmark_data.c <- dcast(benchmark_data.m,  className ~ elementCount, median, fill=0)

normalize <- function(x) { 
  x <- sweep(x, 2, apply(x, 2, min)) 
  sweep(x, 2, apply(x, 2, max), "/") 
} 

benchmark_data.c.norm <- benchmark_data.c
benchmark_data.c.norm[-1] <- normalize(benchmark_data.c[-1]) 

result_fileName <- "/Users/Michael/Development/jmh-dscg-benchmarks/map-sizes-and-statistics_result.csv"
write.csv2(benchmark_data.c, file = result_fileName, quote = FALSE)

resultNorm_fileName <- "/Users/Michael/Development/jmh-dscg-benchmarks/map-sizes-and-statistics_result.norm.csv"
write.csv2(benchmark_data.c.norm, file = resultNorm_fileName, quote = FALSE)

tmp <- data.frame(t(benchmark_data.c.norm[-1]))
colnames(tmp) <- benchmark_data.c.norm[,1]

resultNorm_fileName <- "/Users/Michael/Development/jmh-dscg-benchmarks/map-sizes-and-statistics_result.t.norm.csv"
write.csv2(tmp, file = resultNorm_fileName, quote = FALSE)


benchmark_data.c.norm[,1]