package com.kvendingoldo.jdcl.core

def configProcessor = new ConfigProcessor('DEBUG')
configProcessor.setImportDirectory('/Users/asharov/projects/gd/internal-jobdsl/configuration')
config = configProcessor.processConfig('/Users/asharov/projects/gd/internal-jobdsl/jobdsl/build/configuration/Mobility_Build_Backend_Custom.yml')
print config