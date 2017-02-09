/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['ast/ballerina-ast-factory'], function (BallerinaASTFactory) {

    var SampleAccountMgtService = function () {
        var ballerinaASTFactory = new BallerinaASTFactory();
        var ballerinaAstRoot = ballerinaASTFactory.createBallerinaAstRoot();

        //package definition
        var packageDefinition = ballerinaASTFactory.createPackageDefinition();
        packageDefinition.setPackageName("samples.passthrough");
        ballerinaAstRoot.setPackageDefinition(packageDefinition);

        //import declarations
        var importDeclaration_langMessage = ballerinaASTFactory.createImportDeclaration();
        importDeclaration_langMessage.setPackageName("ballerina.lang.message");
        var importDeclaration_netHttp = ballerinaASTFactory.createImportDeclaration();
        importDeclaration_netHttp.setPackageName("ballerina.net.http");
        ballerinaAstRoot.addImport(importDeclaration_langMessage);
        ballerinaAstRoot.addImport(importDeclaration_netHttp);

        //service definition
        var serviceDefinitions = [];
        var serviceDefinition_passthroughService = ballerinaASTFactory.createServiceDefinition();
        serviceDefinition_passthroughService.setServiceName("PassthroughService");
        serviceDefinition_passthroughService.addAnnotation("BasePath", "/account");

        // Adding Resources
        var resource_passthrough = ballerinaASTFactory.createResourceDefinition();

        //Adding resource argument
        var resourceArgument_m = ballerinaASTFactory.createResourceArgument();
        resourceArgument_m.setType("message");
        resourceArgument_m.setIdentifier("m");
        resource_passthrough.addArgument("message", "m");

        //Adding reply statement
        var statement_reply = ballerinaASTFactory.createReplyStatement();
        statement_reply.setReplyMessage("m");
        var statements = [];
        statements.push(statement_reply);
        resource_passthrough.setStatements(statements);

        var resourceDefinitions = [];
        resourceDefinitions.push(resource_passthrough);
        serviceDefinition_passthroughService.setResourceDefinitions(resourceDefinitions);

        serviceDefinitions.push(serviceDefinition_passthroughService);
        ballerinaAstRoot.setServiceDefinitions(serviceDefinitions);

        return ballerinaAstRoot;
    };

    return SampleAccountMgtService;

});