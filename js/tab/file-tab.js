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
define(['require', 'log', 'jquery', 'lodash', './tab', 'ballerina', 'workspace', 'ballerina/diagram-render/diagram-render-context'], function (require, log, jquery, _, Tab, Ballerina, Workspace, DiagramRenderContext) {
    var FileTab;

    FileTab = Tab.extend({
        initialize: function (options) {
            Tab.prototype.initialize.call(this, options);
            if (!_.has(options, 'file')) {
                this._file = new Workspace.File({isTemp: true}, {storage: this.getParent().getBrowserStorage()});
            } else {
                this._file = _.get(options, 'file');
            }
        },

        getFile: function () {
            return this._file;
        },

        render: function () {
            Tab.prototype.render.call(this);
            var ballerinaEditorOptions = _.get(this.options, 'ballerina_editor');
            _.set(ballerinaEditorOptions, 'container', this.$el.get(0));

//            var ballerinaASTFactory = new Ballerina.ast.BallerinaASTFactory();
//            var ballerinaAstRoot = ballerinaASTFactory.createBallerinaAstRoot();
//            var serviceDefinitions = [];
//            var serviceDefinitions1 = [];
//
//            // Create sample connector definition
//            var connectorDefinitions = [];
//            var connectorDefinition1 = ballerinaASTFactory.createConnectorDefinition();
//            connectorDefinitions.push(connectorDefinition1);
//
//            var serviceDefinition1 = ballerinaASTFactory.createServiceDefinition();
//            serviceDefinition1.setBasePath("/basePath1");
//
//            // Create Sample Resource Definitions
//            var resourceDefinition1 = ballerinaASTFactory.createResourceDefinition();
//
//            ballerinaAstRoot.addChild(serviceDefinition1);
//
//            serviceDefinition1.setResourceDefinitions([resourceDefinition1]);
//            serviceDefinition1.addChild(resourceDefinition1);
//
//            serviceDefinitions.push(serviceDefinition1);
//
//            ballerinaAstRoot.setServiceDefinitions(serviceDefinitions);
//
//            // Create Sample Function Definitions
//            var functionDefinitions = [];
//            var functionDefinitions1 = [];
//
//            var functionDefinition1 = ballerinaASTFactory.createFunctionDefinition();
//            functionDefinitions.push(functionDefinition1);
//            ballerinaAstRoot.addChild(functionDefinition1);
//            ballerinaAstRoot.setFunctionDefinitions(functionDefinitions);

            var sourceGenVisitor = new Ballerina.visitors.SourceGen.BallerinaASTRootVisitor();
            var BallerinaASTFactory = Ballerina.ast.BallerinaASTFactory;
            var ballerinaAstRoot1 = BallerinaASTFactory.createBallerinaAstRoot();

            //package definition
            var packageDefinition = BallerinaASTFactory.createPackageDefinition();
            packageDefinition.setPackageName("samples.passthrough");
            ballerinaAstRoot1.addChild(packageDefinition);
            ballerinaAstRoot1.setPackageDefinition(packageDefinition);

            //import declarations
            var importDeclaration_langMessage = BallerinaASTFactory.createImportDeclaration();
            importDeclaration_langMessage.setPackageName("ballerina.lang.message");
            importDeclaration_langMessage.setParent(ballerinaAstRoot1);
            var importDeclaration_netHttp = BallerinaASTFactory.createImportDeclaration();
            importDeclaration_netHttp.setPackageName("ballerina.net.http");
            importDeclaration_netHttp.setParent(ballerinaAstRoot1);
            var importDeclarations = [];
            importDeclarations.push(importDeclaration_langMessage);
            importDeclarations.push(importDeclaration_netHttp);
            ballerinaAstRoot1.setImportDeclarations(importDeclarations);
            ballerinaAstRoot1.addChild(importDeclaration_langMessage);
            ballerinaAstRoot1.addChild(importDeclaration_netHttp);

            //service definition
            var serviceDefinitions = [];
            var serviceDefinition_passthroughService = BallerinaASTFactory.createServiceDefinition();
            serviceDefinition_passthroughService.setServiceName("PassthroughService");
            serviceDefinition_passthroughService.setBasePath("/account");

            //service definition
            var serviceDefinition_passthroughService2 = BallerinaASTFactory.createServiceDefinition();
            serviceDefinition_passthroughService2.setServiceName("PassthroughService2");
            serviceDefinition_passthroughService2.setBasePath("/account");
            ballerinaAstRoot1.addChild(serviceDefinition_passthroughService2);
            // Adding Resources
            var resource_passthrough2 = BallerinaASTFactory.createResourceDefinition();
            resource_passthrough2.setResourceName('passthrough2');
            serviceDefinition_passthroughService2.addChild(resource_passthrough2);
            // Adding Resources
            var resource_passthrough3 = BallerinaASTFactory.createResourceDefinition();
            resource_passthrough3.setResourceName('passthrough3');
            serviceDefinition_passthroughService2.addChild(resource_passthrough3);

            // Adding Resources
            var resource_passthrough = BallerinaASTFactory.createResourceDefinition();
            resource_passthrough.setResourceName('passthrough');

            //Adding Connector declaration to resource_pasthrough
            var connector_declaration = BallerinaASTFactory.createConnectorDeclaration();
            var connector_declaration1 = BallerinaASTFactory.createConnectorDeclaration();
            var sList = [];
            sList.push(connector_declaration);
            sList.push(connector_declaration1);
            resource_passthrough.setConnections(sList);
           // resource_passthrough.setConnections([connector_declaration1]);
            resource_passthrough.addChild(connector_declaration);
            resource_passthrough.addChild(connector_declaration1);

            //Adding custom resource
            var custom_resource = BallerinaASTFactory.createResourceDefinition();
            custom_resource.setResourceName('customResource');

            //Adding resource argument
            var resourceArgument_m = BallerinaASTFactory.createResourceArgument();
            resourceArgument_m.setType("message");
            resourceArgument_m.setIdentifier("m");
            resource_passthrough.setResourceArguments([resourceArgument_m]);

            //Adding reply statement
            var statement_reply = BallerinaASTFactory.createReplyStatement();
            statement_reply.setReplyMessage("m");
            var statements = [];
            statements.push(statement_reply);
            resource_passthrough.setStatements(statements);

            //var resourceDefinitions = [];
            //resourceDefinitions.push(resource_passthrough);
            //resourceDefinitions.push(custom_resource);

            serviceDefinition_passthroughService.addChild(resource_passthrough);

            //Create Smaple If-else
            // var ifElseStatement1 = ballerinaASTFactory.createIfElseStatement();
            // var ifStatement1 = ballerinaASTFactory.createIfStatement();
            // var elseStatement1 = ballerinaASTFactory.createElseStatement();
            // ifElseStatement1.addChild(ifStatement1);
            // ifElseStatement1.addChild(elseStatement1);
            // ifStatement1.setCondition("Condition1");
            // resource_passthrough.addChild(ifElseStatement1);

            //creating while statement
            // var whileStatement1 = ballerinaASTFactory.createWhileStatement();
            // whileStatement1.setCondition("Condition2");
            // resource_passthrough.addChild(whileStatement1);

            /**
             * Create the sample assignment statement
             */
            var assignmentStatement = BallerinaASTFactory.createAssignmentStatement();
            resource_passthrough.addChild(assignmentStatement);

            /**
             * Create the sample function statement statement
             */
            var functionInvocation = BallerinaASTFactory.createFunctionInvocationStatement();
            //TODO:Commented to view get action statement
            //resource_passthrough.addChild(functionInvocation);

            /**
             * Create the sample logical expression
             */
            var logicalExp = BallerinaASTFactory.createLogicalExpression();
            logicalExp.setExpression('a > b');
            //TODO:Commented to view get action statement
            //resource_passthrough.addChild(logicalExp);

            /**
             * Create the sample arithmetic expression
             */
            var arithmeticExp = BallerinaASTFactory.createArithmeticExpression();
            arithmeticExp.setExpression('a = resp + 123');
            //TODO:Commented to view get action statement
           // resource_passthrough.addChild(arithmeticExp);

            // Create Sample try-catch statement
            var ifElseStatement = BallerinaASTFactory.createIfElseStatement();
            var ifStatement = BallerinaASTFactory.createIfStatement();
            var elseStatement = BallerinaASTFactory.createElseStatement();
            var tryCatchStatement1 = BallerinaASTFactory.createTryCatchStatement();
            var tryStatement1 = BallerinaASTFactory.createTryStatement();
            var catchStatement1 = BallerinaASTFactory.createCatchStatement();
            // catchStatement.setExceptionType("ArithmeticException ex");
            catchStatement1.setExceptionType("ArithmeticException ex");

           //Create get action statement for connector
            var getActionStatement1 = BallerinaASTFactory.createGetActionStatement(connector_declaration1);
            ifElseStatement.addChild(ifStatement);
            ifElseStatement.addChild(elseStatement);
            tryCatchStatement1.addChild(tryStatement1);
            tryCatchStatement1.addChild(catchStatement1);
            //TODO:Commented to view get action statement
           // resource_passthrough.addChild(ifElseStatement);
            //TODO:Commented to view get action statement
            //resource_passthrough.addChild(tryCatchStatement1);
            resource_passthrough.addChild(getActionStatement1);
            // Create sample Worker Declaration
            var workerDeclaration1 = BallerinaASTFactory.createWorkerDeclaration();
            var workerDeclaration2 = BallerinaASTFactory.createWorkerDeclaration();

            // Create Sample Function Definitions
            var functionDefinitions = [];
            var functionDefinitions1 = [];

            var functionDefinition1 = BallerinaASTFactory.createFunctionDefinition();
            functionDefinition1.addChild(workerDeclaration1);
            functionDefinition1.addChild(workerDeclaration2);
            functionDefinitions.push(functionDefinition1);
            ballerinaAstRoot1.addChild(functionDefinition1);
            ballerinaAstRoot1.setFunctionDefinitions(functionDefinitions);

            serviceDefinitions.push(serviceDefinition_passthroughService);
            ballerinaAstRoot1.setServiceDefinitions(serviceDefinitions);
            ballerinaAstRoot1.addChild(serviceDefinition_passthroughService);
            // ballerinaAstRoot1.accept(sourceGenVisitor);

            //Create environment and add add package list
            var ballerinaEnvironment = new Ballerina.env.Environment();

            var diagramRenderingContext = new DiagramRenderContext();

            var fileEditor = new Ballerina.views.BallerinaFileEditor({
                model: ballerinaAstRoot1,
                viewOptions: ballerinaEditorOptions
            });
            fileEditor.render(diagramRenderingContext);
        }

    });

    return FileTab;
});