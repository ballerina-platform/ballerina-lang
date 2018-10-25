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

public type Window abstract object {

    public function process(StreamEvent[] streamEvents);

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[];
};

public type LengthWindow object {

    public int size;
    public LinkedList linkedList;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, size) {
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
            StreamEvent expiredVeresionOfEvent = event.clone();
            expiredVeresionOfEvent.eventType = "EXPIRED";
            linkedList.addLast(expiredVeresionOfEvent);
        }
        match (nextProcessPointer) {
            function (StreamEvent[]) nxtProc => {
                nxtProc(outputEvents);
            }
            () => {
                //do nothing
            }
        }
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events;
        int i = 0;
        foreach e in linkedList.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }
};

public function lengthWindow(int length, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    LengthWindow lengthWindow1 = new(nextProcessPointer, length);
    return lengthWindow1;
}

public type TimeWindow object {

    public int timeInMillis;
    public LinkedList expiredEventQueue;
    public LinkedList timerQueue;
    public function (StreamEvent[])? nextProcessPointer;
    public int lastTimestamp = -0x8000000000000000;

    public new(nextProcessPointer, timeInMillis) {
        expiredEventQueue = new;
        timerQueue = new;
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach event in streamEvents {
                streamEventChunk.addLast(event);
            }

            streamEventChunk.resetToFront();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                int currentTime = time:currentTime().time;
                expiredEventQueue.resetToFront();

                while (expiredEventQueue.hasNext()) {
                    StreamEvent expiredEvent = check <StreamEvent>expiredEventQueue.next();
                    int timeDiff = (expiredEvent.timestamp - currentTime) + timeInMillis;
                    if (timeDiff <= 0) {
                        expiredEventQueue.removeCurrent();
                        expiredEvent.timestamp = currentTime;
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                if (streamEvent.eventType == "CURRENT") {
                    StreamEvent clonedEvent = streamEvent.clone();
                    clonedEvent.eventType = "EXPIRED";
                    expiredEventQueue.addLast(clonedEvent);

                    if (lastTimestamp < clonedEvent.timestamp) {
                        task:Timer timer = new task:Timer(self.invokeProcess, self.handleError, timeInMillis,
                            delay = timeInMillis - (time:currentTime().time - clonedEvent.timestamp));
                        _ = timer.start();
                        timerQueue.addLast(timer);
                        lastTimestamp = clonedEvent.timestamp;
                    }
                } else {
                    streamEventChunk.removeCurrent();
                }
            }
            expiredEventQueue.resetToFront();
        }
        match (nextProcessPointer) {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                        events[lengthof events] = streamEvent;
                    }
                    nxtProc(events);
                }
            }
            () => {
                //do nothing
            }
        }
    }

    public function invokeProcess() returns error? {
        StreamEvent timerEvent = new(("timer", {}), "TIMER", time:currentTime().time);
        StreamEvent[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        process(timerEventWrapper);
        if (!timerQueue.isEmpty()) {
            task:Timer timer = check <task:Timer>timerQueue.removeFirst();
            _ = timer.stop();
        }
        return ();
    }

    public function handleError(error e) {
        io:println("Error occured", e);
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in expiredEventQueue.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }
};

public function timeWindow(int timeLength, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    TimeWindow timeWindow1 = new(nextProcessPointer, timeLength);
    return timeWindow1;
}

public type LengthBatchWindow object {
    public int length;
    public int count;
    public StreamEvent? resetEvent;
    public LinkedList currentEventQueue;
    public LinkedList? expiredEventQueue;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, length) {
        currentEventQueue = new();
        expiredEventQueue = ();
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunks = new();
        LinkedList outputStreamEventChunk = new();
        int currentTime = time:currentTime().time;

        foreach event in streamEvents {
            StreamEvent clonedStreamEvent = event.clone();
            currentEventQueue.addLast(clonedStreamEvent);
            count += 1;
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
                    //        StreamEvent toBeExpired = {eventType: "EXPIRED", eventMap: currentEvent.eventMap,
                    //            timestamp: currentEvent.timestamp};
                    //        expiredEventQueue.addLast(toBeExpired);
                    //    }
                    //}
                    StreamEvent firstInCurrentEventQueue = check <StreamEvent>currentEventQueue.getFirst();
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

        match (nextProcessPointer) {
            function (StreamEvent[]) nxtProc => {
                streamEventChunks.resetToFront();
                while streamEventChunks.hasNext() {
                    StreamEvent[] events = [];
                    LinkedList streamEventChunk = check <LinkedList>streamEventChunks.next();
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                        events[lengthof events] = streamEvent;
                    }
                    nxtProc(events);
                }
            }
            () => {
                //do nothing
            }
        }
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in currentEventQueue.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;

                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }
};

public function lengthBatchWindow(int length, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    LengthBatchWindow lengthBatch = new(nextProcessPointer, length);
    return lengthBatch;
}


public type TimeBatchWindow object {
    public int timeInMilliSeconds;
    public int nextEmitTime = -1;
    public LinkedList currentEventQueue;
    public LinkedList? expiredEventQueue;
    public StreamEvent? resetEvent;
    public task:Timer? timer;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, timeInMilliSeconds) {
        currentEventQueue = new();
        expiredEventQueue = ();
    }

    public function invokeProcess() returns error? {
        StreamEvent timerEvent = new(("timer", {}), "TIMER", time:currentTime().time);
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
            StreamEvent clonedEvent = event.clone();
            currentEventQueue.addLast(clonedEvent);
        }
        if (sendEvents) {
            if (currentEventQueue.getFirst() != ()) {
                if (resetEvent != ()) {
                    outputStreamEvents.addLast(resetEvent);
                    resetEvent = ();
                }
                resetEvent = createResetStreamEvent(check <StreamEvent>currentEventQueue.getFirst());
                currentEventQueue.resetToFront();
                while (currentEventQueue.hasNext()) {
                    StreamEvent streamEvent = check <StreamEvent>currentEventQueue.next();
                    outputStreamEvents.addLast(streamEvent);
                }
            }
            currentEventQueue.clear();
        }
        match nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (outputStreamEvents.getSize() != 0) {
                    StreamEvent[] events = [];
                    outputStreamEvents.resetToFront();
                    while (outputStreamEvents.hasNext()) {
                        StreamEvent streamEvent = check <StreamEvent>outputStreamEvents.next();
                        events[lengthof events] = streamEvent;
                    }
                    nxtProc(events);
                }
            }
            () => {
                // do nothing
            }
        }
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in currentEventQueue.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }

    public function handleError(error e) {
        io:println("Error occured", e);
    }
};

public function timeBatchWindow(int time, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    TimeBatchWindow timeBatch = new(nextProcessPointer, time);
    return timeBatch;
}

public type ExternalTimeWindow object {

    public int timeInMillis;
    public LinkedList expiredEventQueue;
    public function (StreamEvent[])? nextProcessPointer;
    public string timeStamp;

    public new(nextProcessPointer, timeInMillis, timeStamp) {
        expiredEventQueue = new;
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach event in streamEvents {
                streamEventChunk.addLast(event);
            }

            streamEventChunk.resetToFront();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                int currentTime = getTimestamp(streamEvent.data[timeStamp]);
                expiredEventQueue.resetToFront();

                while (expiredEventQueue.hasNext()) {
                    StreamEvent expiredEvent = check <StreamEvent>expiredEventQueue.next();
                    int timeDiff = (getTimestamp(expiredEvent.data[timeStamp]) - currentTime) + timeInMillis;
                    if (timeDiff <= 0) {
                        expiredEventQueue.removeCurrent();
                        expiredEvent.timestamp = currentTime;
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        expiredEventQueue.resetToFront();
                        break;
                    }
                }

                if (streamEvent.eventType == CURRENT) {
                    StreamEvent clonedEvent = streamEvent.clone();
                    clonedEvent.eventType = EXPIRED;
                    expiredEventQueue.addLast(clonedEvent);
                }
                expiredEventQueue.resetToFront();
            }
        }
        match nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                        events[lengthof events] = streamEvent;
                    }
                    nxtProc(events);
                }
            }
            () => {
                //do nothing
            }
        }
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events;
        int i = 0;
        foreach e in expiredEventQueue.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }

    public function getTimestamp(any val) returns (int) {
        match val {
            int value => return value;
            any => {
                error err = { message: "external timestamp should be of type int" };
                throw err;
            }
        }
    }
};

public function externalTimeWindow(string timeStamp, int timeLength, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {

    ExternalTimeWindow timeWindow1 = new(nextProcessPointer, timeLength, timeStamp);
    return timeWindow1;
}

public type ExternalTimeBatchWindow object {
    public int timeToKeep;
    public LinkedList currentEventChunk;
    public LinkedList expiredEventChunk;
    public StreamEvent? resetEvent = null;
    public int startTime = 0;
    public boolean isStartTimeEnabled = false;
    public boolean replaceTimestampWithBatchEndTime = false;
    public boolean flushed = false;
    public int endTime = -1;
    public int schedulerTimeout = 0;
    public int lastScheduledTime;
    public int lastCurrentEventTime = 0;
    public task:Timer? timer;
    public function (StreamEvent[])? nextProcessPointer;
    public string timeStamp;
    public boolean storeExpiredEvents = false;
    public boolean outputExpectsExpiredEvents = false;

    public new(nextProcessPointer, timeToKeep, timeStamp, startTime, schedulerTimeout,
               replaceTimestampWithBatchEndTime) {
        currentEventChunk = new();
        expiredEventChunk = new;
        if (startTime != -1) {
            isStartTimeEnabled = true;
        }
    }

    public function invokeProcess() returns error? {
        StreamEvent timerEvent = new(("timer", {}), TIMER, time:currentTime().time);
        StreamEvent[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        process(timerEventWrapper);
        _ = timer.stop();
        return ();
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == null) {
            return;
        }

        LinkedList complexEventChunks = new;

        lock {
            initTiming(check <StreamEvent>streamEventChunk.getFirst());

            while (streamEventChunk.hasNext()) {

                StreamEvent currStreamEvent = check <StreamEvent>streamEventChunk.next();

                if (currStreamEvent.eventType == TIMER) {
                    if (lastScheduledTime <= currStreamEvent.timestamp) {
                        // implies that there have not been any more events after this schedule has been done.
                        if (!flushed) {
                            flushToOutputChunk(complexEventChunks, lastCurrentEventTime, true);
                            flushed = true;
                        } else {
                            if (currentEventChunk.getFirst() != null) {
                                appendToOutputChunk(complexEventChunks, lastCurrentEventTime, true);
                            }
                        }

                        // rescheduling to emit the current batch after expiring it if no further events arrive.
                        lastScheduledTime = time:currentTime().time + schedulerTimeout;
                        timer = new task:Timer(self.invokeProcess, self.handleError, schedulerTimeout);
                        _ = timer.start();
                    }
                    continue;

                } else if (currStreamEvent.eventType != CURRENT) {
                    continue;
                }

                int currentEventTime = getTimestamp(currStreamEvent.data[timeStamp]);
                if (lastCurrentEventTime < currentEventTime) {
                    lastCurrentEventTime = currentEventTime;
                }

                if (currentEventTime < endTime) {
                    cloneAppend(currStreamEvent);
                } else {
                    if (flushed) {
                        appendToOutputChunk(complexEventChunks, lastCurrentEventTime, false);
                        flushed = false;
                    } else {
                        flushToOutputChunk(complexEventChunks, lastCurrentEventTime, false);
                    }
                    // update timestamp, call next processor
                    endTime = findEndTime(lastCurrentEventTime, startTime, timeToKeep);
                    cloneAppend(currStreamEvent);

                    // triggering the last batch expiration.
                    if (schedulerTimeout > 0) {
                        lastScheduledTime = time:currentTime().time + schedulerTimeout;
                        timer = new task:Timer(self.invokeProcess, self.handleError, schedulerTimeout);
                        _ = timer.start();
                    }
                }
            }
        }

        match nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (complexEventChunks.getSize() != 0) {
                    while (complexEventChunks.hasNext()) {
                        StreamEvent[] streamEvent = check <StreamEvent[]>complexEventChunks.next();
                        foreach event in streamEvent{
                        }
                        nxtProc(streamEvent);
                    }
                }
            }
            () => {
                //do nothing
            }
        }
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events;
        int i = 0;
        foreach e in currentEventChunk.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }

    public function handleError(error e) {
        io:println("Error occured", e);
    }

    public function cloneAppend(StreamEvent currStreamEvent) {
        StreamEvent clonedEvent = currStreamEvent.clone();
        if (replaceTimestampWithBatchEndTime) {
            clonedEvent.data[timeStamp] = endTime;
        }
        currentEventChunk.addLast(clonedEvent);

        if (resetEvent == null) {
            resetEvent = currStreamEvent.clone();
            resetEvent.eventType = RESET;
        }
    }

    public function flushToOutputChunk(LinkedList complexEventChunks, int currentTime, boolean preserveCurrentEvents) {
        LinkedList newEventChunk = new();
        if (expiredEventChunk.getFirst() != null) {
            // mark the timestamp for the expiredType event
            expiredEventChunk.resetToFront();
            while (expiredEventChunk.hasNext()) {
                StreamEvent expiredEvent = check <StreamEvent>expiredEventChunk.next();
                expiredEvent.timestamp = currentTime;
            }
            // add expired event to newEventChunk.
            expiredEventChunk.resetToFront();
            while (expiredEventChunk.hasNext()) {
                newEventChunk.addLast(expiredEventChunk.next());
            }
        }

        if (expiredEventChunk != null) {
            expiredEventChunk.clear();
        }

        if (currentEventChunk.getFirst() != null) {
            // add reset event in front of current events
            match resetEvent {
                StreamEvent streamEvent => {
                    streamEvent.timestamp = currentTime;
                    newEventChunk.addLast(streamEvent);
                    resetEvent = null;
                }
                () => {

                }
            }

            // move to expired events
            if (preserveCurrentEvents || storeExpiredEvents) {
                currentEventChunk.resetToFront();
                while (currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = check <StreamEvent>currentEventChunk.next();
                    StreamEvent toExpireEvent = currentEvent.clone();
                    toExpireEvent.eventType = EXPIRED;
                    expiredEventChunk.addLast(toExpireEvent);
                }
            }

            // add current event chunk to next processor
            currentEventChunk.resetToFront();
            while (currentEventChunk.hasNext()) {
                newEventChunk.addLast(currentEventChunk.next());
            }
        }
        currentEventChunk.clear();

        StreamEvent[] streamEvents = [];
        while (newEventChunk.hasNext()) {
            streamEvents[lengthof streamEvents] = check <StreamEvent>newEventChunk.next();
        }
        if (streamEvents != null) {
            complexEventChunks.addLast(streamEvents);
        }
    }


    public function appendToOutputChunk(LinkedList complexEventChunks, int currentTime, boolean preserveCurrentEvents) {
        LinkedList newEventChunk = new();
        LinkedList sentEventChunk = new();
        if (currentEventChunk.getFirst() != null) {
            if (expiredEventChunk.getFirst() != null) {
                // mark the timestamp for the expiredType event
                expiredEventChunk.resetToFront();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = check <StreamEvent>expiredEventChunk.next();

                    if (outputExpectsExpiredEvents) {
                        // add expired event to newEventChunk.
                        StreamEvent toExpireEvent = expiredEvent.clone();
                        toExpireEvent.timestamp = currentTime;
                        newEventChunk.addLast(toExpireEvent);
                    }

                    StreamEvent toSendEvent = expiredEvent.clone();
                    toSendEvent.eventType = CURRENT;
                    sentEventChunk.addLast(toSendEvent);
                }
            }

            // add reset event in front of current events
            match resetEvent {
                StreamEvent streamEvent => {
                    streamEvent.timestamp = currentTime;
                    newEventChunk.addLast(streamEvent);
                }
                () => {

                }
            }

            //add old events
            sentEventChunk.resetToFront();
            while (sentEventChunk.hasNext()) {
                newEventChunk.addLast(sentEventChunk.next());
            }

            // move to expired events
            if (preserveCurrentEvents || storeExpiredEvents) {
                currentEventChunk.resetToFront();
                while (currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = check <StreamEvent>currentEventChunk.next();
                    StreamEvent toExpireEvent = currentEvent.clone();
                    toExpireEvent.eventType = EXPIRED;
                    expiredEventChunk.addLast(toExpireEvent);
                }
            }

            // add current event chunk to next processor
            currentEventChunk.resetToFront();
            while (currentEventChunk.hasNext()) {
                newEventChunk.addLast(currentEventChunk.next());
            }
        }
        currentEventChunk.clear();

        StreamEvent[] streamEvents = [];
        while (newEventChunk.hasNext()) {
            streamEvents[lengthof streamEvents] = check <StreamEvent>newEventChunk.next();
        }
        if (streamEvents != null) {
            complexEventChunks.addLast(streamEvents);
        }

    }

    public function findEndTime(int currentTime, int startTime_, int timeToKeep_) returns (int) {
        int elapsedTimeSinceLastEmit = (currentTime - startTime_) % timeToKeep_;
        return (currentTime + (timeToKeep_ - elapsedTimeSinceLastEmit));
    }

    public function initTiming(StreamEvent firstStreamEvent) {
        if (endTime < 0) {
            if (isStartTimeEnabled) {
                endTime = startTime + timeToKeep;
            } else {
                startTime = getTimestamp(firstStreamEvent.data[timeStamp]);
                endTime = startTime + timeToKeep;
            }
            if (schedulerTimeout > 0) {
                lastScheduledTime = time:currentTime().time + schedulerTimeout;
                timer = new task:Timer(self.invokeProcess, self.handleError, schedulerTimeout);
                _ = timer.start();
            }
        }
    }

    public function getTimestamp(any val) returns (int) {
        match val {
            int value => return value;
            any => {
                error err = { message: "external timestamp should be of type int" };
                throw err;
            }
        }
    }
};

public function externalTimeBatchWindow(string timestamp, int time, int
    startTime = -1,                     int timeOut = -1, boolean replaceTimestampWithBatchEndTime = false, function (
                                        StreamEvent[])?
                                        nextProcessPointer = ()) returns Window {
    ExternalTimeBatchWindow timeWindow1 = new(nextProcessPointer, time, timestamp, startTime, timeOut,
        replaceTimestampWithBatchEndTime);
    return timeWindow1;
}

public type TimeLengthWindow object {

    public int timeInMilliSeconds;
    public int length;
    public int count = 0;
    public LinkedList expiredEventChunk;
    public function (StreamEvent[])? nextProcessPointer;
    public task:Timer? timer;

    public new(nextProcessPointer, timeInMilliSeconds, length) {
        expiredEventChunk = new;
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == null) {
            return;
        }

        lock {
            int currentTime = time:currentTime().time;

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                expiredEventChunk.resetToFront();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = check <StreamEvent>expiredEventChunk.next();
                    int timeDiff = expiredEvent.timestamp - currentTime + timeInMilliSeconds;
                    if (timeDiff <= 0) {
                        expiredEventChunk.removeCurrent();
                        count -= 1;
                        expiredEvent.timestamp = currentTime;
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                expiredEventChunk.resetToFront();
                if (streamEvent.eventType == CURRENT) {
                    StreamEvent clonedEvent = streamEvent.clone();
                    clonedEvent.eventType = EXPIRED;
                    if (count < length) {
                        count += 1;
                        expiredEventChunk.addLast(clonedEvent);
                    } else {
                        StreamEvent firstEvent = check <StreamEvent>expiredEventChunk.removeFirst();
                        if (firstEvent != null) {
                            firstEvent.timestamp = currentTime;
                            streamEventChunk.insertBeforeCurrent(firstEvent);
                            expiredEventChunk.addLast(clonedEvent);
                        }
                    }
                    timer = new task:Timer(self.invokeProcess, self.handleError, timeInMilliSeconds);
                } else {
                    streamEventChunk.removeCurrent();
                }

            }
        }

        match nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                        events[lengthof events] = streamEvent;
                    }
                    nxtProc(events);
                }
            }
            () => {
                //do nothing
            }
        }
    }

    public function invokeProcess() returns error? {
        StreamEvent timerEvent = new(("timer", {}), "TIMER", time:currentTime().time);
        StreamEvent[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        process(timerEventWrapper);
        _ = timer.stop();
        return ();
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events;
        int i = 0;
        foreach e in expiredEventChunk.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }

    public function handleError(error e) {
        io:println("Error occured", e);
    }

};

public function timeLengthWindow(int timeLength, int length, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    TimeLengthWindow timeLengthWindow1 = new(nextProcessPointer, timeLength, length);
    return timeLengthWindow1;
}

public type UniqueLengthWindow object {

    public string uniqueKey;
    public int length;
    public int count = 0;
    public map uniqueMap;
    public LinkedList expiredEventChunk;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, uniqueKey, length) {
        expiredEventChunk = new;
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == null) {
            return;
        }

        lock {
            int currentTime = time:currentTime().time;
            streamEventChunk.resetToFront();
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                StreamEvent clonedEvent = streamEvent.clone();
                clonedEvent.eventType = EXPIRED;
                StreamEvent eventClonedForMap = clonedEvent.clone();

                string str = <string>eventClonedForMap.data[uniqueKey];
                StreamEvent? oldEvent;
                if (uniqueMap[str] != null) {
                    oldEvent = check <StreamEvent>uniqueMap[str];
                }
                uniqueMap[str] = eventClonedForMap;

                if (oldEvent == null) {
                    count += 1;
                }
                if ((count <= length) && (oldEvent == null)) {
                    expiredEventChunk.addLast(clonedEvent);
                } else {
                    if (oldEvent != null) {
                        while (expiredEventChunk.hasNext()) {
                            StreamEvent firstEventExpired = check <StreamEvent>expiredEventChunk.next();
                            if (firstEventExpired.data[uniqueKey] == oldEvent.data[uniqueKey]) {
                                expiredEventChunk.removeCurrent();
                            }
                        }
                        expiredEventChunk.addLast(clonedEvent);
                        streamEventChunk.insertBeforeCurrent(oldEvent);
                        oldEvent.timestamp = currentTime;
                    } else {
                        StreamEvent firstEvent = check <StreamEvent>expiredEventChunk.removeFirst();
                        if (firstEvent != null) {
                            firstEvent.timestamp = currentTime;
                            streamEventChunk.insertBeforeCurrent(firstEvent);
                            expiredEventChunk.addLast(clonedEvent);
                        } else {
                            streamEventChunk.insertBeforeCurrent(clonedEvent);
                        }
                    }
                }
            }
        }

        match nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                        events[lengthof events] = streamEvent;
                    }
                    nxtProc(events);
                }
            }
            () => {
                //do nothing
            }
        }
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events;
        int i = 0;
        foreach e in expiredEventChunk.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }
};

public function uniqueLengthWindow(string uniqueKey, int length, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    UniqueLengthWindow uniqueLengthWindow1 = new(nextProcessPointer, uniqueKey, length);
    return uniqueLengthWindow1;
}

public type DelayWindow object {

    public int delayInMilliSeconds;
    public LinkedList delayedEventQueue;
    public int lastTimestamp = 0;
    public task:Timer? timer;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, delayInMilliSeconds) {
        delayedEventQueue = new;
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == null) {
            return;
        }

        lock {
            streamEventChunk.resetToFront();
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                int currentTime = time:currentTime().time;

                delayedEventQueue.resetToFront();
                while (delayedEventQueue.hasNext()) {
                    StreamEvent delayedEvent = check <StreamEvent>delayedEventQueue.next();
                    //check if the event has delayed expected time period
                    if (streamEvent.timestamp >= delayedEvent.timestamp + delayInMilliSeconds) {
                        delayedEventQueue.removeCurrent();
                        //insert delayed event before the current event to stream chunk
                        streamEventChunk.insertBeforeCurrent(delayedEvent);
                    } else {
                        break;
                    }
                }

                if (streamEvent.eventType == CURRENT) {
                    delayedEventQueue.addLast(streamEvent);

                    if (lastTimestamp < streamEvent.timestamp) {
                        //calculate the remaining time to delay the current event
                        int delay = delayInMilliSeconds - (currentTime - streamEvent.timestamp);
                        timer = new task:Timer(self.invokeProcess, self.handleError, delay);
                        _ = timer.start();
                        lastTimestamp = streamEvent.timestamp;
                    }
                }
                //current events are not processed, so remove the current event from the stream chunk
                streamEventChunk.removeCurrent();
            }
            delayedEventQueue.resetToFront();
        }

        match nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = check <StreamEvent>streamEventChunk.next();
                        events[lengthof events] = streamEvent;
                    }
                    nxtProc(events);
                }
            }
            () => {
                //do nothing
            }
        }
    }

    public function invokeProcess() returns error? {
        StreamEvent timerEvent = new(("timer", {}), "TIMER", time:currentTime().time);
        StreamEvent[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        process(timerEventWrapper);
        _ = timer.stop();
        return ();
    }

    public function handleError(error e) {
        io:println("Error occured", e);
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events;
        int i = 0;
        foreach e in delayedEventQueue.asArray() {
            match e {
                StreamEvent s => {
                    StreamEvent lshEvent = (isLHSTrigger) ? originEvent : s;
                    StreamEvent rhsEvent = (isLHSTrigger) ? s : originEvent;
                    match (conditionFunc) {
                        function (map e1Data, map e2Data) returns boolean conditionCheckFunc => {
                            if (conditionCheckFunc(lshEvent.data, rhsEvent.data)) {
                                events[i] = (lshEvent, rhsEvent);
                                i += 1;
                            }
                        }
                        () => {
                            events[i] = (lshEvent, rhsEvent);
                            i += 1;
                        }
                    }
                }
                any a => {
                }
            }
        }
        return events;
    }
};

public function delayWindow(int delayInMilliSeconds, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    DelayWindow delayWindow1 = new(nextProcessPointer, delayInMilliSeconds);
    return delayWindow1;
}
