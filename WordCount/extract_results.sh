#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     July 2019

############################################### extract results ########################################################

printf "Extracting bandwidth and latency values\n"

NTHREADS=32

NSOURCE_MAX=4
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-nsource-1))
    for nsplit in $(seq 1 $NSPLIT_MAX);
    do
        printf "extract from tests/output_60s/main_$nsource-$nsource-$nsplit-1_100000.log\n\n"

	    grep "Counter" tests/output_60s/main_$nsource-$nsource-$nsplit-1_100000.log | awk  -F'[, ]' '{ print $20 }' >> tests/output_60s/bandwidth_$nsource-$nsplit.txt
    done
done

for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-nsource-1))
    for nsplit in $(seq 1 $NSPLIT_MAX);
    do
        cat tests/output_60s/bandwidth_$nsource-$nsplit.txt | awk '{ sum += $1 } END { print sum }' >> tests/output_60s/bandwidth.txt
    done
done