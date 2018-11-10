package com.kvendingoldo.jdcl.core

String importDir = '/Users/asharov/projects/gd/internal-jobdsl/configuration'

def configProcessor = new ConfigProcessorLocal(importDir)

config = "/Users/asharov/projects/gd/internal-jobdsl/jobdsl/build/configuration/Mobility_Build_Backend_Custom.yml"

print(configProcessor.processConfig(config.toString()))