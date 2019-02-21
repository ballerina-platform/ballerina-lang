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

# Represents a ballerina task.
public type Scheduler object {
    private Listener taskListener;

    public function __init(TimerConfiguration|AppointmentConfiguration configs) {
        self.taskListener = new(configs);
    }

    # Attaches the provided service to the task.
    #
    # + serviceToAttach - Service which needs to be attached to the task.
    # + serviceParameter - An optional parameter which needs to passed inside the resources.
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    public function attachService(service serviceToAttach, any serviceParameter = ()) returns error? {
        if (serviceParameter != ()) {
            map<any> serviceParameters = { serviceParameter: serviceParameter };
            return self.taskListener.register(serviceToAttach, serviceParameters);
        }
        return self.taskListener.register(serviceToAttach, {});
    }

    # Detach the procided service from the task.
    #
    # + attachedService - service which needs to be detached from the task.
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    public extern function detachService(service attachedService) returns error?;

    # Starts running the task. Task will not run until this has been called.
    #
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    public function run() returns error? {
        return self.taskListener.__start();
    }

    # Cancels the task. This will stop, after finish running the existing jobs.
    #
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    public function cancel() returns error? {
        return self.taskListener.__stop();
    }

    # Pauses the task.
    #
    # + return - Returns error if the task is not running or any other error is occured, nil Otherwise.
    public function pause() returns error? {
        return self.taskListener.pause();
    }

    # Resumes a paused task.
    #
    # + return - Returns error is the task is not paused or, any other error occurred, nil Otherwise.
    public function resume() returns error? {
        return self.taskListener.resume();
    }
};
