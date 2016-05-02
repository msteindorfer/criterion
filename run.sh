#!/bin/bash

mvn clean install
mkdir -p target/results
mkdir -p target/result-logs

# export VALUE_FACTORY_FACTORY="VF_CHAMP,VF_CHAMP_MEMOIZED,VF_SCALA,VF_CLOJURE"
export VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP="VF_CHAMP_MAP_AS_MULTIMAP,VF_CHAMP_MULTIMAP_HCHAMP,VF_CHAMP_MULTIMAP_HHAMT,VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED"
export VALUE_FACTORY_FACTORY="VF_CHAMP_MULTIMAP_HHAMT,VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED,VF_SCALA,VF_CLOJURE"

# Old settings
# export PERF_EVENTS="L1-DCACHE-LOADS,L1-DCACHE-LOAD-MISSES,L2_RQSTS:0x03,L2_RQSTS:0x01,LLC_REFERENCES,LLC_MISSES,MEM_LOAD_RETIRED:0x10"
# export COMMON_JVM_SETTINGS="-Xms4g -Xmx4g -Doverseer.utils.events=$PERF_EVENTS"
# export COMMON_SETTINGS="-wi 15 -i 15 -f 1 -r 1 -p run=0,1,2,3,4 -p sampleDataSelection=MATCH -p producer=PDB_INTEGER -gc true -rf csv -v EXTRA -foe true -bm avgt"

# Settings for cache measurements:
# export COMMON_SETTINGS="-wi 0 -i 200 -r 10 -f 1 -t 1 -p run=0 -p sampleDataSelection=MATCH -gc true -rf csv -v EXTRA -foe true -bm ss"

######
export AGGREGATED_SETTINGS="-jvm /Library/Java/JavaVirtualMachines/jdk1.8.u-hack.jdk/Contents/Home/bin/java -jvmArgsPrepend -Xms8g -jvmArgsPrepend -Xmx8g -jvmArgsPrepend -XX:-TieredCompilation -jvmArgsPrepend -XX:+UseCompressedOops -wi 20 -i 20 -f 1 -r 1 -gc true -rf csv -v NORMAL -foe true -bm avgt -p sampleDataSelection=MATCH -p producer=PDB_INTEGER"

export PACKAGE="io.usethesource.criterion"

export SET_BENCHMARKS="$PACKAGE.JmhSetBenchmarks.(timeContainsKey|timeContainsKeyNotContained|timeInsert|timeInsertContained|timeRemoveKey|timeRemoveKeyNotContained|timeIteration|timeEqualsRealDuplicate|timeEqualsDeltaDuplicate)$"
export MAP_BENCHMARKS="$PACKAGE.JmhMapBenchmarks.(timeContainsKey|timeContainsKeyNotContained|timeInsert|timeInsertContained|timeRemoveKey|timeRemoveKeyNotContained|timeIteration|timeEntryIteration|timeEqualsRealDuplicate|timeEqualsDeltaDuplicate)$"
export MAP_VS_SETMULTIMAP_BENCHMARKS="$PACKAGE.JmhSetMultimapBenchmarks.(timeMapLike.*)$"
export SETMULTIMAP_BENCHMARKS="$PACKAGE.JmhSetMultimapBenchmarks.(timeMultimapLike.*)$"

# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=0 -rff ./target/results/results.JmhSetBenchmarks.run0.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run0.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run0.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=1 -rff ./target/results/results.JmhSetBenchmarks.run1.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run1.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run1.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=2 -rff ./target/results/results.JmhSetBenchmarks.run2.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run2.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run2.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=3 -rff ./target/results/results.JmhSetBenchmarks.run3.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run3.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run3.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=4 -rff ./target/results/results.JmhSetBenchmarks.run4.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run4.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run4.log

# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=0 -rff ./target/results/results.JmhMapBenchmarks.run0.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run0.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run0.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=1 -rff ./target/results/results.JmhMapBenchmarks.run1.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run1.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run1.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=2 -rff ./target/results/results.JmhMapBenchmarks.run2.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run2.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run2.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=3 -rff ./target/results/results.JmhMapBenchmarks.run3.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run3.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run3.log
# LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=4 -rff ./target/results/results.JmhMapBenchmarks.run4.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run4.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run4.log

##
# Map vs SetMultimap
###
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_VS_SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=MAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP -p multimapValueSize=1 -p stepSizeOneToOneSelector=1 -p run=0 -rff ./target/results/results.JmhMapVsSetMultimapBenchmarks.run0.log 1>./target/result-logs/results.std-console.JmhMapVsSetMultimapBenchmarks.run0.log 2>./target/result-logs/results.err-console.JmhMapVsSetMultimapBenchmarks.run0.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_VS_SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=MAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP -p multimapValueSize=1 -p stepSizeOneToOneSelector=1 -p run=1 -rff ./target/results/results.JmhMapVsSetMultimapBenchmarks.run1.log 1>./target/result-logs/results.std-console.JmhMapVsSetMultimapBenchmarks.run1.log 2>./target/result-logs/results.err-console.JmhMapVsSetMultimapBenchmarks.run1.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_VS_SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=MAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP -p multimapValueSize=1 -p stepSizeOneToOneSelector=1 -p run=2 -rff ./target/results/results.JmhMapVsSetMultimapBenchmarks.run2.log 1>./target/result-logs/results.std-console.JmhMapVsSetMultimapBenchmarks.run2.log 2>./target/result-logs/results.err-console.JmhMapVsSetMultimapBenchmarks.run2.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_VS_SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=MAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP -p multimapValueSize=1 -p stepSizeOneToOneSelector=1 -p run=3 -rff ./target/results/results.JmhMapVsSetMultimapBenchmarks.run3.log 1>./target/result-logs/results.std-console.JmhMapVsSetMultimapBenchmarks.run3.log 2>./target/result-logs/results.err-console.JmhMapVsSetMultimapBenchmarks.run3.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_VS_SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=MAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY_MAP_VS_SET_MULTIMAP -p multimapValueSize=1 -p stepSizeOneToOneSelector=1 -p run=4 -rff ./target/results/results.JmhMapVsSetMultimapBenchmarks.run4.log 1>./target/result-logs/results.std-console.JmhMapVsSetMultimapBenchmarks.run4.log 2>./target/result-logs/results.err-console.JmhMapVsSetMultimapBenchmarks.run4.log

LD_LIBRARY_PATH=~/lib/ java -Dio.usethesource.capsule.RangecopyUtils.dontUseSunMiscUnsafeCopyMemory=true -jar target/benchmarks.jar $MAP_VS_SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=MAP -p valueFactoryFactory=VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_NO_COPYMEMORY -p multimapValueSize=1 -p stepSizeOneToOneSelector=1 -p run=0,1,2,3,4 -rff ./target/results/results.JmhMapVsSetMultimapBenchmarks.noCopyMemory.log 1>./target/result-logs/results.std-console.JmhMapVsSetMultimapBenchmarks.noCopyMemory.log 2>./target/result-logs/results.err-console.JmhMapVsSetMultimapBenchmarks.noCopyMemory.log

##
# Amongst SetMultimap Implementations
###
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=SET_MULTIMAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=0 -rff ./target/results/results.JmhSetMultimapBenchmarks.run0.log 1>./target/result-logs/results.std-console.JmhSetMultimapBenchmarks.run0.log 2>./target/result-logs/results.err-console.JmhSetMultimapBenchmarks.run0.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=SET_MULTIMAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=1 -rff ./target/results/results.JmhSetMultimapBenchmarks.run1.log 1>./target/result-logs/results.std-console.JmhSetMultimapBenchmarks.run1.log 2>./target/result-logs/results.err-console.JmhSetMultimapBenchmarks.run1.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=SET_MULTIMAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=2 -rff ./target/results/results.JmhSetMultimapBenchmarks.run2.log 1>./target/result-logs/results.std-console.JmhSetMultimapBenchmarks.run2.log 2>./target/result-logs/results.err-console.JmhSetMultimapBenchmarks.run2.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=SET_MULTIMAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=3 -rff ./target/results/results.JmhSetMultimapBenchmarks.run3.log 1>./target/result-logs/results.std-console.JmhSetMultimapBenchmarks.run3.log 2>./target/result-logs/results.err-console.JmhSetMultimapBenchmarks.run3.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SETMULTIMAP_BENCHMARKS $AGGREGATED_SETTINGS -p dataType=SET_MULTIMAP -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p run=4 -rff ./target/results/results.JmhSetMultimapBenchmarks.run4.log 1>./target/result-logs/results.std-console.JmhSetMultimapBenchmarks.run4.log 2>./target/result-logs/results.err-console.JmhSetMultimapBenchmarks.run4.log
######

(cd ../capsule && git log)   | head -1 > `pwd`/target/result-logs/git.commit.capsule.txt
(cd ../criterion && git log) | head -1 > `pwd`/target/result-logs/git.commit.criterion.txt
shasum -a 256 target/benchmarks.jar    > `pwd`/target/result-logs/benchmarks.jar.sha256sum

TIMESTAMP=`date +"%Y%m%d_%H%M"`

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
PERF_STAT_LOG_FILES=`pwd`/target/result-logs/results.perf-stat.*.log


export PACKAGE="io.usethesource.criterion"
RESULTS_FILE_PERF_STAT=`pwd`/target/results/results.all-$TIMESTAMP.perf-stat.log

PERF_HEADER=`echo $PERF_STAT_LOG_FILES | xargs -n 1 head -n 1 | head -n 1 | sed -e 's/^/benchmark,/'`
{
	for f in $PERF_STAT_LOG_FILES
	do
		CURRENT_BENCHMARK=`echo "$f" | sed 's/.*\.time\([^.]*\)\(.*\)/\1/'`
		tail -n +2 $f | sed -e "s/^/$CURRENT_BENCHMARK,/"
	done
} | cat <(echo $PERF_HEADER) - | xz -9 > $RESULTS_FILE_PERF_STAT.xz

# java -Xmx12G -XX:+UseCompressedOops -javaagent:`echo $(cd $(dirname ~); pwd)/$(basename ~)`/.m2/repository/com/google/memory-measurer/1.0-SNAPSHOT/memory-measurer-1.0-SNAPSHOT.jar -cp target/benchmarks.jar $PACKAGE.CalculateFootprints && mv map-sizes-and-statistics.csv target/map-sizes-and-statistics-32bit-$TIMESTAMP.csv
# java -Xmx12G -XX:-UseCompressedOops -javaagent:`echo $(cd $(dirname ~); pwd)/$(basename ~)`/.m2/repository/com/google/memory-measurer/1.0-SNAPSHOT/memory-measurer-1.0-SNAPSHOT.jar -cp target/benchmarks.jar $PACKAGE.CalculateFootprints && mv map-sizes-and-statistics.csv target/map-sizes-and-statistics-64bit-$TIMESTAMP.csv

ARCHIVE_PATH=`pwd`
ARCHIVE_NAME=$ARCHIVE_PATH/hamt-benchmark-results-$TIMESTAMP.tgz

RESULTS_FILES=`pwd`/target/results/results.all-$TIMESTAMP*

cp $RESULTS_FILES $ARCHIVE_PATH
(cd target && tar -cvzf $ARCHIVE_NAME results result-logs *.csv $RESULTS_FILES)
