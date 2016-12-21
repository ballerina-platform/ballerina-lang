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
        this._selectedInnerDropZoneIndex = -1;

        _.set(this._viewOptions, 'cssClass.group',  _.get(this._viewOptions, 'cssClass.group', 'statement-container'));
        _.set(this._viewOptions, 'cssClass.mainDropZone',  _.get(this._viewOptions, 'cssClass.mainDropZone', 'main-drop-zone'));
        _.set(this._viewOptions, 'cssClass.mainDropZoneHover',  _.get(this._viewOptions, 'cssClass.mainDropZoneHover', 'main-drop-zone-hover'));
        _.set(this._viewOptions, 'cssClass.innerDropZone',  _.get(this._viewOptions, 'cssClass.innerDropZone', 'inner-drop-zone'));
        _.set(this._viewOptions, 'cssClass.innerDropZoneHover',  _.get(this._viewOptions, 'cssClass.innerDropZoneHover', 'inner-drop-zone-hover'));
        _.set(this._viewOptions, 'width',  _.get(this._viewOptions, 'width', 120));
        _.set(this._viewOptions, 'height',  _.get(this._viewOptions, 'height', 260));

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
                    nextStatementView.stopListening(prevStatementView.getBoundingBox(), "bottom-edge-moved");

                    // make new view listen to previous view
                    statementView.listenTo(prevStatementView.getBoundingBox(), "bottom-edge-moved", function(offsetY){
                        statementView.getBoundingBox().move(0, offsetY);
                    });
                }

                // make next view listen to new view
                nextStatementView.listenTo(statementView.getBoundingBox(), "bottom-edge-moved", function(offsetY){
                    nextStatementView.getBoundingBox().move(0, offsetY);
                });

                statementView.render(this.diagramRenderingContext);

                // insert at specific position
                this._managedStatements.splice(this._selectedInnerDropZoneIndex, 0, statement);


                // render a new innerDropZone on top of next statement block's top
                newDropZoneTopCenter = new Point(nextStatementView.getBoundingBox().getTopCenterX(),
                    nextStatementView.getBoundingBox().y() - this._gap);
                _.set(dropZoneOptions, 'topCenter', newDropZoneTopCenter);
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
                this._managedStatements.push(statement);
                this.setLastStatementView(statementView);
                statementView.render(this.diagramRenderingContext);

                // make new view listen to previous view
                statementView.listenTo(lastStatementView.getBoundingBox(), "bottom-edge-moved", function(offsetY){
                    statementView.getBoundingBox().move(0, offsetY);
                });

                newDropZoneTopCenter = new Point(statementView.getBoundingBox().getTopCenterX(),
                                statementView.getBoundingBox().y() - this._gap);

                _.set(dropZoneOptions, 'topCenter', newDropZoneTopCenter);
                var innerDropZone = this._createNextInnerDropZone(dropZoneOptions);
                innerDropZone.listenTo(lastStatementView.getBoundingBox(), 'bottom-edge-moved', innerDropZone.onLastStatementBottomEdgeMoved);
            }

        } else {
            topCenter = this._topCenter.clone().move(0, this._offset.top);
            _.set(args, 'topCenter', topCenter);
            statementView = this._statementViewFactory.getStatementView(args);
            this._managedStatements.push(statement);
            this.setLastStatementView(statementView);
            statementView.render(this.diagramRenderingContext);

            // this is the fist statement - create dropzone on top
            newDropZoneTopCenter = topCenter.clone().move(0, - this._gap);
            _.set(dropZoneOptions, 'topCenter', newDropZoneTopCenter);
            this._createNextInnerDropZone(dropZoneOptions);
            if(this.getBoundingBox().getBottom() < statementView.getBoundingBox().getBottom()){
                this.getBoundingBox().h(this.getBoundingBox().h() + statementView.getBoundingBox().getBottom() +
                    _.get(this._viewOptions, 'offset.bottom')
                );
            }
        }


        if(this.getBoundingBox().w() < statementView.getBoundingBox().w()){
            this.getBoundingBox().w(statementView.getBoundingBox().w());
        }
        this.diagramRenderingContext.setViewOfModel(statement, statementView);

        return statementView;
    };

    StatementContainerView.prototype.setLastStatementView = function(lastStatementView){
            if(!_.isNil(this._lastStatementView)){
                this.stopListening(this._lastStatementView.getBoundingBox(), 'bottom-edge-moved');
            }
            this._lastStatementView = lastStatementView;

            this.getBoundingBox().h((this._lastStatementView.getBoundingBox().getBottom() - this.getBoundingBox().getTop()) +
                    _.get(this._viewOptions, 'offset.bottom'));

            this.listenTo(this._lastStatementView.getBoundingBox(), 'bottom-edge-moved', function(dy){
                    this.getBoundingBox().h(this.getBoundingBox().h() + dy);
            })
    };

    StatementContainerView.prototype.render = function (diagramRenderingContext) {
        this.diagramRenderingContext = diagramRenderingContext;
        var self = this;
        this._mainDropZone = D3Utils.rect(this._topCenter.x() - this._width/2, this._topCenter.y(), this._width,
                this._bottomCenter.absDistInYFrom(this._topCenter), 0, 0, this._rootGroup)
                .classed( _.get(this._viewOptions, 'cssClass.mainDropZone'), true);

        // adjust drop zone height on bottom edge moved
        this.getBoundingBox().on('bottom-edge-moved', function(offset){
            self._mainDropZone.attr('height', parseFloat(self._mainDropZone.attr('height')) + offset);
        });
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
            .classed( _.get(this._viewOptions, 'cssClass.innerDropZone'), true);
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

    return StatementContainerView;
});
