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

            var ballerinaASTFactory = new Ballerina.ast.BallerinaASTFactory();
            var ballerinaAstRoot = ballerinaASTFactory.createBallerinaAstRoot();
            var serviceDefinitions = [];
            var serviceDefinitions1 = [];
            var ifStatements1 = [];
            var elseStatements1 = [];
            var ifCondition = ballerinaASTFactory.createExpression();
            // Create sample connector definition
            var connectorDefinitions = [];
            var connectorDefinition1 = ballerinaASTFactory.createConnectorDefinition();
            connectorDefinitions.push(connectorDefinition1);
            //ballerinaAstRoot.setConnectorDefinitions(connectorDefinitions);
            //ballerinaAstRoot.addChild(connectorDefinition1);

            var serviceDefinition1 = ballerinaASTFactory.createServiceDefinition();
            serviceDefinition1.setBasePath("/basePath1");
            var serviceDefinition2 = ballerinaASTFactory.createServiceDefinition();
            serviceDefinition2.setBasePath("/basePath2");

            var serviceDefinition3 = ballerinaASTFactory.createServiceDefinition();
            serviceDefinition3.setBasePath("/basePath3");
            var serviceDefinition4 = ballerinaASTFactory.createServiceDefinition();
            serviceDefinition4.setBasePath("/basePath4");

            // Create Sample Resource Definitions
            var resourceDefinition1 = ballerinaASTFactory.createResourceDefinition();
            var resourceDefinition2 = ballerinaASTFactory.createResourceDefinition();

            // Create If statement Definition
            var ifElseStatement = ballerinaASTFactory.createIfElseStatement(ifCondition,ifStatements1,elseStatements1);

            resourceDefinition1.resourceParent(serviceDefinition1);
            resourceDefinition2.resourceParent(serviceDefinition2);

            // Create Sample try-catch statement
            var tryCatchStatement1 = ballerinaASTFactory.createTryCatchStatement();
            var tryStatement = ballerinaASTFactory.createTryStatement();
            var catchStatement = ballerinaASTFactory.createCatchStatement();
            var tryCatchStatement2 = ballerinaASTFactory.createTryCatchStatement();
            var tryStatement2 = ballerinaASTFactory.createTryStatement();
            var catchStatement2 = ballerinaASTFactory.createCatchStatement();
            tryCatchStatement2.addChild(tryStatement2);
            tryCatchStatement2.addChild(catchStatement2);
            tryStatement.addChild(tryCatchStatement2);
            tryCatchStatement1.addChild(tryStatement);
            tryCatchStatement1.addChild(catchStatement);

            //Create Smaple If-else

            var ifElseStatement2 = ballerinaASTFactory.createIfElseStatement();
            var ifStatement2 = ballerinaASTFactory.createIfStatement();
            var elseStatement2 = ballerinaASTFactory.createElseStatement();
            ifElseStatement2.addChild(ifStatement2);
            ifElseStatement2.addChild(elseStatement2);

            resourceDefinition1.addChild(tryCatchStatement1);
            resourceDefinition1.addChild(ifElseStatement2);

            ballerinaAstRoot.addChild(serviceDefinition1);
            ballerinaAstRoot.addChild(serviceDefinition2);
            serviceDefinition1.setResourceDefinitions([resourceDefinition1, resourceDefinition2]);
            serviceDefinition1.addChild(resourceDefinition1);
            serviceDefinition1.addChild(resourceDefinition2);

            serviceDefinitions.push(serviceDefinition1);
            serviceDefinitions.push(serviceDefinition2);
            ballerinaAstRoot.setServiceDefinitions(serviceDefinitions);

            serviceDefinitions1.push(serviceDefinition3);
            serviceDefinitions1.push(serviceDefinition4);

            // Create Sample Function Definitions
            var functionDefinitions = [];
            var functionDefinitions1 = [];

            var functionDefinition1 = ballerinaASTFactory.createFunctionDefinition();
            functionDefinitions.push(functionDefinition1);
            ballerinaAstRoot.addChild(functionDefinition1);
            ballerinaAstRoot.setFunctionDefinitions(functionDefinitions);

            var functionDefinition2 = ballerinaASTFactory.createFunctionDefinition();
            var functionDefinition3 = ballerinaASTFactory.createFunctionDefinition();
            functionDefinitions1.push(functionDefinition2);
            functionDefinitions1.push(functionDefinition3);

            var package1 = new Ballerina.env.Package({name: 'PACKAGE1'});
            package1.addServiceDefinitions(serviceDefinitions);
            package1.addFunctionDefinitions(functionDefinitions);

            var package2 = new Ballerina.env.Package({name: 'PACKAGE2'});
            package2.addServiceDefinitions(serviceDefinitions1);
            package2.addFunctionDefinitions(functionDefinitions1);

            //Create environment and add add package list
            var ballerinaEnvironment = new Ballerina.env.Environment();
            ballerinaEnvironment.addPackage(package1);
            ballerinaEnvironment.addPackage(package2);

            this.generateToolPallet(ballerinaEnvironment, toolPallet);

            var fileEditor = new Ballerina.views.BallerinaFileEditor({
                model: ballerinaAstRoot,
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