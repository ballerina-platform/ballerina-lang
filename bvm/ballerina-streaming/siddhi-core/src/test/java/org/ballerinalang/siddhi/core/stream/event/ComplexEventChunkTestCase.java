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

package org.ballerinalang.siddhi.core.stream.event;

import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.converter.ConversionStreamEventChunk;
import org.ballerinalang.siddhi.core.event.stream.converter.StreamEventConverter;
import org.ballerinalang.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class ComplexEventChunkTestCase {
    private int count;
    private StreamEventConverter streamEventConverter;

    @BeforeMethod
    public void init() {
        count = 0;
        streamEventConverter = new ZeroStreamEventConverter();
    }


    @Test
    public void eventChunkTest() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700L, 1L});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700L, 2L});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700L, 3L});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter,
                streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            StreamEvent event = streamEventChunk.next();
            AssertJUnit.assertEquals(count * 1L, event.getOutputData()[2]);
        }
        AssertJUnit.assertEquals(3, count);
    }

    @Test
    public void eventChunkRemoveTest1() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700L, 1L});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700L, 2L});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700L, 3L});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter,
                streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            streamEventChunk.next();
            if (count == 1) {
                streamEventChunk.remove();
            }
        }
        AssertJUnit.assertEquals(streamEvent2, streamEventChunk.getFirst());
    }

    @Test
    public void eventChunkRemoveTest2() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700L, 1L});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700L, 2L});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700L, 3L});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 3);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700L, 4L});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter,
                streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            streamEventChunk.next();
            if (count == 1 || count == 2) {
                streamEventChunk.remove();
            }
        }
        StreamEvent streamEvent = streamEventChunk.getFirst();
        AssertJUnit.assertEquals(streamEvent3, streamEvent);
        AssertJUnit.assertEquals(streamEvent4, streamEvent.getNext());
    }

    @Test
    public void eventChunkRemoveTest3() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700L, 100L});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700L, 100L});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700L, 100L});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 3);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700L, 100L});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter,
                streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            streamEventChunk.next();
            streamEventChunk.remove();
        }

        AssertJUnit.assertNull(streamEventChunk.getFirst());
    }

    @Test
    public void eventChunkRemoveTest4() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700L, 100L});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700L, 100L});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700L, 100L});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 3);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700L, 100L});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter,
                streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            streamEventChunk.next();
            if (count == 2 || count == 4) {
                streamEventChunk.remove();
            }
        }
        StreamEvent streamEvent = streamEventChunk.getFirst();
        AssertJUnit.assertEquals(streamEvent1, streamEvent);
        AssertJUnit.assertEquals(streamEvent3, streamEvent.getNext());
        AssertJUnit.assertNull(streamEvent.getNext().getNext());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void eventChunkRemoveTest5() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700L, 100L});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700L, 100L});

        streamEvent1.setNext(streamEvent2);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter,
                streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        streamEventChunk.remove();
        streamEventChunk.remove();
    }


}
