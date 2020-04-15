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

# Represents an SMTP Client, which interacts with an SMTP Server.
public type SmtpClient client object {

    # Gets invoked during the `email:SmtpClient` initialization.
    #
    # + host - Host of the SMTP Client
    # + username - Username of the SMTP Client
    # + password - Password of the SMTP Client
    # + clientConfig - Configurations for SMTP Client
    public function __init(@untainted string host, @untainted string username, @untainted string password,
            SmtpConfig clientConfig = {}) {
        return initSmtpClientEndpoint(self, java:fromString(host), java:fromString(username),
            java:fromString(password), clientConfig);
    }

# Sends a message.
# ```ballerina
# email:Error? response = smtpClient->send(email);
# ```
#
# + email - An `email:Email` message, which needs to be sent to the recipient
# + return - An `email:SendError` if failed to send the message to the recipient or else `()`
    public remote function send(Email email) returns Error? {
        return send(self, email);
    }

};

function initSmtpClientEndpoint(SmtpClient clientEndpoint, handle host, handle username, handle password,
        SmtpConfig config) = @java:Method {
    name : "initClientEndpoint",
    class : "org.ballerinalang.stdlib.email.client.SmtpClient"
} external;

function send(SmtpClient clientEndpoint, Email email) returns Error? = @java:Method {
    name : "sendMessage",
    class : "org.ballerinalang.stdlib.email.client.SmtpClient"
} external;

# Configuration of the SMTP Endpoint.
#
# + port - Port number of the SMTP server
# + enableSsl - If set to true, use SSL to connect and use the SSL port by default.
#               The default value is true for the "smtps" protocol and false for the "smtp" protocol.
public type SmtpConfig record {|
    int port = 465;
    boolean enableSsl = true;
|};
