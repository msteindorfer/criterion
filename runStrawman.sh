export PARAM_SIZE="8,2048,1048576"
export PARAM_VALUE_FACTORY="VF_SCALA_STRAWMAN,VF_SCALA,VF_CHAMP_SPECIALIZED,VF_CHAMP,VF_DEXX,VF_VAVR,VF_PAGURO,VF_CLOJURE,VF_PCOLLECTIONS"
export MEMORY_MEASURER_AGENT=`echo $(cd $(dirname ~); pwd)/$(basename ~)`/.m2/repository/com/github/msteindorfer/memory-measurer/0.1.0-SNAPSHOT/memory-measurer-0.1.0-SNAPSHOT.jar

####
# MEASURE RUNTIMES 
##################################

java -jar ./target/benchmarks.jar \
	"JmhSetBenchmarks.time(Lookup$|Insert$|Remove$|Iteration|EqualsWorstCase)$" \
	-f 1 -i 5 -wi 5 \
	-p producer=PURE_INTEGER \
	-p size=$PARAM_SIZE \
	-p valueFactoryFactory=$PARAM_VALUE_FACTORY \
	-rf text -rff logStrawman-Runtime-useCapsule-$1.txt \
	-jvmArgsAppend "-Dstrawman.collection.immutable.useCapsule=$1"

# further benchmarks: SubsetOf|(Union|Subtract|Intersect)RealDuplicate

# read -n1 -r -p "Press space to continue..." key

###
MEASURE MEMORY FOOTPRINT 
#################################

java -javaagent:$MEMORY_MEASURER_AGENT -jar ./target/benchmarks.jar \
	"JmhSetBenchmarks.footprint$" \
	-f 1 -bm ss \
	-p producer=PURE_INTEGER \
	-p size=$PARAM_SIZE \
	-p valueFactoryFactory=$PARAM_VALUE_FACTORY \
	-prof io.usethesource.criterion.profiler.MemoryFootprintProfiler \
	-rf text -rff logStrawman-Memory-useCapsule-$1.txt \
	-jvmArgsAppend "-Dstrawman.collection.immutable.useCapsule=$1"
