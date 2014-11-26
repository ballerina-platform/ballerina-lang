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
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventIterator;

public class StreamEventIteratorTestCase {
    private int count;

    @Before
    public void init() {
        count = 0;
    }


    @Test
    public void StreamEventIteratorTest() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 2);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        StreamEventIterator iterator = new StreamEventIterator();
        iterator.assignEvent(streamEvent1);

        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void StreamEventIteratorRemoveTest1() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 2);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        StreamEventIterator iterator = new StreamEventIterator();
        iterator.assignEvent(streamEvent1);

        while (iterator.hasNext()) {
            count++;
            iterator.next();
            if (count == 1) {
                iterator.remove();
            }
        }
        Assert.assertEquals(streamEvent2, iterator.getFirst());
    }

    @Test
    public void StreamEventIteratorRemoveTest2() {
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

        StreamEventIterator iterator = new StreamEventIterator();
        iterator.assignEvent(streamEvent1);

        while (iterator.hasNext()) {
            count++;
            iterator.next();
            if (count == 1 || count == 2) {
                iterator.remove();
            }
        }
        StreamEvent streamEvent = iterator.getFirst();
        Assert.assertEquals(streamEvent3, streamEvent);
        Assert.assertEquals(streamEvent4, streamEvent.getNext());
    }

    @Test
    public void StreamEventIteratorRemoveTest3() {
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

        StreamEventIterator iterator = new StreamEventIterator();
        iterator.assignEvent(streamEvent1);

        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        Assert.assertNull(iterator.getFirst());
    }

    @Test
    public void StreamEventIteratorRemoveTest4() {
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

        StreamEventIterator iterator = new StreamEventIterator();
        iterator.assignEvent(streamEvent1);

        while (iterator.hasNext()) {
            count++;
            iterator.next();
            if (count == 2 || count == 4) {
                iterator.remove();
            }
        }
        StreamEvent streamEvent = iterator.getFirst();
        Assert.assertEquals(streamEvent1, streamEvent);
        Assert.assertEquals(streamEvent3, streamEvent.getNext());
        Assert.assertNull(streamEvent.getNext().getNext());
    }

    @Test(expected = IllegalStateException.class)
    public void StreamEventIteratorRemoveTest5() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 2);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 2);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);

        StreamEventIterator iterator = new StreamEventIterator();
        iterator.assignEvent(streamEvent1);

        iterator.remove();
        iterator.remove();
    }


}
