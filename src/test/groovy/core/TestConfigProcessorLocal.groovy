package com.kvendingoldo.jdcl.core

import com.kvendingoldo.jdcl.core.JenkinsLogger


def logger = new JenkinsLogger('DEBUG', System.out)
def configProcessor = new ConfigProcessor('LOCAL', logger)
configProcessor.setImportDirectory('/Users/asharov/projects/gd/internal-jobdsl/configuration')
config = configProcessor.processConfig('/Users/asharov/projects/gd/internal-jobdsl/configuration/mobility/jobs/precommit.yml')
print config
