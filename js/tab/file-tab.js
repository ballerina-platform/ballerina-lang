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

//            var BallerinaASTFactory = new Ballerina.ast.BallerinaASTFactory();
//            var ballerinaAstRoot = BallerinaASTFactory.createBallerinaAstRoot();
//            var serviceDefinitions = [];
//            var serviceDefinitions1 = [];
//
//            // Create sample connector definition
//            var connectorDefinitions = [];
//            var connectorDefinition1 = BallerinaASTFactory.createConnectorDefinition();
//            connectorDefinitions.push(connectorDefinition1);
//
//            var serviceDefinition1 = BallerinaASTFactory.createServiceDefinition();
//            serviceDefinition1.setBasePath("/basePath1");
//
//            // Create Sample Resource Definitions
//            var resourceDefinition1 = BallerinaASTFactory.createResourceDefinition();
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
//            var functionDefinition1 = BallerinaASTFactory.createFunctionDefinition();
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
            serviceDefinition_passthroughService.setSource({})

            //service definition
            var serviceDefinition_passthroughService2 = BallerinaASTFactory.createServiceDefinition();
            serviceDefinition_passthroughService2.setServiceName("PassthroughService2");
            serviceDefinition_passthroughService2.setBasePath("/account");
            ballerinaAstRoot1.addChild(serviceDefinition_passthroughService2);
            // Adding Resources
            var resource_passthrough2 = BallerinaASTFactory.createResourceDefinition();
            resource_passthrough2.setResourceName('passthrough2');

            // Adding ifelse
            var ifelse1 = BallerinaASTFactory.createIfElseStatement();
            var if1 = BallerinaASTFactory.createIfStatement();
            var else1 = BallerinaASTFactory.createElseStatement();
            var elseIf1 = BallerinaASTFactory.createElseIfStatement();
            var elseIf2 = BallerinaASTFactory.createElseIfStatement();
            ifelse1.addChild(if1);
            //ifelse1.addChild(elseIf1);
            //ifelse1.addChild(elseIf2);
            //ifelse1.addChild(else1);

            var ifelse2 = BallerinaASTFactory.createIfElseStatement();
            var if2 = BallerinaASTFactory.createIfStatement();
            var else2 = BallerinaASTFactory.createElseStatement();
            ifelse2.addChild(if2);
            ifelse2.addChild(else2);

            var ifelse3 = BallerinaASTFactory.createIfElseStatement();
            var if3 = BallerinaASTFactory.createIfStatement();
            var else3 = BallerinaASTFactory.createElseStatement();
            ifelse3.addChild(if3);
            ifelse3.addChild(else3);
            if1.addChild(ifelse2);
            elseIf1.addChild(ifelse3);
            // else1.addChild(ifelse3);
            var ifelse4 = BallerinaASTFactory.createIfElseStatement();
            var if4 = BallerinaASTFactory.createIfStatement();
            var else4 = BallerinaASTFactory.createElseStatement();
            ifelse4.addChild(if4);
            ifelse4.addChild(else4);
            // if2.addChild(ifelse4);
            //
            // if2.addChild(ifelse3);
            //resource_passthrough2.addChild(ifelse1);
            serviceDefinition_passthroughService2.addChild(resource_passthrough2);
            // Adding Resources
            var resource_passthrough3 = BallerinaASTFactory.createResourceDefinition();
            resource_passthrough3.setResourceName('passthrough3');
            var service_variable_declaration = [];

            var variable1 = BallerinaASTFactory.createVariableDeclaration();
            variable1.setType('string');
            variable1.setIdentifier('wso2');
            service_variable_declaration.push(variable1);

            var variable2 = BallerinaASTFactory.createVariableDeclaration();
            variable2.setType('int');
            variable2.setIdentifier('2');
            service_variable_declaration.push(variable2);

            serviceDefinition_passthroughService.setVariableDeclarations(service_variable_declaration);
            serviceDefinition_passthroughService2.addChild(resource_passthrough3);

            // Adding Resources
            var resource_passthrough = BallerinaASTFactory.createResourceDefinition();
            resource_passthrough.setResourceName('passthrough');

            //Adding Connector declaration to resource_pasthrough
            var connector_declaration = BallerinaASTFactory.createConnectorDeclaration();
            connector_declaration.setConnectorType('http:HttpConnector');
            connector_declaration.setConnectorName('nyseEP');
            connector_declaration.setUri('http://localhost:8080/exchange/nyse/');
            var connector_declaration1 = BallerinaASTFactory.createConnectorDeclaration();
            connector_declaration1.setConnectorType('http:HttpConnector');
            connector_declaration1.setConnectorName('nasdaqEP');
            connector_declaration1.setUri('http://localhost:8080/exchange/nasdaq/');
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
            var whileStatement1 = BallerinaASTFactory.createWhileStatement();
            whileStatement1.setCondition("Condition2");

            resource_passthrough.addChild(whileStatement1);

            /**
             * Create the sample assignment statement
             */
            var assignmentStatement = BallerinaASTFactory.createAssignmentStatement();
            resource_passthrough.addChild(assignmentStatement);

            var assignmentStatement2 = BallerinaASTFactory.createAssignmentStatement();
          //  resource_passthrough.addChild(assignmentStatement2);
            var assignmentStatement3 = BallerinaASTFactory.createAssignmentStatement();
         //   resource_passthrough.addChild(assignmentStatement3);
            var assignmentStatement4 = BallerinaASTFactory.createAssignmentStatement();
          //  resource_passthrough.addChild(assignmentStatement4);
            var assignmentStatement5 = BallerinaASTFactory.createAssignmentStatement();
          //  resource_passthrough.addChild(assignmentStatement5);


            /**
             * Create the sample function statement statement
             */
            var functionInvocation = BallerinaASTFactory.createFunctionInvocationStatement();
            //TODO:Commented to view get action statement
            // resource_passthrough.addChild(functionInvocation);

            /**
             * Create the sample logical expression
             */
            var logicalExp = BallerinaASTFactory.createLogicalExpression();
            logicalExp.setExpression('a > b');
            //TODO:Commented to view get action statement
            // resource_passthrough.addChild(logicalExp);
            // whileStatement1.addChild(functionInvocation);

            /**
             * Create the sample arithmetic expression
             */
            var arithmeticExp = BallerinaASTFactory.createArithmeticExpression();
            arithmeticExp.setExpression('a = resp + 123');
            //TODO:Commented to view get action statement
           resource_passthrough.addChild(arithmeticExp);

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
            var actionOpts = {connector:connector_declaration1};
            var getActionStatement1 = BallerinaASTFactory.createActionInvocationStatement(actionOpts);
           // var actionOpts2 = {connector:connector_declaration, isUserDropped:false};
           // var getActionStatement2 = BallerinaASTFactory.createGetActionStatement(actionOpts2);
            ifElseStatement.addChild(ifStatement);
            ifElseStatement.addChild(elseStatement);
            tryCatchStatement1.addChild(tryStatement1);
            tryCatchStatement1.addChild(catchStatement1);
            //TODO:Commented to view get action statement
           // resource_passthrough.addChild(ifElseStatement);
            //TODO:Commented to view get action statement
            //resource_passthrough.addChild(tryCatchStatement1);
            resource_passthrough.addChild(getActionStatement1);
           // resource_passthrough.addChild(getActionStatement2);
            // Create sample Worker Declaration
            var workerDeclaration1 = BallerinaASTFactory.createWorkerDeclaration();
            var workerDeclaration2 = BallerinaASTFactory.createWorkerDeclaration();

            // Create Sample Function Definitions
            var functionDefinitions = [];
            var functionDefinitions1 = [];

            var functionDefinition1 = BallerinaASTFactory.createFunctionDefinition();
            functionDefinition1.addChild(workerDeclaration1);
            functionDefinition1.addChild(workerDeclaration2);
            // functionDefinition1.addChild(ifelse1);
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
                container: this.$el.get(0),
                viewOptions: ballerinaEditorOptions
            });
            this._fileEditor = fileEditor;
            fileEditor.render(diagramRenderingContext);
        },

        getBallerinaFileEditor: function () {
            return this._fileEditor;
        }

    });

    return FileTab;
});