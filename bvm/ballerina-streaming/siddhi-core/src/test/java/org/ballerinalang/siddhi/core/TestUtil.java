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

package org.ballerinalang.siddhi.core;

import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.testng.AssertJUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUtil {

    private TestUtil() {

    }

    /**
     * Add a callback to the given {@link SiddhiAppRuntime} and validate the assertions.
     *
     * @param siddhiAppRuntime the SiddhiAppRuntime
     * @param queryName        the query
     * @param expected         expected event data
     * @return TestCallback
     */
    public static TestCallback addQueryCallback(SiddhiAppRuntime siddhiAppRuntime, String queryName,
                                                Object[]... expected) {

        TestQueryCallback callBack = new TestQueryCallback(expected);
        siddhiAppRuntime.addCallback(queryName, callBack);
        return callBack;
    }

    /**
     * Add a callback to the given {@link SiddhiAppRuntime} and validate the assertions.
     *
     * @param siddhiAppRuntime the SiddhiAppRuntime
     * @param streamName       the stream
     * @param expected         expected event data
     * @return TestCallback
     */
    public static TestCallback addStreamCallback(SiddhiAppRuntime siddhiAppRuntime, String streamName,
                                                 Object[]... expected) {

        TestStreamCallback callBack = new TestStreamCallback(expected);
        siddhiAppRuntime.addCallback(streamName, callBack);
        return callBack;
    }

    /**
     * The API which lets the developers to get the statistics of test results.
     */
    public interface TestCallback {

        /**
         * Returns a list of assertion errors thrown while testing.
         *
         * @return a list of assertion errors
         */
        List<AssertionError> getAssertionErrors();

        /**
         * Returns the number of events received in.
         *
         * @return the number of events received in
         */
        int getInEventCount();

        /**
         * Returns the number of events removed from. This method can be called only for QueryCallbacks. Otherwise
         * throws and {@link UnsupportedOperationException}
         *
         * @return the number of events removed from
         * @throws UnsupportedOperationException if the callback was added for a stream
         */
        int getRemoveEventCount() throws UnsupportedOperationException;

        /**
         * Returns true if there is at-least an event received by the query or stream.
         *
         * @return true if at-least an event is received otherwise false
         */
        boolean isEventArrived();

        /**
         * Throw all the {@link AssertionError}s captured while testing.
         */
        void throwAssertionErrors();
    }

    private static class TestQueryCallback extends QueryCallback implements TestCallback {

        private final Object[][] expected;
        private final int noOfExpectedEvents;
        private AtomicInteger inEventCount = new AtomicInteger(0);
        private AtomicInteger removeEventCount = new AtomicInteger(0);
        private boolean eventArrived;
        private List<AssertionError> assertionErrors = new ArrayList<>();

        public TestQueryCallback(Object[]... expected) {
            this.expected = expected;
            this.noOfExpectedEvents = expected.length;
        }

        @Override
        public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {

            // Print the events
            EventPrinter.print(timestamp, inEvents, removeEvents);

            if (inEvents != null) {
                eventArrived = true;
                for (Event event : inEvents) {
                    int inEvent = inEventCount.incrementAndGet();
                    if (noOfExpectedEvents > 0 && inEvent <= noOfExpectedEvents) {
                        try {
                            AssertJUnit.assertArrayEquals(expected[inEvent - 1], event.getData());
                        } catch (AssertionError e) {
                            assertionErrors.add(e);
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

    private static class TestStreamCallback extends StreamCallback implements TestCallback {

        private final Object[][] expected;
        private final int noOfExpectedEvents;
        private AtomicInteger inEventCount = new AtomicInteger(0);
        private boolean eventArrived;
        private List<AssertionError> assertionErrors = new ArrayList<>();

        public TestStreamCallback(Object[]... expected) {
            this.expected = expected;
            this.noOfExpectedEvents = expected.length;
        }

        @Override
        public void receive(Event[] events) {
            // Print the events
            EventPrinter.print(events);

            if (events != null) {
                eventArrived = true;
                for (Event event : events) {
                    int inEvent = inEventCount.incrementAndGet();
                    if (noOfExpectedEvents > 0 && inEvent <= noOfExpectedEvents) {
                        try {
                            AssertJUnit.assertArrayEquals(expected[inEvent - 1], event.getData());
                        } catch (AssertionError e) {
                            assertionErrors.add(e);
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
