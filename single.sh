export BENCHMARKS="nl.cwi.swat.jmh_dscg_benchmarks.JmhMapBenchmarks.(timeContainsKey)$"
# export BENCHMARKS="nl.cwi.swat.jmh_dscg_benchmarks.JmhMapBenchmarks.(timeContainsKey|timeContainsKeyNotContained|timeInsert|timeInsertContained|timeRemoveKey|timeRemoveKeyNotContained|timeIteration|timeEntryIteration|timeEqualsRealDuplicate|timeEqualsDeltaDuplicate)$"

java -jar target/benchmarks.jar $BENCHMARKS \
	-p valueFactoryFactory=VF_CHAMP,VF_CHAMP_HETEROGENEOUS \
	-p size=1048576 \
	-jvm $GRAAL_HOME/bin/java \
	-jvmArgs "-Xms4g -Xmx4g" -wi 15 -i 15 -f 1 -t 1 -r 1 -p run=0 -p sampleDataSelection=MATCH -p producer=PDB_INTEGER -gc true -rf csv -v EXTRA -foe true -bm avgt -rff ./latest-results.csv
# 1>./latest-std-console.log 2>./latest-err-console.log