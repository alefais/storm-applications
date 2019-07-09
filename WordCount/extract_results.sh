#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     July 2019

############################################### extract results ########################################################

printf "Extracting bandwidth and latency values for WordCount application\n"

NTHREADS=32
NSOURCE_MAX=4
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-nsource-1))
    for nsplit in $(seq 1 $NSPLIT_MAX);
    do
        printf "extract from tests/output_60s/main_$nsource-$nsplit-$nsource-1_10000.log\n\n"

	    # bandwidth
	    grep "Sink" tests/output_60s/main_$nsource-$nsplit-$nsource-1_10000.log | awk  -F'[, ]' '{ print $15 }' >> tests/output_60s/bandwidth_words_$nsource-$nsplit.txt
	    grep "Sink" tests/output_60s/main_$nsource-$nsplit-$nsource-1_10000.log | awk  -F'[, ]' '{ print $17 }' >> tests/output_60s/bandwidth_MB_$nsource-$nsplit.txt

        # latency
	    grep "Sink" tests/output_60s/main_$nsource-$nsplit-$nsource-1_10000.log | awk  -F'[, ]' '{ print $23 }' >> tests/output_60s/latency_$nsource-$nsplit.txt
    done
done

for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-nsource-1))
    for nsplit in $(seq 1 $NSPLIT_MAX);
    do
        cat tests/output_60s/bandwidth_words_$nsource-$nsplit.txt | awk '{ sum += $1 } END { print sum }' >> tests/output_60s/bandwidth_words.txt
        cat tests/output_60s/bandwidth_MB_$nsource-$nsplit.txt | awk '{ sum += $1 } END { print sum }' >> tests/output_60s/bandwidth_MB.txt
    done
done