// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

documentation {
    Schedules a timer task.
}
public type Timer object {

    // The function which gets called when the timer goes off
    private (function() returns error?) onTrigger,
    // The function that gets called if the onTrigger function returns an error
    private (function(error) returns ())? onError,
    // Initial delay before the timer gets triggerred
    private int delay;
    // Timer trigger interval
    private int interval;
    // Unique task ID which will be used when this timer is stopped
    private string taskId;
    // Keeps track whether the task is started to ensure that a started task cannot be started again
    // unless it is stopped
    private boolean isRunning;

    // defaultable delay is -1, which means the delay will be the same as the interval
    public new(onTrigger, onError, interval, delay = -1) {}

    // Start the timer
    public extern function start();
    // Stop the timer
    public extern function stop();
};
