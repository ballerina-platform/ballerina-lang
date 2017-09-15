/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core;

import org.junit.Assert;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUtil {

    private TestUtil() {

    }

    public static TestCallBack addQueryCallback(SiddhiAppRuntime siddhiAppRuntime, String queryName, Object[]... expected) {

        TestQueryCallBack callBack = new TestQueryCallBack(expected);
        siddhiAppRuntime.addCallback(queryName, callBack);
        return callBack;
    }

    public static TestCallBack addStreamCallback(SiddhiAppRuntime siddhiAppRuntime, String streamName, Object[]... expected) {

        TestStreamCallBack callBack = new TestStreamCallBack(expected);
        siddhiAppRuntime.addCallback(streamName, callBack);
        return callBack;
    }

    public interface TestCallBack {

        List<AssertionError> getAssertionErrors();

        int getInEventCount();

        int getRemoveEventCount();

        boolean isEventArrived();

        void throwAssertionErrors();
    }

    private static class TestQueryCallBack extends QueryCallback implements TestCallBack {

        private AtomicInteger inEventCount = new AtomicInteger(0);
        private AtomicInteger removeEventCount = new AtomicInteger(0);
        private boolean eventArrived;
        private List<AssertionError> assertionErrors = new ArrayList<>();
        private final Object[][] expected;
        private final int noOfExpectedEvents;

        public TestQueryCallBack(Object[]... expected) {
            this.expected = expected;
            this.noOfExpectedEvents = expected.length;
        }

        @Override
        public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {

            // Print the events
            EventPrinter.print(timestamp, inEvents, removeEvents);

            if (inEvents != null) {

                int inEventsLength = inEvents.length;
                inEventCount.addAndGet(inEventsLength);
                eventArrived = true;

                if (noOfExpectedEvents > 0) {
                    for (int i = 0; i < inEventsLength; i++) {

                        if (i <= noOfExpectedEvents && expected[i] != null) {
                            try {
                                Assert.assertArrayEquals(expected[i], inEvents[i].getData());
                            } catch (AssertionError e) {
                                // Add the assertion error into the list
                                assertionErrors.add(e);
                            }
                        }
                    }
                }
            }
            if (removeEvents != null) {
                removeEventCount.addAndGet(removeEvents.length);
            }
        }

        @Override
        public List<AssertionError> getAssertionErrors() {
            return this.assertionErrors;
        }

        @Override
        public int getInEventCount() {
            return this.inEventCount.get();
        }

        @Override
        public int getRemoveEventCount() {
            return this.removeEventCount.get();
        }

        @Override
        public boolean isEventArrived() {
            return this.eventArrived;
        }

        @Override
        public void throwAssertionErrors() {
            for (AssertionError error : this.assertionErrors) {
                throw error;
            }
        }
    }

    private static class TestStreamCallBack extends StreamCallback implements TestCallBack {

        private AtomicInteger inEventCount = new AtomicInteger(0);
        private boolean eventArrived;
        private List<AssertionError> assertionErrors = new ArrayList<>();
        private final Object[][] expected;
        private final int noOfExpectedEvents;

        public TestStreamCallBack(Object[]... expected) {
            this.expected = expected;
            this.noOfExpectedEvents = expected.length;
        }

        @Override
        public void receive(Event[] events) {
            // Print the events
            EventPrinter.print(events);

            if (events != null) {

                int inEventsLength = events.length;
                inEventCount.addAndGet(inEventsLength);
                eventArrived = true;

                if (noOfExpectedEvents > 0) {
                    for (int i = 0; i < inEventsLength; i++) {

                        if (i <= noOfExpectedEvents && expected[i] != null) {
                            try {
                                Assert.assertArrayEquals(expected[i], events[i].getData());
                            } catch (AssertionError e) {
                                // Add the assertion error into the list
                                assertionErrors.add(e);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public List<AssertionError> getAssertionErrors() {
            return this.assertionErrors;
        }

        @Override
        public int getInEventCount() {
            return this.inEventCount.get();
        }

        @Override
        public int getRemoveEventCount() {
            throw new UnsupportedOperationException("StreamCallback does not receive remove events");
        }

        @Override
        public boolean isEventArrived() {
            return this.eventArrived;
        }

        @Override
        public void throwAssertionErrors() {
            for (AssertionError error : this.assertionErrors) {
                throw error;
            }
        }
    }
}
