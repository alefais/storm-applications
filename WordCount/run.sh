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

printf "Running Storm tests for WordCount application\n"

NTHREADS=32
NSOURCE_MAX=4
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NSPLIT_MAX=$((NTHREADS-nsource-nsource-1))
    for nsplit in $(seq 1 $NSPLIT_MAX);
    do
        printf "storm_wordcount --nsource $nsource --nsplitter $nsplit --ncounter $nsource --nsink 1 --rate 10000\n\n"

        storm jar target/WordCount-1.0-SNAPSHOT-jar-with-dependencies.jar WordCount.WordCount file data/book.dat $nsource $nsource $nsplit 1 10000 > tests/output_60s/main_$nsource-$nsplit-$nsource-1_10000.log
    done
done
