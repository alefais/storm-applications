#!/bin/bash

# @author Alessandra Fais

if [ -d src/main/java/FraudDetectionOriginal/ ]; then
	echo "removing FraudDetectionOriginal"
	rm -rf src/main/java/FraudDetectionOriginal/
fi

echo "compiling..."
mvn clean install

if [ ! -d logs ]; then
	mkdir logs
fi

#### Tests for bandwidth ###

echo "preparing to execute tests for bandwidth 1-n-1 with n in {1, 2, 4, 8, 16, 24}"
if [ ! -d logs/tests-1-n-1 ]; then
	mkdir logs/tests-1-n-1
fi
for nb in 1 2 4 8 16 24
do
	if [ ! -f logs/tests-1-n-1/output_fd-1-$nb-1.log ]; then
		echo "test-1-$nb-1"
		storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 1 $nb 1 > logs/tests-1-n-1/output_fd-1-$nb-1.log
	fi
done

echo "preparing to execute tests for bandwidth 1-n-n with n in {1, 2, 3, ..., 8}"
if [ ! -d logs/tests-1-n-n ]; then
	mkdir logs/tests-1-n-n
fi
for n in 1 2 3 4 5 6 7 8
do
	if [ ! -f logs/tests-1-n-n/output_fd-1-$n-$n.log ]; then
		echo "test-1-$n-$n"
		storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 1 $n $n > logs/tests-1-n-n/output_fd-1-$n-$n.log
	fi
done

echo "preparing to execute tests for bandwidth n-n-n with n in {1, 2, 3, ..., 8}"
if [ ! -d logs/tests-n-n-n ]; then
	mkdir logs/tests-n-n-n
fi
for m in 1 2 3 4 5 6 7 8
do
	if [ ! -f logs/tests-n-n-n/output_fd-$m-$m-$m.log ]; then
		echo "test-$m-$m-$m"
		storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat $m $m $m > logs/tests-n-n-n/output_fd-$m-$m-$m.log
	fi
done

### Tests for latency ###



### Aggregate results ###

echo "preparing to compute aggregates..."
if [ ! -d aggregates ]; then
	mkdir aggregates
fi

### Bandwidth aggregates ###

echo "computing aggregates from logs/tests-1-n-1"
if [ ! -d aggregates/tests-1-n-1 ]; then
	mkdir aggregates/tests-1-n-1
fi
for i in 1 2 4 8 16 24 # bolt parallelism degree
do
	if [ -f logs/tests-1-n-1/output_fd-1-$i-1.log ]; then

		# prints: number of generated tuples, number of emitted tuples, spout service time, generation rate
		grep "FileParserSpout" logs/tests-1-n-1/output_fd-1-$i-1.log | awk '{ if (NR == 8) print $8 "," $14 "," $17 "," $22 }' > aggregates/tests-1-n-1/FileParserSpout-1-$i-1.csv

		skip=$( expr 2 '*' "$i" )
		echo "$skip"
		# prints: number of processed tuples, bolt service time, number of outliers, source bandwidth
		grep "FraudPredictorBolt" logs/tests-1-n-1/output_fd-1-$i-1.log | awk '{ if (NR > $skip) print $8 "," $11 "," $14 "," $19 }' > aggregates/tests-1-n-1/FraudPredictorBolt-1-$i-1.csv

		# prints: number of processed tuples, sink service time, sink bandwidth
		grep "ConsoleSink" logs/tests-1-n-1/output_fd-1-$i-1.log | awk '{ if (NR == 2) print $8 "," $12 "," $16 }' > aggregates/tests-1-n-1/ConsoleSink-1-$i-1.csv
	fi
done

echo "computing aggregates from logs/tests-1-n-n"
if [ ! -d aggregates/tests-1-n-n ]; then
	mkdir aggregates/tests-1-n-n
fi
for j in 1 2 3 4 5 6 7 8 # bolt and sink parallelism degree
do
	if [ -f logs/tests-1-n-n/output_fd-1-$j-$j.log ]; then

		# prints: number of generated tuples, number of emitted tuples, spout service time, generation rate
		grep "FileParserSpout" logs/tests-1-n-n/output_fd-1-$j-$j.log | awk '{ if (NR == 8) print $8 "," $14 "," $17 "," $22 }' > aggregates/tests-1-n-n/FileParserSpout-1-$j-$j.csv

		skip=$( expr 2 '*' "$j" )
		echo "$skip"
		# prints: number of processed tuples, bolt service time, number of outliers, source bandwidth
		grep "FraudPredictorBolt" logs/tests-1-n-n/output_fd-1-$j-$j.log | awk '{ if (NR > $skip) print $8 "," $11 "," $14 "," $19 }' > aggregates/tests-1-n-n/FraudPredictorBolt-1-$j-$j.csv

		# prints: number of processed tuples, sink service time, sink bandwidth
		grep "ConsoleSink" logs/tests-1-n-n/output_fd-1-$j-$j.log | awk '{ if (NR == 2) print $8 "," $12 "," $16 }' > aggregates/tests-1-n-n/ConsoleSink-1-$j-$j.csv
	fi
done

echo "computing aggregates from logs/tests-n-n-n"
if [ ! -d aggregates/tests-n-n-n ]; then
	mkdir aggregates/tests-n-n-n
fi
for k in 1 2 3 4 5 6 7 8 # source, bolt and sink parallelism degree
do
	if [ -f logs/tests-n-n-n/output_fd-$k-$k-$k.log ]; then

		# prints: number of generated tuples, number of emitted tuples, spout service time, generation rate
		grep "FileParserSpout" logs/tests-n-n-n/output_fd-$k-$k-$k.log | awk '{ if (NR == 8) print $8 "," $14 "," $17 "," $22 }' > aggregates/tests-n-n-n/FileParserSpout-$k-$k-$k.csv

		skip=$( expr 2 '*' "$j" )
		echo "$skip"
		# prints: number of processed tuples, bolt service time, number of outliers, source bandwidth
		grep "FraudPredictorBolt" logs/tests-n-n-n/output_fd-$k-$k-$k.log | awk '{ if (NR > $skip) print $8 "," $11 "," $14 "," $19 }' > aggregates/tests-n-n-n/FraudPredictorBolt-$k-$k-$k.csv

		# prints: number of processed tuples, sink service time, sink bandwidth
		grep "ConsoleSink" logs/tests-n-n-n/output_fd-$k-$k-$k.log | awk '{ if (NR == 2) print $8 "," $12 "," $16 }' > aggregates/tests-n-n-n/ConsoleSink-$k-$k-$k.csv
	fi
done

#grep "ConsoleSink" logs/output_fd-1-$i-$j.log | awk '{ if (NR == 3) print $9 }' >> aggregates/ConsoleSink-1-$i-$j.csv