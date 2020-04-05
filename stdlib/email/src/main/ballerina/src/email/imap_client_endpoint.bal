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

# Represents an IMAP Client, which interacts with an IMAP Server.
public type ImapClient client object {

    # Gets invoked during object initialization.
    #
    # + host - Host of the IMAP Client
    # + username - Username of the IMAP Client
    # + password - Password of the IMAP Client
    # + clientConfig - Configurations for the IMAP Client
    # + return - An `Error` if failed while creating the client
    public function __init(@untainted string host, @untainted string username, @untainted string password,
            ImapConfig clientConfig = {}) returns Error? {
        return initImapClientEndpoint(self, java:fromString(host), java:fromString(username), java:fromString(password),
            clientConfig);
    }

    # Reads a message.
    #
    # + folder - Folder to read emails
    # + return - An`Email` in success and `Error` if failed to receive the message to the recipient
    public remote function read(string folder = DEFAULT_FOLDER) returns Email|Error? {
        return imapRead(self, java:fromString(folder));
    }

};

function initImapClientEndpoint(ImapClient clientEndpoint, handle host, handle username, handle password,
        ImapConfig config) returns Error? = @java:Method {
    name : "initImapClientEndpoint",
    class : "org.ballerinalang.stdlib.email.client.EmailAccessClient"
} external;

function imapRead(ImapClient clientEndpoint, handle folder) returns Email|Error? = @java:Method {
    name : "readMessage",
    class : "org.ballerinalang.stdlib.email.client.EmailAccessClient"
} external;

# Configuration of the IMAP Endpoint.
#
# + port - Port number of the IMAP server
# + enableSsl - If set to true, use SSL to connect and use the SSL port by default.
#   Defaults to true for the "imaps" protocol and false for the "imap" protocol.
public type ImapConfig record {|
    int port = 993;
    boolean enableSsl = true;
|};
