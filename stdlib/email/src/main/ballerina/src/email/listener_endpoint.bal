// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;
import ballerina/lang.'object as lang;
import ballerina/log;
import ballerina/task;

# Represents a service listener that monitors the email server location.
public type Listener object {

    *lang:Listener;

    private ListenerConfig config;
    private task:Scheduler? appointment = ();

    # Gets invoked during the `email:Listener` initialization.
    #
    # + ListenerConfig - Configurations for Email endpoint
    public function init(ListenerConfig listenerConfig) {
        self.config = listenerConfig;
        checkpanic externalInit(self, self.config);
    }

    # Starts the `email:Listener`.
    # ```ballerina
    # email:Error? result = emailListener.__start();
    # ```
    #
    # + return - () or else error upon failure to start the listener
    public function __start() returns error? {
        return self.start();
    }

    # Stops the `email:Listener`.
    # ```ballerina
    # email:Error? result = emailListener.__stop();
    # ```
    #
    # + return - () or else error upon failure to stop the listener
    public function __stop() returns error? {
        check self.stop();
    }

    # Binds a service to the `email:Listener`.
    # ```ballerina
    # email:Error? result = emailListener.__attach(helloService, hello);
    # ```
    #
    # + s - Type descriptor of the service
    # + name - Name of the service
    # + return - `()` or else a `email:Error` upon failure to register the listener
    public function __attach(service s, string? name) returns error? {
        return self.register(s, name);
    }

    # Stops consuming messages and detaches the service from the `email:Listener`.
    # ```ballerina
    # email:Error? result = emailListener.__detach(helloService);
    # ```
    #
    # + s - Type descriptor of the service
    # + return - `()` or else a `email:Error` upon failure to detach the service
    public function __detach(service s) returns error? {

    }

    # Stops the `email:Listener` forcefully.
    # ```ballerina
    # email:Error? result = emailListener.__immediateStop();
    # ```
    #
    # + return - `()` or else a `email:Error` upon failure to stop the listener
    public function __immediateStop() returns error? {
        check self.stop();
    }

    # Stops the `email:Listener` gracefully.
    # ```ballerina
    # email:Error? result = emailListener.__gracefulStop();
    # ```
    #
    # + return - () or else error upon failure to stop the listener
    public function __gracefulStop() returns error? {
        check self.stop();
    }

    function start() returns error? {
        var scheduler = self.config.cronExpression;
        if (scheduler is string) {
            task:AppointmentConfiguration config = {appointmentDetails: scheduler};
            self.appointment = new(config);
        } else {
            task:TimerConfiguration config = {intervalInMillis: self.config.pollingInterval, initialDelayInMillis: 100};
            self.appointment = new (config);
        }
        var appointment = self.appointment;
        if (appointment is task:Scheduler) {
            check appointment.attach(appointmentService, self);
            check appointment.start();
        }
        log:printInfo("User " + self.config.username + " is listening to remote server at " + self.config.host + "...");
    }

    function stop() returns error? {
        var appointment = self.appointment;
        if (appointment is task:Scheduler) {
            check appointment.stop();
        }
        log:printInfo("Stopped listening to remote server at " + self.config.host);
    }

    function poll() returns error? {
        return poll(self);
    }

    # Registers for the Email service.
    # ```ballerina
    # emailListener.register(helloService, hello);
    # ```
    #
    # + emailService - Type descriptor of the service
    # + name - Service name
    public function register(service emailService, string? name) {
        register(self, emailService);
    }
};

service appointmentService = service {
    resource function onTrigger(Listener l) {
        var result = l.poll();
        if (result is error) {
            log:printError("Error while executing poll function", result);
        }
    }
};

# Configuration for Email listener endpoint.
#
# + host - Email server host
# + username - Email server access username
# + password - Email server access password
# + protocol - Email server access protocol, "IMAP" or "POP"
# + protocolConfig - POP3 or IMAP4 protocol configuration
# + pollingInterval - Periodic time interval to check new update
# + cronExpression - Cron expression to check new update
public type ListenerConfig record {|
    string host;
    string username;
    string password;
    string protocol = "IMAP";
    PopConfig|ImapConfig? protocolConfig = ();
    int pollingInterval = 60000;
    string? cronExpression = ();
|};

function poll(Listener listenerEndpoint) returns error? = @java:Method{
    name: "poll",
    class: "org.ballerinalang.stdlib.email.server.EmailListenerHelper"
} external;

function externalInit(Listener listenerEndpoint, ListenerConfig config) returns error? = @java:Method{
    name: "init",
    class: "org.ballerinalang.stdlib.email.server.EmailListenerHelper"
} external;

function register(Listener listenerEndpoint, service emailService) = @java:Method{
    name: "register",
    class: "org.ballerinalang.stdlib.email.server.EmailListenerHelper"
} external;
