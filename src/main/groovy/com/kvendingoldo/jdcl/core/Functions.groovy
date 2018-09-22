package com.kvendingoldo.jdcl.core

static generateJobName(def jc) {
    return generateJobName(jc.jenkins.mainJobDslBranch, jc.job.folder, jc.job.baseName)
}

static generateJobName(String branch, String folder, String baseName) {
    String result = baseName
    if (folder != '') {
        def build = Thread.currentThread().executable
        def currentBranch = build.buildVariableResolver.resolve('BRANCH')

        if (currentBranch != branch) {
            folder = "${currentBranch}/${folder}"
        }

        result = folder + '/' + baseName
    }
    return result
}
