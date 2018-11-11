package com.kvendingoldo.jdcl.core

import com.kvendingoldo.jdcl.core.ConfigProcessor
import com.kvendingoldo.jdcl.core.JenkinsLogger

import hudson.FilePath
import hudson.model.Executor

import org.yaml.snakeyaml.*


class JobGenerator {

    private def dslFactory
    private def logger
    private def configProcessor
    private String classifier

    JobGenerator(dslFactory, String logLevel, String classifier) {
        this.dslFactory = dslFactory
        this.classifier = classifier
        this.logger = new JenkinsLogger(logLevel, this.dslFactory.out)
        this.configProcessor = new ConfigProcessor(this.dslFactory, this.logger)
    }

    def loadBuildClass(def jc) {
        return this.dslFactory.class.classLoader.parseClass(dslFactory.readFileFromWorkspace("jobdsl/${jc.job.type}/jobs/${jc.job.baseClassName}.groovy"))?.newInstance()
    }

    def generate() {
        String configText = this.dslFactory.readFileFromWorkspace("configuration/${this.classifier}/job-generator.yml")
        (new Yaml().load(configText)).jobTypes.each { folder ->
            generate(folder)
        }
    }

    def generate(String type) {
        def configs = new FilePath(Executor.currentExecutor().getCurrentWorkspace(), "jobdsl/${type}/configuration")

        configs.list().each { config ->
            this.logger.printLog('INFO', "\nReading: ${config}")

            this.configProcessor.processConfig(config.toString()).each { jobName, jc ->

                this.logger.printLog('INFO', "Processing ${jobName}...")
                this.logger.printLog('DEBUG', "Config representation (${jobName}): ${jc}")

                if (jc.job.folder != '') {
                    def list = jc.job.folder.split('/').toList()
                    def folderName = "${list[0]}"
                    this.dslFactory.folder(folderName)

                    for (String item : list.drop(1)) {
                        folderName = folderName + "/" + item
                        this.dslFactory.folder(folderName)
                    }
                }

                if ((jc.job).containsKey('classifier')) {
                    if (jc.job.classifier == this.classifier) {
                        def createJobs = { jcLocal ->
                            if (this.configProcessor.isConfigValid(jcLocal)) {
                                this.configProcessor.prettyPrint(jcLocal, this.dslFactory.out)
                                def jobClass = loadBuildClass(jcLocal)
                                jobClass.job(this.dslFactory, jcLocal)

                            }
                        }
                        createJobs(jc)
                    } else {
                        this.logger.printLog('INFO', "Job ${jobName} was skipped by classifier")
                        this.logger.printLog('DEBUG', "Seed classifier=${this.classifier}, job classifier=${jc.job.classifier}\n")
                    }
                } else {
                    this.logger.printLog('WARN', "Key classifier does not exist for ${jobName}\n")
                }
            }
        }
    }
}
