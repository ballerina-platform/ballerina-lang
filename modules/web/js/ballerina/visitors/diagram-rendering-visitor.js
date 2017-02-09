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
define(['require', 'lodash', 'log', './ast-visitor', './../views/ballerina-file-editor', './../views/service-definition-view',
    './../views/resource-definition-view', './../views/function-definition-view'],
    function(require, _, log, ASTVisitor, FileEditor, ServiceDefinitionView, ResourceDefinitionView, FunctionDefinitionView) {

    var DiagramRenderingVisitor = function(containerView) {
        this.containerView = containerView;
        this._viewsList = [];
    };

    DiagramRenderingVisitor.prototype = Object.create(ASTVisitor.prototype);
    DiagramRenderingVisitor.prototype.constructor = DiagramRenderingVisitor;

    DiagramRenderingVisitor.prototype.visitBallerinaASTRoot = function (astNode) {
        log.debug("Visiting BallerinaASTRoot");
        var fileEditor = new FileEditor(null, astNode);
        fileEditor.init(astNode, this.containerView);
        this._viewsList.push(fileEditor);
        return true;
    };

    DiagramRenderingVisitor.prototype.visitReplyStatement = function (astNode) {
        log.debug("Visiting ReplyStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new reply statement view and call render
        return false;
    };

    DiagramRenderingVisitor.prototype.visitConnectionDeclaration = function () {
        log.debug("Visiting ConnectionDefinition");
        return false;
    };

    DiagramRenderingVisitor.prototype.visitWorkerDeclaration = function () {
        return false;
    };

    DiagramRenderingVisitor.prototype.visitReturnStatement = function (astNode) {
        log.debug("Visiting ReturnStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new return statement view and call render
        return false;
    };

    DiagramRenderingVisitor.prototype.visitServiceDefinition = function (astNode) {
        //modelView.render();
        log.debug("Visiting ServiceDefinition");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        for (var id in parent.serviceDefinitions) {
            var serviceDefinition = parent.serviceDefinitions[id];
            var canvas = parentView.canvasList[id];
            var serviceDefinitionView = new ServiceDefinitionView(serviceDefinition, canvas);
            this._viewsList.push(serviceDefinitionView);
            serviceDefinitionView.render();
        }
        return true;
    };

    DiagramRenderingVisitor.prototype.visitResourceDefinition = function (astNode) {
        //modelView.render();
        var parent = astNode.resourceParent();
        var parentView = _.find(this._viewsList, ['_model',parent]);
        if (_.isUndefined(parentView)) {
            log.error('Parent View cannot find in the views List');
            throw 'Parent View cannot find in the views List';
        }
        var canvas = parentView.getContainer();

        var resourceView = _.find(this._viewsList, ['_model',astNode]);
        if (_.isUndefined(resourceView)) {
            var resourceDefinitionView = new ResourceDefinitionView(astNode, canvas);
            this._viewsList.push(resourceDefinitionView);
            resourceDefinitionView.render();
        } else {
            log.debug('Existing Resource View Found');
        }
        log.debug("VISIT RESOURCE DEFINITION");
        return false;
    };

    DiagramRenderingVisitor.prototype.visitThrowStatement = function (astNode) {
        log.debug("Visiting ThrowStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new throw statement view and call render
        return false;
    };

    DiagramRenderingVisitor.prototype.visitWhileStatement = function (astNode) {
        log.debug("Visiting WhileStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new while statement view and call render
        return true;
    };

    DiagramRenderingVisitor.prototype.visitFunctionDefinition = function(astNode) {
        log.debug("Visiting FunctionDefinition");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        for (var id in parent.functionDefinitions) {
            var functionDefinition = parent.functionDefinitions[id];
            var canvas = parentView.canvasList[id];
            var functionDefinitionView = new FunctionDefinitionView(functionDefinition, canvas);
            this._viewsList.push(functionDefinitionView);
            functionDefinitionView.render();
        }
        return true;
    };

    DiagramRenderingVisitor.prototype.visitActionDefinition = function(astNode) {
        return false;
    };

    return DiagramRenderingVisitor;
});