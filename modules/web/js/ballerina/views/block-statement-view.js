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
import log from 'log';
import BallerinaStatementView from './ballerina-statement-view';
import D3Utils from 'd3utils';
import d3 from 'd3';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import StatementContainer from './statement-container';

/**
 * Super view class for all block statements e.g. while, if, else etc.
 * @class BlockStatementView
 * @extends BallerinaStatementView
 */
class BlockStatementView extends BallerinaStatementView {

    /**
     * @param args {*} arguments for the creating view
     * @constructor
     */
    constructor(args) {
        super(args);
        var viewOptions = this.getViewOptions();
        viewOptions.height = _.get(args, 'viewOptions.height', 100);
        viewOptions.width = _.get(args, 'viewOptions.width', 120); // starting width
        viewOptions.minWidth = _.get(args, 'viewOptions.minWidth', 120); // minimum width
        _.set(viewOptions, 'title.text', _.get(args, 'viewOptions.title.text', 'Statement'));
        _.set(viewOptions, 'title.width', _.get(args, 'viewOptions.title.width', 40));
        _.set(viewOptions, 'title.height', _.get(args, 'viewOptions.title.height', 25));
        _.set(viewOptions, 'padding.left', _.get(args, 'viewOptions.padding.left', 7));
        _.set(viewOptions, 'padding.right', _.get(args, 'viewOptions.padding.right', 7));
        _.set(viewOptions, 'padding.top', _.get(args, 'viewOptions.padding.top', viewOptions.title.height));
        _.set(viewOptions, 'padding.bottom', _.get(args, 'viewOptions.padding.bottom', 10));
        _.set(viewOptions, 'contentOffset', _.get(viewOptions, 'contentOffset', {top: 10, bottom: 10}));

        this.getBoundingBox().fromTopCenter(
            this.getTopCenter(), viewOptions.width,
            viewOptions.height);
        this._onForcedWidthZoomed = false;
    }

    render(renderingContext) {
        this.setDiagramRenderingContext(renderingContext);
        var bBox = this.getBoundingBox();
        var model = this.getModel();

        // Creating statement group.
        var statementGroup = D3Utils.group(d3.select(this.getContainer()));
        // "id" is prepend with a "_" to be compatible with HTML4
        statementGroup.attr('id', '_' + model.id);

        var outerRect = D3Utils.rect(bBox.x(), bBox.y(), bBox.w(), bBox.h(), 0, 0, statementGroup)
                               .classed('background-empty-rect', true);
        statementGroup.outerRect = outerRect;
        // Creating title.
        var titleViewOptions = this.getViewOptions().title;
        var titleGroup = D3Utils.group(statementGroup);
        statementGroup.title = titleGroup;
        var titleRect = D3Utils.rect(bBox.x(), bBox.y(), bBox.w(), 25, 0, 0, statementGroup)
                               .classed('statement-title-rect', true);
        this._titleRect = titleRect;
        var titleText = D3Utils.textElement(bBox.x() + 20, bBox.y() + 12, titleViewOptions.text, statementGroup)
                               .classed('statement-text', true);
        var titleTextWrapperPolyline = D3Utils.polyline(getTitlePolyLinePoints(bBox, titleViewOptions),
                                                        statementGroup)
                                              .classed('statement-title-polyline', true);
        titleGroup.titleRect = titleRect;
        titleGroup.titleText = titleText;
        titleGroup.titleTextWrapperPolyline = titleTextWrapperPolyline;
        this.setStatementGroup(statementGroup);

        // Registering event listeners.
        bBox.on('moved', function (offset) {
            outerRect.attr('x', parseFloat(outerRect.attr('x')) + offset.dx);
            outerRect.attr('y', parseFloat(outerRect.attr('y')) + offset.dy);

            titleRect.attr('x', parseFloat(titleRect.attr('x')) + offset.dx);
            titleRect.attr('y', parseFloat(titleRect.attr('y')) + offset.dy);

            titleText.attr('y', parseFloat(titleText.attr('y')) + offset.dy);
            titleText.attr('x', parseFloat(titleText.attr('x')) + offset.dx);
            titleTextWrapperPolyline.attr('points', getTitlePolyLinePoints(bBox, titleViewOptions));
        });
        bBox.on('width-changed', function () {
            outerRect.attr('width', bBox.w());
            titleRect.attr('width', bBox.w());
        });
        bBox.on('height-changed', function () {
            outerRect.attr('height', bBox.h());
        });

        this.getDiagramRenderingContext().setViewOfModel(this.getModel(), this);
        this.renderStatementContainer();
    }

    renderStatementContainer() {
        var viewOptions = this.getViewOptions();
        var model = this.getModel();
        var boundingBox = this.getBoundingBox();
        var topCenter = this.getTopCenter();

        // Creating view options for the new statement container.
        var statementContainerOpts = {};
        _.set(statementContainerOpts, 'model', model);
        _.set(statementContainerOpts, 'topCenter', topCenter.clone().move(0, viewOptions.padding.top));
        _.set(statementContainerOpts, 'bottomCenter', topCenter.clone().move(0, viewOptions.height));
        _.set(statementContainerOpts, 'width', boundingBox.w());
        _.set(statementContainerOpts, 'minWidth', statementContainerOpts.width);
        _.set(statementContainerOpts, 'height', (boundingBox.h() - viewOptions.padding.top));
        _.set(statementContainerOpts, 'minHeight', statementContainerOpts.height);
        _.set(statementContainerOpts, 'padding.left', viewOptions.padding.left);
        _.set(statementContainerOpts, 'padding.right', viewOptions.padding.right);
        _.set(statementContainerOpts, 'offset', {top: viewOptions.title.height, bottom: 30});
        _.set(statementContainerOpts, 'parent', this);
        _.set(statementContainerOpts, 'container', this.getStatementGroup().node());
        _.set(statementContainerOpts, 'toolPalette', this.getToolPalette());

        // Creating new statement container.
        var statementContainer = new StatementContainer(statementContainerOpts);
        this.setStatementContainer(statementContainer);

        statementContainer.render(this.getDiagramRenderingContext());

        // Registering event handlers.
        this.listenTo(statementContainer.getBoundingBox(), 'height-changed', function (dh) {
            boundingBox.h(boundingBox.h() + dh);
        });

        statementContainer.listenTo(this.getBoundingBox(), 'moved', function (offset) {
            statementContainer.isOnWholeContainerMove = true;
            statementContainer.getBoundingBox().move(offset.dx, offset.dy);
            statementContainer.isOnWholeContainerMove = false;
        });

        this.listenTo(statementContainer.getBoundingBox(), 'width-changed', function () {
            boundingBox.w(statementContainer.getBoundingBox().w());
        });

        /* Removing all the registered 'child-added' event listeners for this model. This is needed because we
         are not un-registering registered event while the diagram element deletion. Due to that, sometimes we
         are having two or more view elements listening to the 'child-added' event of same model. */
        model.off('child-added');
        model.on('child-added', function (child) {
            this.visit(child);
        }, this);
        model.accept(this);
    }

    //TODO : rename as visitStatement to avoid conflicts with generic visit
    visit(statement) {
        var args = {
            model: statement,
            container: this.getStatementGroup().node(),
            viewOptions: {},
            toolPalette: this.getToolPalette(),
            messageManager: this.messageManager,
            parent: this
        };
        this.getStatementContainer().renderStatement(statement, args);
    }

    setModel(model) {
        if (_.isNil(model)) {
            var message = 'Model of a block statement cannot be null.';
            log.error(message);
            throw new Error(message);
        } else {
            this._model = model;
        }
    }

    getModel() {
        return this._model;
    }

    setContainer(container) {
        if (_.isNil(container)) {
            var message = 'Container of a block statement cannot be null.';
            log.error(message);
            throw new Error(message);
        } else {
            this._container = container;
        }
    }

    getContainer() {
        return this._container;
    }

    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    setStatementContainer(statementContainer) {
        this._statementContainer = statementContainer;
    }

    getStatementContainer() {
        return this._statementContainer;
    }

    getToolPalette() {
        return this.toolPalette;
    }

    showDebugHit() {
        this._titleRect.classed('highlight-statement', true);
    }

    clearDebugHit() {
        this._titleRect.classed('highlight-statement', false);
    }

    /**
     * Overrides the child remove callback from BallerinaStatementView.
     * @param child {ASTNode} removed child
     */
    childRemovedCallback(child) {
        if (BallerinaASTFactory.isStatement(child)) {
            this.getStatementContainer().childStatementRemovedCallback(child);
        }
    }

    /**
     * Set or get the onForcedWidthZoomed status
     * @param {boolean} flag
     * @return {boolean|undefined}
     */
    onForcedWidthChanged(flag) {
        if (!_.isNil(flag)) {
            this._onForcedWidthZoomed = flag;
        } else {
            return this._onForcedWidthZoomed;
        }
    }
}

/**
 * Returns the points for the title's poly line.
 * @param boundingBox {BBox} bounding box
 * @param titleViewOptions {{width: number, height: number}} title's view options
 * @return {string} pints of thr poly line
 */
function getTitlePolyLinePoints(boundingBox, titleViewOptions) {
    var x = boundingBox.x(), y = boundingBox.y();
    var titleWidth = titleViewOptions.width, titleHeight = titleViewOptions.height;
    var offset = 10;
    return '' + x + ',' + (y + titleHeight) + ' ' +
           (x + titleWidth) + ',' + (y + titleHeight) + ' ' +
           (x + titleWidth + offset) + ',' + y;
}

export default BlockStatementView;
