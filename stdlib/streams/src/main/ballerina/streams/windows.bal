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
    public any[] windowParameters;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, windowParameters) {
        self.linkedList = new;
        self.initParameters(windowParameters);
        self.size = 0;
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 1) {
            match parameters[0] {
                int value => self.size = value;
                any anyValue => {
                    error err = error("Length window expects an int parameter");
                    panic err;
                }
            }
        } else {
            error err = error("Length window should only have one parameter (<int> " +
                "windowLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] outputEvents = [];
        foreach event in streamEvents {
            if (self.linkedList.getSize() == self.size) {
                match self.linkedList.removeFirst() {
                    StreamEvent streamEvent => {
                        outputEvents[outputEvents.length()] = streamEvent;
                    }

                    () => {
                        // do nothing
                    }

                    any anyValue => {
                        // do nothing
                    }
                }
            }

            outputEvents[outputEvents.length()] = event;
            StreamEvent expiredVeresionOfEvent = event.copy();
            expiredVeresionOfEvent.eventType = "EXPIRED";
            self.linkedList.addLast(expiredVeresionOfEvent);
        }
        match (self.nextProcessPointer) {
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
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in self.linkedList.asArray() {
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

public function lengthWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    LengthWindow lengthWindow1 = new(nextProcessPointer, windowParameters);
    return lengthWindow1;
}

public type TimeWindow object {

    public int timeInMillis;
    public any[] windowParameters;
    public LinkedList expiredEventQueue;
    public LinkedList timerQueue;
    public function (StreamEvent[])? nextProcessPointer;
    public int lastTimestamp = -0x8000000000000000;

    public new(nextProcessPointer, windowParameters) {
        self.timeInMillis = 0;
        self.expiredEventQueue = new;
        self.timerQueue = new;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 1) {
            match parameters[0] {
                int value => self.timeInMillis = value;
                any anyValue => {
                    error err = error("Time window expects an int parameter");
                    panic err;
                }
            }
        } else {
            error err = error("Time window should only have one parameter (<int> " +
                "windowTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach event in streamEvents {
                streamEventChunk.addLast(event);
            }

            streamEventChunk.resetToFront();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent;
                any? next = streamEventChunk.next();
                if (next is StreamEvent) {
                    streamEvent = next;
                } else {
                    return;
                }

                int currentTime = time:currentTime().time;
                self.expiredEventQueue.resetToFront();

                while (self.expiredEventQueue.hasNext()) {
                    StreamEvent expiredEvent = getStreamEvent(self.expiredEventQueue.next());
                    int timeDiff = (expiredEvent.timestamp - currentTime) + self.timeInMillis;
                    if (timeDiff <= 0) {
                        self.expiredEventQueue.removeCurrent();
                        expiredEvent.timestamp = currentTime;
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                if (streamEvent.eventType == "CURRENT") {
                    StreamEvent clonedEvent = streamEvent.copy();
                    clonedEvent.eventType = "EXPIRED";
                    self.expiredEventQueue.addLast(clonedEvent);

                    if (self.lastTimestamp < clonedEvent.timestamp) {
                        task:Timer timer = new task:Timer(self.invokeProcess, self.handleError, self.timeInMillis,
                            delay = self.timeInMillis - (time:currentTime().time - clonedEvent.timestamp));
                        _ = timer.start();
                        self.timerQueue.addLast(timer);
                        self.lastTimestamp = clonedEvent.timestamp;
                    }
                } else {
                    streamEventChunk.removeCurrent();
                }
            }
            self.expiredEventQueue.resetToFront();
        }
        match (self.nextProcessPointer) {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                        events[events.length()] = streamEvent;
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
        self.process(timerEventWrapper);
        if (!self.timerQueue.isEmpty()) {
            task:Timer timer = check <task:Timer>self.timerQueue.removeFirst();
            _ = timer.stop();
        }
        return ();
    }

    public function handleError(error e) {
        io:println("Error occured", e.reason());
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in self.expiredEventQueue.asArray() {
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

public function timeWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    TimeWindow timeWindow1 = new(nextProcessPointer, windowParameters);
    return timeWindow1;
}

public type LengthBatchWindow object {
    public int length;
    public any[] windowParameters;
    public int count;
    public StreamEvent? resetEvent;
    public LinkedList currentEventQueue;
    public LinkedList? expiredEventQueue;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, windowParameters) {
        self.length = 0;
        self.count = 0;
        self.resetEvent = ();
        self.currentEventQueue = new();
        self.expiredEventQueue = ();
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 1) {
            match parameters[0] {
                int value => self.length = value;
                any anyValue => {
                    error err = error("LengthBatch window expects an int parameter");
                    panic err;
                }
            }
        } else {
            error err = error("LengthBatch window should only have one parameter (<int> " +
                "windowBatchLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunks = new();
        LinkedList outputStreamEventChunk = new();
        int currentTime = time:currentTime().time;

        foreach event in streamEvents {
            StreamEvent clonedStreamEvent = event.copy();
            self.currentEventQueue.addLast(clonedStreamEvent);
            self.count += 1;
            if (self.count == self.length) {
                //if (expiredEventQueue.getFirst() != ()) {
                //    expiredEventQueue.clear();
                //}
                if (self.currentEventQueue.getFirst() != ()) {
                    if (!(self.resetEvent is ())) {
                        outputStreamEventChunk.addLast(self.resetEvent);
                        self.resetEvent = ();
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
                    StreamEvent firstInCurrentEventQueue = getStreamEvent(self.currentEventQueue.getFirst());
                    self.resetEvent = createResetStreamEvent(firstInCurrentEventQueue);
                    foreach currentEvent in self.currentEventQueue.asArray() {
                        outputStreamEventChunk.addLast(currentEvent);
                    }
                }
                self.currentEventQueue.clear();
                self.count = 0;
                if (outputStreamEventChunk.getFirst() != ()) {
                    streamEventChunks.addLast(outputStreamEventChunk);
                }
            }
        }

        match (self.nextProcessPointer) {
            function (StreamEvent[]) nxtProc => {
                streamEventChunks.resetToFront();
                while streamEventChunks.hasNext() {
                    StreamEvent[] events = [];
                    LinkedList streamEventChunk;
                    any? next = streamEventChunks.next();
                    if (next is LinkedList) {
                        streamEventChunk = next;
                    } else {
                        return;
                    }
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                        events[events.length()] = streamEvent;
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
        foreach e in self.currentEventQueue.asArray() {
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

public function lengthBatchWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    LengthBatchWindow lengthBatch = new(nextProcessPointer, windowParameters);
    return lengthBatch;
}


public type TimeBatchWindow object {
    public int timeInMilliSeconds;
    public any[] windowParameters;
    public int nextEmitTime = -1;
    public LinkedList currentEventQueue;
    public LinkedList? expiredEventQueue;
    public StreamEvent? resetEvent;
    public task:Timer? timer;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, windowParameters) {
        self.timeInMilliSeconds = 0;
        self.resetEvent = ();
        self.timer = ();
        self.currentEventQueue = new();
        self.expiredEventQueue = ();
        self.initParameters(self.windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 1) {
            match parameters[0] {
                int value => self.timeInMilliSeconds = value;
                any anyValue => {
                    error err = error("TimeBatch window expects an int parameter");
                    panic err;
                }
            }
        } else {
            error err = error("TimeBatch window should only have one parameter (<int> " +
                "windowBatchTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    public function invokeProcess() returns error? {
        StreamEvent timerEvent = new(("timer", {}), "TIMER", time:currentTime().time);
        StreamEvent[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        self.process(timerEventWrapper);
        return ();
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList outputStreamEvents = new();
        if (self.nextEmitTime == -1) {
            self.nextEmitTime = time:currentTime().time + self.timeInMilliSeconds;
            self.timer = new task:Timer(self.invokeProcess, self.handleError, self.timeInMilliSeconds, delay =
                self.timeInMilliSeconds);
            _ = self.timer.start();
        }

        int currentTime = time:currentTime().time;
        boolean sendEvents = false;

        if (currentTime >= self.nextEmitTime) {
            self.nextEmitTime += self.timeInMilliSeconds;
            self.timer.stop();
            self.timer = new task:Timer(self.invokeProcess, self.handleError, self.timeInMilliSeconds, delay =
                self.timeInMilliSeconds);
            _ = self.timer.start();
            sendEvents = true;
        } else {
            sendEvents = false;
        }

        foreach event in streamEvents {
            if (event.eventType != "CURRENT") {
                continue;
            }
            StreamEvent clonedEvent = event.copy();
            self.currentEventQueue.addLast(clonedEvent);
        }
        if (sendEvents) {
            if (self.currentEventQueue.getFirst() != ()) {
                if (!(self.resetEvent is ())) {
                    outputStreamEvents.addLast(self.resetEvent);
                    self.resetEvent = ();
                }
                self.resetEvent = createResetStreamEvent(getStreamEvent(self.currentEventQueue.getFirst()));
                self.currentEventQueue.resetToFront();
                while (self.currentEventQueue.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(self.currentEventQueue.next());
                    outputStreamEvents.addLast(streamEvent);
                }
            }
            self.currentEventQueue.clear();
        }
        match self.nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (outputStreamEvents.getSize() != 0) {
                    StreamEvent[] events = [];
                    outputStreamEvents.resetToFront();
                    while (outputStreamEvents.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(outputStreamEvents.next());
                        events[events.length()] = streamEvent;
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
        foreach e in self.currentEventQueue.asArray() {
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
        io:println("Error occured", e.reason());
    }
};

public function timeBatchWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    TimeBatchWindow timeBatch = new(nextProcessPointer, windowParameters);
    return timeBatch;
}

public type ExternalTimeWindow object {

    public int timeInMillis;
    public any[] windowParameters;
    public LinkedList expiredEventQueue;
    public function (StreamEvent[])? nextProcessPointer;
    public string timeStamp;

    public new(nextProcessPointer, windowParameters) {
        self.timeInMillis = 0;
        self.timeStamp = "";
        self.expiredEventQueue = new;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 2) {
            match parameters[0] {
                string value => self.timeStamp = value;
                any anyValue => {
                    error err = error("ExternalTime window's first parameter, timestamp should be of type string");
                    panic err;
                }
            }

            match parameters[1] {
                int value => self.timeInMillis = value;
                any anyValue => {
                    error err = error("ExternalTime window's second parameter, windowTime should be of type int");
                    panic err;
                }
            }
        } else {
            error err = error("ExternalTime window should only have two parameters (<string> timestamp, <int> " +
                "windowTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    public function process(StreamEvent[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach event in streamEvents {
                streamEventChunk.addLast(event);
            }

            streamEventChunk.resetToFront();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                int currentTime = self.getTimestamp(streamEvent.data[self.timeStamp]);
                self.expiredEventQueue.resetToFront();

                while (self.expiredEventQueue.hasNext()) {
                    StreamEvent expiredEvent = getStreamEvent(self.expiredEventQueue.next());
                    int timeDiff = (self.getTimestamp(expiredEvent.data[self.timeStamp]) - currentTime) +
                        self.timeInMillis;
                    if (timeDiff <= 0) {
                        self.expiredEventQueue.removeCurrent();
                        expiredEvent.timestamp = currentTime;
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        self.expiredEventQueue.resetToFront();
                        break;
                    }
                }

                if (streamEvent.eventType == CURRENT) {
                    StreamEvent clonedEvent = streamEvent.copy();
                    clonedEvent.eventType = EXPIRED;
                    self.expiredEventQueue.addLast(clonedEvent);
                }
                self.expiredEventQueue.resetToFront();
            }
        }
        match self.nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                        events[events.length()] = streamEvent;
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
        foreach e in self.expiredEventQueue.asArray() {
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
                error err = error("external timestamp should be of type int");
                panic err;
            }
        }
    }
};

public function externalTimeWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {

    ExternalTimeWindow timeWindow1 = new(nextProcessPointer, windowParameters);
    return timeWindow1;
}

public type ExternalTimeBatchWindow object {
    public int timeToKeep;
    public LinkedList currentEventChunk;
    public LinkedList expiredEventChunk;
    public StreamEvent? resetEvent = null;
    public int startTime = -1;
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
    public any[] windowParameters;

    public new(nextProcessPointer, windowParameters) {
        self.timeToKeep = 0;
        self.lastScheduledTime = 0;
        self.timer = ();
        self.timeStamp = "";
        self.currentEventChunk = new();
        self.expiredEventChunk = new;

        self.initParameters(windowParameters);

        if (self.startTime != -1) {
            self.isStartTimeEnabled = true;
        }
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() >= 2 && parameters.length() <= 5) {
            match parameters[0] {
                string value => self.timeStamp = value;
                any anyValue => {
                    error err = error("ExternalTimeBatch window's first parameter, timestamp should be of type " +
                        "string");
                    panic err;
                }
            }

            match parameters[1] {
                int value => self.timeToKeep = value;
                any anyValue => {
                    error err = error("ExternalTimeBatch window's second parameter, windowTime should be of " +
                        "type int");
                    panic err;
                }
            }

            if(parameters.length() >= 3) {
                match parameters[2] {
                    int value => self.startTime = value;
                    () => self.startTime = -1;
                    any anyValue => {
                        error err = error("ExternalTimeBatch window's third parameter, startTime should be of " +
                            "type int");
                        panic err;
                    }
                }
            }

            if(parameters.length() >= 4) {
                match parameters[3] {
                    int value => self.schedulerTimeout = value;
                    () => self.schedulerTimeout = -1;
                    any anyValue => {
                        error err = error("ExternalTimeBatch window's fourth parameter, timeout should be of " +
                            "type int");
                        panic err;
                    }
                }
            }

            if(parameters.length() == 5) {
                match parameters[4] {
                    boolean value => self.replaceTimestampWithBatchEndTime = value;
                    () => self.replaceTimestampWithBatchEndTime = false;
                    any anyValue => {
                        error err = error("ExternalTimeBatch window's fifth parameter, " +
                            "replaceTimestampWithBatchEndTime should be of type boolean");
                        panic err;
                    }
                }
            }

        } else {
            error err = error("ExternalTimeBatch window should only have two to five " +
                "parameters (<string> timestamp, <int> windowTime, <int> startTime, <int> " +
                "timeout, <boolean> replaceTimestampWithBatchEndTime), but found " + parameters.length()
                + " input attributes");
            panic err;
        }
    }

    public function invokeProcess() returns error? {
        StreamEvent timerEvent = new(("timer", {}), TIMER, time:currentTime().time);
        StreamEvent[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        self.process(timerEventWrapper);
        _ = self.timer.stop();
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
            self.initTiming(getStreamEvent(streamEventChunk.getFirst()));

            while (streamEventChunk.hasNext()) {

                StreamEvent currStreamEvent = getStreamEvent(streamEventChunk.next());

                if (currStreamEvent.eventType == TIMER) {
                    if (self.lastScheduledTime <= currStreamEvent.timestamp) {
                        // implies that there have not been any more events after this schedule has been done.
                        if (!self.flushed) {
                            self.flushToOutputChunk(complexEventChunks, self.lastCurrentEventTime, true);
                            self.flushed = true;
                        } else {
                            if (!(self.currentEventChunk.getFirst() is ())) {
                                self.appendToOutputChunk(complexEventChunks, self.lastCurrentEventTime, true);
                            }
                        }

                        // rescheduling to emit the current batch after expiring it if no further events arrive.
                        self.lastScheduledTime = time:currentTime().time + self.schedulerTimeout;
                        self.timer = new task:Timer(self.invokeProcess, self.handleError, self.schedulerTimeout);
                        _ = self.timer.start();
                    }
                    continue;

                } else if (currStreamEvent.eventType != CURRENT) {
                    continue;
                }

                int currentEventTime = self.getTimestamp(currStreamEvent.data[self.timeStamp]);
                if (self.lastCurrentEventTime < currentEventTime) {
                    self.lastCurrentEventTime = currentEventTime;
                }

                if (currentEventTime < self.endTime) {
                    self.cloneAppend(currStreamEvent);
                } else {
                    if (self.flushed) {
                        self.appendToOutputChunk(complexEventChunks, self.lastCurrentEventTime, false);
                        self.flushed = false;
                    } else {
                        self.flushToOutputChunk(complexEventChunks, self.lastCurrentEventTime, false);
                    }
                    // update timestamp, call next processor
                    self.endTime = self.findEndTime(self.lastCurrentEventTime, self.startTime, self.timeToKeep);
                    self.cloneAppend(currStreamEvent);

                    // triggering the last batch expiration.
                    if (self.schedulerTimeout > 0) {
                        self.lastScheduledTime = time:currentTime().time + self.schedulerTimeout;
                        self.timer = new task:Timer(self.invokeProcess, self.handleError, self.schedulerTimeout);
                        _ = self.timer.start();
                    }
                }
            }
        }

        match self.nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (complexEventChunks.getSize() != 0) {
                    while (complexEventChunks.hasNext()) {
                        StreamEvent[] streamEvent;
                        any? next = complexEventChunks.next();
                        if (next is StreamEvent[]) {
                            streamEvent = next;
                        } else {
                            return;
                        }
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
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in self.currentEventChunk.asArray() {
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
        io:println("Error occured", e.reason());
    }

    public function cloneAppend(StreamEvent currStreamEvent) {
        StreamEvent clonedEvent = currStreamEvent.copy();
        if (self.replaceTimestampWithBatchEndTime) {
            clonedEvent.data[self.timeStamp] = self.endTime;
        }
        self.currentEventChunk.addLast(clonedEvent);

        if (self.resetEvent is ()) {
            self.resetEvent = currStreamEvent.copy();
            self.resetEvent.eventType = RESET;
        }
    }

    public function flushToOutputChunk(LinkedList complexEventChunks, int currentTime, boolean preserveCurrentEvents) {
        LinkedList newEventChunk = new();
        if (self.expiredEventChunk.getFirst() != null) {
            // mark the timestamp for the expiredType event
            self.expiredEventChunk.resetToFront();
            while (self.expiredEventChunk.hasNext()) {
                StreamEvent expiredEvent = getStreamEvent(self.expiredEventChunk.next());
                expiredEvent.timestamp = currentTime;
            }
            // add expired event to newEventChunk.
            self.expiredEventChunk.resetToFront();
            while (self.expiredEventChunk.hasNext()) {
                newEventChunk.addLast(self.expiredEventChunk.next());
            }
        }

        if (self.expiredEventChunk != null) {
            self.expiredEventChunk.clear();
        }

        if (self.currentEventChunk.getFirst() != null) {
            // add reset event in front of current events
            match self.resetEvent {
                StreamEvent streamEvent => {
                    streamEvent.timestamp = currentTime;
                    newEventChunk.addLast(streamEvent);
                    self.resetEvent = ();
                }
                () => {

                }
            }

            // move to expired events
            if (preserveCurrentEvents || self.storeExpiredEvents) {
                self.currentEventChunk.resetToFront();
                while (self.currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = getStreamEvent(self.currentEventChunk.next());
                    StreamEvent toExpireEvent = currentEvent.copy();
                    toExpireEvent.eventType = EXPIRED;
                    self.expiredEventChunk.addLast(toExpireEvent);
                }
            }

            // add current event chunk to next processor
            self.currentEventChunk.resetToFront();
            while (self.currentEventChunk.hasNext()) {
                newEventChunk.addLast(self.currentEventChunk.next());
            }
        }
        self.currentEventChunk.clear();

        StreamEvent[] streamEvents = [];
        while (newEventChunk.hasNext()) {
            streamEvents[streamEvents.length()] = getStreamEvent(newEventChunk.next());
        }
        complexEventChunks.addLast(streamEvents);
    }


    public function appendToOutputChunk(LinkedList complexEventChunks, int currentTime, boolean preserveCurrentEvents) {
        LinkedList newEventChunk = new();
        LinkedList sentEventChunk = new();
        if (self.currentEventChunk.getFirst() != null) {
            if (self.expiredEventChunk.getFirst() != null) {
                // mark the timestamp for the expiredType event
                self.expiredEventChunk.resetToFront();
                while (self.expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = getStreamEvent(self.expiredEventChunk.next());

                    if (self.outputExpectsExpiredEvents) {
                        // add expired event to newEventChunk.
                        StreamEvent toExpireEvent = expiredEvent.copy();
                        toExpireEvent.timestamp = currentTime;
                        newEventChunk.addLast(toExpireEvent);
                    }

                    StreamEvent toSendEvent = expiredEvent.copy();
                    toSendEvent.eventType = CURRENT;
                    sentEventChunk.addLast(toSendEvent);
                }
            }

            // add reset event in front of current events
            match self.resetEvent {
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
            if (preserveCurrentEvents || self.storeExpiredEvents) {
                self.currentEventChunk.resetToFront();
                while (self.currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = getStreamEvent(self.currentEventChunk.next());
                    StreamEvent toExpireEvent = currentEvent.copy();
                    toExpireEvent.eventType = EXPIRED;
                    self.expiredEventChunk.addLast(toExpireEvent);
                }
            }

            // add current event chunk to next processor
            self.currentEventChunk.resetToFront();
            while (self.currentEventChunk.hasNext()) {
                newEventChunk.addLast(self.currentEventChunk.next());
            }
        }
        self.currentEventChunk.clear();

        StreamEvent[] streamEvents = [];
        while (newEventChunk.hasNext()) {
            streamEvents[streamEvents.length()] = getStreamEvent(newEventChunk.next());
        }
        complexEventChunks.addLast(streamEvents);
    }

    public function findEndTime(int currentTime, int startTime_, int timeToKeep_) returns (int) {
        int elapsedTimeSinceLastEmit = (currentTime - startTime_) % timeToKeep_;
        return (currentTime + (timeToKeep_ - elapsedTimeSinceLastEmit));
    }

    public function initTiming(StreamEvent firstStreamEvent) {
        if (self.endTime < 0) {
            if (self.isStartTimeEnabled) {
                self.endTime = self.startTime + self.timeToKeep;
            } else {
                self.startTime = self.getTimestamp(firstStreamEvent.data[self.timeStamp]);
                self.endTime = self.startTime + self.timeToKeep;
            }
            if (self.schedulerTimeout > 0) {
                self.lastScheduledTime = time:currentTime().time + self.schedulerTimeout;
                self.timer = new task:Timer(self.invokeProcess, self.handleError, self.schedulerTimeout);
                _ = self.timer.start();
            }
        }
    }

    public function getTimestamp(any val) returns (int) {
        match val {
            int value => return value;
            any => {
                error err = error("external timestamp should be of type int");
                panic err;
            }
        }
    }
};

public function externalTimeBatchWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    ExternalTimeBatchWindow timeWindow1 = new(nextProcessPointer, windowParameters);
    return timeWindow1;
}

public type TimeLengthWindow object {

    public int timeInMilliSeconds;
    public int length;
    public any[] windowParameters;
    public int count = 0;
    public LinkedList expiredEventChunk;
    public function (StreamEvent[])? nextProcessPointer;
    public task:Timer? timer;

    public new(nextProcessPointer, windowParameters) {
        self.timeInMilliSeconds = 0;
        self.length = 0;
        self.timer = ();
        self.expiredEventChunk = new;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 2) {
            match parameters[0] {
                int value => self.timeInMilliSeconds = value;
                any anyValue => {
                    error err = error("TimeLength window's first parameter, windowTime should be of type int");
                    panic err;
                }
            }

            match parameters[1] {
                int value => self.length = value;
                any anyValue => {
                    error err = error("TimeLength window's second parameter, windowLength should be of type int");
                    panic err;
                }
            }
        } else {
            error err = error("TimeLength window should only have two parameters (<int> windowTime, <int> " +
                "windowLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
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
                StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                self.expiredEventChunk.resetToFront();
                while (self.expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = getStreamEvent(self.expiredEventChunk.next());
                    int timeDiff = expiredEvent.timestamp - currentTime + self.timeInMilliSeconds;
                    if (timeDiff <= 0) {
                        self.expiredEventChunk.removeCurrent();
                        self.count -= 1;
                        expiredEvent.timestamp = currentTime;
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                self.expiredEventChunk.resetToFront();
                if (streamEvent.eventType == CURRENT) {
                    StreamEvent clonedEvent = streamEvent.copy();
                    clonedEvent.eventType = EXPIRED;
                    if (self.count < self.length) {
                        self.count += 1;
                        self.expiredEventChunk.addLast(clonedEvent);
                    } else {
                        StreamEvent firstEvent = getStreamEvent(self.expiredEventChunk.removeFirst());
                        if (firstEvent != null) {
                            firstEvent.timestamp = currentTime;
                            streamEventChunk.insertBeforeCurrent(firstEvent);
                            self.expiredEventChunk.addLast(clonedEvent);
                        }
                    }
                    self.timer = new task:Timer(self.invokeProcess, self.handleError, self.timeInMilliSeconds);
                } else {
                    streamEventChunk.removeCurrent();
                }

            }
        }

        match self.nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                        events[events.length()] = streamEvent;
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
        self.process(timerEventWrapper);
        _ = self.timer.stop();
        return ();
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in self.expiredEventChunk.asArray() {
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
        io:println("Error occured", e.reason());
    }

};

public function timeLengthWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    TimeLengthWindow timeLengthWindow1 = new(nextProcessPointer, windowParameters);
    return timeLengthWindow1;
}

public type UniqueLengthWindow object {

    public string uniqueKey;
    public int length;
    public any[] windowParameters;
    public int count = 0;
    public map<StreamEvent> uniqueMap;
    public LinkedList expiredEventChunk;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, windowParameters) {
        self.uniqueKey = "";
        self.length = 0;
        self.uniqueMap = {};
        self.expiredEventChunk = new;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 2) {
            match parameters[0] {
                string value => self.uniqueKey = value;
                any anyValue => {
                    error err = error("UniqueLength window's first parameter, uniqueAttribute should be of type " +
                        "string");
                    panic err;
                }
            }

            match parameters[1] {
                int value => self.length = value;
                any anyValue => {
                    error err = error("UniqueLength window's second parameter, windowLength should be of type
                    int");
                    panic err;
                }
            }
        } else {
            error err = error("UniqueLength window should only have two parameters (<string> uniqueAttribute, " +
                "<int> windowLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
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
                StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                StreamEvent clonedEvent = streamEvent.copy();
                clonedEvent.eventType = EXPIRED;
                StreamEvent eventClonedForMap = clonedEvent.copy();

                anydata? data = eventClonedForMap.data[self.uniqueKey];
                StreamEvent? oldEvent;
                if (data is string) {
                    if (self.uniqueMap[data] is StreamEvent) {
                        oldEvent = self.uniqueMap[data];
                    }
                    self.uniqueMap[data] = eventClonedForMap;
                }

                if (oldEvent is ()) {
                    self.count += 1;
                }


                if ((self.count <= self.length) &&
                    !(oldEvent is StreamEvent)) { // TODO: oldEvent is () can not be used - a bug!
                    self.expiredEventChunk.addLast(clonedEvent);
                } else {
                    if (oldEvent is StreamEvent) {
                        while (self.expiredEventChunk.hasNext()) {
                            StreamEvent firstEventExpired = getStreamEvent(self.expiredEventChunk.next());
                            if (firstEventExpired.data[self.uniqueKey] == oldEvent.data[self.uniqueKey]) {
                                self.expiredEventChunk.removeCurrent();
                            }
                        }
                        self.expiredEventChunk.addLast(clonedEvent);
                        streamEventChunk.insertBeforeCurrent(oldEvent);
                        oldEvent.timestamp = currentTime;
                    } else {
                        StreamEvent firstEvent = getStreamEvent(self.expiredEventChunk.removeFirst());
                        if (firstEvent != null) {
                            firstEvent.timestamp = currentTime;
                            streamEventChunk.insertBeforeCurrent(firstEvent);
                            self.expiredEventChunk.addLast(clonedEvent);
                        } else {
                            streamEventChunk.insertBeforeCurrent(clonedEvent);
                        }
                    }
                }
            }
        }

        match self.nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                        events[events.length()] = streamEvent;
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
        foreach e in self.expiredEventChunk.asArray() {
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

public function uniqueLengthWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    UniqueLengthWindow uniqueLengthWindow1 = new(nextProcessPointer, windowParameters);
    return uniqueLengthWindow1;
}

public type DelayWindow object {

    public int delayInMilliSeconds;
    public any[] windowParameters;
    public LinkedList delayedEventQueue;
    public int lastTimestamp = 0;
    public task:Timer? timer;
    public function (StreamEvent[])? nextProcessPointer;

    public new(nextProcessPointer, windowParameters) {
        self.delayInMilliSeconds = 0;
        self.timer = ();
        self.delayedEventQueue = new;
        self.initParameters(self.windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(parameters.length() == 1) {
            match parameters[0] {
                int value => self.delayInMilliSeconds = value;
                any anyValue => {
                    error err = error("Delay window expects an int parameter");
                    panic err;
                }
            }
        } else {
            error err = error("Delay window should only have one parameter (<int> " +
                "delayTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
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
                StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                int currentTime = time:currentTime().time;

                self.delayedEventQueue.resetToFront();
                while (self.delayedEventQueue.hasNext()) {
                    StreamEvent delayedEvent = getStreamEvent(self.delayedEventQueue.next());
                    //check if the event has delayed expected time period
                    if (streamEvent.timestamp >= delayedEvent.timestamp + self.delayInMilliSeconds) {
                        self.delayedEventQueue.removeCurrent();
                        //insert delayed event before the current event to stream chunk
                        streamEventChunk.insertBeforeCurrent(delayedEvent);
                    } else {
                        break;
                    }
                }

                if (streamEvent.eventType == CURRENT) {
                    self.delayedEventQueue.addLast(streamEvent);

                    if (self.lastTimestamp < streamEvent.timestamp) {
                        //calculate the remaining time to delay the current event
                        int delay = self.delayInMilliSeconds - (currentTime - streamEvent.timestamp);
                        self.timer = new task:Timer(self.invokeProcess, self.handleError, delay);
                        _ = self.timer.start();
                        self.lastTimestamp = streamEvent.timestamp;
                    }
                }
                //current events are not processed, so remove the current event from the stream chunk
                streamEventChunk.removeCurrent();
            }
            self.delayedEventQueue.resetToFront();
        }

        match self.nextProcessPointer {
            function (StreamEvent[]) nxtProc => {
                if (streamEventChunk.getSize() != 0) {
                    StreamEvent[] events = [];
                    streamEventChunk.resetToFront();
                    while (streamEventChunk.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                        events[events.length()] = streamEvent;
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
        self.process(timerEventWrapper);
        _ = self.timer.stop();
        return ();
    }

    public function handleError(error e) {
        io:println("Error occured", e.reason());
    }

    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map e1Data, map e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach e in self.delayedEventQueue.asArray() {
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

public function delayWindow(any[] windowParameters, function (StreamEvent[])? nextProcessPointer = ())
                    returns Window {
    DelayWindow delayWindow1 = new(nextProcessPointer, windowParameters);
    return delayWindow1;
}
