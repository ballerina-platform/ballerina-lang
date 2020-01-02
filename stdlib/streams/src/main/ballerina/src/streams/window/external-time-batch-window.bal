// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/time;

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
    public Scheduler? scheduler = ();

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

    public function getScheduler() returns Scheduler {
        return <Scheduler> self.scheduler;
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
                "timeout, <boolean> replaceTimestampWithBatchEndTime), but found " + parameters.length().toString()
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

        if (streamEventChunk.getFirst() is ()) {
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
                        self.getScheduler().notifyAt(self.lastScheduledTime);
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
                        self.getScheduler().notifyAt(self.lastScheduledTime);
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
                    nextProcessFuncPointer(streamEvent);
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
                        public boolean isLHSTrigger = true)
                        returns @tainted [StreamEvent?, StreamEvent?][] {
        [StreamEvent?, StreamEvent?][] events = [];
        int i = 0;
        foreach var e in self.currentEventChunk.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc(lshEvent.data, rhsEvent.data)) {
                        events[i] = [lshEvent, rhsEvent];
                        i += 1;
                    }
                } else {
                    events[i] = [lshEvent, rhsEvent];
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
        StreamEvent? evnt = self.resetEvent;
        if (evnt is ()) {
            var resetEvent = currStreamEvent.copy();
            resetEvent.eventType = RESET;
            self.resetEvent = resetEvent;
        }
    }

    public function flushToOutputChunk(LinkedList complexEventChunks, int currentTime, boolean preserveCurrentEvents) {
        LinkedList newEventChunk = new();
        if (!(self.expiredEventChunk.getFirst() is ())) {
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

        if (self.expiredEventChunk.isEmpty()) {
            self.expiredEventChunk.clear();
        }

        if (!(self.currentEventChunk.getFirst() is ())) {
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
        if (!(self.currentEventChunk.getFirst() is ())) {
            if (!(self.expiredEventChunk.getFirst() is ())) {
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
                self.getScheduler().notifyAt(self.lastScheduledTime);
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
public function externalTimeBatch(any[] windowParameters, public function (StreamEvent?[])? nextProcessPointer = ())
                                                                                                                                                                                                                                                                                                                                                                                                                                  returns Window {
    ExternalTimeBatchWindow timeWindow1 = new(nextProcessPointer, windowParameters);
    return timeWindow1;
}
