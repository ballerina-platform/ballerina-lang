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

import ballerinax/java;

public function initSmtpClientEndpoint(SmtpClient clientEndpoint, map<anydata> config) returns error? = @java:Method {
    name : "initClientEndpoint",
    class : "org.ballerinalang.stdlib.email.client.SmtpClient"
} external;

public function send(SmtpClient clientEndpoint, map<anydata> email) returns error? = @java:Method {
    name : "sendMessage",
    class : "org.ballerinalang.stdlib.email.client.SmtpClient"
} external;


public function initPopClientEndpoint(PopClient clientEndpoint, map<anydata> config, boolean isPop) returns error? = @java:Method {
    name : "initClientEndpoint",
    class : "org.ballerinalang.stdlib.email.client.PopClient"
} external;

public function popRead(PopClient clientEndpoint, map<anydata> message, boolean isPop) returns map<anydata>|error? = @java:Method {
    name : "readMessage",
    class : "org.ballerinalang.stdlib.email.client.PopClient"
} external;


public function initImapClientEndpoint(ImapClient clientEndpoint, map<anydata> config, boolean isPop) returns error? = @java:Method {
    name : "initClientEndpoint",
    class : "org.ballerinalang.stdlib.email.client.PopClient"
} external;

public function imapRead(ImapClient clientEndpoint, map<anydata> message, boolean isPop) returns map<anydata>|error? = @java:Method {
    name : "readMessage",
    class : "org.ballerinalang.stdlib.email.client.PopClient"
} external;