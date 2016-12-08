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
define(['require', 'log', 'jquery', 'lodash', './tab', 'ballerina', 'workspace'],
    function (require, log, jquery, _, Tab, Ballerina, Workspace) {
    var  FileTab;

    FileTab = Tab.extend({
        initialize: function (options) {
            Tab.prototype.initialize.call(this, options);
            if(!_.has(options, 'file')){
                this._file = new Workspace.File({isTemp: true}, {storage: this.getParent().getBrowserStorage()});
            } else {
                this._file = _.get(options, 'file');
            }
        },

        getFile: function(){
            return this._file;
        },

        render: function () {
            Tab.prototype.render.call(this);
            var ballerinaEditorOptions = _.get(this.options, 'ballerina_editor');
            _.set(ballerinaEditorOptions, 'toolPalette', this.getParent().options.toolPalette);
            _.set(ballerinaEditorOptions, 'container', this.$el.get(0));

            var ballerinaASTFactory = new Ballerina.ast.BallerinaASTFactory();
            var ballerinaAstRoot = ballerinaASTFactory.createBallerinaAstRoot();
            var serviceDefinitions = [];
            // Create sample connector definition
            var connectorDefinitions = [];
            var connectorDefinition1 = ballerinaASTFactory.createConnectorDefinition();
            connectorDefinitions.push(connectorDefinition1);
            ballerinaAstRoot.setConnectorDefinitions(connectorDefinitions);
            ballerinaAstRoot.addChild(connectorDefinition1);

            var serviceDefinition1 = ballerinaASTFactory.createServiceDefinition();
            serviceDefinition1.setBasePath("/basePath1");
            var serviceDefinition2 = ballerinaASTFactory.createServiceDefinition();
            serviceDefinition2.setBasePath("/basePath2");

            // Create Sample Resource Definitions
            var resourceDefinition1 = ballerinaASTFactory.createResourceDefinition();
            var resourceDefinition2 = ballerinaASTFactory.createResourceDefinition();
            resourceDefinition1.resourceParent(serviceDefinition1);
            resourceDefinition2.resourceParent(serviceDefinition2);

            // Create Sample try-catch statement
            var tryCatchStatement1 = ballerinaASTFactory.createTryCatchStatement();

            resourceDefinition1.addChild(tryCatchStatement1);

            ballerinaAstRoot.addChild(serviceDefinition1);
            ballerinaAstRoot.addChild(serviceDefinition2);
            serviceDefinition1.setResourceDefinitions([resourceDefinition1, resourceDefinition2]);
            serviceDefinition1.addChild(resourceDefinition1);
            serviceDefinition1.addChild(resourceDefinition2);

            serviceDefinitions.push(serviceDefinition1);
            serviceDefinitions.push(serviceDefinition2);
            ballerinaAstRoot.setServiceDefinitions(serviceDefinitions);

            // Create Sample Function Definitions
            var functionDefinitions = [];
            var functionDefinition1 = ballerinaASTFactory.createFunctionDefinition();
            functionDefinitions.push(functionDefinition1);
            ballerinaAstRoot.addChild(functionDefinition1);
            ballerinaAstRoot.setFunctionDefinitions(functionDefinitions);

            var fileEditor = new  Ballerina.views.BallerinaFileEditor({model: ballerinaAstRoot, viewOptions: ballerinaEditorOptions});

            /**
             * Testing the source-gen traverse
             *
             */
            var sourceGenVisitor = new Ballerina.visitors.SourceGen.BallerinaASTRootVisitor();
            ballerinaAstRoot.accept(sourceGenVisitor);
            ballerinaAstRoot.accept(fileEditor);
            log.info(sourceGenVisitor.getGeneratedSource());
        }
    });

    return FileTab;
});