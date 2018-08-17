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

documentation { Represents JMS Connection
    F{{config}} Used to store configurations related to a JMS connection
}
public type Connection object {

    public ConnectionConfiguration config;

    documentation { JMS connection constructor
    }
    public new(config) {
        createConnection();
    }

    extern function createConnection();

    documentation { Starts (or restarts) a connection's delivery of incoming messages.
         A call to start on a connection that has already been started is ignored.
    }
    public extern function start();

    documentation { Temporarily stops a connection's delivery of incoming messages.
        Delivery can be restarted using the connection's start method.
    }
    public extern function stop();
};

documentation { Configurations related to a JMS connection
    F{{initialContextFactory}} JMS provider specific inital context factory
    F{{providerUrl}} JMS provider specific provider URL used to configure a connection
    F{{connectionFactoryName}} JMS connection factory to be used in creating JMS connections
    F{{username}} Username for the JMS connection
    F{{password}} Password for the JMS connection
    F{{properties}} Additional properties use in initializing the initial context
}
public type ConnectionConfiguration record {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string? username;
    string? password;
    map properties;
};
