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

# Represents a ballerina task listener.
public type Listener object {
    *AbstractListener;

    private TimerConfiguration|AppointmentConfiguration listenerConfiguration;

    public function __init(TimerConfiguration|AppointmentConfiguration configs) {
        if (configs is TimerConfiguration) {
            if (configs["initialDelay"] == ()) {
                configs.initialDelay = configs.interval;
            }
        }
        self.listenerConfiguration = configs;
        var initResult = self.init();
        if (initResult is error) {
            panic initResult;
        }
    }

    public function __attach(service s, map<any> annotationData) returns error? {
        return self.register(s, annotationData);
    }

    public function __start() returns error? {
        return self.start();
    }

    public function __stop() returns error? {
        return self.stop();
    }

    extern function init() returns error?;

    extern function register(service s, map<any> annotationData) returns error?;

    extern function start() returns error?;

    extern function stop() returns error?;

    extern function detachService(service attachedService) returns error?;

    # Pauses the task.
    #
    # + return - Returns error if an error is occured while resuming, nil Otherwise.
    public extern function pause() returns error?;

    # Resumes a paused task.
    #
    # + return - Returns error when an error occurred while pausing, nil Otherwise.
    public extern function resume() returns error?;
};
