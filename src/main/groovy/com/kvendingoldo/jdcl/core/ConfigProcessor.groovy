package com.kvendingoldo.jdcl.core

import org.yaml.snakeyaml.*

/**
 * General class for process configuration files
 *
 * If you want try to run it locally, you should replace
 * this.dslFactory.readFileFromWorkspace(jcPath) to
 * new File(jcPath).getText('UTF-8')
 *
 */


class ConfigProcessor {

    private static String importDirectory
    private def dslFactory

    public ConfigProcessor() {
        this.importDirectory = 'configuration'
    }

    public ConfigProcessor(dslFactory) {
        this.dslFactory = dslFactory
        this.importDirectory = 'configuration'
    }

    public def processConfig(String jcPath) {
        def jc = new Yaml().load(this.dslFactory.readFileFromWorkspace(jcPath))
        if (jc.containsKey('imports')) {
            def jcChild = jc.findAll {key, _ -> !(key in ['imports'])}

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

    private def validate(def config) {
        // TODO
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
