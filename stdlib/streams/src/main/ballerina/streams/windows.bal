// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/time;
import ballerina/task;

public type EventType "CURRENT"|"EXPIRED"|"ALL"|"RESET"|"TIMER";

public type StreamEvent record {
    EventType eventType;
    any eventObject;
    int timestamp;
};

public type Window object {

    public function process(StreamEvent[] streamEvents) {

    }
};

public type LengthWindow object {

    public int size;
    public LinkedList linkedList;
    public function (StreamEvent[]) nextProcessorPointer;

    public new(nextProcessorPointer, size) {
        linkedList = new;
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] outputEvents = [];
        foreach event in streamEvents {
            if (linkedList.getSize() == size) {
                match linkedList.removeFirst() {
                    StreamEvent streamEvent => {
                        outputEvents[lengthof outputEvents] = streamEvent;
                    }

                    () => {
                        // do nothing
                    }

                    any anyValue => {
                        // do nothing
                    }
                }
            }

            outputEvents[lengthof outputEvents] = event;
            StreamEvent expiredVeresionOfEvent = {eventType : "EXPIRED", eventObject: event.eventObject,
                timestamp: event.timestamp};
            linkedList.addLast(expiredVeresionOfEvent);
        }
        nextProcessorPointer(outputEvents);
    }
};

public function lengthWindow(function (StreamEvent[]) nextProcessorPointer, int length)
                    returns LengthWindow {
    LengthWindow lengthWindow1 = new(nextProcessorPointer, length);
    return lengthWindow1;
}

public type TimeWindow object {

    public int counter;
    public int timeLength;

    private LinkedList linkedList;
    private function (StreamEvent[]) nextProcessorPointer;

    public new (nextProcessorPointer, timeLength) {
        linkedList = new;
    }

    function startEventRemovalWorker() {

        StreamEvent frontEvent = check <StreamEvent>linkedList.getFirst();
        StreamEvent rearEvent = check <StreamEvent>linkedList.getLast();
        StreamEvent[] expiredEvents = [];
        int index = 0;

        while (!linkedList.isEmpty() && rearEvent.timestamp > frontEvent.timestamp + timeLength) {
            if (!linkedList.isEmpty()) {
                StreamEvent streamEvent = check <StreamEvent>linkedList.removeFirst();
                EventType evType = "EXPIRED";
                StreamEvent event = {eventType : evType, eventObject : streamEvent.eventObject,
                    timestamp : streamEvent.timestamp};
                expiredEvents[index] = event;
                index += 1;
                frontEvent = check <StreamEvent>linkedList.getFirst();
                rearEvent = check <StreamEvent>linkedList.getLast();
            }
        }

        if (lengthof expiredEvents > 0) {
            nextProcessorPointer(expiredEvents);
        }
    }

    public function add(StreamEvent event) {
        if (!linkedList.isEmpty()) {
            StreamEvent rearEvent = check <StreamEvent>linkedList.getLast();
            if (rearEvent.timestamp <= event.timestamp) {
                linkedList.addLast(event);
            }
        } else {
            linkedList.addLast(event);
        }
        startEventRemovalWorker();
    }
};

public function timeWindow(function(StreamEvent[]) nextProcessPointer, int timeLength)
                    returns TimeWindow {
    TimeWindow timeWindow1 = new(nextProcessPointer, timeLength);
    return timeWindow1;
}

public type LengthBatchWindow object {
    private int length;
    private int count;
    private StreamEvent? resetEvent;
    private LinkedList currentEventQueue;
    private LinkedList? expiredEventQueue;
    private function (StreamEvent[]) nextProcessorPointer;

    public new (nextProcessorPointer, length) {
        currentEventQueue = new();
        expiredEventQueue = ();
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunks = new();
        LinkedList outputStreamEventChunk = new();
        int currentTime = time:currentTime().time;

        foreach event in streamEvents {
            StreamEvent clonedStreamEvent = cloneStreamEvent(event);
            currentEventQueue.addLast(clonedStreamEvent);
            count++;
            if (count == length) {
                //if (expiredEventQueue.getFirst() != ()) {
                //    expiredEventQueue.clear();
                //}
                if (currentEventQueue.getFirst() != ()) {
                    if (resetEvent != ()) {
                        outputStreamEventChunk.addLast(resetEvent);
                        resetEvent = ();
                    }
                    //if (expiredEventQueue != ()) {
                    //    currentEventQueue.resetToFront();
                    //    while (currentEventQueue.hasNext()) {
                    //        StreamEvent currentEvent = check <StreamEvent> currentEventQueue.next();
                    //        StreamEvent toBeExpired = {eventType: "EXPIRED", eventObject: currentEvent.eventObject,
                    //            timestamp: currentEvent.timestamp};
                    //        expiredEventQueue.addLast(toBeExpired);
                    //    }
                    //}
                    StreamEvent firstInCurrentEventQueue = check <StreamEvent> currentEventQueue.getFirst();
                    resetEvent = createResetStreamEvent(firstInCurrentEventQueue);
                    foreach currentEvent in currentEventQueue.asArray() {
                        outputStreamEventChunk.addLast(currentEvent);
                    }
                }
                currentEventQueue.clear();
                count = 0;
                if (outputStreamEventChunk.getFirst() != ()) {
                    streamEventChunks.addLast(outputStreamEventChunk);
                }
            }
        }

        streamEventChunks.resetToFront();
        while streamEventChunks.hasNext() {
            StreamEvent[] events = [];
            LinkedList streamEventChunk = check <LinkedList> streamEventChunks.next();
            streamEventChunk.resetToFront();
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = check <StreamEvent> streamEventChunk.next();
                events[lengthof events] = streamEvent;
            }
            nextProcessorPointer(events);
        }
    }
};

public function lengthBatchWindow(function(StreamEvent[]) nextProcessPointer, int length)
                    returns LengthBatchWindow {
    LengthBatchWindow lengthBatch = new(nextProcessPointer, length);
    return lengthBatch;
}


public type TimeBatchWindow object {
    private int timeInMilliSeconds;
    private int nextEmitTime = -1;
    private LinkedList currentEventQueue;
    private LinkedList? expiredEventQueue;
    private StreamEvent? resetEvent;
    private task:Timer? timer;
    private function (StreamEvent[]) nextProcessorPointer;

    public new(nextProcessorPointer, timeInMilliSeconds) {
        currentEventQueue = new();
        expiredEventQueue = ();
    }

    function invokeProcess() returns error? {
        StreamEvent timerEvent = {eventType : "TIMER", eventObject: (), timestamp: time:currentTime().time};
        StreamEvent[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        process(timerEventWrapper);
        return ();
    }
    public function process(StreamEvent[] streamEvents) {
        LinkedList outputStreamEvents = new();
        if (nextEmitTime == -1) {
            nextEmitTime = time:currentTime().time + timeInMilliSeconds;
            timer = new task:Timer(self.invokeProcess, self.handleError, timeInMilliSeconds, delay =
                timeInMilliSeconds);
            _ = timer.start();
        }

        int currentTime = time:currentTime().time;
        boolean sendEvents = false;

        if (currentTime >= nextEmitTime) {
            nextEmitTime += timeInMilliSeconds;
            timer.stop();
            timer = new task:Timer(self.invokeProcess, self.handleError, timeInMilliSeconds, delay =
                timeInMilliSeconds);
            _ = timer.start();
            sendEvents = true;
        } else {
            sendEvents = false;
        }

        foreach event in streamEvents {
            if (event.eventType != "CURRENT") {
                continue;
            }
            StreamEvent clonedEvent = cloneStreamEvent(event);
            currentEventQueue.addLast(clonedEvent);
        }
        if (sendEvents) {
            if (currentEventQueue.getFirst() != ()) {
                if (resetEvent != ()) {
                    outputStreamEvents.addLast(resetEvent);
                    resetEvent = ();
                }
                resetEvent = createResetStreamEvent(check <StreamEvent> currentEventQueue.getFirst());
                currentEventQueue.resetToFront();
                while (currentEventQueue.hasNext()) {
                    StreamEvent streamEvent = check <StreamEvent> currentEventQueue.next();
                    outputStreamEvents.addLast(streamEvent);
                }
            }
            currentEventQueue.clear();
        }
        if (outputStreamEvents.getSize() != 0) {
            StreamEvent[] events = [];
            outputStreamEvents.resetToFront();
            while (outputStreamEvents.hasNext()) {
                StreamEvent streamEvent = check <StreamEvent> outputStreamEvents.next();
                events[lengthof events] = streamEvent;
            }
            nextProcessorPointer(events);
        }
    }

    function handleError(error e) {
        io:println("Error occured", e);
    }
};

public function timeBatchWindow(function(StreamEvent[]) nextProcessPointer, int time)
                    returns TimeBatchWindow {
    TimeBatchWindow timeBatch = new(nextProcessPointer, time);
    return timeBatch;
}
