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

define(['lodash','d3', 'jquery', './ballerina-view', './../ast/connector-declaration', 'log', 'd3utils','./life-line'],
    function (_, d3, $, BallerinaView, ConnectorDeclaration, log, D3utils, LifeLine) {

        /**
         * The view to represent a connection declaration which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ConnectionDeclaration} args.model - The connection declaration model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ConnectorDeclarationView = function (args) {
            this._totalHeightGap = 50;
            this._parentView = _.get(args, "parentView");
            this.messageManager =  _.get(args, "messageManager");
            this._LifeLineCenterGap = 180;
            // At the moment we consider by default the connector is HTTP
            _.set(args, 'title',  _.get(args, 'model').getConnectorVariable() || "HTTP");
            _.set(args, 'cssClass.group',  _.get(args, 'cssClass.group', 'connector-life-line'));
            _.set(args, 'line.height',  _.get(args, 'lineHeight', 290));
            LifeLine.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ConnectorDeclaration)) {
                log.error("Connection declaration is undefined or is of different type." + this._model);
                throw "Connection declaration is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for connection declaration is undefined." + this._container);
                throw "Container for connection declaration is undefined." + this._container;
            }

            this.init();
        };

        ConnectorDeclarationView.prototype = Object.create(LifeLine.prototype);
        ConnectorDeclarationView.prototype.constructor = ConnectorDeclaration;

        ConnectorDeclarationView.prototype.init = function () {
            this._model.on('before-remove', this.onBeforeModelRemove, this);
            this.listenTo(this._parentView, 'resourceHeightChangedEvent', this.resourceHeightChangedCallback);
        };

        /**
         * Callback function for resource's height changed event
         * @param height
         */
        ConnectorDeclarationView.prototype.resourceHeightChangedCallback = function (height) {
            this.setHeight(height - this._totalHeightGap);
        };

        ConnectorDeclarationView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ConnectorDeclaration) {
                this._model = model;
            } else {
                log.error("Connection declaration is undefined or is of different type." + model);
                throw "Connection declaration is undefined or is of different type." + model;
            }
        };

        ConnectorDeclarationView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for connection declaration is undefined." + container);
                throw "Container for connection declaration is undefined." + container;
            }
        };

        ConnectorDeclarationView.prototype.getModel = function () {
            return this._model;
        };

        ConnectorDeclarationView.prototype.getContainer = function () {
            return this._container;
        };

        ConnectorDeclarationView.prototype.initDropZone = function (options) {
            var dropZone = _.get(options, 'dropZone'),
                hoverClass = _.get(options, 'hoverClass'),
                self = this;

            var getDroppedNodeIndex = function(node){
                if(!_.gte(self._selectedInnerDropZoneIndex, 0)){
                    return -1;
                }
                var neighbourStatement = _.nth(self._managedStatements, self._selectedInnerDropZoneIndex),
                    newNodeIndex = self._model.getIndexOfChild(neighbourStatement) + 1;
                log.debug("Index for the dropped node is " + newNodeIndex);
                return newNodeIndex;
            };
            var mouseOverHandler = function() {
                //if someone is dragging a tool from tool-palette
                if(self.messageManager.isOnDrag()){
                    self._selectedInnerDropZoneIndex = _.findIndex(self._managedInnerDropzones, d3.select(this));

                    // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                    // tool view will use this to provide feedback on impossible drop zones
                    self.messageManager.setActivatedDropTarget(self._model, function(nodeBeingDragged){
                        var nodeFactory = self._model.getFactory();
                        // IMPORTANT: override resource definition node's default validation logic
                        // This drop zone is for statements only.
                        // Statements should only be allowed here.
                        //return nodeFactory.isStatement(nodeBeingDragged);
                        return true;
                    });

                    self.setStylesWithOpacity(self._middleRectangle);

                    // reset ui feed back on drop target change
                    self.messageManager.once("drop-target-changed", function(){
                        self.setStyles(self._middleRectangle);
                    });
                }
            };

            var mouseOutHandler = function() {
                // reset ui feed back on hover out
                if(self.messageManager.isOnDrag()){
                    if(_.isEqual(self.messageManager.getActivatedDropTarget(), self._model)){
                        self.setStyles(self._middleRectangle);
                    }
                }
                d3.event.stopPropagation();
            };
            dropZone.on("mouseover", mouseOverHandler);
            dropZone.on("mouseout", mouseOutHandler);
        };

        ConnectorDeclarationView.prototype.renderMiddleRectangle = function () {
            var self = this;
            this._middleRectangle = D3utils.centeredRect(this.getMiddleLineCenter(),
                this._viewOptions.rect.width, this.getMiddleLineHeight(), 0, 0, this._rootGroup);

            this.setStyles(this._middleRectangle);

            // adjust drop zone height change
            this.getBoundingBox().on('height-changed', function(offset){
                var newH = parseFloat(self._middleRectangle.attr('height')) + offset;
                self._middleRectangle.attr('height', newH > 0 ? newH : 0);
                self.getBottomCenter().move(0, offset);
            });
            this.getBoundingBox().on('right-edge-moved', function(offset){
                var currentX = parseInt(self._middleRectangle.attr('x'));
                var currentY = parseInt(self._middleRectangle.attr('y'));
                self._middleRectangle.attr('x', currentX + offset)
            });
            var dropZoneOptions = {
                dropZone: this._middleRectangle,
                //hoverClass: _.get(this._viewOptions, 'cssClass.mainDropZoneHover'),
                hoverClass: 'main-drop-zone-hover'
            };
            this.initDropZone(dropZoneOptions);

        };

        ConnectorDeclarationView.prototype.setStyles = function (d3Element) {
            d3Element.attr("fill-opacity", 0);
            d3Element.attr("fill", '#008000');
            d3Element.attr("stroke-width", 0);
        };

        ConnectorDeclarationView.prototype.setStylesWithOpacity = function (d3Element) {
            d3Element.attr("fill-opacity", 0.2);
            d3Element.attr("fill", '#008000');
            d3Element.attr("stroke-width", 0);
        };

        ConnectorDeclarationView.prototype.createPropertyPane = function() {
            var editableProperty = {
                propertyType: "text",
                key: "ConnectorDeclaration",
                model: this._model,
                getterMethod: this._model.getConnectorExpression,
                setterMethod: this._model.setConnectorExpression,
                getDisplayTitle: this._model.getConnectorVariable
            };

            var args = {
                model: this._model,
                lifeLineGroup:this._rootGroup,
                editableProperties: editableProperty
            };

            LifeLine.prototype.createPropertyPane.call(this, args)
        }

        return ConnectorDeclarationView;
    });
