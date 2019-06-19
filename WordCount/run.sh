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

NSOURCE_MAX=10
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-1))
    for nsplit in $(seq 1 $((NSPLIT_MAX / 2)));
    do
        printf "storm_test --nsource $nsource --nsplitter $nsplit --ncounter $nsplit --nsink 1 --rate -1\n\n"

        storm jar target/WordCount-1.0-SNAPSHOT-jar-with-dependencies.jar WordCount.WordCount data/books.dat $nsource $nsplit $nsplit 1 | tee tests/output_60s/main_$nsource-$nsplit-$nsplit-1_-1.log
    done
done
