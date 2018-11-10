package com.kvendingoldo.jdcl.core

import com.kvendingoldo.jdcl.core.ConfigProcessor
import com.kvendingoldo.jdcl.core.JenkinsLogger

import hudson.FilePath
import hudson.model.Executor

import org.yaml.snakeyaml.*


class JobGenerator {
    def dslFactory
    def classifier
    def workspace
    def loggerInfo
    def loggerDebug
    def loggerWarn
    def loggerErr

    JobGenerator() {}

    JobGenerator(dslFactory, classifier) {
        this.dslFactory = dslFactory
        this.classifier = classifier
        this.workspace = Executor.currentExecutor().getCurrentWorkspace()

        this.loggerInfo = new JenkinsLogger('INFO', this.dslFactory.out)
        //this.loggerInfo.printLog "test"


        this.loggerDebug = new JenkinsLogger('DEBUG', this.dslFactory.out)
        //this.loggerDebug.printLog "test"

        this.loggerWarn = new JenkinsLogger('WARN', this.dslFactory.out)
        //this.loggerWarn.printLog "test"

        this.loggerErr = new JenkinsLogger('ERROR', this.dslFactory.out)
        //this.loggerErr.printLog "test"

    }

    def generate() {
        String configText = this.dslFactory.readFileFromWorkspace("configuration/${this.classifier}/job-generator.yml")
        (new Yaml().load(configText)).jobTypes.each { folder ->
            generate(folder)
        }
    }

    def loadBuildClass(def bc) {
        return this.dslFactory.class.classLoader.parseClass(dslFactory.readFileFromWorkspace("jobdsl/${bc.job.type}/jobs/${bc.job.baseClassName}.groovy"))?.newInstance()
    }

    def generate(type) {
        def configs = new FilePath(workspace, "jobdsl/${type}/configuration")
        def configProcessor = new ConfigProcessor(this.dslFactory)

        configs.list().each { config ->
            this.loggerInfo.printLog "Reading: ${config}"

            configProcessor.processConfig(config.toString()).each { jobName, jc ->
                if (jc.job.folder != '') {
                    def list = jc.job.folder.split("/").toList()
                    def folderName = "${list[0]}"
                    this.dslFactory.folder(folderName)

                    for (String item : list.drop(1)) {
                        folderName = folderName + "/" + item
                        this.dslFactory.folder(folderName)
                    }
                }

                this.loggerDebug.printLog "Current config: ${jc}"

                if ((jc.job).containsKey('classifier')) {
                    if (jc.job.classifier == this.classifier) {
                        def createJobs = { jcLocal ->
                            this.loggerInfo.printLog "Processing..."
                            ConfigProcessor.prettyPrint(jcLocal, this.dslFactory.out)
                            def jobClass = loadBuildClass(jcLocal)
                            jobClass.job(this.dslFactory, jcLocal)
                        }
                        createJobs(jc)
                    } else {
                        this.loggerWarn.printLog "Skip..."
                    }
                } else {
                    this.loggerErr.printLog "Key classifier does not exist in the jc"
                }

            }
        }
    }
}
