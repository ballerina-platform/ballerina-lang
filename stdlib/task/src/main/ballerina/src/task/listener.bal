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

import ballerina/lang.'object;
import ballerinax/java;

# Represents a ballerina task listener, which can be used to schedule and execute tasks periodically.
public type Listener object {
    *'object:Listener;
    boolean started = false;

    private TimerConfiguration|AppointmentConfiguration listenerConfiguration;

    public function __init(TimerConfiguration|AppointmentConfiguration configs) {
        if (configs is TimerConfiguration) {
            if (configs["initialDelayInMillis"] == ()) {
                configs.initialDelayInMillis = configs.intervalInMillis;
            }
        }
        self.listenerConfiguration = configs;
        initExternal(self);
    }

    public function __attach(service s, string? name = ()) returns error? {
        // ignore param 'name'
        var result = attachExternal(self, s, {});
        if (result is error) {
            panic result;
        }
    }

    public function __detach(service s) returns error? {
        return detachExternal(self, s);
    }

    public function __start() returns error? {
        var result = startExternal(self);
        if (result is error) {
            panic result;
        }
        lock {
            self.started = true;
        }
    }

    public function __gracefulStop() returns error? {
        var result = stopExternal(self);
        if (result is error) {
            panic result;
        }
        lock {
            self.started = false;
        }
    }

    public function __immediateStop() returns error? {
        var result = stopExternal(self);
        if (result is error) {
            panic result;
        }
        lock {
            self.started = false;
        }
    }

    function isStarted() returns boolean {
        return self.started;
    }

    # Pauses the task listener.
    #
    # + return - Returns `task:ListenerError` if an error is occurred while resuming, nil Otherwise.
    public function pause() returns ListenerError? {
        return pauseExternal(self);
    }

    # Resumes a paused task listener.
    #
    # + return - Returns `task:ListenerError` when an error occurred while pausing, nil Otherwise.
    public function resume() returns ListenerError? {
        return resumeExternal(self);
    }
};

function pauseExternal(Listener task) returns ListenerError? = @java:Method {
    name: "pause",
    class: "org.ballerinalang.stdlib.task.actions.TaskActions"
} external;

function resumeExternal(Listener task) returns ListenerError? = @java:Method {
    name: "resume",
    class: "org.ballerinalang.stdlib.task.actions.TaskActions"
} external;

function stopExternal(Listener task) returns ListenerError? = @java:Method {
    name: "stop",
    class: "org.ballerinalang.stdlib.task.actions.TaskActions"
} external;

function startExternal(Listener task) returns ListenerError? = @java:Method {
    name: "start",
    class: "org.ballerinalang.stdlib.task.actions.TaskActions"
} external;

function initExternal(Listener task) = @java:Method {
    name: "init",
    class: "org.ballerinalang.stdlib.task.actions.TaskActions"
} external;

function detachExternal(Listener task, service attachedService) returns ListenerError? = @java:Method {
    name: "detach",
    class: "org.ballerinalang.stdlib.task.actions.TaskActions"
} external;

function attachExternal(Listener task, service s, map<any> config) returns ListenerError? = @java:Method {
    name: "attach",
    class: "org.ballerinalang.stdlib.task.actions.TaskActions"
} external;
