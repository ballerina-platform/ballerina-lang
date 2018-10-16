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

# Provides the socket related actions for interacting with caller.
public type CallerAction object {

    # Write given data to the client socket.
    #
    # + content - - the content that wish to send to the client socket
    # + return - - number of byte got written or an error if encounters an error while writing
    public extern function write(byte[] content) returns int|error;

    # Close the client socket connection.
    #
    # + return - - an error if encounters an error while closing the connection or returns nil otherwise
    public extern function close() returns error?;
};
