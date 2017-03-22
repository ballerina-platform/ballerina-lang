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
import require from 'require';
import _ from 'lodash';
import log from 'log';
import ASTVisitor from './ast-visitor';
import FileEditor from './../views/ballerina-file-editor';
import ServiceDefinitionView from './../views/service-definition-view';
import ResourceDefinitionView from './../views/resource-definition-view';
import FunctionDefinitionView from './../views/function-definition-view';

class DiagramRenderingVisitor extends ASTVisitor {
    constructor(containerView) {
        this.containerView = containerView;
        this._viewsList = [];
    }

    visitBallerinaASTRoot(astNode) {
        log.debug("Visiting BallerinaASTRoot");
        var fileEditor = new FileEditor(null, astNode);
        fileEditor.init(astNode, this.containerView);
        this._viewsList.push(fileEditor);
        return true;
    }

    visitReplyStatement(astNode) {
        log.debug("Visiting ReplyStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new reply statement view and call render
        return false;
    }

    visitConnectionDeclaration() {
        log.debug("Visiting ConnectionDefinition");
        return false;
    }

    visitWorkerDeclaration() {
        return false;
    }

    visitReturnStatement(astNode) {
        log.debug("Visiting ReturnStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new return statement view and call render
        return false;
    }

    visitServiceDefinition(astNode) {
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
    }

    visitResourceDefinition(astNode) {
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
    }

    visitThrowStatement(astNode) {
        log.debug("Visiting ThrowStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new throw statement view and call render
        return false;
    }

    visitWhileStatement(astNode) {
        log.debug("Visiting WhileStatement");
        var parent = astNode.getParent();
        var parentView  = _.find(this._viewsList, ['_model',parent]);
        //TODO create new while statement view and call render
        return true;
    }

    visitFunctionDefinition(astNode) {
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
    }

    visitActionDefinition(astNode) {
        return false;
    }
}

export default DiagramRenderingVisitor;
