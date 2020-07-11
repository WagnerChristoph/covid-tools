#!/bin/bash

cd ../download/src/main/protobuf || exit
protoc --java_out=../java/ *.proto