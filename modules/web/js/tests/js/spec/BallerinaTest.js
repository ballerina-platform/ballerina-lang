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

define(['lodash', 'log', 'jquery', 'ballerina'], function(_, log, $, Ballerina) {

var getModelBackend = "http://localhost:8289/ballerina/model/content";
var getFileContentBackend = "http://localhost:8289/service/workspace";

    //Test running function
    function ballerinaTestRunner(sourceList){

        var expectedSource = readFile(sourceList);
        var generatedSource = ballerinaASTDeserializer(expectedSource);

        describe("Ballerina Tests", function() {
            it(sourceList.replace(/^.*[\\\/]/, '') + " Service Test", function() {
                expectedSource = expectedSource.replace(/\s/g, '');
                generatedSource = generatedSource.replace(/(\r\n|\n|\r)/gm,"");
                generatedSource = generatedSource.replace(/\s/g, '');
                if(generatedSource!=expectedSource){
                    log.error('error');
                }
                expect(generatedSource).to.equal(expectedSource);
            });
        });
    }

    //Ballerina AST Deserializer
    function ballerinaASTDeserializer(fileContent){
        var backend = new Ballerina.views.Backend({"url" : getModelBackend})
        var response = backend.parse(fileContent);
        var ASTModel = Ballerina.ast.BallerinaASTDeserializer.getASTModel(response);
        var sourceGenVisitor = new Ballerina.visitors.SourceGen.BallerinaASTRootVisitor();
        ASTModel.accept(sourceGenVisitor);
        var source = sourceGenVisitor.getGeneratedSource();
        return source;
    }

    function getTestResourceLocation(){
        var resourcePath = document.location.pathname;
        return resourcePath.substring(resourcePath.indexOf('/'), resourcePath.lastIndexOf('/')) + "/resources/";
    }

    function readFile(filePath){
        var workspaceServiceURL = getFileContentBackend;
        var saveServiceURL = workspaceServiceURL + "/read";

        //TODO remove picking folder location and get file content from ajax call running test on http server
        var fileContent;

        $.ajax({
            url: saveServiceURL,
            type: "POST",
            data: filePath,
            contentType: "text/plain; charset=utf-8",
            async: false,
            success: function (data, textStatus, xhr) {
                if (xhr.status == 200) {
                    fileContent = data.content;
                }
            },

            error: function (res, errorCode, error) {}
        });
        return fileContent;
    }

    var testFileList = readFile(getTestResourceLocation() + "BalList.json");
    var sourceList = JSON.parse(testFileList)["sources"]["source"];

    //Running test for provide source list
    sourceList.forEach(function(testFile) {
        ballerinaTestRunner(getTestResourceLocation() +testFile);
    });
});