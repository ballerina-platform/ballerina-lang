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

import ballerina/task;
import ballerina/time;

# The `Window` abstract objects is the base object for implementing windows in Ballerina streams. The `process`
# function contains the logic of processing events when events are received. `getCandidateEvents` function is used
# inside the `Select` object to return the events in the window to perform joining.
# The window names in the window objects cannot be used in the queries. Always a function which returns the specific
# window has to be used in streaing query.
# E.g. If `LengthWindow` has to be used in a streaming query, the function `streams:length` has to be used for
# streaming  query without the module identifier `streams`. An example is shown below.
#
#       from inputStream window `length`(5)
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
public type Window abstract object {

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents);

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[];
};

# The `LengthWindow` is a sliding length window, that holds last windowLength events, and gets updated on every event
# arrival and expiry.
# E.g.
#       from inputStream window `length(5)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `length` window should only have one parameter (<int> windowLength)
#
# + size - description
# + linkedList - description
# + windowParameters - description
# + nextProcessPointer - description
public type LengthWindow object {
    *Window;
    *Snapshotable;
    public int size;
    public LinkedList linkedList;
    public any[] windowParameters;
    public function (StreamEvent?[])? nextProcessPointer;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.linkedList = new;
        self.size = 0;
        self.initParameters(windowParameters);
    }


    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any value = parameters[0];
            if (value is int) {
                self.size = value;
            } else {
                error err = error("Length window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("Length window should only have one parameter (<int> " +
                "windowLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] outputEvents = [];
        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent>evt;
            if (self.linkedList.getSize() == self.size) {

                any anyValue = self.linkedList.removeFirst();
                if (anyValue is StreamEvent) {
                    outputEvents[outputEvents.length()] = anyValue;
                }
            }

            outputEvents[outputEvents.length()] = event;
            StreamEvent expiredVeresionOfEvent = event.copy();
            expiredVeresionOfEvent.eventType = "EXPIRED";
            self.linkedList.addLast(expiredVeresionOfEvent);
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            nextProcessFuncPointer.call(outputEvents);
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.linkedList.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] eventsList = toSnapshottableEvents(self.linkedList.asArray());
        return {
            "eventsList": eventsList
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var eventsList = state["eventsList"];
        if (eventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] streamEvents = toStreamEvents(eventsList);
            self.linkedList = new;
            self.linkedList.addAll(streamEvents);
        }
    }
};

# The `length` function creates a `LengthWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function length(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    LengthWindow lengthWindow1 = new(nextProcessPointer, windowParameters);
    return lengthWindow1;
}

# The `TimeWindow` is a sliding time window, that holds events for that arrived during last windowTime period, and
# gets updated on every event arrival and expiry.
# E.g.
#       from inputStream window `time(5000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `time` window should only have one parameter (<int> windowTime)
#
# + timeInMillis - description
# + windowParameters - description
# + expiredEventQueue - description
# + nextProcessPointer - description
# + lastTimestamp - description
# + scheduler - description
public type TimeWindow object {
    *Window;
    *Snapshotable;
    public int timeInMillis;
    public any[] windowParameters;
    public LinkedList expiredEventQueue;
    public function (StreamEvent?[])? nextProcessPointer;
    public int lastTimestamp = -0x8000000000000000;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMillis = 0;
        self.expiredEventQueue = new;
        self.initParameters(windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any parameter0 = parameters[0];
            if (parameter0 is int) {
                self.timeInMillis = parameter0;
            } else {
                error err = error("Time window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("Time window should only have one parameter (<int> " +
                "windowTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach var event in streamEvents {
                streamEventChunk.addLast(event);
            }

            streamEventChunk.resetToFront();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = <StreamEvent>streamEventChunk.next();
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
                        self.scheduler.notifyAt(clonedEvent.timestamp + self.timeInMillis);
                        self.lastTimestamp = clonedEvent.timestamp;
                    }
                } else {
                    streamEventChunk.removeCurrent();
                }
            }
            self.expiredEventQueue.resetToFront();
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.expiredEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] expiredEventsList = toSnapshottableEvents(self.expiredEventQueue.asArray());
        return {
            "expiredEventsList": expiredEventsList,
            "lastTimestamp": self.lastTimestamp
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var expiredEventsList = state["expiredEventsList"];
        if (expiredEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] streamEvents = toStreamEvents(expiredEventsList);
            self.expiredEventQueue = new;
            self.expiredEventQueue.addAll(streamEvents);
        }
        any lastTimestamp = state["lastTimestamp"];
        if (lastTimestamp is int) {
            self.lastTimestamp = lastTimestamp;
        }
    }
};

# The `time` function creates a `TimeWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function time(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    TimeWindow timeWindow1 = new(nextProcessPointer, windowParameters);
    return timeWindow1;
}

# This is a batch (tumbling) length window, that holds up to the given length of events, and gets updated on every
# given number of events arrival.
# E.g.
#       from inputStream window `lengthBatch(5)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `lengthBatch` window should only have one parameter (<int> windowBatchLength)
#
# + length - description
# + windowParameters - description
# + count - description
# + resetEvent - description
# + currentEventQueue - description
# + nextProcessPointer - description
public type LengthBatchWindow object {
    *Window;
    *Snapshotable;
    public int length;
    public any[] windowParameters;
    public int count;
    public StreamEvent? resetEvent;
    public LinkedList currentEventQueue;
    public (function (StreamEvent?[]))? nextProcessPointer;

    public function __init((function (StreamEvent?[]))? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.length = 0;
        self.count = 0;
        self.resetEvent = ();
        self.currentEventQueue = new();
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any parameter0 = parameters[0];
            if (parameter0 is int) {
                self.length = parameter0;
            } else {
                error err = error("LengthBatch window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("LengthBatch window should only have one parameter (<int> " +
                "windowBatchLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunks = new();
        LinkedList outputStreamEventChunk = new();
        int currentTime = time:currentTime().time;

        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent>evt;
            StreamEvent clonedStreamEvent = event.copy();
            self.currentEventQueue.addLast(clonedStreamEvent);
            self.count += 1;
            if (self.count == self.length) {
                if (self.currentEventQueue.getFirst() != ()) {
                    if (!(self.resetEvent is ())) {
                        outputStreamEventChunk.addLast(self.resetEvent);
                        self.resetEvent = ();
                    }
                    StreamEvent firstInCurrentEventQueue = getStreamEvent(self.currentEventQueue.getFirst());
                    self.resetEvent = createResetStreamEvent(firstInCurrentEventQueue);
                    foreach var currentEvent in self.currentEventQueue.asArray() {
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

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            streamEventChunks.resetToFront();
            while streamEventChunks.hasNext() {
                StreamEvent?[] events = [];
                LinkedList streamEventChunk;
                any next = streamEventChunks.next();
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
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.currentEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] currentEventsList = toSnapshottableEvents(self.currentEventQueue.asArray());
        StreamEvent? resetStreamEvt = self.resetEvent;
        SnapshottableStreamEvent? resetEvt = (resetStreamEvt is StreamEvent)
            ? toSnapshottableEvent(resetStreamEvt) : ();
        return {
            "currentEventsList": currentEventsList,
            "resetEvt": resetEvt,
            "count": self.count
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var currentEventsList = state["currentEventsList"];
        if (currentEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] currentEvents = toStreamEvents(currentEventsList);
            self.currentEventQueue = new;
            self.currentEventQueue.addAll(currentEvents);
        }
        var resetEvt = state["resetEvt"];
        if (resetEvt is SnapshottableStreamEvent) {
            StreamEvent r = toStreamEvent(resetEvt);
            self.resetEvent = r;
        }
        any c = state["count"];
        if (c is int) {
            self.count = c;
        }
    }
};

# The `lengthBatch` function creates a `LengthBatchWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function lengthBatch(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    LengthBatchWindow lengthBatchWindow = new(nextProcessPointer, windowParameters);
    return lengthBatchWindow;
}

# This is a batch (tumbling) time window, that holds events arrived between window time periods, and gets updated for
# every window time.
# E.g.
#       from inputStream window `timeBatch(5000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `timeBatch` window should only have one parameter (<int> windowBatchTime)
#
# + timeInMilliSeconds - description
# + windowParameters - description
# + nextEmitTime - description
# + currentEventQueue - description
# + resetEvent - description
# + nextProcessPointer - description
# + scheduler - description
public type TimeBatchWindow object {
    *Window;
    *Snapshotable;
    public int timeInMilliSeconds;
    public any[] windowParameters;
    public int nextEmitTime = -1;
    public LinkedList currentEventQueue;
    public StreamEvent? resetEvent;
    public function (StreamEvent?[])? nextProcessPointer;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMilliSeconds = 0;
        self.resetEvent = ();
        self.currentEventQueue = new();
        self.initParameters(self.windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any parameter0 = parameters[0];
            if (parameter0 is int) {
                self.timeInMilliSeconds = parameter0;
            } else {
                error err = error("TimeBatch window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("TimeBatch window should only have one parameter (<int> " +
                "windowBatchTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList outputStreamEvents = new();
        if (self.nextEmitTime == -1) {
            self.nextEmitTime = time:currentTime().time + self.timeInMilliSeconds;
            self.scheduler.notifyAt(self.nextEmitTime);
        }

        int currentTime = time:currentTime().time;
        boolean sendEvents = false;

        if (currentTime >= self.nextEmitTime) {
            self.nextEmitTime += self.timeInMilliSeconds;
            self.scheduler.notifyAt(self.nextEmitTime);
            sendEvents = true;
        } else {
            sendEvents = false;
        }

        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent>evt;
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

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (outputStreamEvents.getSize() != 0) {
                StreamEvent?[] events = [];
                outputStreamEvents.resetToFront();
                while (outputStreamEvents.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(outputStreamEvents.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.currentEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] currentEventsList = toSnapshottableEvents(self.currentEventQueue.asArray());
        StreamEvent? resetStreamEvt = self.resetEvent;
        SnapshottableStreamEvent? resetEvt = (resetStreamEvt is StreamEvent)
            ? toSnapshottableEvent(resetStreamEvt) : ();
        return {
            "currentEventsList": currentEventsList,
            "resetEvt": resetEvt,
            "timeInMilliSeconds": self.timeInMilliSeconds,
            "nextEmitTime": self.nextEmitTime
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var currentEventsList = state["currentEventsList"];
        if (currentEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] currentEvents = toStreamEvents(currentEventsList);
            self.currentEventQueue = new;
            self.currentEventQueue.addAll(currentEvents);
        }
        var resetEvt = state["resetEvt"];
        if (resetEvt is SnapshottableStreamEvent) {
            StreamEvent r = toStreamEvent(resetEvt);
            self.resetEvent = r;
        }
        any millis = state["timeInMilliSeconds"];
        if (millis is int) {
            self.timeInMilliSeconds = millis;
        }
        any nxtEmitTime = state["nextEmitTime"];
        if (nxtEmitTime is int) {
            self.nextEmitTime = nxtEmitTime;
        }
    }
};

# The `timeBatch` function creates a `TimeBatchWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function timeBatch(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    TimeBatchWindow timeBatchWindow = new(nextProcessPointer, windowParameters);
    return timeBatchWindow;
}

# This is a sliding time window based on external time, that holds events for that arrived during last window time
# period from the external timestamp, and gets updated on every monotonically increasing timestamp.
# E.g.
#       from inputStream window `externalTime(inputStream.timestamp, 4000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `externalTime` window should only have two parameters (timestamp field, <int> windowTime)
#
# + timeInMillis - description
# + windowParameters - description
# + expiredEventQueue - description
# + nextProcessPointer - description
# + timeStamp - description
public type ExternalTimeWindow object {
    *Window;
    *Snapshotable;
    public int timeInMillis;
    public any[] windowParameters;
    public LinkedList expiredEventQueue;
    public function (StreamEvent?[])? nextProcessPointer;
    public string timeStamp;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMillis = 0;
        self.timeStamp = "";
        self.expiredEventQueue = new;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 2) {
            any parameter0 = parameters[0];
            if (parameter0 is string) {
                self.timeStamp = parameter0;
            } else {
                error err = error("ExternalTime window's first parameter, timestamp should be of type string");
                panic err;
            }

            any parameter1 = parameters[1];
            if (parameter1 is int) {
                self.timeInMillis = parameter1;
            } else {
                error err = error("ExternalTime window's second parameter, windowTime should be of type int");
                panic err;
            }
        } else {
            error err = error("ExternalTime window should only have two parameters (<string> timestamp, <int> " +
                "windowTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach var event in streamEvents {
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

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.expiredEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] expiredEventsList = toSnapshottableEvents(self.expiredEventQueue.asArray());
        return {
            "expiredEventsList": expiredEventsList,
            "timeInMillis": self.timeInMillis,
            "timeStamp": self.timeStamp
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var expiredEventsList = state["expiredEventsList"];
        if (expiredEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] expiredEvents = toStreamEvents(expiredEventsList);
            self.expiredEventQueue = new;
            self.expiredEventQueue.addAll(expiredEvents);
        }
        any millis = state["timeInMillis"];
        if (millis is int) {
            self.timeInMillis = millis;
        }
        any ts = state["timeStamp"];
        if (ts is string) {
            self.timeStamp = ts;
        }
    }

    public function getTimestamp(any val) returns (int) {
        if (val is int) {
            return val;
        } else {
            error err = error("external timestamp should be of type int");
            panic err;
        }
    }
};

# The `externalTime` function creates a `ExternalTimeWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function externalTime(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {

    ExternalTimeWindow timeWindow1 = new(nextProcessPointer, windowParameters);
    return timeWindow1;
}

# This is a batch (tumbling) time window based on external time, that holds events arrived during window time periods,
# and gets updated for every window time.
# E.g.
#       from inputStream window `externalTimeBatch(inputStream.timestamp, 1000, 500, 1200, true)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `externalTimeBatch` window should only have two to five parameters (timestamp field, <int> windowTime, <int>
# startTime, <int> timeout, <boolean> replaceTimestampWithBatchEndTime)
#
# + timeToKeep - description
# + currentEventChunk - description
# + expiredEventChunk - description
# + resetEvent - description
# + startTime - description
# + isStartTimeEnabled - description
# + replaceTimestampWithBatchEndTime - description
# + flushed - description
# + endTime - description
# + schedulerTimeout - description
# + lastScheduledTime - description
# + lastCurrentEventTime - description
# + nextProcessPointer - description
# + timeStamp - description
# + storeExpiredEvents - description
# + outputExpectsExpiredEvents - description
# + windowParameters - description
# + scheduler - description
public type ExternalTimeBatchWindow object {
    *Window;
    *Snapshotable;
    public int timeToKeep;
    public LinkedList currentEventChunk;
    public LinkedList expiredEventChunk;
    public StreamEvent? resetEvent = ();
    public int startTime = -1;
    public boolean isStartTimeEnabled = false;
    public boolean replaceTimestampWithBatchEndTime = false;
    public boolean flushed = false;
    public int endTime = -1;
    public int schedulerTimeout = 0;
    public int lastScheduledTime;
    public int lastCurrentEventTime = 0;
    public function (StreamEvent?[])? nextProcessPointer;
    public string timeStamp;
    public boolean storeExpiredEvents = false;
    public boolean outputExpectsExpiredEvents = false;
    public any[] windowParameters;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeToKeep = 0;
        self.lastScheduledTime = 0;
        self.timeStamp = "";
        self.currentEventChunk = new();
        self.expiredEventChunk = new;

        self.initParameters(windowParameters);

        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });

        if (self.startTime != -1) {
            self.isStartTimeEnabled = true;
        }
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() >= 2 && parameters.length() <= 5) {

            any parameter0 = parameters[0];
            if (parameter0 is string) {
                self.timeStamp = parameter0;
            } else {
                error err = error("ExternalTimeBatch window's first parameter, timestamp should be of type " +
                    "string");
                panic err;
            }

            any parameter1 = parameters[1];
            if (parameter1 is int) {
                self.timeToKeep = parameter1;
            } else {
                error err = error("ExternalTimeBatch window's second parameter, windowTime should be of " +
                    "type int");
                panic err;
            }

            if (parameters.length() >= 3) {
                any parameter2 = parameters[2];
                if (parameter2 is int) {
                    self.startTime = parameter2;
                } else if (parameter2 is ()) {
                    self.startTime = -1;
                } else {
                    error err = error("ExternalTimeBatch window's third parameter, startTime should be of " +
                        "type int");
                    panic err;
                }
            }

            if (parameters.length() >= 4) {
                any parameter3 = parameters[3];
                if (parameter3 is int) {
                    self.schedulerTimeout = parameter3;
                } else if (parameter3 is ()) {
                    self.schedulerTimeout = -1;
                } else {
                    error err = error("ExternalTimeBatch window's fourth parameter, timeout should be of " +
                        "type int");
                    panic err;
                }
            }

            if (parameters.length() == 5) {
                any parameter4 = parameters[4];
                if (parameter4 is boolean) {
                    self.replaceTimestampWithBatchEndTime = parameter4;
                } else if (parameter4 is ()) {
                    self.replaceTimestampWithBatchEndTime = false;
                } else {
                    error err = error("ExternalTimeBatch window's fifth parameter, " +
                        "replaceTimestampWithBatchEndTime should be of type boolean");
                    panic err;
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

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach var event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == ()) {
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
                        self.scheduler.notifyAt(self.lastScheduledTime);
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
                        self.scheduler.notifyAt(self.lastScheduledTime);
                    }
                }
            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (complexEventChunks.getSize() != 0) {
                while (complexEventChunks.hasNext()) {
                    StreamEvent?[] streamEvent;
                    any next = complexEventChunks.next();
                    if (next is StreamEvent?[]) {
                        streamEvent = next;
                    } else {
                        return;
                    }
                    foreach var event in streamEvent{
                    }
                    nextProcessFuncPointer.call(streamEvent);
                }
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.currentEventChunk.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] currentEventsList = toSnapshottableEvents(self.currentEventChunk.asArray());
        SnapshottableStreamEvent?[] expiredEventsList = toSnapshottableEvents(self.expiredEventChunk.asArray());
        StreamEvent? resetStreamEvt = self.resetEvent;
        SnapshottableStreamEvent? resetEvt = (resetStreamEvt is StreamEvent)
            ? toSnapshottableEvent(resetStreamEvt) : ();
        return {
            "currentEventsList": currentEventsList,
            "expiredEventsList": expiredEventsList,
            "resetEvt": resetEvt,
            "flushed": self.flushed,
            "endTime": self.endTime,
            "lastScheduledTime": self.lastScheduledTime,
            "lastCurrentEventTime": self.lastCurrentEventTime,
            "storeExpiredEvents": self.storeExpiredEvents,
            "outputExpectsExpiredEvents": self.outputExpectsExpiredEvents
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var currentEventsList = state["currentEventsList"];
        if (currentEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] currentEvents = toStreamEvents(currentEventsList);
            self.currentEventChunk = new;
            self.currentEventChunk.addAll(currentEvents);
        }
        var expiredEventsList = state["expiredEventsList"];
        if (expiredEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] expiredEvents = toStreamEvents(expiredEventsList);
            self.expiredEventChunk = new;
            self.expiredEventChunk.addAll(expiredEvents);
        }
        var resetEvt = state["resetEvt"];
        if (resetEvt is SnapshottableStreamEvent) {
            StreamEvent r = toStreamEvent(resetEvt);
            self.resetEvent = r;
        }
        any f = state["flushed"];
        if (f is boolean) {
            self.flushed = f;
        }
        any et = state["endTime"];
        if (et is int) {
            self.endTime = et;
        }
        any lst = state["lastScheduledTime"];
        if (lst is int) {
            self.lastScheduledTime = lst;
        }
        any lct = state["lastCurrentEventTime"];
        if (lct is int) {
            self.lastCurrentEventTime = lct;
        }
        any se = state["storeExpiredEvents"];
        if (se is boolean) {
            self.storeExpiredEvents = se;
        }
        any outExp = state["outputExpectsExpiredEvents"];
        if (outExp is boolean) {
            self.outputExpectsExpiredEvents = outExp;
        }
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
        if (self.expiredEventChunk.getFirst() != ()) {
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

        if (self.expiredEventChunk != ()) {
            self.expiredEventChunk.clear();
        }

        if (self.currentEventChunk.getFirst() != ()) {
            // add reset event in front of current events
            any streamEvent = self.resetEvent;
            if (streamEvent is StreamEvent) {
                streamEvent.timestamp = currentTime;
                newEventChunk.addLast(streamEvent);
                self.resetEvent = ();
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

        StreamEvent?[] streamEvents = [];
        while (newEventChunk.hasNext()) {
            streamEvents[streamEvents.length()] = getStreamEvent(newEventChunk.next());
        }
        complexEventChunks.addLast(streamEvents);
    }


    public function appendToOutputChunk(LinkedList complexEventChunks, int currentTime, boolean preserveCurrentEvents) {
        LinkedList newEventChunk = new();
        LinkedList sentEventChunk = new();
        if (self.currentEventChunk.getFirst() != ()) {
            if (self.expiredEventChunk.getFirst() != ()) {
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
            any streamEvent = self.resetEvent;
            if (streamEvent is StreamEvent) {
                streamEvent.timestamp = currentTime;
                newEventChunk.addLast(streamEvent);
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

        StreamEvent?[] streamEvents = [];
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
                self.scheduler.notifyAt(self.lastScheduledTime);
            }
        }
    }

    public function getTimestamp(any val) returns (int) {
        if (val is int) {
            return val;
        } else {
            error err = error("external timestamp should be of type int");
            panic err;
        }
    }
};

# The `externalTimeBatch` function creates a `ExternalTimeBatchWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function externalTimeBatch(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    ExternalTimeBatchWindow timeWindow1 = new(nextProcessPointer, windowParameters);
    return timeWindow1;
}

# This is a sliding time window that, at a given time holds the last windowLength events that arrived during last
# windowTime period, and gets updated for every event arrival and expiry.
# E.g.
#       from inputStream window `timeLength(4000, 10)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `timeLength` window should only have two parameters (<int> windowTime, <int> windowLength)
#
# + timeInMilliSeconds - description
# + length - description
# + windowParameters - description
# + count - description
# + expiredEventChunk - description
# + nextProcessPointer - description
# + scheduler - description
public type TimeLengthWindow object {
    *Window;
    *Snapshotable;
    public int timeInMilliSeconds;
    public int length;
    public any[] windowParameters;
    public int count = 0;
    public LinkedList expiredEventChunk;
    public function (StreamEvent?[])? nextProcessPointer;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMilliSeconds = 0;
        self.length = 0;
        self.expiredEventChunk = new;
        self.initParameters(windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 2) {

            any parameter0 = parameters[0];
            if(parameter0 is int) {
                self.timeInMilliSeconds = parameter0;
            } else {
                error err = error("TimeLength window's first parameter, windowTime should be of type int");
                panic err;
            }

            any parameter1 = parameters[1];
            if(parameter1 is int) {
                self.length = parameter1;
            } else {
                error err = error("TimeLength window's second parameter, windowLength should be of type int");
                panic err;
            }
        } else {
            error err = error("TimeLength window should only have two parameters (<int> windowTime, <int> " +
                "windowLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach var event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == ()) {
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
                        if (firstEvent != ()) {
                            firstEvent.timestamp = currentTime;
                            streamEventChunk.insertBeforeCurrent(firstEvent);
                            self.expiredEventChunk.addLast(clonedEvent);
                        }
                    }
                    self.scheduler.notifyAt(clonedEvent.timestamp + self.timeInMilliSeconds);
                } else {
                    streamEventChunk.removeCurrent();
                }

            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.expiredEventChunk.asArray() {
            if(e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if(conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] expiredEventsList = toSnapshottableEvents(self.expiredEventChunk.asArray());
        return {
            "expiredEventsList": expiredEventsList,
            "count": self.count
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var expiredEventsList = state["expiredEventsList"];
        if (expiredEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] expiredEvents = toStreamEvents(expiredEventsList);
            self.expiredEventChunk = new;
            self.expiredEventChunk.addAll(expiredEvents);
        }
        any c = state["count"];
        if (c is int) {
            self.count = c;
        }
    }
};

# The `timeLength` function creates a `TimeLengthWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function timeLength(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    TimeLengthWindow timeLengthWindow1 = new(nextProcessPointer, windowParameters);
    return timeLengthWindow1;
}

# This is a length window which only keeps the unique events.
# E.g.
#       from inputStream window `uniqueLength(inputStream.timestamp, 4000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `uniqueLength` window should only have two parameters (stream field, <int> windowLength)
#
# + uniqueKey - description
# + length - description
# + windowParameters - description
# + count - description
# + uniqueMap - description
# + expiredEventChunk - description
# + nextProcessPointer - description
public type UniqueLengthWindow object {
    *Window;
    *Snapshotable;
    public string uniqueKey;
    public int length;
    public any[] windowParameters;
    public int count = 0;
    public map<StreamEvent> uniqueMap;
    public LinkedList expiredEventChunk;
    public function (StreamEvent?[])? nextProcessPointer;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.uniqueKey = "";
        self.length = 0;
        self.uniqueMap = {};
        self.expiredEventChunk = new;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 2) {

            any parameter0 = parameters[0];
            if(parameter0 is string) {
                self.uniqueKey = parameter0;
            } else {
                error err = error("UniqueLength window's first parameter, uniqueAttribute should be of type " +
                    "string");
                panic err;
            }

            any parameter1 = parameters[1];
            if(parameter1 is int) {
                self.length = parameter1;
            } else {
                error err = error("UniqueLength window's second parameter, windowLength should be of type
                    int");
                panic err;
            }
        } else {
            error err = error("UniqueLength window should only have two parameters (<string> uniqueAttribute, " +
                "<int> windowLength), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach var event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == ()) {
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

                anydata valueOrNil = eventClonedForMap.data[self.uniqueKey];
                string str = string.convert(valueOrNil);
                StreamEvent? oldEvent;
                if (self.uniqueMap[str] is StreamEvent) {
                    oldEvent = self.uniqueMap[str];
                }
                self.uniqueMap[str] = eventClonedForMap;

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
                        if (firstEvent != ()) {
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

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.expiredEventChunk.asArray() {
            if(e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if(conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] expiredEventsList = toSnapshottableEvents(self.expiredEventChunk.asArray());
        map<SnapshottableStreamEvent> uMap = {};
        foreach var (k, v) in self.uniqueMap {
            uMap[k] = toSnapshottableEvent(v);
        }
        return {
            "expiredEventsList": expiredEventsList,
            "uMap": uMap,
            "count": self.count
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var expiredEventsList = state["expiredEventsList"];
        if (expiredEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] expiredEvents = toStreamEvents(expiredEventsList);
            self.expiredEventChunk = new;
            self.expiredEventChunk.addAll(expiredEvents);
        }
        var uMap = state["uMap"];
        if (uMap is map<SnapshottableStreamEvent>) {
            self.uniqueMap = {};
            foreach var (k, v) in uMap {
                self.uniqueMap[k] = toStreamEvent(v);
            }
        }
        any c = state["count"];
        if (c is int) {
            self.count = c;
        }
    }
};

# The `uniqueLength` function creates a `UniqueLengthWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function uniqueLength(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    UniqueLengthWindow uniqueLengthWindow1 = new(nextProcessPointer, windowParameters);
    return uniqueLengthWindow1;
}

# This window will delay the incoming events for a given amount of time.
# E.g.
#       from inputStream window `delay(4000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `delay` window should only have one parameter (<int> delayTime)
#
# + delayInMilliSeconds - description
# + windowParameters - description
# + delayedEventQueue - description
# + lastTimestamp - description
# + nextProcessPointer - description
# + scheduler - description
public type DelayWindow object {
    *Window;
    *Snapshotable;
    public int delayInMilliSeconds;
    public any[] windowParameters;
    public LinkedList delayedEventQueue;
    public int lastTimestamp = 0;
    public function (StreamEvent?[])? nextProcessPointer;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.delayInMilliSeconds = 0;
        self.delayedEventQueue = new;
        self.initParameters(self.windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });

    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any parameter0 = parameters[0];
            if(parameter0 is int) {
                self.delayInMilliSeconds = parameter0;
            } else {
                error err = error("Delay window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("Delay window should only have one parameter (<int> " +
                "delayTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach var event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == ()) {
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
                        int delayInMillis = self.delayInMilliSeconds - (currentTime - streamEvent.timestamp);
                        self.scheduler.notifyAt(delayInMillis);
                        self.lastTimestamp = streamEvent.timestamp;
                    }
                }
                //current events are not processed, so remove the current event from the stream chunk
                streamEventChunk.removeCurrent();
            }
            self.delayedEventQueue.resetToFront();
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.delayedEventQueue.asArray() {
            if(e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if(conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] delayedEventsList = toSnapshottableEvents(self.delayedEventQueue.asArray());
        return {
            "delayedEventsList": delayedEventsList,
            "lastTimestamp": self.lastTimestamp
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var delayedEventsList = state["delayedEventsList"];
        if (delayedEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] delayedEvents = toStreamEvents(delayedEventsList);
            self.delayedEventQueue = new;
            self.delayedEventQueue.addAll(delayedEvents);
        }
        any lts = state["lastTimestamp"];
        if (lts is int) {
            self.lastTimestamp = lts;
        }
    }
};

# The `delay` function creates a `DelayWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function delay(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    DelayWindow delayWindow1 = new(nextProcessPointer, windowParameters);
    return delayWindow1;
}

# The sort window hold a given number of events and emit the expired events in the ordered by the given fields.
# E.g.
#       from inputStream window `sort(10, inputStream.age, "ascending", inputStream.name, "descending")`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `sort` window should have three or more odd no of parameters (<int> windowLength, stream field, <string>
# order1,  stream field, <string> order2, ...)
#
# + lengthToKeep - description
# + windowParameters - description
# + sortedWindow - description
# + fields - description
# + sortTypes - description
# + nextProcessPointer - description
# + fieldFuncs - description
# + mergeSort - description
public type SortWindow object {
    *Window;
    *Snapshotable;
    public int lengthToKeep;
    public any [] windowParameters;
    public LinkedList sortedWindow;
    public string[] fields;
    public string[] sortTypes;
    public function (StreamEvent?[])? nextProcessPointer;
    public (function (map<anydata>) returns anydata)?[] fieldFuncs;
    public MergeSort mergeSort;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.sortedWindow = new;
        self.lengthToKeep = 0;
        self.fields = [];
        self.sortTypes = [];
        self.fieldFuncs = [];
        self.mergeSort = new(self.fieldFuncs, self.sortTypes);
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(!(parameters.length() >= 3 && parameters.length() % 2 == 1)) {
            error err = error("Sort window should have three or more odd no of" +
                "parameters (<int> windowLength, <string> attribute1, <string> order1, " +
                "<string> attribute2, <string> order2, ...), but found " + parameters.length()
                + " input attributes" );
            panic err;
        }

        any parameter0 = parameters[0];
        if(parameter0 is int) {
            self.lengthToKeep = parameter0;
        } else {
            error err = error("Sort window's first parameter, windowLength should be of type int");
            panic err;
        }

        int i = 1;
        while(i < parameters.length()) {
            any nextParameter = parameters[i];
            if(nextParameter is string) {
                if (i % 2 == 1) {
                    self.fields[self.fields.length()] = nextParameter;
                } else {
                    if (nextParameter == ASCENDING || nextParameter == DESCENDING) {
                        self.sortTypes[self.sortTypes.length()] = nextParameter;
                    } else {
                        error err = error("Expected ascending or descending at parameter " + (i + 1) +
                            " of sort window");
                        panic err;
                    }
                }
            } else if(nextParameter is int) {
                error err = error("Expected string parameter at parameter " + (i + 1) +
                    " of sort window, but found <int>");
                panic err;
            } else if(nextParameter is float) {
                error err = error("Expected string parameter at parameter " + (i + 1) +
                    " of sort window, but found <float>");
                panic err;
            } else if(nextParameter is boolean) {
                error err = error("Expected string parameter at parameter " + (i + 1) +
                    " of sort window, but found <boolean>");
                panic err;
            } else {
                error err = error("Incompatible parameter type" );
                panic err;
            }
            i += 1;
        }

        foreach string field in self.fields {
            self.fieldFuncs[self.fieldFuncs.length()] = function (map<anydata> x) returns anydata {
                return x[field];
            };
        }

        self.mergeSort = new(self.fieldFuncs, self.sortTypes);
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    #
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach var event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() == ()) {
            return;
        }

        lock {
            int currentTime = time:currentTime().time;

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = <StreamEvent>streamEventChunk.next();

                StreamEvent clonedEvent = streamEvent.copy();
                clonedEvent.eventType = EXPIRED;

                self.sortedWindow.addLast(clonedEvent);
                if (self.sortedWindow.getSize() > self.lengthToKeep) {
                    StreamEvent?[] events = [];
                    self.sortedWindow.resetToFront();

                    while (self.sortedWindow.hasNext()) {
                        StreamEvent streamEven = <StreamEvent>self.sortedWindow.next();
                        events[events.length()] = streamEven;
                    }

                    self.mergeSort.topDownMergeSort(events);
                    self.sortedWindow.clear();
                    foreach var event in events {
                        self.sortedWindow.addLast(event);
                    }

                    StreamEvent expiredEvent = <StreamEvent>self.sortedWindow.removeLast();
                    expiredEvent.timestamp = currentTime;
                    streamEventChunk.insertBeforeCurrent(expiredEvent);
                }
            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = <StreamEvent>streamEventChunk.next();
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.sortedWindow.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] sortEventsList = toSnapshottableEvents(self.sortedWindow.asArray());
        return {
            "sortEventsList": sortEventsList
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var sortEventsList = state["sortEventsList"];
        if (sortEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] sortedEvents = toStreamEvents(sortEventsList);
            self.sortedWindow = new;
            self.sortedWindow.addAll(sortedEvents);
        }
    }
};

# The `sort` function creates a `SortWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function sort(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    SortWindow sortWindow1 = new(nextProcessPointer, windowParameters);
    return sortWindow1;
}

# The  `TimeAccumulatingWindow` holds the events but if the events are not received for a specific time period, the
# collected events are released, at the point the time exceeds the given time period from the time when the last
# event is received.
# E.g.
#       from inputStream window `timeAccum(4000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# Time accumulating window should only have one parameter (<int> timePeriod)
#
# + timeInMillis - description
# + windowParameters - description
# + currentEventQueue - description
# + resetEvent - description
# + nextProcessPointer - description
# + lastTimestamp - description
# + scheduler - description
public type TimeAccumulatingWindow object {
    *Window;
    *Snapshotable;
    public int timeInMillis;
    public any[] windowParameters;
    public LinkedList currentEventQueue;
    public StreamEvent? resetEvent;
    public function (StreamEvent?[])? nextProcessPointer;
    public int lastTimestamp = -1;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMillis = 0;
        self.currentEventQueue = new;
        self.resetEvent = ();
        self.initParameters(windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any parameter0 = parameters[0];
            if (parameter0 is int) {
                self.timeInMillis = parameter0;
            } else {
                error err = error("Time accumulating window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("Time accumulating window should only have one parameter (<int> " +
                "timePeriod), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList outputStreamEvents = new();

        int currentTime = time:currentTime().time;
        boolean sendEvents = false;

        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent> evt;
            if (event.eventType != "CURRENT") {
                continue;
            }

            if (event.eventType == "CURRENT" && event.timestamp < self.lastTimestamp - self.timeInMillis) {
                continue;
            }

            StreamEvent clonedEvent = event.copy();

            if(self.lastTimestamp < clonedEvent.timestamp) {
                self.lastTimestamp = clonedEvent.timestamp;
            }

            self.currentEventQueue.addLast(clonedEvent);
            self.scheduler.notifyAt(self.lastTimestamp + self.timeInMillis);
        }

        if (self.currentEventQueue.getLast() != ()) {
            if (currentTime - self.lastTimestamp >= self.timeInMillis) {
                self.resetEvent = createResetStreamEvent(getStreamEvent(self.currentEventQueue.getFirst()));
                outputStreamEvents.addLast(self.resetEvent);
                self.currentEventQueue.resetToFront();
                while (self.currentEventQueue.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(self.currentEventQueue.next());
                    outputStreamEvents.addLast(streamEvent);
                }
                self.currentEventQueue.clear();
            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (outputStreamEvents.getSize() != 0) {
                StreamEvent?[] events = [];
                outputStreamEvents.resetToFront();
                while (outputStreamEvents.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(outputStreamEvents.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.currentEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] currentEventsList = toSnapshottableEvents(self.currentEventQueue.asArray());
        StreamEvent? resetStreamEvt = self.resetEvent;
        SnapshottableStreamEvent? resetEvt = (resetStreamEvt is StreamEvent)
            ? toSnapshottableEvent(resetStreamEvt) : ();
        return {
            "currentEventsList": currentEventsList,
            "resetEvt": resetEvt,
            "lastTimestamp": self.lastTimestamp
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var currentEventsList = state["currentEventsList"];
        if (currentEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] currentEvents = toStreamEvents(currentEventsList);
            self.currentEventQueue = new;
            self.currentEventQueue.addAll(currentEvents);
        }
        any lts = state["lastTimestamp"];
        if (lts is int) {
            self.lastTimestamp = lts;
        }
        var resetEvt = state["resetEvt"];
        if (resetEvt is SnapshottableStreamEvent) {
            StreamEvent r = toStreamEvent(resetEvt);
            self.resetEvent = r;
        }
    }
};

# The `timeAccum` function creates a `TimeAccumulatingWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function timeAccum(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    TimeAccumulatingWindow timeAccumulatingWindow1 = new(nextProcessPointer, windowParameters);
    return timeAccumulatingWindow1;
}

# The hopping window releases the events in batches defined by a time period every given time interval. The batch is
# also determined by  the time period given in the window. When the time interval the events being released and the
# time period it hold the events are equal, the hopping window acts as a `TimeBatch` window.
# E.g.
#       from inputStream window `hopping(5000, 4000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# Hopping window should only have two parameters (<int> windowTime, <int> hoppingTime)
#
# + timeInMilliSeconds - description
# + hoppingTime - description
# + windowParameters - description
# + nextEmitTime - description
# + currentEventQueue - description
# + resetEvent - description
# + nextProcessPointer - description
# + scheduler - description
public type HoppingWindow object {
    *Window;
    *Snapshotable;
    public int timeInMilliSeconds;
    public int hoppingTime;
    public any[] windowParameters;
    public int nextEmitTime = -1;
    public LinkedList currentEventQueue;
    public StreamEvent? resetEvent;
    public function (StreamEvent?[])? nextProcessPointer;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMilliSeconds = 0;
        self.hoppingTime = 0;
        self.resetEvent = ();
        self.currentEventQueue = new();
        self.initParameters(self.windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 2) {
            any windowTimeParam = parameters[0];
            if (windowTimeParam is int) {
                self.timeInMilliSeconds = windowTimeParam;
            } else {
                error err = error("Hopping window's first parameter, windowTime should be of type int");
                panic err;
            }

            any hoppingTimeParam = parameters[1];
            if (hoppingTimeParam is int) {
                self.hoppingTime = hoppingTimeParam;
            } else {
                error err = error("Hopping window's second parameter, hoppingTime should be of type int");
                panic err;
            }
        } else {
            error err = error("Hopping window should only have two parameters (<int> windowTime, <int> " +
                "hoppingTime), but found " + parameters.length() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList outputStreamEvents = new();
        if (self.nextEmitTime == -1) {
            self.nextEmitTime = time:currentTime().time + self.hoppingTime;
            self.scheduler.notifyAt(self.nextEmitTime);
        }

        int currentTime = time:currentTime().time;
        boolean sendEvents = false;

        if (currentTime >= self.nextEmitTime) {
            self.nextEmitTime += self.hoppingTime;
            self.scheduler.notifyAt(self.nextEmitTime);
            sendEvents = true;
        } else {
            sendEvents = false;
        }

        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent>evt;
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
                    if (streamEvent.timestamp >= self.nextEmitTime - self.hoppingTime - self
                        .timeInMilliSeconds && streamEvent.timestamp < self.nextEmitTime - self.hoppingTime) {
                        outputStreamEvents.addLast(streamEvent);
                    } else {
                        self.currentEventQueue.removeCurrent();
                    }
                }
            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (outputStreamEvents.getSize() != 0) {
                StreamEvent?[] events = [];
                outputStreamEvents.resetToFront();
                while (outputStreamEvents.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(outputStreamEvents.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.currentEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] currentEventsList = toSnapshottableEvents(self.currentEventQueue.asArray());
        StreamEvent? resetStreamEvt = self.resetEvent;
        SnapshottableStreamEvent? resetEvt = (resetStreamEvt is StreamEvent)
            ? toSnapshottableEvent(resetStreamEvt) : ();
        return {
            "currentEventsList": currentEventsList,
            "resetEvt": resetEvt,
            "nextEmitTime": self.nextEmitTime
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var currentEventsList = state["currentEventsList"];
        if (currentEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] currentEvents = toStreamEvents(currentEventsList);
            self.currentEventQueue = new;
            self.currentEventQueue.addAll(currentEvents);
        }
        var resetEvt = state["resetEvt"];
        if (resetEvt is SnapshottableStreamEvent) {
            StreamEvent r = toStreamEvent(resetEvt);
            self.resetEvent = r;
        }
        any n = state["nextEmitTime"];
        if (n is int) {
            self.nextEmitTime = n;
        }
    }
};

# The `hopping` function creates a `HoppingWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function hopping(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {
    HoppingWindow hoppingWindow = new(nextProcessPointer, windowParameters);
    return hoppingWindow;
}

# The `TimeOrderWindow` sorts the events to be expired by the given timestamp field by comparing that timestamp value
# to engine system time.
# E.g.
#       from inputStream window `timeOrder(inputStream.timestamp, 4000, true)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# `timeOrder` window should only have three parameters (timestamp field, <int> windowTime, <boolean> dropOlderEvents)
#
# + timeInMillis - description
# + windowParameters - description
# + expiredEventQueue - description
# + nextProcessPointer - description
# + timestamp - description
# + lastTimestamp - description
# + dropOlderEvents - description
# + mergeSort - description
# + scheduler - description
public type TimeOrderWindow object {
    *Window;
    *Snapshotable;
    public int timeInMillis;
    public any[] windowParameters;
    public LinkedList expiredEventQueue;
    public function (StreamEvent?[])? nextProcessPointer;
    public string timestamp;
    public int lastTimestamp;
    public boolean dropOlderEvents;
    public MergeSort mergeSort;
    public Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMillis = 0;
        self.lastTimestamp = 0;
        self.timestamp = "";
        self.dropOlderEvents = false;
        self.expiredEventQueue = new;
        self.mergeSort = new([], []);
        self.initParameters(windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 3) {
            any timestampParam = parameters[0];
            if (timestampParam is string) {
                self.timestamp = timestampParam;
            } else {
                error err = error("TimeOrder window's first parameter, timestamp should be of type string");
                panic err;
            }

            any timeInMillisParam = parameters[1];
            if (timeInMillisParam is int) {
                self.timeInMillis = timeInMillisParam;
            } else {
                error err = error("TimeOrder window's second parameter, windowTime should be of type int");
                panic err;
            }

            any dropOlderEventsParam = parameters[2];
            if (dropOlderEventsParam is boolean) {
                self.dropOlderEvents = dropOlderEventsParam;
            } else {
                error err = error("TimeOrder window's third parameter, dropOlderEvents should be of type boolean");
                panic err;
            }
        } else {
            error err = error("TimeOrder window should only have three parameters (<string> timestamp, <int> " +
                "windowTime, <boolean> dropOlderEvents), but found " + parameters.length() + " input attributes");
            panic err;
        }

        (function (map<anydata>) returns anydata)?[] fieldFuncs = [function (map<anydata> x) returns anydata {
            return x[self.timestamp];
        }];
        string[] sortTypes = [ASCENDING];
        self.mergeSort = new(fieldFuncs, sortTypes);
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach var event in streamEvents {
                streamEventChunk.addLast(event);
            }

            streamEventChunk.resetToFront();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                int currentTime = time:currentTime().time;
                self.expiredEventQueue.resetToFront();

                while (self.expiredEventQueue.hasNext()) {
                    StreamEvent expiredEvent = getStreamEvent(self.expiredEventQueue.next());
                    int timeDiff = (expiredEvent.timestamp - currentTime) + self.timeInMillis;
                    if (timeDiff <= 0) {
                        self.expiredEventQueue.removeCurrent();
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                if (streamEvent.eventType == CURRENT) {
                    if (self.dropOlderEvents && (currentTime - self.getTimestamp(streamEvent.data[self.timestamp])) >
                        self.timeInMillis) {
                        streamEventChunk.removeCurrent();
                    } else {
                        StreamEvent clonedEvent = streamEvent.copy();
                        clonedEvent.eventType = EXPIRED;
                        clonedEvent.timestamp = self.getTimestamp(clonedEvent.data[self.timestamp]);
                        self.expiredEventQueue.addLast(clonedEvent);
                        StreamEvent?[] events = [];
                        foreach var e in self.expiredEventQueue.asArray() {
                            if (e is StreamEvent) {
                                events[events.length()] = e;
                            }
                        }
                        self.mergeSort.topDownMergeSort(events);
                        self.expiredEventQueue.clear();
                        foreach var event in events {
                            self.expiredEventQueue.addLast(event);
                        }
                    }

                    if (self.lastTimestamp < self.getTimestamp(streamEvent.data[self.timestamp])) {
                        self.scheduler.notifyAt(self.getTimestamp(streamEvent.data[self.timestamp]) + self
                                .timeInMillis);
                        self.lastTimestamp = self.getTimestamp(streamEvent.data[self.timestamp]);
                    }
                } else {
                    streamEventChunk.removeCurrent();
                }
            }
            self.expiredEventQueue.resetToFront();
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer.call(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (StreamEvent?, StreamEvent?)[] {
        (StreamEvent?, StreamEvent?)[] events = [];
        int i = 0;
        foreach var e in self.expiredEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc.call(lshEvent.data, rhsEvent.data)) {
                        events[i] = (lshEvent, rhsEvent);
                        i += 1;
                    }
                } else {
                    events[i] = (lshEvent, rhsEvent);
                    i += 1;
                }
            }
        }
        return events;
    }

    public function getTimestamp(any val) returns (int) {
        if (val is int) {
            return val;
        } else {
            error err = error("timestamp should be of type int");
            panic err;
        }
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] expiredEventsList = toSnapshottableEvents(self.expiredEventQueue.asArray());
        return {
            "expiredEventsList": expiredEventsList,
            "lastTimestamp": self.lastTimestamp
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var expiredEventsList = state["expiredEventsList"];
        if (expiredEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] expiredEvents = toStreamEvents(expiredEventsList);
            self.expiredEventQueue = new;
            self.expiredEventQueue.addAll(expiredEvents);
        }
        any lts = state["lastTimestamp"];
        if (lts is int) {
            self.lastTimestamp = lts;
        }
    }
};

# The `timeOrder` function creates a `TimeOrderWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function timeOrder(any[] windowParameters, function (StreamEvent?[])? nextProcessPointer = ())
                    returns Window {

    TimeOrderWindow timeOrderWindow = new(nextProcessPointer, windowParameters);
    return timeOrderWindow;
}
