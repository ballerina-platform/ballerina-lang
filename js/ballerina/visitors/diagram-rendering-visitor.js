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
define(['require', 'lodash', 'log', 'ast_visitor', 'views/ballerina-file-editor', 'views/service-definition-view'],
    function(require, _, log, ASTVisitor, FileEditor, ServiceDefinitionView) {

    var DiagramRenderingVisitor = function(containerView) {
        this.containerView = containerView;
        this.viewMap = new Map();
    };

    DiagramRenderingVisitor.prototype = Object.create(ASTVisitor.prototype);
    DiagramRenderingVisitor.prototype.constructor = DiagramRenderingVisitor;

    DiagramRenderingVisitor.prototype.visitBallerinaASTRoot = function (astNode) {
        log.info("Visiting BallerinaASTRoot");
        var fileEditor = new FileEditor(null, astNode);
        fileEditor.init(astNode, this.containerView);
        this.viewMap.set(astNode, fileEditor);
        return true;
    };

    DiagramRenderingVisitor.prototype.visitReplyStatement = function (astNode) {
        log.info("Visiting ReplyStatement");
        var parent = astNode.getParent();
        var parentView  = this.viewMap.get(parent);
        //TODO create new reply statement view and call render
        return false;
    };

    DiagramRenderingVisitor.prototype.visitConnectionDeclaration = function () {
        log.info("Visiting ConnectionDefinition");
        return false;
    };

    DiagramRenderingVisitor.prototype.visitWorkerDeclaration = function () {
        return false;
    };

    DiagramRenderingVisitor.prototype.visitReturnStatement = function (astNode) {
        log.info("Visiting ReturnStatement");
        var parent = astNode.getParent();
        var parentView  = this.viewMap.get(parent);
        //TODO create new return statement view and call render
        return false;
    };

    DiagramRenderingVisitor.prototype.visitServiceDefinition = function (astNode) {
        //modelView.render();
        log.info("Visiting ServiceDefinition");
        var parent = astNode.getParent();
        var parentView  = this.viewMap.get(parent);
        _.forEach(parent.serviceDefinitions, function(serviceDefinition, index) {
            var canvas = parentView.canvasList[index];
            var serviceDefinitionView = new ServiceDefinitionView(serviceDefinition, canvas);
            serviceDefinitionView.render();
        });
        return true;
    };

    DiagramRenderingVisitor.prototype.visitResourceDefinition = function () {
        //modelView.render();
        window.console.log("VISIT RESOURCE DEFINITION");
        return false;
    };

    DiagramRenderingVisitor.prototype.visitThrowStatement = function (astNode) {
        log.info("Visiting ThrowStatement");
        var parent = astNode.getParent();
        var parentView  = this.viewMap.get(parent);
        //TODO create new throw statement view and call render
        return false;
    };

    DiagramRenderingVisitor.prototype.visitWhileStatement = function (astNode) {
        log.info("Visiting WhileStatement");
        var parent = astNode.getParent();
        var parentView  = this.viewMap.get(parent);
        //TODO create new while statement view and call render
        return true;
    };

    return DiagramRenderingVisitor;
});