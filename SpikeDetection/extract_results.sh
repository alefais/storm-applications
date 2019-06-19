#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     June 2019

############################################### extract results ########################################################

printf "Extracting bandwidth and latency values for SpikeDetection\n"

NTHREADS=32

NSOURCE_MAX=8
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NAVG_MAX=$((NTHREADS-nsource-1))
    for navg in $(seq 1 $NAVG_MAX);
    do
        printf "extract from tests/output_60s/main_$nsource-$navg-1-1_-1.log\n\n"

	    grep "Average" tests/output_60s/main_$nsource-$navg-1-1_-1.log | awk  -F'[, ]' '{ print $17 }' >> tests/output_60s/bandwidth_$nsource.txt
    done
done