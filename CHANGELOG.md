## 1.3.0 (May 15, 2019)

IMPROVEMENTS:

* Add `getFolderName` function
  * At this moment if jc.job.folder will be empty folder will be represented by `jc.job.classifier + / + jc.job.type` value.
* Rewrite `generateJobName` function
  * Adopt to the new  `getFolderName` function

BUG FIXES:
* Fix issue in case if `job: {}` in configuration file. Now merge function works fine.