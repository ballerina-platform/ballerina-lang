define(['require', 'log4javascript'], function (require, log4javascript) {
    var logger = log4javascript.getLogger("client-logger");
    var ajaxAppender = new log4javascript.AjaxAppender(workspaceServiceURL + "/log");
    var consoleAppender = new log4javascript.BrowserConsoleAppender();
    logger.addAppender(ajaxAppender);
    logger.addAppender(consoleAppender);
    return logger;
});