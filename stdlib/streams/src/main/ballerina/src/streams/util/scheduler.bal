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
import ballerina/task;
import ballerina/io;

# The `Scheduler` object is responsible for generating streams:TIMER events at the given timestamp. Once the event is
# generated, the timer event is passed to the provided `processFunc` function pointer. The function pointer is the
# `process` function of the target processor, to which the timer event should be sent.
public type Scheduler object {

    private LinkedList toNotifyQueue;
    private boolean running;
    private task:Scheduler timer;
    private function (StreamEvent?[] streamEvents) processFunc;

    public function __init(function (StreamEvent?[] streamEvents) processFunc) {
        self.toNotifyQueue = new;
        self.running = false;
        self.timer = new({ intervalInMillis: 1 });
        self.processFunc = processFunc;
    }

    # Schedule to send a timer events at the given timestamp.
    # + timestamp - The timestamp at which the timer event will be generated and passed to the provided `processFunc`.
    public function notifyAt(int timestamp) {
        self.toNotifyQueue.addLast(timestamp);
        self.schedule(timestamp);
    }


    function schedule(int timestamp) {
        if (self.toNotifyQueue.getSize() == 1 && self.running == false) {
            lock {
                if (self.running == false) {
                    self.running = true;
                    int timeDiff = timestamp > time:currentTime().time ? timestamp - time:currentTime().time : 0;
                    int timeDelay = timeDiff > 0 ? timeDiff : -1;

                    checkpanic self.timer.stop();
                    self.timer = new({ intervalInMillis: timeDiff, initialDelayInMillis: timeDelay, noOfRecurrences: 1 });
                    checkpanic self.timer.attach(schedulerService, self);
                    checkpanic self.timer.start();
                }
            }
        }
    }

    function wrapperFunc() {
        checkpanic self.sendTimerEvents();
    }

    # Creates the timer events.
    # +return - Returns error if sending timer events failed.
    public function sendTimerEvents() returns error? {
        any first = self.toNotifyQueue.getFirst();
        int currentTime = time:currentTime().time;
        while (first != () && <int>first - currentTime <= 0) {
            _ = self.toNotifyQueue.removeFirst();
            map<anydata> data = {};
            StreamEvent timerEvent = new(["timer", data], "TIMER", <int>first);
            StreamEvent?[] timerEventWrapper = [];
            timerEventWrapper[0] = timerEvent;
            function (StreamEvent?[] streamEvents) processFunction = self.processFunc;
            processFunction(timerEventWrapper);

            first = self.toNotifyQueue.getFirst();
            currentTime = time:currentTime().time;
        }

        checkpanic self.timer.stop();
        first = self.toNotifyQueue.getFirst();
        currentTime = time:currentTime().time;

        if (first != ()) {
            if (<int>first - currentTime <= 0) {
                _ = self.wrapperFunc();
            } else {
                self.timer = new({ intervalInMillis: <int>first - currentTime, noOfRecurrences: 1 });
                checkpanic self.timer.attach(schedulerService, self);
                checkpanic self.timer.start();
            }
        } else {
            lock {
                self.running = false;
                if (!(self.toNotifyQueue.getFirst() is LinkedList)) {
                    self.running = true;
                    self.timer = new({ intervalInMillis: 1, initialDelayInMillis: 0, noOfRecurrences: 1 });
                    checkpanic self.timer.attach(schedulerService, self);
                    checkpanic self.timer.start();
                }
            }
        }
        return ();
    }
};

# `schedulerService` triggers the timer event generation at the given timestamp.
service schedulerService = service {
    resource function onTrigger(Scheduler scheduler) {
        var e = scheduler.sendTimerEvents();
    }
};
