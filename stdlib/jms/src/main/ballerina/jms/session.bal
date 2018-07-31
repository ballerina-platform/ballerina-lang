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

# Represents JMS session
#
# + config - Used to store configurations related to a JMS session
public type Session object {

    public SessionConfiguration config;

    # Default constructor of the JMS session
    public new(Connection connection, SessionConfiguration c) {
        self.config = c;
        self.initEndpoint(connection);
    }

    extern function initEndpoint(Connection connection);

    # Creates a JMS message which holds text content
    #
    # + content - the text content used to initialize this message
    public extern function createTextMessage(string content) returns Message|error;

    # Creates a JMS message which holds Map content
    #
    # + content - the Map content used to initialize this message
    public extern function createMapMessage(map content) returns Message|error;

    # Unsubscribes a durable subscription that has been created by a client.
    # It is erroneous for a client to delete a durable subscription while there is an active (not closed) consumer
    # for the subscription, or while a consumed message is part of a pending transaction or has not been
    # acknowledged in the session.
    #
    # + subscriptionId - the name used to identify this subscription
    public extern function unsubscribe(string subscriptionId) returns error?;
};

# Configurations related to a JMS session
#
# + acknowledgementMode - specifies the session mode that will be used. Legal values are "AUTO_ACKNOWLEDGE",
#                         "CLIENT_ACKNOWLEDGE", "SESSION_TRANSACTED" and "DUPS_OK_ACKNOWLEDGE"
public type SessionConfiguration record {
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
};
