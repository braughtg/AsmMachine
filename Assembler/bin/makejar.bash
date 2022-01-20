#!/bin/bash

# Create the .jar file for the Assembler program.

echo "Main-Class: Assembler" > manifest

jar cmf manifest Assembler.jar *.class
