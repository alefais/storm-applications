#!/bin/bash

# @author Alessandra Fais

if [ -d src/main/java/FraudDetectionOriginal/ ]; then
	echo "removing FraudDetectionOriginal"
	rm -rf src/main/java/FraudDetectionOriginal/
fi

echo "compiling..."
mvn clean install

echo "preparing to execute tests..."
if [ ! -d logs ]; then
	mkdir logs
fi
for n_sink in 1 #2 4 8 16 24
do
	for n_bolt in 1 #2 4 8 16 24
	do
		if [ ! -f logs/output_fd-1-$n_bolt-$n_sink.log ]; then
			echo "test-1-$n_bolt-$n_sink"
			storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat $n_bolt $n_sink > logs/output_fd-1-$n_bolt-$n_sink.log
		fi
	done
done

echo "preparing to compute aggregates..."
if [ ! -d aggregates ]; then
	mkdir aggregates
fi
j=1 # sink parallelism degree
for i in 1 2 4 8 16 24 # bolt parallelism degree
do
	echo "computing aggregates..."

	if [ -f logs/output_fd-1-$i-$j.log ]; then

		# prints: number of generated tuples, spout service time, generation rate
		grep "FileParserSpout" logs/output_fd-1-$i-$j.log | awk '{ if (NR == 8) print $8 "," $11 "," $16 }' > aggregates/FileParserSpout-1-$i-$j.csv

		# prints: number of processed tuples, bolt service time, #outliers, source bandwidth
		grep "FraudPredictorBolt" logs/output_fd-1-$i-$j.log | awk '{ if (NR == 3) print $8 "," $11 "," $14 "," $19 }' > aggregates/FraudPredictorBolt-1-$i-$j.csv

		# prints: number of processed tuples, sink service time, sink bandwidth, average latency
		grep "ConsoleSink" logs/output_fd-1-$i-$j.log | awk '{ if (NR == 2) print $8 "," $12 "," $16 }' > aggregates/ConsoleSink-1-$i-$j.csv
		grep "ConsoleSink" logs/output_fd-1-$i-$j.log | awk '{ if (NR == 3) print $9 }' >> aggregates/ConsoleSink-1-$i-$j.csv
	fi
done 
