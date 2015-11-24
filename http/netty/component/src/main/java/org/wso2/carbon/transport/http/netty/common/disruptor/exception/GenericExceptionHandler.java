/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.common.disruptor.exception;

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler class of the Disruptor.
 */
public class GenericExceptionHandler implements ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionHandler.class);

    public void handleEventException(Throwable ex, long sequence, Object event) {
        logger.error("Caught unhandled exception while processing: " + event.toString(), ex);
    }

    public void handleOnStartException(Throwable ex) {
        logger.error("Unexpected exception during startup.", ex);
    }

    public void handleOnShutdownException(Throwable ex) {
        logger.error("Unexpected exception during shutdown.", ex);
    }
}
