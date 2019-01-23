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

import ballerina/streams;
import ballerina/time;

public type TimeBatch object {
    public string attrExpiredTimestamp;
    public int timeInMilliSeconds = -1;
    public streams:LinkedList expiredEventQueue;
    public streams:Scheduler scheduler;
    public int expiredEventTime = -1;
    public int startTime = -1;
    public any[] windowParameters;
    public function (streams:StreamEvent[])? nextProcessPointer;

    public function __init(function(streams:StreamEvent[])? nextProcessPointer, any[] windowParameters) {
        self.scheduler = new(function (streams:StreamEvent[] events) {
                self.process(events);
            });
        self.expiredEventQueue = new;
        self.attrExpiredTimestamp = "expiryTimestamp";
        self.windowParameters = windowParameters;
        self.nextProcessPointer = nextProcessPointer;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] windowParameters) {
        if (windowParameters.length() == 1) {
            self.timeInMilliSeconds = <int> windowParameters[0];
        } else if (windowParameters.length() == 2) {
            self.timeInMilliSeconds = <int>windowParameters[0];
            self.startTime = <int> windowParameters[1];
        } else {
            error err = error("Can not have more than 2 parameters");
            panic err;
        }
    }

    public function process(streams:StreamEvent[] streamEvents) {
        lock {
            if (self.expiredEventTime == -1) {
                int currentTime = time:currentTime().time;
                if (self.startTime != -1) {
                    self.expiredEventTime = self.addTimeShift(currentTime);
                } else {
                    self.expiredEventTime = time:currentTime().time + self.timeInMilliSeconds;
                }
                self.scheduler.notifyAt(self.expiredEventTime);
            }

            int currentTime = time:currentTime().time;
            boolean sendEvents;
            if (currentTime >= self.expiredEventTime) {
                self.expiredEventTime += self.timeInMilliSeconds;
                self.scheduler.notifyAt(self.expiredEventTime);
                sendEvents = true;
            } else {
                sendEvents = false;
            }

            foreach var event in streamEvents {
                streams:StreamEvent streamEvent = <streams:StreamEvent>event;
                if (streamEvent.eventType != streams:CURRENT) {
                    continue;
                }
                streamEvent.addAttribute(self.attrExpiredTimestamp, self.expiredEventTime);
                streams:StreamEvent clonedEvent = streamEvent.copy();
                clonedEvent.eventType = streams:EXPIRED;
                self.expiredEventQueue.addLast(clonedEvent);
            }

            if (sendEvents) {
                self.expiredEventQueue.resetToFront();
                while (self.expiredEventQueue.hasNext()) {
                    streams:StreamEvent streamEvent = streams:getStreamEvent(self.expiredEventQueue.next());
                    streamEvents[streamEvents.length()] = streamEvent;
                }
                self.expiredEventQueue.clear();
            }
        }

        if (streamEvents.length() > 0) {
            self.nextProcessPointer.call(streamEvents);
        }
    }

    public function addTimeShift(int currentTime) returns int {
        int timePassedUntilNow = (currentTime - self.startTime) % self.timeInMilliSeconds;
        return currentTime + (self.timeInMilliSeconds - timePassedUntilNow);
    }

    public function getCandidateEvents(
                        streams:StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (streams:StreamEvent?, streams:StreamEvent?)[] {
        // do nothing;
        return [((), ())];
    }
};

public function timeBatch(any[] windowParameters, function (streams:StreamEvent[])? nextProcessPointer = ())
                    returns streams:Window {
    TimeBatch timeBatchProcessor = new(nextProcessPointer, windowParameters);
    return timeBatchProcessor;
}