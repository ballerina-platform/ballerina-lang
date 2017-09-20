/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.logging.util;

/**
 * Constants used by the logging module
 *
 * @since 0.94
 */
public class Constants {

    /* GENERAL */
    public static final String BALLERINA = "ballerina";
    public static final String BALLERINA_RUNTIME = BALLERINA + ".runtime";
    public static final String BALLERINA_RUNTIME_LOG_FILE = BALLERINA_RUNTIME + ".log";

    /* LOG DESTINATIONS */
    public static final String LOG_DEST_CONSOLE = "__console";

    /* HANDLERS */
    public static final String BRE_LOG_FILE_HANDLER = "org.ballerinalang.logging.handlers.BRELogFileHandler";
    public static final String BRE_LOG_FILE_HANDLER_LEVEL = BRE_LOG_FILE_HANDLER + ".level";
    public static final String BRE_LOG_FILE_HANDLER_PATTERN = BRE_LOG_FILE_HANDLER + ".pattern";
    public static final String BRE_LOG_FILE_HANDLER_LIMIT = BRE_LOG_FILE_HANDLER + ".limit";
    public static final String BRE_LOG_FILE_HANDLER_APPEND = BRE_LOG_FILE_HANDLER + ".append";
    public static final String BRE_LOG_FILE_HANDLER_FORMATTER = BRE_LOG_FILE_HANDLER + ".formatter";

    /* LOGGERS */
    // Root logger
    public static final String HANDLERS = "handlers";
    public static final String LEVEL = ".level";

    // Ballerina user level loggers
    public static final String BALLERINA_LEVEL = "log.level";
    public static final String BALLERINA_LOG_FORMAT = "log.format";

    // HTTP trace logger
    public static final String HTTP_TRACELOG = "tracelog.http";
    public static final String HTTP_TRACELOG_LEVEL = HTTP_TRACELOG + ".level";
    public static final String HTTP_TRACELOG_USE_PARENT_HANDLERS = HTTP_TRACELOG + ".useParentHandlers";

    /* FORMATTERS */
    public static final String BRE_LOG_FORMATTER = "org.ballerinalang.logging.formatters.jul.BRELogFormatter";
    public static final String BRE_LOG_FORMATTER_FORMAT = BRE_LOG_FORMATTER + ".format";

    public static final String HTTP_TRACELOG_FORMATTER =
            "org.ballerinalang.logging.formatters.jul.HTTPTraceLogFormatter";
    public static final String HTTP_TRACELOG_FORMATTER_FORMAT = HTTP_TRACELOG_FORMATTER + ".format";

    /* FORMAT STRING PLACEHOLDERS */
    public static final String FMT_TIMESTAMP = "{{timestamp}}";
    public static final String FMT_LEVEL = "{{level}}";
    public static final String FMT_LOGGER = "{{logger}}";
    public static final String FMT_PACKAGE = "{{package}}";
    public static final String FMT_UNIT = "{{unit}}";
    public static final String FMT_FILE = "{{file}}";
    public static final String FMT_LINE = "{{line}}";
    public static final String FMT_WORKER = "{{worker}}";
    public static final String FMT_MESSAGE = "{{msg}}";
    public static final String FMT_ERROR = "{{err}}";
}
