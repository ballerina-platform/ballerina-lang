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

define(['lodash','jquery', 'ballerina'], function(_, $, Ballerina) {

    function ballerinaTestRunner(val){

        var expectedSource = readTestFile(val);
        var generatedSource = ballerinaASTDeserializer(expectedSource);

        describe("Ballerina Tests", function() {
            it(val + " Service Test", function() {
                expectedSource = expectedSource.replace(/\s/g, '');
                generatedSource = generatedSource.replace(/(\r\n|\n|\r)/gm,"");
                generatedSource = generatedSource.replace(/\s/g, '');
                expect(generatedSource).to.equal(expectedSource);
            });
        });
    }

    function ballerinaASTDeserializer(fileContent){
        var backend = new Ballerina.views.Backend({"url" : "http://localhost:8289/ballerina/model/content"})
        var response = backend.parse(fileContent);
        var ASTModel = Ballerina.ast.BallerinaASTDeserializer.getASTModel(response);
        var sourceGenVisitor = new Ballerina.visitors.SourceGen.BallerinaASTRootVisitor();
        ASTModel.accept(sourceGenVisitor);
        var source = sourceGenVisitor.getGeneratedSource();
        return source;
    }

    function readTestFile(file){
        var workspaceServiceURL = "http://localhost:8289/service/workspace";
        var saveServiceURL = workspaceServiceURL + "/read";
        var path = "/home/malintha/projects/wso2/myBallerina/ballerina/samples/getting_started/" + file +"/" + file + ".bal";
        var fileContent;

        $.ajax({
            url: saveServiceURL,
            type: "POST",
            data: path,
            contentType: "text/plain; charset=utf-8",
            async: false,
            success: function (data, textStatus, xhr) {
                if (xhr.status == 200) {
                    fileContent = data.content;
                } else {}
            },

            error: function (res, errorCode, error) {}
        });

        return fileContent;
    }

    var testFileList = ['echoService', 'helloWorldService'];

        testFileList.forEach(function(testFile) {
        ballerinaTestRunner(testFile);
    });
});