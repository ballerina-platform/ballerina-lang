/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.processor.window;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.*;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.expression.Expression;

public abstract class WindowProcessor implements Processor {

    private static final Logger log = Logger.getLogger(WindowProcessor.class);

    protected Processor nextProcessor;
    protected Expression[] parameters;


    /**
     * Initialization method for window processors. Should set parameters accordingly and configure processor
     * to an executable status.
     *
     * @param metaStreamEvent
     * @param streamEventCloner
     */
    public abstract void init(MetaStreamEvent metaStreamEvent, StreamEventCloner streamEventCloner);

    public Processor getNextProcessor() {
        return nextProcessor;
    }

    public void setNextProcessor(Processor processor) {
        this.nextProcessor = processor;
    }

    public void process(ComplexEventChunk<StreamEvent> streamEventChunk){
        streamEventChunk.reset();
        try {
            process(streamEventChunk, nextProcessor);
        }catch (Throwable t){    //todo improve
            log.error(t.getMessage(),t);
        }
    }

    protected abstract void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor);

    public void setToLast(Processor processor) {
        if (nextProcessor == null) {
            this.nextProcessor = processor;
        } else {
            this.nextProcessor.setToLast(processor);
        }
    }

    public void setParameters(Expression[] parameters) {
        this.parameters = parameters;
    }

    public abstract Processor cloneProcessor();

    public void initProcessor(MetaStreamEvent metaStreamEvent, StreamEventPool streamEventPool) {
        init(metaStreamEvent, new StreamEventCloner(metaStreamEvent, streamEventPool));

    }
}
