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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils', './point', './ballerina-view', './statement-view-factory', 'event_channel'],
    function (_, $, d3, log, D3Utils, Point, BallerinaView, StatementViewFactory, EventChannel) {

    /**
     * View to represent a collection of statements
     * @param args {object} - config
     * @param args.statements {ASTNode[]} - a collection of statement
     * @param args.container {SVGGElement} - SVG group element to draw the container
     * @param args.topCenter {Point} - top center point
     * @param args.bottomCenter {Point} - bottom center point
     * @param args.width {number} - width
     * @param args.height {number} - initial height
     * @param args.padding {number} - padding
     * @param args.cssClass {object} - css classes
     * @param args.cssClass.group {string} - css class for the group
     *
     * @class StatementContainerView
     * @augments BallerinaView
     * @constructor
     */
    var StatementContainerView = function (args) {
        BallerinaView.call(this, args);
        this._containerD3 = d3.select(this._container);
        this._viewOptions = args;
        this._managedStatements = [];
        this._managedInnerDropzones = [];
        this._lastStatementView = undefined;
        /**
         * View of the statement that has the highest width in this statement container.
         * @type {BallerinaStatementView}
         * @private
         */
        this._widestStatementView = undefined;
        this._selectedInnerDropZoneIndex = -1;

        _.set(this._viewOptions, 'cssClass.group',  _.get(this._viewOptions, 'cssClass.group', 'statement-container'));
        _.set(this._viewOptions, 'cssClass.mainDropZone',  _.get(this._viewOptions, 'cssClass.mainDropZone', 'main-drop-zone'));
        _.set(this._viewOptions, 'cssClass.mainDropZoneHover',  _.get(this._viewOptions, 'cssClass.mainDropZoneHover', 'main-drop-zone-hover'));
        _.set(this._viewOptions, 'cssClass.innerDropZone',  _.get(this._viewOptions, 'cssClass.innerDropZone', 'inner-drop-zone'));
        _.set(this._viewOptions, 'cssClass.innerDropZoneHover',  _.get(this._viewOptions, 'cssClass.innerDropZoneHover', 'inner-drop-zone-hover'));
        _.set(this._viewOptions, 'width',  _.get(this._viewOptions, 'width', 140));
        _.set(this._viewOptions, 'minWidth',  _.get(this._viewOptions, 'minWidth', 140)); // min width of the BBox
        _.set(this._viewOptions, 'height',  _.get(this._viewOptions, 'height', 260));
        _.set(this._viewOptions, 'minHeight',  _.get(this._viewOptions, 'minHeight', 260)); // min height of the BBox
        // left, right padding between an inner statement and container's BBox edges
        _.set(this._viewOptions, 'leftPadding',  _.get(this._viewOptions, 'leftPadding', 0));
        _.set(this._viewOptions, 'rightPadding',  _.get(this._viewOptions, 'rightPadding', 0));

        _.set(this._viewOptions, 'offset',  _.get(this._viewOptions, 'offset', {top: 100, bottom: 100}));
        _.set(this._viewOptions, 'gap',  _.get(this._viewOptions, 'gap', 10));

        this._topCenter =  _.get(this._viewOptions, 'topCenter').clone();
        this._width =  _.get(this._viewOptions, 'width');
        this._bottomCenter =  _.get(this._viewOptions, 'bottomCenter').clone();
        // this._gap =  _.get(this._viewOptions, 'gap', 30);
        this._gap = 30;
        this._offset = _.get(this._viewOptions, 'offset');
        this._rootGroup = D3Utils.group(this._containerD3)
            .classed(_.get(this._viewOptions, 'cssClass.group'), true);
        this._statementViewFactory = new StatementViewFactory();
        this.getBoundingBox().fromTopCenter(this._topCenter, this._width,
            this._bottomCenter.absDistInYFrom(this._topCenter));
        // a flag to indicate a whole container move - so that we can avoid resizing on container move
        this.isOnWholeContainerMove = false;
    };

    StatementContainerView.prototype = Object.create(BallerinaView.prototype);
    StatementContainerView.prototype.constructor = StatementContainerView;

    StatementContainerView.prototype.translate = function (x, y) {
        this._rootGroup.attr("transform", "translate(" + x + "," + y + ")");
    };

    /**
     * Render a given statement
     * @param statement {Statement}
     * @param args {Object}
     */
    StatementContainerView.prototype.renderStatement = function (statement, args) {
        var topCenter, statementView, newDropZoneTopCenter,  dropZoneOptions = {width: 120, height: this._gap};

        if (!_.isEmpty(this._managedStatements)) {
            // We have previously added statements, and adding a new one to them.
            var hasPendingInnerDropRender = _.gte(this._selectedInnerDropZoneIndex, 0);
            if(hasPendingInnerDropRender){
                var nextStatement = _.nth(this._managedStatements, this._selectedInnerDropZoneIndex),
                    nextStatementView = this.diagramRenderingContext.getViewOfModel(nextStatement),
                    topX = nextStatementView.getBoundingBox().getTopCenterX();
                    topY = nextStatementView.getBoundingBox().getTop();
                    topCenter = new Point(topX, topY);

                _.set(args, 'topCenter', topCenter);

                statementView = this._statementViewFactory.getStatementView(args);

                // move next statementView below
                var newY = statementView.getBoundingBox().getBottom() + this._gap;
                nextStatementView.getBoundingBox().y(newY);

                // if current index is not 0, meaning we have to do some logic to swap things to fit new top neighbour
                if(!_.isEqual(this._selectedInnerDropZoneIndex, 0)){
                    var prevStatement = _.nth(this._managedStatements, this._selectedInnerDropZoneIndex - 1),
                        prevStatementView = this.diagramRenderingContext.getViewOfModel(prevStatement);

                    // nextStatementView and the prevStatementView are still the neighbours - separate them.
                   prevStatementView.getBoundingBox().off("bottom-edge-moved");

                    // make new view listen to previous view
                    prevStatementView.getBoundingBox().on("bottom-edge-moved", function(offsetY){
                        statementView.getBoundingBox().move(0, offsetY);
                    });

                    // Re bind the broken event for the current drop zone
                    this._managedInnerDropzones[this._selectedInnerDropZoneIndex].listenTo(statementView.getBoundingBox(),
                        'bottom-edge-moved', this._managedInnerDropzones[this._selectedInnerDropZoneIndex].onLastStatementBottomEdgeMoved);
                } else{
                    statementView.listenTo(this.getBoundingBox(), 'top-edge-moved', function(dy){
                        statementView.getBoundingBox().y(statementView.getBoundingBox().y() + dy);
                    });
                    nextStatementView.stopListening(this.getBoundingBox(), 'top-edge-moved');
                }

                // make next view listen to new view
                statementView.getBoundingBox().on("bottom-edge-moved", function(offsetY){
                    nextStatementView.getBoundingBox().move(0, offsetY);
                });

                statementView.render(this.diagramRenderingContext);

                // insert at specific position
                this._managedStatements.splice(this._selectedInnerDropZoneIndex, 0, statement);


                // render a new innerDropZone on top of next statement block's top
                newDropZoneTopCenter = new Point(this.getBoundingBox().getTopCenterX(),
                    nextStatementView.getBoundingBox().y() - this._gap);
                _.set(dropZoneOptions, 'topCenter', newDropZoneTopCenter);
                _.set(dropZoneOptions, 'statementId', statementView._model.id);
                var innerDropZone = this._createNextInnerDropZone(dropZoneOptions, _.findIndex(this._managedStatements, ['id', nextStatement.id]));
                innerDropZone.listenTo(statementView.getBoundingBox(), 'bottom-edge-moved', innerDropZone.onLastStatementBottomEdgeMoved);

                // reset index of activatedInnerDropZone after handling inner drop
                this._selectedInnerDropZoneIndex = -1;
            } else {
                var lastStatement = _.last(this._managedStatements),
                    lastStatementView = this.diagramRenderingContext.getViewOfModel(lastStatement),
                    lastStatementViewBBox = lastStatementView.getBoundingBox(),
                    topX, topY;

                topX = lastStatementViewBBox.getTopCenterX();
                topY = lastStatementViewBBox.y() + lastStatementViewBBox.h() + this._gap;
                topCenter = new Point(topX, topY);
                _.set(args, 'topCenter', topCenter);
                statementView = this._statementViewFactory.getStatementView(args);
                this.setLastStatementView(statementView);
                statementView.render(this.diagramRenderingContext);
                this._managedStatements.push(statement);

                // make new view listen to previous view
                lastStatementView.getBoundingBox().on("bottom-edge-moved", function(offsetY){
                    statementView.getBoundingBox().move(0, offsetY);
                });

                newDropZoneTopCenter = new Point(statementView.getBoundingBox().getTopCenterX(),
                                statementView.getBoundingBox().y() - this._gap);

                _.set(dropZoneOptions, 'topCenter', newDropZoneTopCenter);
                _.set(dropZoneOptions, 'statementId', statementView._model.id);
                var innerDropZone = this._createNextInnerDropZone(dropZoneOptions);
                innerDropZone.listenTo(lastStatementView.getBoundingBox(), 'bottom-edge-moved', innerDropZone.onLastStatementBottomEdgeMoved);
            }

            // Newly created 'statementView' might be the widest, let's find out.
            if (this._widestStatementView.getBoundingBox().w() < statementView.getBoundingBox().w()) {
                this._widestStatementView = statementView;
                this._updateContainerWidth(statementView.getBoundingBox().w());
            }
        } else {
            // We are adding the very first statement into this container.
            topCenter = this._topCenter.clone().move(0, this._offset.top);
            _.set(args, 'topCenter', topCenter);
            statementView = this._statementViewFactory.getStatementView(args);
            this.setLastStatementView(statementView);
            statementView.render(this.diagramRenderingContext);
            this._managedStatements.push(statement);

            // this is the fist statement - create dropzone on top
            newDropZoneTopCenter = topCenter.clone().move(0, - this._gap);
            _.set(dropZoneOptions, 'topCenter', newDropZoneTopCenter);
            _.set(dropZoneOptions, 'statementId', statementView._model.id);
            var innerDropZone = this._createNextInnerDropZone(dropZoneOptions);
            if(this.getBoundingBox().getBottom() < statementView.getBoundingBox().getBottom()){
                this.getBoundingBox().h(statementView.getBoundingBox().h() +
                    statementView.getBoundingBox().getBottom() - this.getBoundingBox().getBottom() +
                    _.get(this._viewOptions, 'offset.bottom')
                );
            }
            // first inner drop zone should not move for resource default worker
            if(this._model.getFactory().isStatement(this._model)){
                innerDropZone.listenTo(this._parent.getBoundingBox(),
                    'top-edge-moved', innerDropZone.onLastStatementBottomEdgeMoved);
            }
            statementView.listenTo(this.getBoundingBox(), 'top-edge-moved', function(dy){
                statementView.getBoundingBox().y(statementView.getBoundingBox().y() + dy);
            });

            // Since we have only one statement view, it is the widest statement view.
            this._widestStatementView = statementView;
            this._updateContainerWidth(statementView.getBoundingBox().w());
        }

        statementView.getBoundingBox().on('width-changed', function (widthChange) {
            if (widthChange < 0) {
                // Width of the bounding box of 'statementView' has been decreased.
                if (statementView === this._widestStatementView) {
                    /* 'statementView' was the widest one. Since its width has been decreased, we need to compute the
                     new widest statement view.*/
                    var widestStatement = _.maxBy(this._managedStatements, function (statement) {
                        return this.diagramRenderingContext.getViewOfModel(statement).getBoundingBox().w();
                    }.bind(this));
                    this._widestStatementView = this.diagramRenderingContext.getViewOfModel(widestStatement);
                    this._updateContainerWidth(this._widestStatementView.getBoundingBox().w());
                } else {
                    // 'statementView' wasn't the widest one. As its width has been decreased, it isn't the widest now.
                }
            } else if (0 < widthChange) {
                // Width of the bounding box of 'statementView' has been increased.
                if (statementView === this._widestStatementView) {
                    // 'statementView' was the widest one, and still is.
                    this._updateContainerWidth(this._widestStatementView.getBoundingBox().w());
                } else {
                    // 'statementView' wasn't the widest one before.
                    var newWidth = statementView.getBoundingBox().w();
                    if (this._widestStatementView.getBoundingBox().w() < newWidth) {
                        // 'statementView' has become the widest one.
                        this._widestStatementView = statementView;
                        this._updateContainerWidth(newWidth);
                    } else {
                        // Even though 'statementView' has been expanded, it still not the widest.
                    }
                }
            } else {
                // widthChange === 0;
            }

        }, this);
        this.diagramRenderingContext.setViewOfModel(statement, statementView);

        return statementView;
    };

    StatementContainerView.prototype._updateContainerWidth = function (newWidth) {
        var viewOptions = this._viewOptions;
        this.getBoundingBox().zoomWidth(viewOptions.leftPadding + Math.max(newWidth, viewOptions.minWidth) +
                                        viewOptions.rightPadding);
    };

    StatementContainerView.prototype.setLastStatementView = function(lastStatementView){
        if(!_.isNil(this._lastStatementView)){
            this._lastStatementView.getBoundingBox().off('bottom-edge-moved');
        }
        this._lastStatementView = lastStatementView;

        if(this.getBoundingBox().getBottom() <
            (this._lastStatementView.getBoundingBox().getBottom() + _.get(this._viewOptions, 'offset.bottom'))){
            this.getBoundingBox().h((this._lastStatementView.getBoundingBox().getBottom() - this.getBoundingBox().getTop()) +
                _.get(this._viewOptions, 'offset.bottom'));
        }

        this._lastStatementView.getBoundingBox().on('bottom-edge-moved', function(dy){
            var newHeight = this.getBoundingBox().h() + dy;
                if(!this.isOnWholeContainerMove){
                    // If the new height is smaller than the minimum height we set the height of the statement container to the minimum allowed
                    if (newHeight > this._viewOptions.minHeight) {
                        this.getBoundingBox().h(newHeight);
                    } else {
                        this.getBoundingBox().h(this._viewOptions.minHeight);
                    }
                }
                this.isOnWholeContainerMove = false;
        }, this);
    };

    StatementContainerView.prototype.render = function (diagramRenderingContext) {
        this.diagramRenderingContext = diagramRenderingContext;
        var self = this;
        this._mainDropZone = D3Utils.rect(this._topCenter.x() - this._width/2, this._topCenter.y(), this._width,
                this._bottomCenter.absDistInYFrom(this._topCenter), 0, 0, this._rootGroup)
                .classed( _.get(this._viewOptions, 'cssClass.mainDropZone'), true);

        // adjust drop zone height on bottom edge moved
        var boundingBox = this.getBoundingBox();
        this.listenTo(boundingBox, 'height-changed', function (offset) {
            self._mainDropZone.attr('height', parseFloat(self._mainDropZone.attr('height')) + offset);
        });
        boundingBox.on('width-changed', function(offset){
            self._mainDropZone.attr('width', boundingBox.w());
        });

        // adjust drop zone height on bottom edge moved
        boundingBox.on('moved', function(offset){
            self._mainDropZone.attr('x', boundingBox.x());
            self._mainDropZone.attr('y', boundingBox.y());
            self._topCenter.move(offset.dx, offset.dy);
        }, this);
        var dropZoneOptions = {
            dropZone: this._mainDropZone,
            hoverClass: _.get(this._viewOptions, 'cssClass.mainDropZoneHover')
        };
        this.initDropZone(dropZoneOptions);
        return this;
    };

    StatementContainerView.prototype._createNextInnerDropZone = function(options, index){
        var innerDZone = D3Utils.rect(options.topCenter.x() - options.width/2,
            options.topCenter.y(), options.width,
            options.height, 0, 0, this._rootGroup)
            .classed( _.get(this._viewOptions, 'cssClass.innerDropZone'), true).attr('id',options.statementId);
        var dropZoneOptions = {
            dropZone: innerDZone,
            hoverClass: _.get(this._viewOptions, 'cssClass.innerDropZoneHover')
        };
        this.initDropZone(dropZoneOptions);

        // wrap dropzone using an event channel object to use eventing on top of it
        var innerDropZoneObj = new EventChannel();
        innerDropZoneObj.d3el = innerDZone;

        innerDropZoneObj.onLastStatementBottomEdgeMoved = function(dy){
            this.d3el.attr('y', parseFloat(this.d3el.attr('y')) + dy);
        };

        if(!_.isNil(index)){
            this._managedInnerDropzones.splice(index, 0, innerDropZoneObj)
        } else {
            this._managedInnerDropzones.push(innerDropZoneObj);
        }
        return innerDropZoneObj;
    };

    StatementContainerView.prototype.initDropZone = function (options) {
        var dropZone = _.get(options, 'dropZone'),
            hoverClass = _.get(options, 'hoverClass'),
            self = this;

        var getDroppedNodeIndex = function(node){
           if(!_.gte(self._selectedInnerDropZoneIndex, 0)){
               return -1;
           }
           var neighbourStatement = _.nth(self._managedStatements, self._selectedInnerDropZoneIndex),
               newNodeIndex = self._model.getIndexOfChild(neighbourStatement);
           log.debug("Index for the dropped node is " + newNodeIndex);
           return newNodeIndex;
        };
        var mouseOverHandler = function() {
            //if someone is dragging a tool from tool-palette
            if(self.toolPalette.dragDropManager.isOnDrag()){
                self._selectedInnerDropZoneIndex = _.findIndex(self._managedInnerDropzones, ['d3el', d3.select(this)]);

                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)){
                    return;
                }

                // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                // tool view will use this to provide feedback on impossible drop zones
                self.toolPalette.dragDropManager.setActivatedDropTarget(self._model, function(nodeBeingDragged){
                    var nodeFactory = self._model.getFactory();
                    // IMPORTANT: override resource definition node's default validation logic
                    // This drop zone is for statements only.
                    // Statements should only be allowed here.
                    return nodeFactory.isStatement(nodeBeingDragged);
                }, getDroppedNodeIndex);

                // indicate drop area
                dropZone.classed(hoverClass, true);

                // reset ui feed back on drop target change
                self.toolPalette.dragDropManager.once("drop-target-changed", function(){
                    dropZone.classed(hoverClass, false);
                });
            }
            d3.event.stopPropagation();
        };

        var mouseOutHandler = function() {
            // reset ui feed back on hover out
            if(self.toolPalette.dragDropManager.isOnDrag()){
                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self._model)){
                    dropZone.classed(hoverClass, false);
                }
            }
            d3.event.stopPropagation();
        };
        dropZone.on("mouseover", mouseOverHandler);
        dropZone.on("mouseout", mouseOutHandler);
    };

    /**
     * Callback function for statement removing
     * @param {ASTNode} childStatement - removed child statement
     */
    StatementContainerView.prototype.childStatementRemovedCallback = function (childStatement) {
        var childStatementView = this.diagramRenderingContext.getViewModelMap()[childStatement.id];
        var childStatementIndex = _.findIndex(this._managedStatements, function (child) {
            return child.id === childStatement.id;
        });
        var previousStatementView = childStatementIndex === 0 ?
            undefined : this.diagramRenderingContext.getViewOfModel(this._managedStatements[childStatementIndex - 1]);
        var nextStatementView = childStatementIndex === this._managedStatements.length - 1 ?
            undefined : this.diagramRenderingContext.getViewOfModel(_.nth(this._managedStatements, childStatementIndex + 1));

        childStatementView.getBoundingBox().off('bottom-edge-moved');
        if (!_.isNil(nextStatementView)) {
            if (!_.isNil(previousStatementView)) {
                var nextDropZone = this._managedInnerDropzones[childStatementIndex].d3el;
                var nextDropZoneOffset = -(this.getDiagramRenderingContext().getViewOfModel(childStatement).getBoundingBox().h()) - 30;
                previousStatementView.getBoundingBox().on('bottom-edge-moved', function (offsetY) {
                    nextStatementView.getBoundingBox().move(0, offsetY);
                });
                nextDropZone.attr('y', parseFloat(nextDropZone.attr('y')) + nextDropZoneOffset);
            } else {
                // If the previous element is null then we have deleted the first element
                // then the next element becomes the top/ first element

                // Stop listening to the top-edge-moved by the deleted element
                childStatementView.stopListening(this.getBoundingBox(), 'top-edge-moved');

                // Use the listenTo since the event has to be manipulated by this view's self
                nextStatementView.listenTo(this.getBoundingBox(), 'top-edge-moved', function(dy){
                    nextStatementView.getBoundingBox().y(nextStatementView.getBoundingBox().y() + dy);
                });
            }
        } else if (_.isNil(nextStatementView) && !_.isNil(previousStatementView)) {
            // This means we have deleted the last statement and it is not the only element.
            // We have to reset the last statement
            this.setLastStatementView(previousStatementView)
        }

        this._managedStatements.splice(childStatementIndex, 1);
    };

    /**
     * Remove the inner drop zone
     * @param {ASTNode} child - child node
     */
    StatementContainerView.prototype.removeInnerDropZone = function (child) {
        var childStatementIndex = _.findIndex(this._managedStatements, function(childStatement) {
            return childStatement.id === child.id;
        });
        var innerDropZone = undefined;
        var removeIndex = -1;
        // If this is the only element
        if (this._managedStatements.length === 1 && this._managedInnerDropzones.length === 1) {
            removeIndex = 0;
        } else if ((this._managedStatements.length - 1 === childStatementIndex) || childStatementIndex === 0) {
            // If this is the first or the last element
            removeIndex = childStatementIndex;
        } else {
            removeIndex = childStatementIndex;
            var previousView = this.diagramRenderingContext.getViewOfModel(this._managedStatements[removeIndex - 1]);
            var viewToDelete = this.diagramRenderingContext.getViewOfModel(this._managedStatements[removeIndex]);
            this._managedInnerDropzones[removeIndex + 1].stopListening(viewToDelete.getBoundingBox());
            this._managedInnerDropzones[removeIndex + 1].listenTo(previousView.getBoundingBox(), 'bottom-edge-moved',
                this._managedInnerDropzones[removeIndex + 1].onLastStatementBottomEdgeMoved);
        }

        if (removeIndex > -1) {
            innerDropZone = this._managedInnerDropzones[removeIndex];
            innerDropZone.d3el.node().remove();
            this._managedInnerDropzones.splice(removeIndex, 1);
        }
    };

        /**
         * Get the managed statements array
         * @return {Array}
         */
    StatementContainerView.prototype.getManagedStatements = function () {
        return this._managedStatements;
    };

    StatementContainerView.prototype.changeDropZoneHeight = function (dh) {
        this._mainDropZone.attr('height', parseFloat(this._mainDropZone.attr('height')) + dh);
    };

    StatementContainerView.prototype.changeHeightSilent = function (h) {
        this.getBoundingBox().h(h, true);
        this._mainDropZone.attr('height', h);
    };

    return StatementContainerView;
});
