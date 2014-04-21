/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.query.processor.handler;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.BundleEvent;
import org.wso2.siddhi.query.api.query.QueryEventSource;

public class TableHandlerProcessor extends SimpleHandlerProcessor {

    static final Logger log = Logger.getLogger(TableHandlerProcessor.class);

    public TableHandlerProcessor(QueryEventSource queryEventSource,
                                 SiddhiContext siddhiContext) {
        super(queryEventSource, null, null, siddhiContext);

    }


    protected void processHandler(BundleEvent bundleEvent) {
        if (bundleEvent != null) {
            if (bundleEvent instanceof AtomicEvent) {
                next.process((AtomicEvent) bundleEvent);
            } else {  //BundleEvent
                next.process(bundleEvent);
            }
        }
    }

    protected void processHandler(AtomicEvent atomicEvent) {
        if (atomicEvent != null) {
            if (atomicEvent instanceof AtomicEvent) {
                next.process(atomicEvent);
            } else {  //BundleEvent
                next.process((BundleEvent) atomicEvent);
            }
        }
    }
}
