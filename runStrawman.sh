java -jar ./target/benchmarks.jar \
	"JmhSetBenchmarks.time(Lookup$|Insert$|Remove$|Iteration|Equals|SubsetOf|(Union|Subtract|Intersect)RealDuplicate)$" \
	-f 1 -i 5 -wi 5 \
	-p producer=PURE_INTEGER \
	-p size="1048576" \
	-p valueFactoryFactory="VF_SCALA_STRAWMAN,VF_SCALA,VF_JAVASLANG" \
	-jvmArgsAppend "-Dstrawman.collection.immutable.$1"

##################################
# Further operations to benchmark:
##################################
# Size|HashCode|