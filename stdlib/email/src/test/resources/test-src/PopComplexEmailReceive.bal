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

import ballerina/email;
import ballerina/mime;
import ballerina/lang.'string as strings;

email:PopConfig popConfig = {
     port: 3110,
     enableSsl: false
};

function testReceiveComplexEmail(string host, string username, string password) returns @tainted string[] {
    string[] returnArray = [];
    email:PopClient|email:Error popClient = new (host, username, password, popConfig);
    if (popClient is email:PopClient) {
        email:Email|email:Error? emailResponse = popClient->read();
        if (emailResponse is email:Email) {
            returnArray[0] = emailResponse.subject;
            returnArray[1] = emailResponse.body;
            returnArray[2] = emailResponse.'from;
            returnArray[3] = getNonNilString(emailResponse?.sender);
            returnArray[4] = concatStrings(emailResponse.to);
            returnArray[5] = concatStrings(emailResponse?.cc);
            returnArray[6] = concatStrings(emailResponse?.replyTo);
            mime:Entity[]? attachments = emailResponse?.attachments;
            if (attachments is mime:Entity[]) {
                string|error attachment1 = attachments[0].getText();
                returnArray[7] = (attachment1 is string) ? attachment1 : "";
                var attachment2 = attachments[1].getJson();
                if (attachment2 is json) {
                    returnArray[8] = !(attachment2 is ()) ? attachment2.toJsonString() : "";
                } else {
                    return [];
                }
                string attachment3 = "";
                xml|error xml1 = attachments[2].getXml();
                if (xml1 is xml) {
                    attachment3 = xml1.toString();
                }
                returnArray[9] = attachment3;
                var attachment4 = attachments[3].getByteArray();
                if (attachment4 is byte[]) {
                    string|error byteString = strings:fromBytes(attachment4);
                    if (byteString is string) {
                        returnArray[10] = byteString;
                    } else {
                        return [];
                    }
                } else {
                    return [];
                }
                returnArray[11] = attachments[0].getHeader("H1");
                returnArray[12] = attachments[0].getContentType();
            }
            return returnArray;
        } else if (emailResponse is ()) {
            return [];
        } else {
            return [];
        }
    } else {
        return [];
    }
}

function getNonNilString(string? sender) returns string {
    if sender is string {
        return sender;
    } else {
        return "";
    }
}

function concatStrings(string[]? addresses) returns string {
    string result = "";
    int i = 0;
    if addresses is string[] {
        while (i < addresses.length()) {
            result = result + addresses[i];
            i = i + 1;
        }
    }
    return result;
}
