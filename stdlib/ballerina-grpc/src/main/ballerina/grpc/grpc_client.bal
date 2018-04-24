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

documentation {
    Represents the gRPC client.

    F{{host}} - The server host name.
    F{{port}} - The server port.
}
public type GrpcClient object {
    public {
        int port;
        string host;
    }

    documentation {
        Sends request message to the server.

        P{{res}} - The inbound request message.
    }
    public native function send(any res) returns error?;

    documentation {
        Informs the server, caller finished sending messages.
    }
    public native function complete() returns error?;

    documentation {
        Sends error response to the server.

        P{{statusCode}} - Error status code.
        P{{message}} - Error message.
    }
    public native function sendError(int statusCode, string message) returns error?;
};
