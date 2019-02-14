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

public type Scheduler object {

    private LinkedList toNotifyQueue;
    private boolean running;
    private task:Listener timer;
    private function (StreamEvent?[] streamEvents) processFunc;

    public function __init(function (StreamEvent?[] streamEvents) processFunc) {
        self.toNotifyQueue = new;
        self.running = false;
        self.timer = new({ interval: 0 });
        self.processFunc = processFunc;
    }

    public function notifyAt(int timestamp) {
        self.toNotifyQueue.addLast(timestamp);
        self.schedule(timestamp);
    }

    public function schedule(int timestamp) {
        if (self.toNotifyQueue.getSize() == 1 && self.running == false) {
            lock {
                if (self.running == false) {
                    self.running = true;
                    int timeDiff = timestamp > time:currentTime().time ? timestamp - time:currentTime().time : 0;
                    int timeDelay = timeDiff > 0 ? timeDiff : -1;

                    _ = self.timer.cancel();
                    self.timer = new({ interval: timeDiff, delay: timeDelay });
                    _ = self.timer.attach(schedulerService, serviceParameter = self);
                    _ = self.timer.start();
                }
            }
        }
    }

    public function wrapperFunc() {
        _ = self.sendTimerEvents();
    }

    public function sendTimerEvents() returns error? {
        any? first = self.toNotifyQueue.getFirst();
        int currentTime = time:currentTime().time;
        while (first != () && <int>first - currentTime <= 0) {
            _ = self.toNotifyQueue.removeFirst();
            map<anydata> data = {};
            StreamEvent timerEvent = new(("timer", data), "TIMER", <int>first);
            StreamEvent?[] timerEventWrapper = [];
            timerEventWrapper[0] = timerEvent;
            self.processFunc.call(timerEventWrapper);

            first = self.toNotifyQueue.getFirst();
            currentTime = time:currentTime().time;
        }

        _ = self.timer.cancel();
        self.timer = new({ interval: 0 });

        first = self.toNotifyQueue.getFirst();
        currentTime = time:currentTime().time;

        if (first != ()) {
            self.timer = new({ interval: <int>first - currentTime });
            _ = self.timer.attach(schedulerService, serviceParameter = self);
            _ = self.timer.start();
        } else {
            lock {
                self.running = false;
                if (self.toNotifyQueue.getFirst() != ()) {
                    self.running = true;
                    self.timer = new({ interval: 0 });
                    _ = self.timer.attach(schedulerService, serviceParameter = self);
                    _ = self.timer.start();
                }
            }
        }
        return ();
    }
};

service schedulerService = service {
    resource function onTrigger(Scheduler scheduler) returns error? {
        return scheduler.sendTimerEvents();
    }

    resource function onError(error e, Scheduler scheduler) {
        io:println("Error occured: ", e.reason());
    }
};
