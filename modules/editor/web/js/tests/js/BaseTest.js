/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['lodash','jquery', ], function(_, $ ){

return {
 value: myFunction2()
 }



function myFunction(p1, p2) {
    var workspaceServiceURL = "http://localhost:8289/service/workspace";
    var saveServiceURL = workspaceServiceURL + "/read";

    var path = "/home/malintha/projects/wso2/myBallerina/ballerina/samples/getting_started/echoService/echoService.bal";
    var fileName = "echoService.bal";
    $.ajax({
        url: saveServiceURL,
        type: "POST",
        data: path,
        contentType: "text/plain; charset=utf-8",
        async: false,
        success: function (data, textStatus, xhr) {
        if (xhr.status == 200) {
              var file = new File({
                name: 'echoService.bal',
                path: '/home/malintha/projects/wso2/myBallerina/ballerina/samples/getting_started/echoService',
                content: data.content
              });

        } else {
            alertError();
        }
       },
        error: function (res, errorCode, error) {
            alertError();
        }
});
}

function myFunction2(){

    $.ajax({
        url: "http://localhost:8289/ballerina/model/content",
        type: "POST",
                            data: "import ballerina.net.http;@BasePath (\"/echo\")service echo {    @POST    resource echo (message m) {        http:convertToResponse(m);        reply m;    }}",
                            contentType: "application/json; charset=utf-8",
                            async: false,
                            dataType: "json",
                            success: function (data, textStatus, xhr) {
                                if (xhr.status == 200) {
            //                        var BallerinaASTDeserializer = Ballerina.ast.BallerinaASTDeserializer;
            //                        var root = BallerinaASTDeserializer.getASTModel(data);
            //                        onSuccessCallBack(root);
                                } else {
                                    log.error("Error while parsing the source. " + JSON.stringify(xhr));
                                }
                            },
                            error: function (res, errorCode, error) {
                                log.error("Error while parsing the source. " + JSON.stringify(res));
                            }
                        });
}


});

