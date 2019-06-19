#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     18/06/2019

############################################## create test directories #################################################

if [ ! -d tests ]; then
    mkdir tests
fi
if [ ! -d tests/output_60s ]; then
    mkdir tests/output_60s
fi

#################################################### run tests #########################################################

printf "Running Storm tests with rate -1\n"

NCORES=16
NTHREADS=32

NSOURCE_MAX=8
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NDET_MAX=$((NTHREADS-nsource-nsource-1))
    for ndet in $(seq 1 $NDET_MAX);
    do
        printf "storm_test --nsource $nsource --naverage $ndet --ndet 1 --nsink 1 --rate -1\n\n"

        storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat $nsource $ndet 1 1 | tee tests/output_60s/main_$nsource-$ndet-1-1_-1.log
    done
done
