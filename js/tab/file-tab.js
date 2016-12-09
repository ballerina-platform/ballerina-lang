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
define(['require', 'log', 'jquery', 'lodash', './tab', 'ballerina', 'workspace'], function (require, log, jquery, _, Tab, Ballerina, Workspace) {
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
            _.set(ballerinaEditorOptions, 'toolPalette', this.getParent().options.toolPalette);
            _.set(ballerinaEditorOptions, 'container', this.$el.get(0));
            var toolPallet = _.get(this.options.application, 'toolPalette');

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
            var ballerinaASTFactory = new Ballerina.ast.BallerinaASTFactory();
            var ballerinaAstRoot1 = ballerinaASTFactory.createBallerinaAstRoot();

            //package definition
            var packageDefinition = ballerinaASTFactory.createPackageDefinition();
            packageDefinition.setPackageName("samples.passthrough");
            ballerinaAstRoot1.addChild(packageDefinition);
            ballerinaAstRoot1.setPackageDefinition(packageDefinition);

            //import declarations
            var importDeclaration_langMessage = ballerinaASTFactory.createImportDeclaration();
            importDeclaration_langMessage.setPackageName("ballerina.lang.message");
            importDeclaration_langMessage.setParent(ballerinaAstRoot1);
            var importDeclaration_netHttp = ballerinaASTFactory.createImportDeclaration();
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
            var serviceDefinition_passthroughService = ballerinaASTFactory.createServiceDefinition();
            serviceDefinition_passthroughService.setServiceName("PassthroughService");
            serviceDefinition_passthroughService.setBasePath("/account");

            // Adding Resources
            var resource_passthrough = ballerinaASTFactory.createResourceDefinition();
            resource_passthrough.setResourceName('passthrough');

            //Adding custom resource
            var custom_resource = ballerinaASTFactory.createResourceDefinition();
            custom_resource.setResourceName('customResource');

            //Adding resource argument
            var resourceArgument_m = ballerinaASTFactory.createResourceArgument();
            resourceArgument_m.setType("message");
            resourceArgument_m.setIdentifier("m");
            resource_passthrough.setResourceArguments([resourceArgument_m]);

            //Adding reply statement
            var statement_reply = ballerinaASTFactory.createReplyStatement();
            statement_reply.setReplyMessage("m");
            var statements = [];
            statements.push(statement_reply);
            resource_passthrough.setStatements(statements);

            //var resourceDefinitions = [];
            //resourceDefinitions.push(resource_passthrough);
            //resourceDefinitions.push(custom_resource);

            serviceDefinition_passthroughService.addChild(resource_passthrough);

           //Create Smaple If-else
           var ifElseStatement1 = ballerinaASTFactory.createIfElseStatement();
           var ifStatement1 = ballerinaASTFactory.createIfStatement();
           var elseStatement1 = ballerinaASTFactory.createElseStatement();
           ifElseStatement1.addChild(ifStatement1);
           ifElseStatement1.addChild(elseStatement1);
           ifStatement1.setCondition("Condition1");
           resource_passthrough.addChild(ifElseStatement1);

           //creating while statement
           var whileStatement1 = ballerinaASTFactory.createWhileStatement();
           whileStatement1.setCondition("Condition2");
           resource_passthrough.addChild(whileStatement1);

           // Create Sample try-catch statement
           var tryCatchStatement = ballerinaASTFactory.createTryCatchStatement();
           var tryStatement = ballerinaASTFactory.createTryStatement();
           var catchStatement = ballerinaASTFactory.createCatchStatement();
           catchStatement.setExceptionType("ArithmeticException ex");
           tryCatchStatement.addChild(tryStatement);
           tryCatchStatement.addChild(catchStatement);
           resource_passthrough.addChild(tryCatchStatement);

           serviceDefinitions.push(serviceDefinition_passthroughService);
           ballerinaAstRoot1.setServiceDefinitions(serviceDefinitions);
           ballerinaAstRoot1.addChild(serviceDefinition_passthroughService);
           ballerinaAstRoot1.accept(sourceGenVisitor);

            //Create environment and add add package list
           var ballerinaEnvironment = new Ballerina.env.Environment();

           this.generateToolPallet(ballerinaEnvironment, toolPallet);

           var fileEditor = new Ballerina.views.BallerinaFileEditor({
                model: ballerinaAstRoot1,
                viewOptions: ballerinaEditorOptions
            });
            fileEditor.render();
        },

        generateToolPallet: function (environment, toolPallet) {
            var self = this;
            var mainElementsToolGroup = toolPallet.getElementToolGroups()[0];
            var statementsToolGroup = toolPallet.getElementToolGroups()[1];
            var packageList = environment.getPackages();

            _.each(packageList, function (pckg) {
                if (!_.isEmpty(pckg.getServiceDefinitions())) {
                    var service = self.isToolAvailableInPallet(mainElementsToolGroup, "Service");
                    if (!service) {
                        var serviceDefinitions = pckg.getServiceDefinitions();
                        var serviceDef = {
                            id: "service",
                            name: "Service",
                            icon: "images/tool-icons/lifeline.svg",
                            title: "Service",
                            node: serviceDefinitions[0]
                        };
                        mainElementsToolGroup.get("toolDefinitions").push(serviceDef);
                        toolPallet.updateToolGroup(serviceDef, mainElementsToolGroup);
                    }

                    _.each(pckg.getServiceDefinitions(), function (serviceDef) {
                        if (!_.isEmpty(serviceDef.getResourceDefinitions())) {

                        }
                    });
                }
                if (!_.isEmpty(pckg.getFunctionDefinitions())) {

                }
                if (!_.isEmpty(pckg.getConnectorDefinitions())) {

                }
                if (!_.isEmpty(pckg.getTypeDefinitions())) {

                }
            });
        },

        isToolAvailableInPallet: function (elementGroup, name) {
            var elementGroupDefinitions = elementGroup.get("toolDefinitions");
            for (var i = 0; i < elementGroupDefinitions.length; i++) {
                if (elementGroupDefinitions[i].name == name) {
                    return true;
                }
            }
            return false;
        }
    });

    return FileTab;
});