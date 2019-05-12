#!/usr/bin/env bash

# Storm tests bandwidth

storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 1 1 1 | tee ./output_fd60s/main_111-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 1 2 1 | tee ./output_fd60s/main_121-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 1 4 1 | tee ./output_fd60s/main_141-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 1 8 1 | tee ./output_fd60s/main_181-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 1 14 1 | tee ./output_fd60s/main_1141-1.log

storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 2 4 1 | tee ./output_fd60s/main_241-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 2 8 1 | tee ./output_fd60s/main_281-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 2 13 1 | tee ./output_fd60s/main_2131-1.log

storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 4 8 1 | tee ./output_fd60s/main_481-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 4 11 1 | tee ./output_fd60s/main_4111-1.log
storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 5 10 1 | tee ./output_fd60s/main_5101-1.log

#storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 6 10 1 | tee ./output_fd60s/main_6101-1.log
#storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 8 15 1 | tee ./output_fd60s/main_8151-1.log
#storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 11 20 1 | tee ./output_fd60s/main_11201-1.log
#storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 12 19 1 | tee ./output_fd60s/main_12191-1.log
#storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 13 18 1 | tee ./output_fd60s/main_13181-1.log
#storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection ../../data/app/fd/credit-card.dat 14 17 1 | tee ./output_fd60s/main_14171-1.log