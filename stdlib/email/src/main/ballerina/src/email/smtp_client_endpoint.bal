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
    public function init(@untainted string host, @untainted string username, @untainted string password,
            SmtpConfig clientConfig = {}) {
        initSmtpClientEndpoint(self, host, username, password, clientConfig);
    }

    # Sends a message.
    # ```ballerina
    # email:Error? response = smtpClient->send(email);
    # ```
    #
    # + email - An `email:Email` message, which needs to be sent to the recipient
    # + return - An `email:Error` if failed to send the message to the recipient or else `()`
    public remote function send(Email email) returns Error? {
        var body = email.body;
        if (body is xml) {
            if (email?.contentType == ()) {
                email.contentType = "application/xml";
            } else if (!self.containsType(email?.contentType, "xml")) {
                return SendError(message = "Content type of the email should be XML.");
            }
            body = body.toString();
        } else if (body is string) {
            if (email?.contentType == ()) {
                email.contentType = "text/plain";
            } else if (!self.containsType(email?.contentType, "text")) {
                return SendError(message = "Content type of the email should be text.");
            }
        } else {
            if (email?.contentType == ()) {
                email.contentType = "application/json";
            } else if (!self.containsType(email?.contentType, "json")) {
                return SendError(message = "Content type of the email should be json.");
            }
            body = body.toJsonString();
        }
        return send(self, email);
    }

    private function containsType(string? contentType, string typeString) returns boolean {
        if (contentType is string) {
            string canonicalizedCtype = contentType.toLowerAscii();
            int? stringIndex = canonicalizedCtype.indexOf(typeString);
            return stringIndex is int;
        }
        return false;
    }

};

function initSmtpClientEndpoint(SmtpClient clientEndpoint, string host, string username, string password,
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
#               The default value is true for the "smtps" protocol and false for the "smtp" protocol
# + properties - SMTP properties to override the existing configuration
public type SmtpConfig record {|
    int port = 465;
    boolean enableSsl = true;
    map<string>? properties = ();
|};
