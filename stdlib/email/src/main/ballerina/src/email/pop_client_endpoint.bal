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

# Represents a POP Client, which interacts with a POP Server.
public type PopClient client object {

    # Gets invoked during the `email:PopClient` initialization.
    #
    # + host - Host of the POP Client
    # + username - Username of the POP Client
    # + password - Password of the POP Client
    # + clientConfig - Configurations for the POP Client
    # + return - An `email:Error` if creating the client failed or else `()`
    public function init(@untainted string host, @untainted string username, @untainted string password,
            PopConfig clientConfig = {}) returns Error? {
        return initPopClientEndpoint(self, host, username, password, clientConfig);
    }

    # Reads a message.
    # ```ballerina
    # email:Email|email:Error? emailResponse = popClient->read();
    # ```
    #
    # + folder - Folder to read emails. The default value is `INBOX`
    # + return - An`email:Email` if reading the message is successful, `()` if there are no emails in the specified
    #            folder, or else an `email:Error` if the recipient failed to receive the message
    public remote function read(string folder = DEFAULT_FOLDER) returns Email|Error? {
        return popRead(self, folder);
    }

};

function initPopClientEndpoint(PopClient clientEndpoint, string host, string username, string password,
        PopConfig config) returns Error? = @java:Method {
    name : "initPopClientEndpoint",
    class : "org.ballerinalang.stdlib.email.client.EmailAccessClient"
} external;

function popRead(PopClient clientEndpoint, string folder) returns Email|Error? = @java:Method {
    name : "readMessage",
    class : "org.ballerinalang.stdlib.email.client.EmailAccessClient"
} external;

# Configuration of the POP Endpoint.
#
# + port - Port number of the POP server
# + enableSsl - If set to true, use SSL to connect and use the SSL port by default.
#               The default value is true for the "pops" protocol and false for the "pop" protocol
# + properties - POP3 properties to override the existing configuration
public type PopConfig record {|
    int port = 995;
    boolean enableSsl = true;
    map<string>? properties = ();
|};
