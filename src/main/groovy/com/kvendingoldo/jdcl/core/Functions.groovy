package com.kvendingoldo.jdcl.core

static generateJobName(def jc) {
    return generateJobName(jc.job.folder, jc.job.baseName)
}

static generateJobName(String folder, String baseName, String classifier = null) {
    String result = baseName
    if (folder != '') {
        result = folder + '/' + baseName
    }
    if (classifier != null && classifier != '') {
        result = "${result}_${classifier}"
    }
    return result
}
