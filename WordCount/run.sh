#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     July 2019

############################################## create test directories #################################################

if [ ! -d tests ]; then
    mkdir tests
fi
if [ ! -d tests/output_60s ]; then
    mkdir tests/output_60s
fi

#################################################### run tests #########################################################

printf "Running Storm tests with rate 100000\n"

NCORES=16
NTHREADS=32

NSOURCE_MAX=4
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-nsource-1))
    for nsplit in $(seq 1 $NSPLIT_MAX);
    do
        printf "storm_test --nsource $nsource --nsplitter $nsource --ncounter $nsplit --nsink 1 --rate 100000\n\n"

        timeout 10m storm jar target/WordCount-1.0-SNAPSHOT-jar-with-dependencies.jar WordCount.WordCount data/books.dat $nsource $nsource $nsplit 1 100000 > tests/output_60s/main_$nsource-$nsource-$nsplit-1_100000.log
    done
done
