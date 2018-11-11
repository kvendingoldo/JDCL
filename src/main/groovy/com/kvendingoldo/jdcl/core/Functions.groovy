package com.kvendingoldo.jdcl.core

static generateJobName(def jc) {
    return generateJobName(jc.job.folder, jc.job.baseName)
}

static generateJobName(String folder, String baseName) {
    String result = baseName

    if (folder != '') {
        result = folder + '/' + baseName
    }

    return result
}