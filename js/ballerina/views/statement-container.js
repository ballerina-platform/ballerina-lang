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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils', './point', './ballerina-view', './statement-view-factory'],
    function (_, $, d3, log, D3Utils, Point, BallerinaView, StatementViewFactory) {

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

        _.set(this._viewOptions, 'cssClass.group',  _.get(this._viewOptions, 'cssClass.group', 'statement-container'));
        _.set(this._viewOptions, 'cssClass.mainDropZone',  _.get(this._viewOptions, 'cssClass.mainDropZone', 'main-drop-zone'));
        _.set(this._viewOptions, 'cssClass.mainDropZoneHover',  _.get(this._viewOptions, 'cssClass.mainDropZoneHover', 'main-drop-zone-hover'));
        _.set(this._viewOptions, 'width',  _.get(this._viewOptions, 'width', 120));
        _.set(this._viewOptions, 'height',  _.get(this._viewOptions, 'height', 260));

        _.set(this._viewOptions, 'offset',  _.get(this._viewOptions, 'offset', {top: 200, bottom: 25 }));
        _.set(this._viewOptions, 'gap',  _.get(this._viewOptions, 'gap', 15));

        this._topCenter =  _.get(this._viewOptions, 'topCenter').clone();
        this._width =  _.get(this._viewOptions, 'width');
        this._bottomCenter =  _.get(this._viewOptions, 'bottomCenter').clone();
        this._gap =  _.get(this._viewOptions, 'gap');
        this._offset = _.get(this._viewOptions, 'offset');
        this._rootGroup = D3Utils.group(this._containerD3)
            .classed(_.get(this._viewOptions, 'cssClass.group'), true);
        this._statementViewFactory = new StatementViewFactory();
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
        var topCenter, lastStatementView;
        if (!_.isEmpty(this._managedStatements)) {
            var lastStatement = _.last(this._managedStatements),
                lastStatementView = this.diagramRenderingContext.getViewOfModel(lastStatement),
                lastStatementViewBBox = lastStatementView.getBoundingBox(),
                x = lastStatementViewBBox.x(),
                y = lastStatementViewBBox.y() + lastStatementViewBBox.h() + this._gap;
                topCenter = new Point(x, y);
        } else {
            topCenter = this._topCenter.clone().move(0, this._offset.top);
        }
        _.set(args, 'topCenter', topCenter);

        var statementView = this._statementViewFactory.getStatementView(args);
        this.setLastStatementView(statementView);
        statementView.render(this.diagramRenderingContext);

        this.diagramRenderingContext.setViewOfModel(statement, statementView);

        this._managedStatements.push(statement);
        this._createNextInnerDropZone();
        return statementView;
    };

    StatementContainerView.prototype.setLastStatementView = function(lastStatementView){
            if(!_.isNil(this._lastStatementView)){
                this.stopListening(this._lastStatementView, 'bottom-edge-moved');
            }
            this._lastStatementView = lastStatementView;
            this.getBoundingBox().y(lastStatementView.getBoundingBox().y() +  _.get(this._viewOptions, 'offset.bottom'));
            this.listenTo(lastStatementView.getBoundingBox(), 'bottom-edge-moved', function(dy){
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
            self._mainDropZone.attr('height', self._mainDropZone.attr('height') + offset);
        });
        this.initMainDropZone();
        return this;
    };

    StatementContainerView.prototype._createNextInnerDropZone = function(){

    };

    StatementContainerView.prototype.initMainDropZone = function () {
        var self = this,
            hoverClass = _.get(this._viewOptions, 'cssClass.mainDropZoneHover');

        var getDroppedNodeIndex = function(node){
            var managedIndex = _.findIndex(self._managedStatements, ['id', node.id]);
            if(managedIndex > 0){
                var previousStatement = _.nth(self._managedStatements, managedIndex - 1);
                return self._model.getIndexOfChild(previousStatement);
            } else if(managedIndex == 0){
                return 0;
            } else {
                return -1;
            }
        };
        var mouseOverHandler = function() {
            //if someone is dragging a tool from tool-palette
            if(self.toolPalette.dragDropManager.isOnDrag()){

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
                self._mainDropZone.classed(hoverClass, true);

                // reset ui feed back on drop target change
                self.toolPalette.dragDropManager.once("drop-target-changed", function(){
                    self._mainDropZone.classed(hoverClass, false);
                });
            }
            d3.event.stopPropagation();

        };

        var mouseOutHandler = function() {
            // reset ui feed back on hover out
            if(self.toolPalette.dragDropManager.isOnDrag()){
                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self._model)){
                    self._mainDropZone.classed(hoverClass, false);
                }
            }
            d3.event.stopPropagation();

        };
        this._mainDropZone.on("mouseover", mouseOverHandler);
        this._mainDropZone.on("mouseout", mouseOutHandler);
    };

    return StatementContainerView;
});