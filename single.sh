export BENCHMARKS="nl.cwi.swat.jmh_dscg_benchmarks.JmhMapBenchmarks.(timeInsert)$"
# export BENCHMARKS="nl.cwi.swat.jmh_dscg_benchmarks.JmhMapBenchmarks.(timeContainsKey|timeContainsKeyNotContained|timeInsert|timeInsertContained|timeRemoveKey|timeRemoveKeyNotContained|timeIteration|timeEntryIteration|timeEqualsRealDuplicate|timeEqualsDeltaDuplicate)$"

# export JVM_HOME=/Library/Java/JavaVirtualMachines/jdk1.9.0.jdk/Contents/Home
export JVM_HOME=$GRAAL_HOME

$JVM_HOME/bin/java -jar target/benchmarks.jar $BENCHMARKS \
	-p valueFactoryFactory=VF_CHAMP_HETEROGENEOUS \
	-p size=16,2048,1048576 \
	-jvm $JVM_HOME/bin/java \
	-jvmArgs "-Xms4g -Xmx4g" -wi 5 -i 10 -f 1 -t 1 -r 1 -p run=0 -p sampleDataSelection=MATCH -p producer=PDB_INTEGER -gc true -rf csv -v EXTRA -foe true -bm avgt -rff ./latest-results.csv
# 1>./latest-std-console.log 2>./latest-err-console.log