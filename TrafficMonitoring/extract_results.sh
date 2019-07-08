#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     July 2019

############################################### extract results ########################################################

printf "Extracting bandwidth and latency values\n"

NTHREADS=32

NSOURCE_MAX=1
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NMATCH_MAX=$((NTHREADS-3))
    RATE=$((nsource*1000))
    for nmatch in $(seq 1 $NMATCH_MAX);
    do
        printf "extract from tests/output_60s/main_1-$nmatch-1-1_$RATE.log\n\n"

	    grep "MapMatch" tests/output_60s/main_1-$nmatch-1-1_$RATE.log | awk  -F'[, ]' '{ print $17 }' >> tests/output_60s/bandwidth_$RATE-$nmatch.txt
    done
done

for nsource in $(seq 1 $NSOURCE_MAX);
do
    NMATCH_MAX=$((NTHREADS-3))
    RATE=$((nsource*1000))
    for nmatch in $(seq 1 $NMATCH_MAX);
    do
        cat tests/output_60s/bandwidth_$RATE-$nmatch.txt | awk '{ sum += $1 } END { print sum }' >> tests/output_60s/bandwidth.txt
    done
done