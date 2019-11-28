# Benchmarching Applications - Storm version

This repository contains a collection of Data Stream Processing applications implemented with [Apache Storm](http://storm.apache.org/). Applications can be run in local mode, further information can be found in each application README and in the [official documentation](https://storm.apache.org/releases/current/Local-mode.html). In order to improve performance, Storm's reliability mechanism has been deactivated by not tracking the tuple tree for spout tuples. Hence, tuples downstream in the topology are emitted as unanchored tuples, meaning that even if they are not acked they won't cause any spout tuples to fail. More details about Storm's guaranteed message processing can be found in the [official documentation](http://storm.apache.org/releases/current/Guaranteeing-message-processing.html).

This work is based upon [briskstream project](https://github.com/ShuhaoZhangTony/briskstream) and a [collection of real-time applications](https://github.com/mayconbordin/storm-applications).

Two more implementations of these applications have been provided:
* the first one using [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-release-1.7/), can be found in the [flink-applications](https://github.com/alefais/flink-applications) repository,
* the second one using [WindFlow](https://github.com/ParaGroup/WindFlow) C++17 library, can be found in the [windflow-applications](https://github.com/alefais/windflow-applications) repository.