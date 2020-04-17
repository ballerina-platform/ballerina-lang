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

email:SmtpConfig smtpConfig = {
    port: 3025,
    enableSsl: false
};

function testSendComplexEmail(string host, string username, string password, string subject, string body,
        string fromAddress, string sender, string[] toAddresses, string[] ccAddresses, string[] bccAddresses,
        string[] replyToAddresses) returns email:Error? {

    email:SmtpClient|email:Error smtpClient = new (host, username,  password, smtpConfig);

    //Create a text body part.
    mime:Entity bodyPart1 = new;
    bodyPart1.setText("Ballerina text body part");

    //Create a body part with json content.
    mime:Entity bodyPart2 = new;
    bodyPart2.setJson({"bodyPart":"jsonPart"});

    //Create another body part with a xml file.
    mime:Entity bodyPart3 = new;
    bodyPart3.setFileAsEntityBody("src/test/resources/datafiles/file.xml", mime:TEXT_XML);

    //Create another body part with a text file.
    mime:Entity bodyPart4 = new;
    mime:ContentDisposition disposition4 = new;
    disposition4.fileName = "test.tmp";
    disposition4.disposition = "attachment";
    disposition4.name = "test";
    bodyPart4.setContentDisposition(disposition4);
    bodyPart4.setContentId("bodyPart4");
    bodyPart4.setHeader("H1", "V1");
    bodyPart4.setFileAsEntityBody("src/test/resources/datafiles/test.tmp");

    //Create another body part with an image file.
    mime:Entity bodyPart5 = new;
    mime:ContentDisposition disposition5 = new;
    disposition5.fileName = "corona_virus.jpg";
    disposition5.disposition = "inline";
    bodyPart5.setContentDisposition(disposition5);
    bodyPart5.setFileAsEntityBody("src/test/resources/datafiles/corona_virus.jpg", mime:IMAGE_JPEG);

    //Create another body part with binary content.
    string binaryString = "Test content";
    byte[] binary = binaryString.toBytes();
    mime:Entity bodyPart6 = new;
    bodyPart6.setByteArray(binary);

    //Create an array to hold all the body parts.
    mime:Entity[] bodyParts = [bodyPart1, bodyPart2, bodyPart3, bodyPart4, bodyPart5, bodyPart6];

    email:Email email = {
        to: toAddresses,
        cc: ccAddresses,
        bcc: bccAddresses,
        subject: subject,
        body: body,
        'from: fromAddress,
        sender: sender,
        replyTo: replyToAddresses,
        attachments: bodyParts
    };
    if (smtpClient is email:SmtpClient) {
        return smtpClient->send(email);
    } else {
        return smtpClient;
    }
}
