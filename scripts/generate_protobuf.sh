#!/bin/bash

cd ../src/main/protobuf || exit
protoc --java_out=../java/ *.proto