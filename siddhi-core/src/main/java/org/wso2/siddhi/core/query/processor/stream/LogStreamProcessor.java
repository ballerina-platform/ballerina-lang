/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.processor.stream;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Input attributes to log is (priority (String), log.message (String), is.event.logged (Bool))
 */
@Extension(
        name = "log",
        namespace = "",
        description = "The logger stream processor logs the message with or without event for the given log priority.",
        parameters = {
                @Parameter(name = "priority",
                        description = "The priority/type of this log message.",
                        type = {DataType.STRING}),
                @Parameter(name = "log.message",
                        description = "This submit the log message.",
                        type = {DataType.STRING}),
                @Parameter(name = "is.event.logged",
                        description = "Use this command to enable the logging of one or more events.",
                        type = {DataType.BOOL})
        },
        examples = {
                @Example(
                        syntax = "from fooStream#log(\"INFO\", \"Sample Event :\", true)\n" +
                                "select *\n" +
                                "insert into barStream;",
                        description = "This will log as INFO with the message \"Sample Event :\" + fooStream:events."
                ),
                @Example(
                        syntax = "from fooStream#log(\"Sample Event :\", true)\n" +
                                "select *\n" +
                                "insert into barStream;",
                        description = "This will logs with default log level as INFO."
                ),
                @Example(
                        syntax = "from fooStream#log(\"Sample Event :\", fasle)\n" +
                                "select *\n" +
                                "insert into barStream;",
                        description = "This will only log message."
                ),
                @Example(
                        syntax = "from fooStream#log(true)\n" +
                                "select *\n" +
                                "insert into barStream;",
                        description = "This will only log fooStream:events."
                ),
                @Example(
                        syntax = "from fooStream#log(\"Sample Event :\")\n" +
                                "select *\n" +
                                "insert into barStream;",
                        description = "This will log message and fooStream:events."
                )
        }
)
public class LogStreamProcessor extends StreamProcessor {
    private static final Logger log = Logger.getLogger(LogStreamProcessor.class);
    private ExpressionExecutor isLogEventExpressionExecutor = null;
    private ExpressionExecutor logMessageExpressionExecutor = null;
    private ExpressionExecutor logPriorityExpressionExecutor = null;
    private LogPriority logPriority = LogPriority.INFO;
    private String logPrefix;

    /**
     * The init method of the StreamFunction
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors for the function parameters
     * @param siddhiAppContext         siddhi app context
     * @param configReader this hold the {@link LogStreamProcessor} configuration reader.
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[]
            attributeExpressionExecutors, ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        int inputExecutorLength = attributeExpressionExecutors.length;
        if (inputExecutorLength == 1) {
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
                logMessageExpressionExecutor = attributeExpressionExecutors[0];
            } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.BOOL) {
                isLogEventExpressionExecutor = attributeExpressionExecutors[0];
            } else {
                throw new SiddhiAppValidationException("Input attribute is expected to be 'isEventLogged (Bool)' " +
                        "or 'logMessage (String)' or 'isEventLogged (Bool), logMessage (String)' or 'priority " +
                        "(String), isEventLogged (Bool), logMessage (String)', but its 1st attribute is " +
                        attributeExpressionExecutors[0].getReturnType());
            }
        } else if (inputExecutorLength == 2) {
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING &&
                    attributeExpressionExecutors[1].getReturnType() == Attribute.Type.BOOL) {
                logMessageExpressionExecutor = attributeExpressionExecutors[0];
                isLogEventExpressionExecutor = attributeExpressionExecutors[1];
            } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING &&
                    attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING) {
                if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                    logPriority = LogPriority.valueOf(((String) attributeExpressionExecutors[0].execute(null))
                            .toUpperCase());
                } else {
                    logPriorityExpressionExecutor = attributeExpressionExecutors[0];
                }
                logMessageExpressionExecutor = attributeExpressionExecutors[1];
            } else {
                throw new SiddhiAppValidationException("Input attribute is expected to be 'logMessage (String), " +
                        "isEventLogged (Bool)' or 'priority (String), logMessage (String)', but its returning are '"
                        + attributeExpressionExecutors[0].getReturnType() + ", " + attributeExpressionExecutors[1]
                        .getReturnType() + "'");
            }
        } else if (inputExecutorLength == 3) {
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
                if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                    logPriority = LogPriority.valueOf(((String) attributeExpressionExecutors[0].execute(null))
                            .toUpperCase());
                } else {
                    logPriorityExpressionExecutor = attributeExpressionExecutors[0];
                }
            } else {
                throw new SiddhiAppValidationException("Input attribute is expected to be 'priority (String), " +
                        "logMessage (String), isEventLogged (Bool)', but its 1st attribute is returning " +
                        attributeExpressionExecutors[0].getReturnType());
            }
            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING) {
                logMessageExpressionExecutor = attributeExpressionExecutors[1];
            } else {
                throw new SiddhiAppValidationException("Input attribute is expected to be 'priority (String), " +
                        "logMessage (String), isEventLogged (Bool)', but its 2nd attribute is returning " +
                        attributeExpressionExecutors[1].getReturnType());
            }
            if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.BOOL) {
                isLogEventExpressionExecutor = attributeExpressionExecutors[2];
            } else {
                throw new SiddhiAppValidationException("Input attribute is expected to be 'priority (String), " +
                        "logMessage (String), isEventLogged (Bool)', but its 3rd attribute is returning " +
                        attributeExpressionExecutors[2].getReturnType());
            }
        } else if (inputExecutorLength > 3) {
            throw new SiddhiAppValidationException("Input parameters for Log can be logMessage (String), " +
                    "isEventLogged (Bool), but there are " + attributeExpressionExecutors.length + " in the input!");
        }
        logPrefix = siddhiAppContext.getName() + ": ";
        return new ArrayList<Attribute>();
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        while (streamEventChunk.hasNext()) {
            ComplexEvent complexEvent = streamEventChunk.next();
            switch (attributeExpressionLength) {
                case 0:
                    log.info(logPrefix + complexEvent);
                    break;
                case 1:
                    if (isLogEventExpressionExecutor != null) {
                        if ((Boolean) isLogEventExpressionExecutor.execute(complexEvent)) {
                            log.info(logPrefix + complexEvent);
                        } else {
                            log.info(logPrefix + "Event Arrived");
                        }
                    } else {
                        log.info(logPrefix + logMessageExpressionExecutor.execute(complexEvent) + ", " + complexEvent);
                    }
                    break;
                case 2:
                    if (isLogEventExpressionExecutor != null) {
                        if ((Boolean) isLogEventExpressionExecutor.execute(complexEvent)) {
                            log.info(logPrefix + logMessageExpressionExecutor.execute(complexEvent) + ", " +
                                    complexEvent);
                        } else {
                            log.info(logPrefix + logMessageExpressionExecutor.execute(complexEvent));
                        }
                    } else {
                        LogPriority tempLogPriority = logPriority;
                        if (logPriorityExpressionExecutor != null) {
                            tempLogPriority = LogPriority.valueOf((String) logPriorityExpressionExecutor.execute
                                    (complexEvent));
                        }
                        String message = logPrefix + logMessageExpressionExecutor.execute(complexEvent) + ", " +
                                complexEvent;
                        logMessage(tempLogPriority, message);
                    }
                    break;
                default:
                    String message;
                    if ((Boolean) isLogEventExpressionExecutor.execute(complexEvent)) {
                        message = logPrefix + logMessageExpressionExecutor.execute(complexEvent) + ", " + complexEvent;
                    } else {
                        message = logPrefix + logMessageExpressionExecutor.execute(complexEvent);
                    }
                    LogPriority tempLogPriority = logPriority;
                    if (logPriorityExpressionExecutor != null) {
                        tempLogPriority = LogPriority.valueOf((String) logPriorityExpressionExecutor.execute
                                (complexEvent));
                    }
                    logMessage(tempLogPriority, message);
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    private void logMessage(LogPriority logPriority, String message) {
        switch (logPriority) {
            case INFO:
                log.info(message);
                break;
            case DEBUG:
                log.debug(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case FATAL:
                log.fatal(message);
                break;
            case ERROR:
                log.error(message);
                break;
            case OFF:
                break;
            case TRACE:
                log.trace(message);
                break;
        }
    }

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }

    @Override
    public Map<String, Object> currentState() {
        //No state
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }

    enum LogPriority {
        INFO,
        DEBUG,
        WARN,
        FATAL,
        ERROR,
        OFF,
        TRACE
    }


}
