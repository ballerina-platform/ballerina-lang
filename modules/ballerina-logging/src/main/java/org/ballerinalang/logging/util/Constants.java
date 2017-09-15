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
 * Created by pubudu on 9/7/17.
 */
public class Constants {

    /* LOG DESTINATIONS */
    public static final String LOG_DEST_CONSOLE = "__console";

    /* HANDLERS */
    public static final String JUL_CONSOLE_HANDLER = "java.util.logging.ConsoleHandler";
    public static final String JUL_CONSOLE_HANDLER_LEVEL = JUL_CONSOLE_HANDLER + ".level";
    public static final String JUL_CONSOLE_HANDLER_FORMATTER = JUL_CONSOLE_HANDLER + ".formatter";

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
    public static final String BALLERINA = "ballerina";
    public static final String BALLERINA_HANDLERS = BALLERINA + ".handlers";
    public static final String BALLERINA_LEVEL = BALLERINA + ".level";
    public static final String BALLERINA_USE_PARENT_HANDLERS = BALLERINA + ".useParentHandlers";

    // Ballerina Log API loggers
    public static final String BALLERINA_LOG_API = "org.ballerinalang.nativeimpl.utils.logger";
    public static final String BALLERINA_LOG_API_HANDLERS = BALLERINA_LOG_API + ".handlers";

    // HTTP trace logger
    public static final String HTTP_TRACELOG = "tracelog.http";
    public static final String HTTP_TRACELOG_LEVEL = HTTP_TRACELOG + ".level";
    public static final String HTTP_TRACELOG_USE_PARENT_HANDLERS = HTTP_TRACELOG +".useParentHandlers";

    /* FORMATTERS */
    public static final String BALLERINA_LOG_FORMATTER = "org.ballerinalang.logging.formatters.BallerinaLogFormatter";
    public static final String BALLERINA_LOG_FORMATTER_FORMAT = BALLERINA_LOG_FORMATTER + ".format";
    public static final String BALLERINA_LOG_FORMAT = "%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS,%1$tL  %2$-5s [%3$s:%4$s] " +
            "[%5$s:%6$s] [%7$s] - \"%8$s\" %n";

//    public static final String DEFAULT_LOG_FORMATTER = "org.ballerinalang.logging.formatters.DefaultLogFormatter";
//    public static final String DEFAULT_LOG_FORMATTER_FORMAT = DEFAULT_LOG_FORMATTER + ".format";
//    public static final String DEFAULT_LOG_FORMAT = "[%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS,%1$tL]  %2$-5s {%3$s} - %4$s %5$s %n";

    public static final String BRE_LOG_FORMATTER = "org.ballerinalang.logging.formatters.BRELogFormatter";
    public static final String BRE_LOG_FORMATTER_FORMAT = BRE_LOG_FORMATTER + ".format";

    public static final String HTTP_TRACELOG_FORMATTER = "org.ballerinalang.logging.formatters.HTTPTraceLogFormatter";
    public static final String HTTP_TRACELOG_FORMATTER_FORMAT = HTTP_TRACELOG_FORMATTER + ".format";
    public static final String HTTP_TRACELOG_FORMAT = "[%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS,%1$tL]  %2$-5s {%3$s} - %4$s %5$s %n";
}
