#!/bin/bash
cd /home/kavia/workspace/code-generation/simple-calculator-app-281360/calculator_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

