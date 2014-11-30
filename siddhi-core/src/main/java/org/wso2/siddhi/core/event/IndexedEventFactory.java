/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.event;

public class IndexedEventFactory implements com.lmax.disruptor.EventFactory<IndexedEventFactory.IndexedEvent> {


    public IndexedEvent newInstance() {
        return new IndexedEvent();
    }

    public class IndexedEvent {

        private int streamIndex;
        private Event event;

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        public int getStreamIndex() {
            return streamIndex;
        }

        public void setStreamIndex(int streamIndex) {
            this.streamIndex = streamIndex;
        }
    }
}