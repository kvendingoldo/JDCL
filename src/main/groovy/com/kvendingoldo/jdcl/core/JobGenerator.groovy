package com.kvendingoldo.jdcl.core

import com.kvendingoldo.jdcl.core.ConfigProcessor

import hudson.FilePath
import hudson.model.Executor

import org.yaml.snakeyaml.*
import groovy.util.*;


class JobGenerator {
    def dslFactory
    def workspace

    public JobGenerator(dslFactory) {
        this.dslFactory = dslFactory
        this.workspace = Executor.currentExecutor().getCurrentWorkspace()
    }

    def generate() {
        String configText = this.dslFactory.readFileFromWorkspace('configuration/job-generator.yml')
        (new Yaml().load(configText)).jobTypes.each { folder ->
            generate(folder)
        }
    }

    def loadBuildClass(def bc) {
        return dslFactory.class.classLoader.parseClass(dslFactory.readFileFromWorkspace("jobdsl/${bc.job.type}/jobs/${bc.job.baseClassName}.groovy"))?.newInstance()
    }

    def generate(type) {
        def configs = new FilePath(workspace, "jobdsl/${type}/configuration")
        def configProcessor = new ConfigProcessor(dslFactory)

        configs.list().each { config ->
            configProcessor.processConfig(config.toString()).each { jobName, jc ->
                if (jc.job.folder != '') {
                    def list = jc.job.folder.split("/").toList()
                    def folderName = "${list[0]}"
                    dslFactory.folder(folderName)

                    for (String item : list.drop(1)) {
                        folderName = folderName + "/" + item
                        dslFactory.folder(folderName)
                    }
                }

                def jobClass = loadBuildClass(jc)
                jobClass.job(dslFactory, jc)
            }
        }
    }
}
