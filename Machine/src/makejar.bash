#!/bin/bash

# Create the .jar file for the Assembler program.

echo "Main-Class: Machine" > manifest

jar cmf manifest Machine.jar *.class
