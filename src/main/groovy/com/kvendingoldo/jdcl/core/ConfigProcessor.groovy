package com.kvendingoldo.jdcl.core

import com.kvendingoldo.jdcl.core.JenkinsLogger

import org.yaml.snakeyaml.*

/**
 * General class for process configuration files
 */

class ConfigProcessor {

    private def dslFactory
    private def logger
    private String importDirectory = 'configuration'
    private String type = 'JENKINS'

    ConfigProcessor(String type, def logger) {
        this.logger = logger
        this.type = type
    }

    def setImportDirectory(String importDirectory) {
        this.importDirectory = importDirectory
    }

    def setDslFactory(def dslFactory) {
        this.dslFactory = dslFactory
    }

    def processConfig(String jcPath) {
        def jc

        if (this.type == 'LOCAL') {
            jc = new Yaml().load(new File(jcPath).getText('UTF-8'))
        } else if (this.type == 'JENKINS') {
            jc = new Yaml().load(this.dslFactory.readFileFromWorkspace(jcPath))
        }

        if (jc.containsKey('imports')) {
            def jcChild = jc.findAll { key, _ -> !(key in ['imports']) }

            (jc.imports).each { jcImport ->
                def jcParent = processConfig("${this.importDirectory}/${jcImport}")
                if (jcChild.job) {
                    jcChild = merge(jcChild, jcParent)
                } else {
                    jcChild.findAll { key, _ -> !(key in ['job']) }.each { key, value ->
                        jcChild."${key}" = merge(jcChild."${key}", jcParent)
                    }
                }
            }
            return jcChild
        } else {
            return jc
        }
    }

    private def merge(def config, def object) {
        object?.each { key, value ->
            if (config."${key}" == null) {
                config."${key}" = value
            } else {
                if (!(config."${key}" instanceof String)) {
                    config."${key}" = merge(config."${key}", value)
                }
            }
        }
        return config
    }

    def isConfigValid(jc) {
        this.logger.printLog('DEBUG', "Validating ${jc.job.baseName} ...")
        if (jc.size() == 1) {
            this.logger.printLog('DEBUG', "${jc.job.baseName} is valid")
            return true
        } else {
            this.logger.printLog('ERROR', "${jc.job.baseName} is invalid and job won't be deployed")
            return false
        }
    }

    static def clone(def config) {
        def clonedConfig = [:]
        config.each { key, value ->
            clonedConfig."${key}" = value
        }
    }

    /**
     * This function is used to add some job properties to console output
     * @param jc job config object
     * @param out output stream
     * @return additional text for console output
     */
    static void prettyPrint(def jc, def out) {
        def jobName = Functions.generateJobName(jc)
        out.println """============================================================================
                       |Job Name: ${jc.job.baseName}
                       |Job Location: ${jobName}
                       |Job Type: ${jc.job.type}
                       |Job Class: ${jc.job.baseClassName}
                      |============================================================================
                    """.stripMargin().stripIndent()
    }
}
