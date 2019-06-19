#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     June 2019

############################################### extract results ########################################################

printf "Extracting bandwidth and latency values\n"

NTHREADS=32

NSOURCE_MAX=10
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-1))
    for nsplit in $(seq 1 $((NSPLIT_MAX / 2)));
    do
        printf "extract from tests/output_60s/main_1-$nmatch-1-1_$RATE.log\n\n"

	    grep "Counter" tests/output_60s/main_$nsource-$nsplit-$nsplit-1_-1.log | awk  -F'[, ]' '{ print $20 }' >> tests/output_60s/bandwidth_$nsource.txt
    done
done