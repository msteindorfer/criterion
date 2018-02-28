#!/bin/bash

mvn clean install
mkdir -p target/results
mkdir -p target/result-logs

export VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP="VF_CHAMP_MAP_AS_MULTIMAP,VF_CHAMP_MULTIMAP_HHAMT"
export VALUE_FACTORY_FACTORY="VF_CHAMP_MULTIMAP_HHAMT,VF_SCALA,VF_CLOJURE"


######
export SIZE=1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768,65536,131072,262144,524288,1048576,2097152,4194304,8388608 # short: 16,2048,1048576
export RUN=0,1,2,3,4 # short: 0
export WARMUP_ITERATIONS=3 # 10
export BENCHMARK_ITERATIONS=3 # 20
#####
export AGGREGATED_SETTINGS=" -jvmArgsPrepend -Xms8g -jvmArgsPrepend -Xmx8g -jvmArgsPrepend -XX:-TieredCompilation -jvmArgsPrepend -XX:+UseCompressedOops -wi $WARMUP_ITERATIONS -i $BENCHMARK_ITERATIONS -f 1 -r 1 -gc true -rf csv -v NORMAL -foe true -bm avgt -p sampleDataSelection=MATCH -p producer=PDB_INTEGER -p size=$SIZE"
#####

export PACKAGE="io.usethesource.criterion"

export MAP_VS_SETMULTIMAP_BENCHMARKS="$PACKAGE.JmhSetMultimapBenchmarks.(timeMapLikeContainsKey|timeMapLikeContainsKeyNotContained|timeMapLikePut|timeMapLikeRemove|timeMapLikeIterationKey|timeMapLikeIterationNativeEntry)$"
export SETMULTIMAP_BENCHMARKS="$PACKAGE.JmhSetMultimapBenchmarks.(timeMultimapLikeContainsTuple|timeMultimapLikeContainsTupleNotContained|timeMultimapLikeInsertTuple|timeMultimapLikeRemoveTuple)$"

##
# Map vs SetMultimap
###
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_VS_SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=MAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP -p multimapValueSize=1 -p stepSizeOneToOneSelector=1 -p run=$RUN -rff ./target/results/results.JmhMapVsSetMultimapBenchmarks.run.log 2>./target/result-logs/results.err-console.JmhMapVsSetMultimapBenchmarks.run.log | tee ./target/result-logs/results.std-console.JmhMapVsSetMultimapBenchmarks.run.log
######

##
# Amongst SetMultimap Implementations
###
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=SET_MULTIMAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=$RUN -rff ./target/results/results.JmhSetMultimapBenchmarks.run.log 2>./target/result-logs/results.err-console.JmhSetMultimapBenchmarks.run.log | tee ./target/result-logs/results.std-console.JmhSetMultimapBenchmarks.run.log
######

(cd ../code && git log)   | head -1 > `pwd`/target/result-logs/git.commit.capsule.txt
(cd ../benchmark && git log) | head -1 > `pwd`/target/result-logs/git.commit.criterion.txt
shasum -a 256 target/benchmarks.jar    > `pwd`/target/result-logs/benchmarks.jar.sha256sum

TIMESTAMP=`date +"%Y%m%d_%H%M"`
echo $TIMESTAMP > LAST_TIMESTAMP_MICROBENCHMARKS.txt


INPUT_FILES=`pwd`/target/results/results.Jmh*.log
RESULTS_FILE=`pwd`/target/results/results.all-$TIMESTAMP.log

RESULT_HEADER=`echo $INPUT_FILES | xargs -n 1 head -n 1 | head -n 1`
{
	for f in $INPUT_FILES
	do
		tail -n +2 $f
	done
} | cat <(echo $RESULT_HEADER) - > $RESULTS_FILE

STD_CONSOLE_LOG_FILES=`pwd`/target/result-logs/results.std-console.*.log


export PACKAGE="io.usethesource.criterion"
java -Xmx12G -XX:+UseCompressedOops -javaagent:`echo $(cd $(dirname ~); pwd)/$(basename ~)`/.m2/repository/com/github/msteindorfer/memory-measurer/0.1.0-SNAPSHOT/memory-measurer-0.1.0-SNAPSHOT.jar -cp target/benchmarks.jar $PACKAGE.CalculateFootprintsHeterogeneous $SIZE && mv target/map_sizes_heterogeneous_exponential_32bit_latest.csv target/map_sizes_heterogeneous_exponential_32bit_$TIMESTAMP.csv
java -Xmx12G -XX:-UseCompressedOops -javaagent:`echo $(cd $(dirname ~); pwd)/$(basename ~)`/.m2/repository/com/github/msteindorfer/memory-measurer/0.1.0-SNAPSHOT/memory-measurer-0.1.0-SNAPSHOT.jar -cp target/benchmarks.jar $PACKAGE.CalculateFootprintsHeterogeneous $SIZE && mv target/map_sizes_heterogeneous_exponential_64bit_latest.csv target/map_sizes_heterogeneous_exponential_64bit_$TIMESTAMP.csv


mkdir -p `pwd`/../data/$TIMESTAMP
ARCHIVE_PATH=`echo $(cd ../data/$TIMESTAMP; pwd)`

ARCHIVE_NAME=$ARCHIVE_PATH/hamt-benchmark-results-$TIMESTAMP.tgz

RESULTS_FILES_LOG=`pwd`/target/results/results.all-$TIMESTAMP*
RESULTS_FILES_CSV=`pwd`/target/*.csv

cp $RESULTS_FILES_LOG $RESULTS_FILES_CSV $ARCHIVE_PATH
(cd target && tar -cvzf $ARCHIVE_NAME results result-logs $RESULTS_FILES_LOG $RESULTS_FILES_CSV)
