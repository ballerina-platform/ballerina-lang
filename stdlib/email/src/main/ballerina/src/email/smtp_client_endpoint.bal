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

# Represents an SMTP Client that interacts with an SMTP Server
public type SmtpClient client object {

    # Gets invoked during object initialization
    #
    # + clientConfig - Configurations for SMTP Client
    public function __init(SmtpConfig clientConfig) {
        error? response = initSmtpClientEndpoint(self, clientConfig);
    }

    # Used to send a message
    #
    # + email - String message
    # + return - An `error` if failed to send the message to the recipient
    public remote function send(Email email) returns error? {
        return send(self, email);
    }

};

# Configuration of the SMTP Endpoint
#
# + host - Host address of SMTP server
# + port - Port number of SMTP server
# + username - Username to access SMTP server
# + password - Password to access SMTP server
public type SmtpConfig record {|
    //ConnectionConfig? connConfig = ();
    string host;
    int? port = 587;
    string username;
    string password;
|};
