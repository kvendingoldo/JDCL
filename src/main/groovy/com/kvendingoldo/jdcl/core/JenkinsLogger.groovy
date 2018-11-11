package com.kvendingoldo.jdcl.core

/**
 * General class for Jenkins logging.
 * We use this class to send formatted and colored messages
 * to Console Output of Jenkins builds.
 */

class JenkinsLogger {
    private def out
    private String logLevel

    private def colors = ['ERROR': "\033[31m", 'WARN': "\033[33m", 'INFO': "\033[32m", 'DEBUG': "\033[34m"]

    /**
     * This is construction method for JenkinsLogger class.
     * @param logLevel logging level. Available choices: ERROR, WARN, INFO, DEBUG
     * With DEBUG level you will be able to print all logging's levels
     * With INFO level you will be able to print ERROR,WARN and INFO levels
     * With WARN level you will be able to print ERROR and WARN levels
     * With ERROR level you will be able to print only ERROR level
     * @param out it's a PrintStream which had println method
     */
    JenkinsLogger(logLevel, out) {
        this.logLevel = logLevel.toUpperCase()
        this.out = out
        if (!colors[logLevel])
            throw new IllegalArgumentException("### Invalid logging level, aborting! ###")
    }

    /**
     * Gets message type, text and text's color as string parameters
     * and returns it as a concatenated string.
     * @param type message type: DEBUG, INFO, WARN, ERROR
     * @param text message text
     * @param color message color
     * @param timestamp timestamp
     * @return concatenated message with attributes as string
     */
    String getLogColored(String type, String text, String color, timestamp = new Date().toTimestamp()) {
        "${color}[${timestamp}][${type}] ${text}\033[0m"
    }

    /**
     * Gets message's type, text and message's level as string parameters,
     * generates color and calls getLogColored then returns formatted log
     *
     * If current log level is higher than provided in JenkinsLogger then
     * message will not be displayed.
     * @param type message type: DEBUG, INFO, WARN, ERROR
     * @param text message text
     * @return formatted log
     */
    def getLog(String type = 'INFO', def text) {
        type = type.toUpperCase()
        text = text.toString()

        if (!colors[type])
            throw new IllegalArgumentException("### Invalid logging level, aborting! ###")

        if (colors.find({ it.key in [type, logLevel] }).key == type)
            return getLogColored(type, text, colors[type])
        return ''
    }

    /**
     * Gets the log string and prints it in build's console output
     * @return log string
     */
    def printLog(Object... args) { out.println getLog( * args ) }

    /**
     * Gets the log string and prints it colored in build's console output
     * @return log string
     */
    def printColored(Object... args) { out.println getLogColored( * args ) }
}