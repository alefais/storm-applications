#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     July 2019

############################################### extract results ########################################################

printf "Extracting bandwidth and latency values for FraudDetection application\n"

NTHREADS=32
NSOURCE_MAX=4
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NPRED_MAX=$((NTHREADS-nsource-1))
    for npred in $(seq 1 $NPRED_MAX);
    do
        printf "extract from tests/output_60s/main_$nsource-$npred-1_-1.log\n\n"

        # bandwidth
	    grep "Predictor" tests/output_60s/main_$nsource-$npred-1_-1.log | awk  -F'[, ]' '{ print $20 }' >> tests/output_60s/bandwidth_$nsource-$npred.txt

        # latency
	    grep "Sink" tests/output_60s/main_$nsource-$npred-1_-1.log | awk  -F'[, ]' 'FNR == 3 { print $10 " " $12 " " $14 " " $16 " " $18 " " $20 " " $22 " " $24 }' >> tests/output_60s/latency.txt
        grep "Sink" tests/output_60s/main_$nsource-$npred-1_-1.log | awk  -F'[, ]' 'FNR == 3 { print $10 }' >> tests/output_60s/latency_mean.txt
    done
done

for nsource in $(seq 1 $NSOURCE_MAX);
do
    NPRED_MAX=$((NTHREADS-nsource-1))
    for npred in $(seq 1 $NPRED_MAX);
    do
        cat tests/output_60s/bandwidth_$nsource-$npred.txt | awk '{ sum += $1 } END { print sum }' >> tests/output_60s/bandwidth.txt
    done
done