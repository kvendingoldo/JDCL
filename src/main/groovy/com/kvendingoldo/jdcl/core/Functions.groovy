package com.kvendingoldo.jdcl.core

static getFolderName(def jc) {
  if (jc.job.folder!=null) {
    return jc.job.folder
  } else {
    return jc.job.classifier + '/' + jc.job.type
  }
}

static generateJobName(def jc) {
    return getFolderName(jc) + '/' + jc.job.baseName
}
