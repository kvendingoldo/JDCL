package com.kvendingoldo.jdcl.core

import org.yaml.snakeyaml.*

class ConfigProcessor {

    private static String importDirectory
    private def dslFactory

    public ConfigProcessor(dslFactory) {
        this.dslFactory = dslFactory
        this.importDirectory = 'configuration'
    }

    private def getCleanConfig(def config) {
        return config.findAll { key, _ -> !(key in ['imports']) }
    }

    public def processConfig(String path) {
        String configText = this.dslFactory.readFileFromWorkspace(path)
        def imports = null
        def config = new Yaml().load(configText)

        if (config.imports) {
            imports = config.imports
        }

        config.findAll {
            key, _ -> !(key in ['imports'])
        }.each { key, value ->
            if (value.imports) {
                def customImports = value.imports
                value = applyImports(getCleanConfig(value), imports)
                config."${key}" = applyImports(value, customImports)
            } else {
                config."${key}" = applyImports(value, imports)
            }
        }

        return getCleanConfig(config)
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

    private def applyImports(def config, def imports) {
        if (imports) {
            imports.each { importObject ->
                String importText = this.dslFactory.readFileFromWorkspace("${importDirectory}/${importObject}")
                merge(config, new Yaml().load(importText))
            }
        }
        return config
    }

    private def validate(def config) {
        // TODO
    }

    static def getConfigObject(def config) {
        def сonfigObject = new ConfigObject()
        сonfigObject.putAll(config)
        return сonfigObject
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
