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
define(['lodash', 'd3','log', './ballerina-statement-view', './../ast/action-invocation-expression','./point', 'd3utils'],
    function (_, d3, log, BallerinaStatementView, ActionInvocationExpression, Point, D3Utils) {

        var ActionInvocationStatementView = function (args) {
            BallerinaStatementView.call(this, args);
            this._connectorView = {};

            if (_.isNil(this._model) || !(this._model.getFactory().isActionInvocationStatement(this._model))) {
                log.error("Action invocation statement definition is undefined or is of different type." + this._model);
                throw "Action invocation statement is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for action statement is undefined." + this._container);
                throw "Container for action statement is undefined." + this._container;
            }

            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);

        };

        ActionInvocationStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ActionInvocationStatementView.prototype.constructor = ActionInvocationStatementView;


        ActionInvocationStatementView.prototype.init = function(){
            this.getModel().on("drawConnectionForAction",this.drawActionConnections,this);
        };
        ActionInvocationStatementView.prototype.setDiagramRenderingContext = function(context){
            this._diagramRenderingContext = context;
        };
        ActionInvocationStatementView.prototype.getDiagramRenderingContext = function(){
            return this._diagramRenderingContext;
        };

        // TODO : Please revisit this method. Needs a refactor
        ActionInvocationStatementView.prototype.drawActionConnections = function(startPoint,parent){
            log.info("Drawing connections for http connector actions");
            // TODO : Please alter this logic
            if(!_.isNil(this.getModel().getConnector())) {
                var connector = this.getDiagramRenderingContext().getViewModelMap()[this.messageManager.getActivatedDropTarget().id];
                this._model.setConnector(this.messageManager.getActivatedDropTarget());
                this.DrawArrow(this.getDiagramRenderingContext());
            }
        };

        ActionInvocationStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ActionInvocationExpression) {
                this._model = model;
            } else {
                log.error("Action statement definition is undefined or is of different type." + model);
                throw "Action statement definition is undefined or is of different type." + model;
            }
        };

        ActionInvocationStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for action statement is undefined." + container);
                throw "Container for action statement is undefined." + container;
            }
        };

        ActionInvocationStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ActionInvocationStatementView.prototype.getModel = function () {
            return this._model;
        };

        ActionInvocationStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ActionInvocationStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        ActionInvocationStatementView.prototype.setConnectorView = function(view){
            this._connectorView = view;
        };
        ActionInvocationStatementView.prototype.getConnectorView = function(){
            return this._connectorView;
        };


        /**
         * Rendering the view for get-Action statement.
         * @returns {group} The svg group which contains the elements of the action statement view.
         */
        ActionInvocationStatementView.prototype.render = function (renderingContext) {
            // TODO : Please revisit this method. Needs a refactor
            this.setDiagramRenderingContext(renderingContext);
            var connectorModel =  this._model.getConnector();

            if(!_.isUndefined(connectorModel)) {
                this.connector = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
            }

            var assignmentStatementGroup = D3Utils.group(d3.select(this._container));
            var parentGroup = this._container;
            assignmentStatementGroup.attr("id", "_" + this._model.id);//added attribute 'id' starting with '_' to be compatible with HTML4
            var width = this.getBoundingBox().w();
            var height = this.getBoundingBox().h();
            var x = this.getBoundingBox().getLeft();
            var y = this.getBoundingBox().getTop();

            var assignmentRect = D3Utils.rect(x, y, width, height, 0, 0, assignmentStatementGroup).classed('statement-rect', true);
            // TODO : Please revisit these calculations.
            var processorConnectorPoint = D3Utils.circle((x + width), ((y + height / 2)), 10, assignmentStatementGroup);
            processorConnectorPoint.attr("fill-opacity", 0.01);

            this.processorConnectPoint = processorConnectorPoint;
            var assignmentText = "HTTP:get";
            // TODO : Please revisit these calculations.
            var expressionText = D3Utils.textElement(x + width / 2, y + height / 2, assignmentText, assignmentStatementGroup).classed('statement-text', true);
            this._model.accept(this);

            // Creating property pane
            var editableProperties = [
                {
                    propertyType: "text",
                    key: "Variable",
                    model: this._model,
                    getterMethod: this._model.getVariableAccessor,
                    setterMethod: this._model.setVariableAccessor
                },
                {
                    propertyType: "text",
                    key: "Action",
                    model: this._model,
                    getterMethod: this._model.getAction,
                    setterMethod: this._model.setAction
                },
                {
                    propertyType: "text",
                    key: "Path",
                    model: this._model,
                    getterMethod: this._model.getPath,
                    setterMethod: this._model.setPath
                },
                {
                    propertyType: "text",
                    key: "Message",
                    model: this._model,
                    getterMethod: this._model.getMessage,
                    setterMethod: this._model.setMessage
                }
            ];
            this._createPropertyPane({
                model: this._model,
                statementGroup: assignmentStatementGroup,
                editableProperties: editableProperties
            });

            this.parentContainer = d3.select(parentGroup);
            var self = this;
            this.processorConnectPoint.on("mousedown", function () {
                d3.event.preventDefault();
                d3.event.stopPropagation();
                var m = d3.mouse(this);

                var width = self.getBoundingBox().w();
                var height = self.getBoundingBox().h();
                var x = self.getBoundingBox().getLeft();
                var y = self.getBoundingBox().getTop();

                // TODO : Remove magic numbers
                var sourcePointX = x + width;
                var sourcePointY = y + height / 2;

                self.sourcePoint = new Point(sourcePointX, sourcePointY);
                //setting resource's container as the parent for the message.
                var parent = self.getDiagramRenderingContext().currentResource.getContainer();
                self.messageManager.startDrawMessage(self._model, self.sourcePoint, parent);
            });

            this.processorConnectPoint.on("mouseover", function () {
                processorConnectorPoint.style("fill", "red").style("fill-opacity", 0.5)
                    .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
            });

            this.processorConnectPoint.on("mouseout", function () {
                processorConnectorPoint.style("fill", "#2c3e50").style("fill-opacity", 0.01);
            });

            var self = this;
            this.getBoundingBox().on('top-edge-moved', function(dy){
                assignmentRect.attr('y',  parseFloat(assignmentRect.attr('y')) + dy);
                expressionText.attr('y',  parseFloat(expressionText.attr('y')) + dy);
                processorConnectorPoint.attr('cy',  parseFloat(processorConnectorPoint.attr('cy')) + dy);

                if(!_.isUndefined(self.processorConnector) && !_.isUndefined(self.processorConnector2)){
                    self.processorConnector.attr('y1', parseFloat(self.processorConnector.attr('y1')) + dy);
                    self.processorConnector2.attr('y1', parseFloat(self.processorConnector2.attr('y1')) + dy);

                    var x=  parseFloat(self.processorConnector.attr('x2')) - 5;
                    var y = parseFloat(self.processorConnector.attr('y2')) + dy;
                    var arrowHeadPoints = "" + x + "," + (y - 5) + " " + (x + 5) + "," + (y) + " " + x + "," + (y + 5);
                    self.arrowHead.attr("points", arrowHeadPoints);

                    x= parseFloat(self.processorConnector2.attr('x1'));
                    var y = parseFloat(self.processorConnector2.attr('y2')) + dy;
                    var backArrowHeadPoints = "" + x + "," + y + " " + (x + 5) + "," + (y - 5) + " " + (x + 5) + "," + (y + 5);
                    self.backArrowHead.attr("points", backArrowHeadPoints);

                    self.processorConnector.attr('y2', parseFloat(self.processorConnector.attr('y2')) + dy);
                    self.processorConnector2.attr('y2', parseFloat(self.processorConnector2.attr('y2')) + dy);
                }
            });
        };

        ActionInvocationStatementView.prototype.DrawArrow = function (context) {
            var processorConnector = undefined;
            var arrowHead = undefined;
            var processorConnector2 = undefined;

            this.setDiagramRenderingContext(context);
            var connectorModel = this._model.getConnector();

            if(!_.isUndefined(connectorModel)) {
                this.connector = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
            }

            if(!_.isUndefined(this.connector)) {

                var parent = this.parentContainer;
                var width = this.getBoundingBox().w();
                var height = this.getBoundingBox().h();
                var x = this.getBoundingBox().getLeft();
                var y = this.getBoundingBox().getTop();


                var sourcePointX = x + width;
                var sourcePointY = y + height / 2;

                var startPoint = new Point(sourcePointX, sourcePointY);

                //var connectorViewOpts = this.getDiagramRenderingContext().getViewModelMap()[this.messageManager.getActivatedDropTarget()].getViewOptions();
                var connectorCenterPointX = this.connector.getMiddleLineCenter().x();
                var connectorCenterPointY = this.connector.getMiddleLineCenter().y();
                var startX = Math.round(startPoint.x());
                this.processorConnector = D3Utils.line(Math.round(startPoint.x()), Math.round(startPoint.y()), Math.round(connectorCenterPointX),
                    Math.round(startPoint.y()), parent).classed("action-line", true);
                this.arrowHead = D3Utils.inputTriangle(Math.round(connectorCenterPointX) - 5, Math.round(startPoint.y()), parent).classed("action-arrow", true);
                this.processorConnector2 = D3Utils.line(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, Math.round(connectorCenterPointX),
                    Math.round(startPoint.y()) + 8, parent).classed("action-dash-line", true);
                this.backArrowHead = D3Utils.outputTriangle(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, parent).classed("action-arrow", true);

            }
        };

        return ActionInvocationStatementView;

    });