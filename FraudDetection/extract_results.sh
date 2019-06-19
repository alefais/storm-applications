#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     June 2019

############################################### extract results ########################################################

printf "Extracting bandwidth and latency values\n"

NTHREADS=32

NSOURCE_MAX=5
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NPRED_MAX=$((NTHREADS-nsource-1))
    for npred in $(seq 1 $NPRED_MAX);
    do
        printf "extract from tests/output_60s/main_$nsource-$npred-1_-1.log\n\n"

        grep "Source" tests/output_60s/main_$nsource-$npred-1_-1.log | awk  -F'[, ]' '{ print $20 }' >> tests/output_60s/bandwidth.txt
	    grep "Predictor" tests/output_60s/main_$nsource-$npred-1_-1.log | awk  -F'[, ]' '{ print $20 }' >> tests/output_60s/latency.txt
    done
done