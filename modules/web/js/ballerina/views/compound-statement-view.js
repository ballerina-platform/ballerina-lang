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
import Point from './point';
import StatementViewFactory from './statement-view-factory';

/**
 * Super view class for all compound statements e.g. if-else, try-catch etc.
 * @class CompoundStatementView
 * @extends BallerinaStatementView
 */
class CompoundStatementView extends BallerinaStatementView {
    /**
     * @param args {*} arguments for the creating view
     * @constructor
     */
    constructor(args) {
        super(args);

        /**
         * Real width of the child statements of this compound statement.
         * @type {number[]}
         * @private
         */
        this._childrenViewsActualWidths = [];

        var viewOptions = this.getViewOptions();
        viewOptions.width = _.get(args, 'viewOptions.width', 120);
        viewOptions.height = _.get(args, 'viewOptions.height', 0);
        this._childStatementDefaultWidth = 120;
        this._onWholeContainerMove = false;

        this.getBoundingBox().fromTopCenter(this.getTopCenter(), viewOptions.width, viewOptions.height);
    }

    render(renderingContext) {
        this.setDiagramRenderingContext(renderingContext);
        var bBox = this.getBoundingBox();
        var model = this.getModel();
        var parentStatementContainerBBox = this.getParent().getStatementContainer().getBoundingBox();

        // Creating statement group.
        var statementGroup = D3Utils.group(d3.select(this.getContainer()));
        // "id" is prepend with a "_" to be compatible with HTML4
        statementGroup.attr('id', '_' + model.id);
        this.setStatementGroup(statementGroup);

        this.listenTo(bBox, 'width-changed', function () {
            if (parentStatementContainerBBox.w() > bBox.w()) {
                bBox.x(parentStatementContainerBBox.x() + (parentStatementContainerBBox.w() - bBox.w())/2);
            }
        });

        this.getDiagramRenderingContext().setViewOfModel(this.getModel(), this);

        model.accept(this);
    }

    /**
     * @param childStatement
     * @return {BlockStatementView}
     */
    visitChildStatement(childStatement) {
        var boundingBox = this.getBoundingBox();
        var topCenter = this.getTopCenter();
        var renderingContext = this.getDiagramRenderingContext();
        /** @type {BlockStatementView[]} */
        var childStatementViews = this.getChildrenViewsList();
        var childrenViewsActualWidths = this._childrenViewsActualWidths;
        var self = this;

        // Creating child statement view.
        var childStatementViewTopCenter;
        if (_.isEmpty(childStatementViews)) {
            childStatementViewTopCenter = new Point(topCenter.x(), topCenter.y());
        } else {
            childStatementViewTopCenter =
                new Point(topCenter.x(), _.last(childStatementViews).getBoundingBox().getBottom());
        }
        var childStatementViewArgs = {
            model: childStatement,
            container: this.getStatementGroup().node(),
            viewOptions: {'width' : boundingBox.w()},
            parent: this,
            topCenter: childStatementViewTopCenter,
            messageManager: this.messageManager,
            toolPalette: this.getToolPalette(),
            isChildOfWorker: childStatement.parent._isChildOfWorker
        };
        var statementViewFactory = new StatementViewFactory();
        /** @type {BlockStatementView} */
        var childStatementView = statementViewFactory.getStatementView(childStatementViewArgs);

        childStatementView.listenTo(boundingBox, 'left-edge-moved', function () {
            childStatementView.getBoundingBox().x(boundingBox.x());
        });

        // If there are previously added child statements, then get the last one.
        var lastChildStatementView = _.last(childStatementViews);
        if (!_.isUndefined(lastChildStatementView)) {
            // When the last child statement's height change, adding child statement should move accordingly.
            childStatementView.listenTo(lastChildStatementView.getBoundingBox(), 'bottom-edge-moved', function (offset) {
                if (!self._onWholeContainerMove) {
                    childStatementView.getBoundingBox().move(0, offset);
                }
            });
        }

        this.listenTo(childStatementView.getBoundingBox(), 'width-changed', function () {
            if (!childStatementView.onForcedWidthChanged()) {
                var widestChildOfChildBlock = self.getWidestChildOfChildBlock();
                var newWidth = !_.isNil(widestChildOfChildBlock) ? widestChildOfChildBlock.getBoundingBox().w() +
                    childStatementView.getStatementContainer().getWidthGap() : self._childStatementDefaultWidth;
                if (newWidth !== self.getBoundingBox().w()) {
                    _.forEach(childStatementViews, function (childView) {
                        childView.onForcedWidthChanged(true);
                        childView.getStatementContainer().getBoundingBox().w(newWidth);
                    });
                    self.getBoundingBox().w(newWidth);
                } else {
                    childStatementView.onForcedWidthChanged(true);
                    childStatementView.getStatementContainer().getBoundingBox().w(newWidth);
                }
            } else {
                childStatementView.onForcedWidthChanged(false);
            }
        });

        childStatementViews.push(childStatementView);
        childStatementView.render(renderingContext);

        // Height changed event is positioned after the rendering. This is because when we open a file then the
        // height change events are fired for the statements inside the second block statement, which will change the
        // height of the compound statement
        this.listenTo(childStatementView.getBoundingBox(), 'height-changed', function (dh) {
            boundingBox.h(boundingBox.h() + dh);
        });

        childrenViewsActualWidths.push(childStatementView.getBoundingBox().w());

        if (childStatementViews.length === 1) {
            // This is the very first child statement.
            boundingBox.h(childStatementView.getBoundingBox().h());
        } else {
            boundingBox.h(boundingBox.h() + childStatementView.getBoundingBox().h());
        }

        childStatementView.listenTo(boundingBox, 'moved', function (offset) {
            self._onWholeContainerMove = true;
            childStatementView.getBoundingBox().move(offset.dx, offset.dy);
            self._onWholeContainerMove = false;
        });

        return childStatementView;
    }

    setModel(model) {
        if (_.isNil(model)) {
            var message = 'Model of a compound statement cannot be null.';
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
            var message = 'Container of a compound statement cannot be null.';
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

    removeAllChildStatements() {
        _.forEach(this.getChildrenViewsList(), function (childStatementView) {
            childStatementView.stopListening();
        });
        this.getStatementGroup().node().remove();
        // resize the bounding box in order to the other objects to resize
        var gap = this.getParent().getStatementContainer().getInnerDropZoneHeight();
        this.getBoundingBox().move(0, -this.getBoundingBox().h() - gap).w(0);
    }

    showDebugHit() {
        this.getChildrenViewsList()[0]._titleRect.classed('highlight-statement', true);
    }

    clearDebugHit() {
        this.getChildrenViewsList()[0]._titleRect.classed('highlight-statement', false);
    }

    /**
     * Get the widest child of a child block
     * @return {BallerinaStatementView}
     */
    getWidestChildOfChildBlock() {
        var childrenViews = this.getChildrenViewsList();
        var blockStatementWidestChildren = [];
        _.forEach(childrenViews, function (child) {
            blockStatementWidestChildren.push(child.getStatementContainer().getWidestStatementView());
        });
        var sortedChildren = _.sortBy(blockStatementWidestChildren, function (child) {
            if (_.isNil(child)) {
                return -1;
            } else {
                return child.getBoundingBox().w();
            }
        });

        return _.last(sortedChildren);
    }
}

export default CompoundStatementView;
