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

# Represents an POP Client that interacts with an POP Server
public type PopClient client object {

    # Gets invoked during object initialization
    #
    # + clientConfig - Configurations for POP Client
    public function __init(PopConfig clientConfig) {
        error? response = initPopClientEndpoint(self, clientConfig, true);
    }

    # Used to read a message
    #
    # + return - An `error` if failed to send the message to the recipient
    public remote function read() returns Email|error? {
        Filter filter = {
            folder: "INBOX"
        };
        map<anydata>|error? email = popRead(self, filter, true);
        if(email is error) {
            return email;
        } else {
            return <Email?>email;
        }
    }

};

# Configuration of the POP Endpoint
#
# + host - Host address of POP server
# + port - Port number of POP server
# + username - Username to access POP server
# + password - Password to access POP server
public type PopConfig record {|
    string host;
    int? port = 995;
    string username;
    string password;
|};
