#!/bin/bash

TOP_DIR="$(cd "$(dirname "$0")"/../"" && pwd)"
cd $TOP_DIR/bin/ && java -classpath '.' org.ucx.jucx.tests.perftest.Perftest "$@"
