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

email:SmtpConfig smtpConfig = {
    port: 3025,
    enableSsl: false
};

function testSendComplexEmail(string host, string username, string password, string subject, string body,
        string fromAddress, string sender, string[] toAddresses, string[] ccAddresses, string[] bccAddresses,
        string[] replyToAddresses) returns email:Error? {
    email:SmtpClient|email:Error smtpClient = new (host, username,  password, smtpConfig);
    email:Email email = {
        to: toAddresses,
        cc: ccAddresses,
        bcc: bccAddresses,
        subject: subject,
        body: body,
        'from: fromAddress,
        sender: sender,
        replyTo: replyToAddresses
    };
    if (smtpClient is email:SmtpClient) {
        return smtpClient->send(email);
    } else {
        return smtpClient;
    }
}
