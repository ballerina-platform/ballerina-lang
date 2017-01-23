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
define(['lodash', 'log', 'd3', './ballerina-view', './variables-view', 'ballerina/ast/ballerina-ast-factory', './canvas', './point'], function (_, log, d3, BallerinaView, VariablesView, BallerinaASTFactory, Canvas, Point) {
    var StructDefinitionView = function (args) {
        Canvas.call(this, args);

        this._parentView = _.get(args, "parentView");
        this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
        this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
        //set panel icon for the struct
        this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.struct_icon");
        //set initial height for the struct container svg
        this._totalHeight = 50;

        if (_.isNil(this._model) || !(BallerinaASTFactory.isStructDefinition(this._model))) {
            log.error("Struct definition is undefined or is of different type." + this._model);
            throw "Struct definition is undefined or is of different type." + this._model;
        }

        if (_.isNil(this._container)) {
            log.error("Container for Struct definition is undefined." + this._container);
            throw "Container for Struct definition is undefined." + this._container;
        }
        this.init();
    };

    StructDefinitionView.prototype = Object.create(Canvas.prototype);
    StructDefinitionView.prototype.constructor = Canvas;

    StructDefinitionView.prototype.init = function(){
        //Registering event listeners
        this.listenTo(this._model, 'child-removed', this.childViewRemovedCallback);
    };

    StructDefinitionView.prototype.canVisitStructDefinition = function (structDefinition) {
        return true;
    };

    /**
     * Rendering the view of the Struct definition.
     * @returns {Object} - The svg group which the struct definition view resides in.
     */
    StructDefinitionView.prototype.render = function (diagramRenderingContext) {
        this.diagramRenderingContext = diagramRenderingContext;
        this.drawAccordionCanvas(this._container, this._viewOptions, this._model.id, this._model.type.toLowerCase(), this._model._structName);
        var divId = this._model.id;
        var currentContainer = $('#' + divId);
        this._container = currentContainer;
        this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
        this.getModel().accept(this);
        var self = this;

        $("#title-" + this._model.id).addClass("struct-title-text").text(this._model.getStructName())
            .on("change paste keyup", function (e) {
                self._model.setStructName($(this).text());
            }).on("click", function (event) {
            event.stopPropagation();
        }).on("keydown", function (e) {
            // Check whether the Enter key has been pressed. If so return false. Won't type the character
            if (e.keyCode === 13) {
                return false;
            }
        });

        this._model.on('child-added', function (child) {
            self.visit(child);
            self._model.trigger("child-visited", child);

            // Show/Hide scrolls.
            self._showHideScrolls(self._container, self.getChildContainer().node().ownerSVGElement);
        });

        var operationsPane = this.getOperationsPane();

        // Creating annotation icon.
        var panelAnnotationIcon = $("<i/>", {
            class: "fw fw-annotation pull-right right-icon-clickable hoverable"
        }).appendTo(operationsPane);

        // Stopping event propagation to the elements behind.
        panelAnnotationIcon.click(function (event) {
            event.stopPropagation();
        });

        // Adding separator for annotation icon.
        $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(operationsPane);

        this.setServiceContainerWidth(this._container.width());
    };

    StructDefinitionView.prototype.getChildContainer = function () {
        return this._rootGroup;
    };

    return StructDefinitionView;
});