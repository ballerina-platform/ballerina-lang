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

import ballerina/http;
import ballerina/io;
import ballerina/system;

# This function opens the file for writing content.
#
# + filePath - File path
# + encoding - Encoding
# + return - CharacterChannel of the file after writing to the file
function openForWriting(string filePath, string encoding) returns @tainted io:WritableCharacterChannel {
    io:WritableByteChannel byteChannel = checkpanic io:openWritableFile(filePath);
    io:WritableCharacterChannel result = new io:WritableCharacterChannel(byteChannel, encoding);
    return result;
}

# This service updates the access token.
@http:ServiceConfig {
    basePath:"/update-settings"
}
service update_token on new http:Listener(9295) {

    # Updates the access token.
    #
    # + caller - Endpoint
    # + request - Request object
    # + token - Access token
    @http:ResourceConfig {
        methods:["GET"],
        path:"/{token}"
    }
    resource function one_px_image (http:Caller caller, http:Request request, string token) {
        http:Response response = new;
        response.setPayload("<svg xmlns=\"http://www.w3.org/2000/svg\"/>");
        response.setHeader("Content-Type", "image/svg+xml");
        var destinationChannel = openForWriting(system:getUserHome() + "/.ballerina/Settings.toml", "UTF-8");
        var result = destinationChannel.write("[central]\naccesstoken= \"" + token  + "\"", 0);
        io:println("Token updated");
        checkpanic caller->respond(response);
    }
}
