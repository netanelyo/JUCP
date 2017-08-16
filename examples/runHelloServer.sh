#!/bin/bash

TOP_DIR="$(cd "$(dirname "$0")"/../"" && pwd)"
cd $TOP_DIR/bin/ && java org.ucx.jucx.examples.helloworld.HelloServer "$@"
