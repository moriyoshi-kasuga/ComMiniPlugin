#!/bin/bash
google-java-format --replace $(find ./src -type f -name '*.java' -type f -not -path '*/dependencies/*')
