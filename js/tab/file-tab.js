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
            //packageDefinition.setPackageName("samples.echo");
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
