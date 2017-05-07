#!/bin/bash

TOP_DIR="$(cd "$(dirname "$0")"/../"" && pwd)"
cd $TOP_DIR && git push origin HEAD:master
