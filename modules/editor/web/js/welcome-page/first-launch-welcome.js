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
define(['require', 'log', 'jquery', 'backbone', 'command', 'ballerina'],
    function (require, log, $, Backbone, CommandManager, Ballerina) {

        var FirstLaunchWelcomePage = Backbone.View.extend({
            initialize: function (options) {
                var errMsg;
                if (!_.has(options, 'tab')) {
                    errMsg = 'unable to find a reference for editor tab';
                    log.error(errMsg);
                    throw errMsg;
                }
                this._tab = _.get(options, 'tab');
                var container = $(this._tab.getContentContainer());
                // make sure default tab content are cleared
                container.empty();
                // check whether container element exists in dom
                if (!container.length > 0) {
                    errMsg = 'unable to find container for welcome screen with selector: ' + _.get(options, 'container');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._$parent_el = container;
                this._options = options;
            },

            hide: function(){
                //Hiding menu bar
                this._options.application.menuBar.show();
                this.$el.hide();
            },

            show: function(){
                //Hiding menu bar
                this._options.application.menuBar.hide();
                this.$el.show();
            },

            render: function () {
                var backgroundDiv = $('<div></div>');
                var mainWelcomeDiv = $('<div></div>');
                var headingDiv = $('<div></div>');
                var headingTitleSpan = $('<span></span>');
                var headingImage = $('<img>');
                var headingGroup1 = $('<div></div>');
                var wrapTitle = $('<div></div>');
                var wrapImage = $('<div></div>');

                var bodyDiv = $('<div></div>');
                var newButton = $('<button></button>');
                var openButton = $('<button></button>');
                var buttonGroup1 = $('<div></div>');
                var buttonGroup2 = $('<div></div>');

                var bodyTitleSpan = $('<span></span>');
                var samplesDiv = $('<div></div>');

                backgroundDiv.addClass(_.get(this._options, 'cssClass.parent'));
                mainWelcomeDiv.addClass(_.get(this._options, 'cssClass.outer'));
                headingDiv.addClass(_.get(this._options, 'cssClass.heading'));
                headingTitleSpan.addClass(_.get(this._options, 'cssClass.headingTitle'));
                headingImage.addClass(_.get(this._options, 'cssClass.headingIcon'));
                headingImage.attr('src', 'images/Ballerina.svg');
                newButton.addClass(_.get(this._options, 'cssClass.buttonNew'));
                openButton.addClass(_.get(this._options, 'cssClass.buttonOpen'));
                headingGroup1.addClass(_.get(this._options, 'cssClass.headingTop'));
                buttonGroup1.addClass(_.get(this._options, 'cssClass.btnWrap1'));
                buttonGroup2.addClass(_.get(this._options, 'cssClass.btnWrap2'));

                bodyDiv.addClass(_.get(this._options, 'cssClass.body'));
                bodyTitleSpan.addClass(_.get(this._options, 'cssClass.bodyTitle'));
                samplesDiv.addClass(_.get(this._options, 'cssClass.samples'));
                samplesDiv.attr('id', 'samplePanel');

                newButton.text("New");
                openButton.text("Open");

                headingTitleSpan.text("Welcome to");
                bodyTitleSpan.text("Try out our samples / Templates");

                wrapTitle.append(headingTitleSpan);
                headingGroup1.append(wrapTitle);
                wrapImage.append(headingImage);
                headingGroup1.append(wrapImage);

                buttonGroup1.append(newButton);
                buttonGroup2.append(openButton);

                headingDiv.append(headingGroup1);
                headingDiv.append(buttonGroup1);
                headingDiv.append(buttonGroup2);

                bodyDiv.append(bodyTitleSpan);
                bodyDiv.append(samplesDiv);

                mainWelcomeDiv.append(headingDiv);
                mainWelcomeDiv.append(bodyDiv);
                backgroundDiv.append(mainWelcomeDiv);

                this._$parent_el.append(backgroundDiv);
                this.$el = backgroundDiv;

                var innerDiv = $('<div></div>');
                innerDiv.attr('id', "innerSamples");
                samplesDiv.append(innerDiv);

                var command = this._options.application.commandManager;
                var browserStorage = this._options.application.browserStorage;
                var echoSampleAST = this.generateEchoSampleAST();
                var helloFunctionSampleAST = this.generateHelloFunctionSampleAST();
                var passthroughSampleAST = this.generatePassthroughSampleAST();
                var config;
                var servicePreview;

                // Rendering echo sample preview
                config =
                {
                    "sampleName": "echo.bal",
                    "parentContainer": "#innerSamples",
                    "firstItem": true,
                    "clickEventCallback": function () {
                        command.dispatch("create-new-tab", echoSampleAST);
                        browserStorage.put("pref:passedFirstLaunch", true);
                    }
                };
                servicePreview = new Ballerina.views.ServicePreviewView(config);
                servicePreview.render();

                // Rendering hello function sample preview
                config =
                {
                    "sampleName": "helloFunction.bal",
                    "parentContainer": "#innerSamples",
                    "clickEventCallback": function () {
                        command.dispatch("create-new-tab", helloFunctionSampleAST);
                        browserStorage.put("pref:passedFirstLaunch", true);
                    }
                };
                servicePreview = new Ballerina.views.ServicePreviewView(config);
                servicePreview.render();

                // Rendering passthrough sample preview
                config =
                {
                    "sampleName": "passthrough.bal",
                    "parentContainer": "#innerSamples",
                    "clickEventCallback": function () {
                        command.dispatch("create-new-tab", passthroughSampleAST);
                        browserStorage.put("pref:passedFirstLaunch", true);
                    }
                };
                servicePreview = new Ballerina.views.ServicePreviewView(config);
                servicePreview.render();

                // Rendering CBR sample preview
                config =
                {
                    "sampleName": "contentBasedRouter.bal",
                    "parentContainer": "#innerSamples",
                    "clickEventCallback": function () {
                        command.dispatch("create-new-tab", passthroughSampleAST);
                        browserStorage.put("pref:passedFirstLaunch", true);
                    }
                };
                servicePreview = new Ballerina.views.ServicePreviewView(config);
                servicePreview.render();

                var command = this._options.application.commandManager;
                var browserStorage = this._options.application.browserStorage;

                $(newButton).on('click', function () {
                    command.dispatch("create-new-tab");
                    browserStorage.put("pref:passedFirstLaunch", true);
                });

                // upon welcome tab remove, set flag to indicate first launch pass
                this._tab.on('removed', function(){
                    browserStorage.put("pref:passedFirstLaunch", true);
                });
            },

            
            generateEchoSampleAST : function () {
                var BallerinaASTFactory = Ballerina.ast.BallerinaASTFactory;
                var ballerinaAstRoot1 = BallerinaASTFactory.createBallerinaAstRoot();

                //package definition
                var packageDefinition = BallerinaASTFactory.createPackageDefinition();
                packageDefinition.setPackageName("samples.echo");
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
                //importDeclarations.push(importDeclaration_langMessage);
                importDeclarations.push(importDeclaration_netHttp);
                ballerinaAstRoot1.setImportDeclarations(importDeclarations);
                //ballerinaAstRoot1.addChild(importDeclaration_langMessage);
                ballerinaAstRoot1.addChild(importDeclaration_netHttp);

                //service definition
                var serviceDefinition_passthroughService2 = BallerinaASTFactory.createServiceDefinition();
                serviceDefinition_passthroughService2.setServiceName("EchoService");
                serviceDefinition_passthroughService2.addAnnotation("BasePath", "/");
                ballerinaAstRoot1.addChild(serviceDefinition_passthroughService2);
                // Adding Resources
                var resource_passthrough2 = BallerinaASTFactory.createResourceDefinition();
                resource_passthrough2.setResourceName('echoResource');
                resource_passthrough2.addAnnotation("Method", "POST");
                resource_passthrough2.addAnnotation("Path", "/*");

                //Adding resource argument
                var resourceArgument_m = BallerinaASTFactory.createResourceArgument();
                resourceArgument_m.setType("message");
                resourceArgument_m.setIdentifier("m");

                var resourceArguments = [];
                resourceArguments.push(resourceArgument_m);
                resource_passthrough2.addArgument("message", "m");

                var functionInvocation = BallerinaASTFactory.createFunctionInvocationStatement();
                functionInvocation.setPackageName("http");
                functionInvocation.setFunctionName("convertToResponse");
                functionInvocation.setParams("m");
                resource_passthrough2.addChild(functionInvocation);

                //Adding reply statement
                var statement_reply = BallerinaASTFactory.createReplyStatement();
                statement_reply.setReplyMessage("m");
                resource_passthrough2.addChild(statement_reply);

                serviceDefinition_passthroughService2.addChild(resource_passthrough2);

                return ballerinaAstRoot1;
            },

            generateHelloFunctionSampleAST : function () {
                var BallerinaASTFactory = Ballerina.ast.BallerinaASTFactory;
                var ballerinaAstRoot1 = BallerinaASTFactory.createBallerinaAstRoot();

                //package definition
                var packageDefinition = BallerinaASTFactory.createPackageDefinition();
                packageDefinition.setPackageName("samples.echo");
                ballerinaAstRoot1.addChild(packageDefinition);
                ballerinaAstRoot1.setPackageDefinition(packageDefinition);

                //import declarations
                var importDeclaration_langSystem = BallerinaASTFactory.createImportDeclaration();
                importDeclaration_langSystem.setPackageName("ballerina.lang.system");
                importDeclaration_langSystem.setParent(ballerinaAstRoot1);
                var importDeclaration_netHttp = BallerinaASTFactory.createImportDeclaration();
                importDeclaration_netHttp.setPackageName("ballerina.net.http");
                importDeclaration_netHttp.setParent(ballerinaAstRoot1);
                var importDeclarations = [];
                importDeclarations.push(importDeclaration_langSystem);
                //importDeclarations.push(importDeclaration_netHttp);
                ballerinaAstRoot1.setImportDeclarations(importDeclarations);
                ballerinaAstRoot1.addChild(importDeclaration_langSystem);
                //ballerinaAstRoot1.addChild(importDeclaration_netHttp);

                //function definition

                var functionDefinition1 = BallerinaASTFactory.createFunctionDefinition();
                functionDefinition1.setFunctionName("main");
                ballerinaAstRoot1.addChild(functionDefinition1);

                var functionInvocation = BallerinaASTFactory.createFunctionInvocationStatement();
                functionInvocation.setPackageName("system");
                functionInvocation.setFunctionName("println");
                functionInvocation.setParams('"Hello world"');
                functionDefinition1.addChild(functionInvocation);

                return ballerinaAstRoot1;
            },

            generatePassthroughSampleAST : function () {
                var BallerinaASTFactory = Ballerina.ast.BallerinaASTFactory;
                var ballerinaAstRoot1 = BallerinaASTFactory.createBallerinaAstRoot();

                //package definition
                var packageDefinition = BallerinaASTFactory.createPackageDefinition();
                packageDefinition.setPackageName("samples.message.passthrough");
                ballerinaAstRoot1.addChild(packageDefinition);
                ballerinaAstRoot1.setPackageDefinition(packageDefinition);

                //import declarations
                var importDeclaration_langMessage = BallerinaASTFactory.createImportDeclaration();
                importDeclaration_langMessage.setPackageName("ballerina.lang.message");
                importDeclaration_langMessage.setParent(ballerinaAstRoot1);
                var importDeclaration_netHttp = BallerinaASTFactory.createImportDeclaration();
                importDeclaration_netHttp.setPackageName("ballerina.net.http as http");
                importDeclaration_netHttp.setParent(ballerinaAstRoot1);
                var importDeclarations = [];
                importDeclarations.push(importDeclaration_langMessage);
                importDeclarations.push(importDeclaration_netHttp);
                ballerinaAstRoot1.setImportDeclarations(importDeclarations);
                ballerinaAstRoot1.addChild(importDeclaration_langMessage);
                ballerinaAstRoot1.addChild(importDeclaration_netHttp);

                //service definition

                //service definition
                var serviceDefinition_passthroughService2 = BallerinaASTFactory.createServiceDefinition();
                serviceDefinition_passthroughService2.setServiceName("PassthroughService");
                serviceDefinition_passthroughService2.addAnnotation("BasePath", "/passthrough");
                ballerinaAstRoot1.addChild(serviceDefinition_passthroughService2);
                // Adding Resources
                var resource_passthrough2 = BallerinaASTFactory.createResourceDefinition();
                resource_passthrough2.setResourceName('passthrough');
                resource_passthrough2.addAnnotation("Method", "POST");
                resource_passthrough2.addAnnotation("Path", "/stocks");

                //Adding resource argument
                var resourceArgument_m = BallerinaASTFactory.createResourceArgument();
                resourceArgument_m.setType("message");
                resourceArgument_m.setIdentifier("m");

                var resourceArguments = [];
                resourceArguments.push(resourceArgument_m);
                resource_passthrough2.addArgument("message", "m");

                var connector_declaration = BallerinaASTFactory.createConnectorDeclaration();
                connector_declaration.setConnectorName("nyseEP");
                connector_declaration.setConnectorType("http:HTTPConnector");
                connector_declaration.setUri("http://localhost:9090");
                connector_declaration.setTimeout("100");
                resource_passthrough2.addChild(connector_declaration);

                var variable1 = BallerinaASTFactory.createVariableDeclaration();
                variable1.setType('message');
                variable1.setIdentifier('response');
                resource_passthrough2.addChild(variable1);

                var actionOpts = {connector:connector_declaration, isUserDropped:false};
                var getActionStatement1 = BallerinaASTFactory.createActionInvocationExpression(actionOpts);
                getActionStatement1.setConnector(connector_declaration);
                getActionStatement1.setAction("post");
                getActionStatement1.setVariableAccessor("response");
                getActionStatement1.setMessage("m");
                getActionStatement1.setPath("/NYSEStocks");
                resource_passthrough2.addChild(getActionStatement1);

                //Adding reply statement
                var statement_reply = BallerinaASTFactory.createReplyStatement();
                statement_reply.setReplyMessage("response");
                resource_passthrough2.addChild(statement_reply);

                serviceDefinition_passthroughService2.addChild(resource_passthrough2);

                //service definition
                var serviceDefinition_NYSEStockQuote = BallerinaASTFactory.createServiceDefinition();
                serviceDefinition_NYSEStockQuote.setServiceName("NYSEStockQuote");
                serviceDefinition_NYSEStockQuote.addAnnotation("BasePath", "/NYSEStocks");
                ballerinaAstRoot1.addChild(serviceDefinition_NYSEStockQuote);
                // Adding Resources
                var resource_stocks = BallerinaASTFactory.createResourceDefinition();
                resource_stocks.setResourceName('stocks');
                resource_stocks.addAnnotation("Method", "POST");
                resource_stocks.addAnnotation("Path", "/*");

                //Adding resource argument
                var resourceArgument_m = BallerinaASTFactory.createResourceArgument();
                resourceArgument_m.setType("message");
                resourceArgument_m.setIdentifier("m");

                var resourceArguments = [];
                resourceArguments.push(resourceArgument_m);
                resource_passthrough2.addArgument("message", "m");

                var variableResponse = BallerinaASTFactory.createVariableDeclaration();
                variableResponse.setType('message');
                variableResponse.setIdentifier('response');
                resource_stocks.addChild(variableResponse);

                var variablePayload = BallerinaASTFactory.createVariableDeclaration();
                variablePayload.setType('json');
                variablePayload.setIdentifier('payload');
                resource_stocks.addChild(variablePayload);

                var functionInvocation = BallerinaASTFactory.createFunctionInvocationStatement();
                functionInvocation.setPackageName("message");
                functionInvocation.setFunctionName("setJsonPayload");
                functionInvocation.setParams('response, payload');
                resource_stocks.addChild(functionInvocation);

                //Adding reply statement
                var statement_reply = BallerinaASTFactory.createReplyStatement();
                statement_reply.setReplyMessage("response");
                resource_stocks.addChild(statement_reply);

                serviceDefinition_NYSEStockQuote.addChild(resource_stocks);

                return ballerinaAstRoot1;
            },

        });

        return FirstLaunchWelcomePage;

    });

