/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import _ from 'lodash';
import ASTVisitor from './../visitors/ast-visitor';

/**
 * An abstract class which consists functions of moving or resizing views.
 * @class BallerinaView
 * @extends ASTVisitor
 */
class BallerinaView extends ASTVisitor {
    /**
     * @param {Object} args - Arguments for creating the view.
     * @param {ASTNode} args.model - Any ASTNode as the model.
     * @param {Object} args.container - The HTML container to which the view should be added to.
     * @param {Object} [args.viewOptions={}] - Configuration values for the view.
     * @param {ToolPalette} args.toolPalette - reference for tool palette
     * @param {DiagramRenderContext} args.diagramRenderingContext - Diagram rendering context for the view.
     * @constructor
     */
    constructor(args) {
        super(args);
        this._parent = _.get(args, 'parent');
        this._parseFailed = _.get(args, 'parseFailed');
        this.setModel(_.get(args, 'model'));
        this._container = _.get(args, 'container');
        this._viewOptions = _.get(args, 'viewOptions', {});
        this._backendEndpointsOptions = _.get(args, 'backendEndpointsOptions', {});
        this.toolPalette = _.get(args, 'toolPalette');
        this.messageManager = _.get(args, 'messageManager');
        this.diagramRenderingContext = _.get(args, 'diagramRenderContext');
        this.id = uuid();
    }

    setParent(parent) {
        this._parent = parent;
    }

    getParent() {
        return this._parent;
    }

    setModel(model) {
        this._model = model;
    }

    getModel() {
        return this._model;
    }

    setContainer(container) {
        this._container = container;
    }

    getContainer() {
        return this._container;
    }

    setToolPalette(toolPalette) {
        this.toolPalette = toolPalette;
    }

    getToolPalette() {
        return this.toolPalette;
    }

    setMessageManager(messageManager) {
        this.messageManager = messageManager;
    }

    getMessageManager() {
        return this.messageManager;
    }

    setDiagramRenderingContext(diagramRenderContext) {
        this.diagramRenderingContext = diagramRenderContext;
    }

    getDiagramRenderingContext() {
        return this.diagramRenderingContext;
    }

    /**
     * Renders/Draws the view for a specific model(i.e {@link BallerinaView#_model}).
     * @abstract
     */
    render() {
        throw 'Method not implemented';
    }
}

// Auto generated Id for service definitions (for accordion views)
var uuid = function () {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return `${s4() + s4()}-${s4()}-${s4()}-${
        s4()}-${s4()}${s4()}${s4()}`;
};

export default BallerinaView;
