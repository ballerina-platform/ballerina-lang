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

package org.wso2.siddhi.core.stream.event.iterator;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.event.EventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.converter.EventConverter;
import org.wso2.siddhi.core.event.stream.converter.PassThroughStreamEventConverter;


public class EventChunkTestCase {
    private int count;
    private EventConverter eventConverter;
    private int beforeWindowDataSize;
    private int onAfterWindowDataSize;
    private int outputDataSize;

    @Before
    public void init() {
        count = 0;
        eventConverter = new PassThroughStreamEventConverter();


    }


    @Test
    public void EventChunkTest() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 2);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        EventChunk eventChunk = new EventChunk(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize, eventConverter);
        eventChunk.assignConvertedEvent(streamEvent1);

        while (eventChunk.hasNext()) {
            count++;
            eventChunk.next();
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void EventChunkRemoveTest1() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 2);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        EventChunk eventChunk = new EventChunk(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize, eventConverter);
        eventChunk.assignConvertedEvent(streamEvent1);

        while (eventChunk.hasNext()) {
            count++;
            eventChunk.next();
            if (count == 1) {
                eventChunk.remove();
            }
        }
        Assert.assertEquals(streamEvent2, eventChunk.getFirst());
    }

    @Test
    public void EventChunkRemoveTest2() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 2);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 2);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        EventChunk eventChunk = new EventChunk(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize, eventConverter);
        eventChunk.assignConvertedEvent(streamEvent1);

        while (eventChunk.hasNext()) {
            count++;
            eventChunk.next();
            if (count == 1 || count == 2) {
                eventChunk.remove();
            }
        }
        StreamEvent streamEvent = eventChunk.getFirst();
        Assert.assertEquals(streamEvent3, streamEvent);
        Assert.assertEquals(streamEvent4, streamEvent.getNext());
    }

    @Test
    public void EventChunkRemoveTest3() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 2);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 2);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        EventChunk eventChunk = new EventChunk(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize, eventConverter);
        eventChunk.assignConvertedEvent(streamEvent1);

        while (eventChunk.hasNext()) {
            eventChunk.next();
            eventChunk.remove();
        }

        Assert.assertNull(eventChunk.getFirst());
    }

    @Test
    public void EventChunkRemoveTest4() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 2);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 2);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        EventChunk eventChunk = new EventChunk(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize, eventConverter);
        eventChunk.assignConvertedEvent(streamEvent1);

        while (eventChunk.hasNext()) {
            count++;
            eventChunk.next();
            if (count == 2 || count == 4) {
                eventChunk.remove();
            }
        }
        StreamEvent streamEvent = eventChunk.getFirst();
        Assert.assertEquals(streamEvent1, streamEvent);
        Assert.assertEquals(streamEvent3, streamEvent.getNext());
        Assert.assertNull(streamEvent.getNext().getNext());
    }

    @Test(expected = IllegalStateException.class)
    public void EventChunkRemoveTest5() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);

        EventChunk eventChunk = new EventChunk(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize, eventConverter);
        eventChunk.assignConvertedEvent(streamEvent1);

        eventChunk.remove();
        eventChunk.remove();
    }


}
