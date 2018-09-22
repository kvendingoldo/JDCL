package com.kvendingoldo.jdcl.core

import com.kvendingoldo.jdcl.core.ConfigProcessor
import com.kvendingoldo.jdcl.core.JenkinsLogger

import hudson.FilePath
import hudson.model.*

import org.yaml.snakeyaml.*


class JobGenerator {
    def dslFactory
    def workspace
    def logger

    public JobGenerator(dslFactory) {
        this.dslFactory = dslFactory
        this.workspace = Executor.currentExecutor().getCurrentWorkspace()
        this.logger = new JenkinsLogger('INFO', this.dslFactory.out)

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
            logger.printLog "Reading: ${config}"
            configProcessor.processConfig(config.toString()).each { jobName, jc ->
                if (jc.job.folder != '') {

                    def build = Thread.currentThread().executable
                    def currentBranch = build.buildVariableResolver.resolve('BRANCH')
                    def finalFolder =  jc.job.folder

                    if (currentBranch != jc.jenkins.mainJobDslBranch) {
                        finalFolder = "${currentBranch}/${finalFolder}"
                    }

                    def list = finalFolder.split("/").toList()
                    def folderName = "${list[0]}"
                    dslFactory.folder(folderName)

                    for (String item : list.drop(1)) {
                        folderName = folderName + "/" + item
                        dslFactory.folder(folderName)
                    }
                }

                def createJobs = { jcLocal ->
                    logger.printLog "Processing..."
                    ConfigProcessor.prettyPrint(jcLocal, this.dslFactory.out)
                    def jobClass = loadBuildClass(jcLocal)
                    jobClass.job(dslFactory, jcLocal)
                }
                createJobs(jc)
            }
        }
    }
}
