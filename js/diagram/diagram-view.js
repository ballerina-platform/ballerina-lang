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

var Diagrams = (function (diagrams) {

    var views = diagrams.Views || {};

    var DiagramElementView = Backbone.View.extend(
        /** @lends DiagramElementView.prototype */
        {
            /**
             * @augments DiagramElementView.View
             * @constructs
             * @class DiagramElementView Represents the view for elements in a diagram.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                _.extend(this, _.pick(options, "options"));
            },

            /**
             * Default drag move handler which will translate view by appending new offsets
             * to current translate element.
             * @param {d3.event} event D3 event object.
             */
            dragMove: function (event) {
                var d = this.modelAttr("dragData");
                var dx = this.horizontalDrag() ? event.dx : 0;
                var dy = this.verticalDrag() ? event.dy : 0;
                d.x += dx;
                d.y += dy;
                var snappedPoint = this.snapToGrid(new GeoCore.Models.Point({'x': d.x, 'y': d.y}));

                if (this.model instanceof SequenceD.Models.LifeLine) {
                    this.model.get('centerPoint').move(dx, 0);
                }
                this.d3el.translate(snappedPoint.x(), snappedPoint.y());
                this.model.trigger("elementMoved", {dx: dx, dy: dy});
            },

            /**
             * Default drag start handler which captures original position
             * of the view.
             * @param {d3.event} event D3 event object.
             */
            dragStart: function (event) {
                if (this.modelAttr("dragData") === undefined) {
                    this.modelAttr("dragData", {
                        x: this.horizontalDrag() ? event.dx : 0,
                        y: this.verticalDrag() ? event.dy : 0
                    });
                }
            },

            /**
             * Default empty drag stop handler. Extending views need to override
             * for custom behaviour.
             */
            dragStop: function () {
            },

            /**
             * Snaps given point in to nearest grid.
             *
             * @param {Point} point
             */
            snapToGrid: function (point) {
                var newX = Math.round(point.x() / this.diagramView().gridWidth()) * this.diagramView().gridWidth();
                var newY = Math.round(point.y() / this.diagramView().gridHeight()) * this.diagramView().gridHeight();
                return new GeoCore.Models.Point({'x': newX, 'y': newY});
            },

            /**
             * Sets, un-sets or gets an attribute of underline model. If a value is not passed
             * gets the value of modelAttr. Otherwise, set the value of attribute.
             * if the passed value is null, un-set the attribute.
             *
             * @param {string} name Name of the model attribute.
             * @param {*} [value] Value of the model attribute.
             * @returns {*|void} Returns value if value is not passed. Else void.
             */
            modelAttr: function (name, value) {
                if (value === undefined) {
                    return this.model.get(name);
                }
                if (name !== undefined) {
                    if (value !== null) {
                        var data = {};
                        data[name] = value;
                        this.model.set(data);
                    } else {
                        this.model.unset(name);
                    }
                }
            },

            diagramView: function (diagramView) {
                if (_.isUndefined(diagramView)) {
                    return this.diagramViewRef;
                } else {
                    this.diagramViewRef = diagramView;
                }
            },

            /**
             * Checks whether this view supports horizontal drag.
             *
             * @returns {boolean}
             */
            horizontalDrag: function () {
                return true;
            },

            /**
             * Checks whether this view supports vertical drag.
             *
             * @returns {boolean}
             */
            verticalDrag: function () {
                return true;
            },

            render: function (paperID) {
                this.modelAttr("paperID", this.modelAttr("paperID") || paperID);
            },

            /**
             * Returns wrapped D3 reference for drawing in paper.
             *
             * @param paperSelector
             * @returns {*}
             */
            getD3Ref: function (paperSelector) {
                if (_.isUndefined(paperSelector)) {
                    var paperID = this.modelAttr("paperID");
                    if (_.isUndefined(paperID)) {
                        throw "Paper is not defined for rendering svg.";
                    }
                }
                return D3Utils.decorate(d3.select(paperSelector || this.modelAttr("paperID")))
            }
        });

    var ShapeView = DiagramElementView.extend(
        /** @lends ShapeView.prototype */
        {
            /**
             * @augments DiagramElementView.View
             * @constructs
             * @class ShapeView Represents the view for shapes in a diagram.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                DiagramElementView.prototype.initialize.call(this, options);
            },

            render: function (paperID) {
                DiagramElementView.prototype.render.call(this, paperID);
                this.model.on("connectionPointAdded", this.onConnectionPointAdded, this);
            },

            onConnectionPointAdded: function (connectionPoint) {
                var view = Diagrams.Utils.createViewForModel(connectionPoint, {});
                view.render(this.modelAttr("paperID"));
            }
        }
    );

    var ConnectionView = DiagramElementView.extend(
        /** @lends ConnectionView.prototype */
        {
            /**
             * @augments DiagramElementView.View
             * @constructs
             * @class ConnectionView Represents the view for connections in a diagram.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                DiagramElementView.prototype.initialize.call(this, options);
            },

            render: function (paperID) {
                DiagramElementView.prototype.render.call(this, paperID);
            }
        });

    var ConnectionPointView = DiagramElementView.extend(
        /** @lends ConnectionPointView.prototype */
        {
            /**
             * @augments DiagramElementView.View
             * @constructs
             * @class ConnectionPointView Represents the view for connection points in a diagram.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                DiagramElementView.prototype.initialize.call(this, options);
                this.model.on("connectionMade", this.onConnectionMade, this);
            },

            render: function (paperID) {
                DiagramElementView.prototype.render.call(this, paperID);
            },

            onConnectionMade: function (connection, x, y) {
                connection.point(this.getNextAvailableConnectionPoint(connection, x, y));
            },

            getNextAvailableConnectionPoint: function () {
                return new GeoCore.Models.Point({x: 0, y: 0});
            }
        });

    var LinkView = DiagramElementView.extend(
        /** @lends LinkView.prototype */
        {
            /**
             * @augments DiagramElementView.View
             * @constructs
             * @class LinkView Represents the view for links in a diagram.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                DiagramElementView.prototype.initialize.call(this, options);
            },
            // disable horizontal drag for links
            horizontalDrag: function () {
                return false;
            },

            sourceMoved: function (event) {
                this.d3el.attr('x1', this.model.source().point().x());
                this.d3el.attr('y1', this.model.source().point().y());
            },

            destinationMoved: function (event) {
                this.d3el.attr('x2', this.model.destination().point().x());
                this.d3el.attr('y2', this.model.destination().point().y());
            },

            render: function (paperID) {

                DiagramElementView.prototype.render.call(this, paperID);

                var line = this.getD3Ref().draw.lineFromPoints(this.model.source().point(), this.model.destination().point())
                    .classed(this.options.class, true);

                this.model.source().on("connectingPointChanged", this.sourceMoved, this);
                this.model.destination().on("connectingPointChanged", this.destinationMoved, this);

                this.d3el = line;
                this.el = line.node();
                return line;
            }

        });


    var DiagramView = Backbone.View.extend(
        /** @lends DiagramView.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class DiagramView Represents the view for the diagram
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                var opts = options.options || {};
                opts.selector = opts.selector || ".editor";
                opts.diagram = opts.diagram || {};
                opts.diagram.height = opts.diagram.height || "100%";
                opts.diagram.width = opts.diagram.width || "100%";
                opts.diagram.class = opts.diagram.class || "diagram";
                opts.diagram.selector = opts.diagram.selector || ".diagram";
                opts.diagram.wrapper = opts.diagram.wrapper || {};
                opts.diagram.wrapper.id = opts.diagram.wrapper.id || "diagramWrapper";
                opts.diagram.grid = opts.diagram.grid || {};
                opts.diagram.grid.height = opts.diagram.grid.height || 25;
                opts.diagram.grid.width = opts.diagram.grid.width || 25;
                this.options = opts;
                this.model.on("messageDrawStart", this.onMessageDrawStart, this);
                this.model.on("messageDrawEnd", this.onMessageDrawEnd, this);

                var container = d3.select(this.options.selector);
                if (_.isUndefined(container)) {
                    throw this.options.selector + " is not a valid query selector for container";
                }
                // wrap d3 with custom drawing apis
                container = D3Utils.decorate(container);

                var svg = container.draw.svg(this.options.diagram);
                this.d3svg = svg;
                svg.on("click", this.onClickDiagram);

                /*var svgPanNZoom = $(svg.node()).svgPanZoom({
                    events: {

                        // enables mouse wheel zooming events
                        mouseWheel: true,

                        // enables double-click to zoom-in events
                        doubleClick: false,

                        // enables drag and drop to move the SVG events
                        drag: true,

                        // cursor to use while dragging the SVG
                        dragCursor: "move"
                    },

                    // time in milliseconds to use as default for animations.
                    // Set 0 to remove the animation
                    animationTime: 300,

                    // how much to zoom-in or zoom-out
                    zoomFactor: 0.25,

                    // maximum zoom in, must be a number bigger than 1
                    maxZoom: 3,

                    // how much to move the viewBox when calling .panDirection() methods
                    panFactor: 100,

                    // the initial viewBox, if null or undefined will try to use the viewBox set in the svg tag.
                    // Also accepts string in the format "X Y Width Height"
                    initialViewBox: {

                        // the top-left corner X coordinate
                        x: 0,

                        // the top-left corner Y coordinate
                        y: 0,

                        // the width of the viewBox
                        width: 1000,

                        // the height of the viewBox
                        height: 1000
                    },

                    // the limits in which the image can be moved.
                    // If null or undefined will use the initialViewBox plus 15% in each direction
                    limits: {
                        x: -150,
                        y: -150,
                        x2: 1150,
                        y2: 1150
                    }
                });
                $(svg.node()).dblclick(function(){
                    svgPanNZoom.reset();
                });
                */

            },


            addContainableProcessorElement: function (processor, center) {
                var containableProcessorElem =  new SequenceD.Models.ContainableProcessorElement(lifeLineOptions);
                processor.containableProcessorElements().add(containableProcessorElem);
            },

            handleDropEvent: function (event, ui) {
                var newDraggedElem = $(ui.draggable).clone();
                //var type = newDraggedElem.attr('id');
                console.log("droped");
                var id = ui.draggable.context.lastChild.id;
                var position = {};
                position.x = ui.offset.left - $(this).offset().left;
                position.y = ui.offset.top - $(this).offset().top;
                if (Processors.manipulators[id] && diagram.selectedNode) {
                    //manipulators are unit processors
                    var processor = diagram.selectedNode.createProcessor(
                        Processors.manipulators[id].title,
                        createPoint(position.x, position.y),
                        Processors.manipulators[id].id,
                        {type: Processors.manipulators[id].type || "UnitProcessor", initMethod:Processors.manipulators[id].init},
                        {colour: Processors.manipulators[id].colour},
                        {parameters: Processors.manipulators[id].parameters},
                        {getMySubTree: Processors.manipulators[id].getMySubTree}
                    );
                    diagram.selectedNode.addChild(processor);
                    //diagram.trigger("renderDiagram");
                    diagramView.render();
                } else if (Processors.flowControllers[id] && diagram.selectedNode) {
                    var center = createPoint(position.x, position.y);
                    var processor = diagram.selectedNode.createProcessor(
                        Processors.flowControllers[id].title,
                        center,
                        Processors.flowControllers[id].id,
                        {type: Processors.flowControllers[id].type, initMethod:Processors.flowControllers[id].init },
                        {colour: Processors.flowControllers[id].colour},
                        {parameters: Processors.flowControllers[id].parameters},
                        {getMySubTree: Processors.flowControllers[id].getMySubTree}
                    );
                    diagram.selectedNode.addChild(processor);

                    if(processor.type == "TryBlockMediator") {
                        var containableProcessorElem1 = new SequenceD.Models.ContainableProcessorElement(lifeLineOptions);
                        containableProcessorElem1.set('title', "Try");
                        processor.containableProcessorElements().add(containableProcessorElem1);

                        var containableProcessorElem2 = new SequenceD.Models.ContainableProcessorElement(lifeLineOptions);
                        containableProcessorElem2.set('title', "Catch");
                        processor.containableProcessorElements().add(containableProcessorElem2);
                    }


                    diagramView.render();
                } else if (id == "EndPoint") {
                    endpointLifelineCounter++;
                    diagramView.renderMainElement(id, endpointLifelineCounter, MainElements.lifelines.EndPointLifeline.colour);

                } else if (id == "Resource") {
                    resourceLifelineCounter++;
                    if(resourceLifelineCounter == 1) {
                        diagramView.renderMainElement(id, resourceLifelineCounter, MainElements.lifelines.ResourceLifeline.colour);
                    }
                } else {

                }
            },


            render: function () {

                //Remove previous diagram
                if (this.d3el) {
                    this.d3el.remove();
                    for (var element in diagramViewElements) {
                        diagramViewElements[element].remove();
                    }
                } else {
                    // When re-rendering the same event handler do not need to re-register.
                    // Otherwise same function will call for multiple times.
                    this.model.on("addElement", this.onAddElement, this);
                    this.model.on("renderDiagram", this.renderDiagram);
                }
                diagramViewElements = [];


                var definitions = this.d3svg.append("defs");

                var mainGroup = this.d3svg.draw.group(this.d3svg).attr("id", this.options.diagram.wrapper.id)
                    .attr("width", "100%")
                    .attr("height", "100%");

                // add marker definitions
                definitions.append("marker")
                    .attr("id", "markerArrow")
                    .attr("markerWidth", "13")
                    .attr("markerHeight", "13")
                    .attr("refX", "10")
                    .attr("refY", "6")
                    .attr("orient", "auto")
                    .append("path")
                    .attr("d", "M2,2 L2,11 L10,6 L2,2")
                    .attr("class", "marker");

                var filter = definitions.append("filter")
                    .attr("id", "drop-shadow")
                    .attr("height", "130%");

                filter.append("feGaussianBlur")
                    .attr("in", "SourceAlpha")
                    .attr("stdDeviation", 1)
                    .attr("result", "blur");

                filter.append("feOffset")
                    .attr("in", "blur")
                    .attr("dx", 5)
                    .attr("dy", 5)
                    .attr("result", "offsetBlur");

                var feMerge = filter.append("feMerge");

                feMerge.append("feMergeNode")
                    .attr("in", "offsetBlur");
                feMerge.append("feMergeNode")
                    .attr("in", "SourceGraphic");



                this.d3el = mainGroup;
                this.el = mainGroup.node();
                this.htmlDiv = $(this.options.selector);
                this.htmlDiv.droppable({
                    drop: this.handleDropEvent,
                    tolerance: "pointer"
                });


                this.htmlDiv.attr("ondragstart", "return false");

                for (var id in this.model.attributes.diagramResourceElements.models) {
                    if (this.model.attributes.diagramResourceElements.models[id] instanceof SequenceD.Models.LifeLine) {
                        var lifeLine = this.model.attributes.diagramResourceElements.models[id];
                        var lifeLineView = new SequenceD.Views.LifeLineView({
                            model: lifeLine,
                            options: lifeLineOptions
                        });
                        diagramViewElements[diagramViewElements.length] = (lifeLineView);
                        var rectColour = this.model.attributes.diagramResourceElements.models[id].attributes.colour;
                        lifeLineView.render("#" + this.options.diagram.wrapper.id, "processors", rectColour);
                    }
                }

                for (var id in this.model.attributes.diagramResourceElements.models) {
                    if (this.model.attributes.diagramResourceElements.models[id] instanceof SequenceD.Models.LifeLine) {
                        var lifeLine = this.model.attributes.diagramResourceElements.models[id];
                        var lifeLineView = new SequenceD.Views.LifeLineView({
                            model: lifeLine,
                            options: lifeLineOptions
                        });
                        diagramViewElements[diagramViewElements.length] = (lifeLineView);
                        var rectColour = this.model.attributes.diagramResourceElements.models[id].attributes.colour;
                        lifeLineView.render("#" + this.options.diagram.wrapper.id, "messages", rectColour);
                    }
                }

                for (var id in this.model.attributes.diagramEndpointElements.models) {
                    if (this.model.attributes.diagramEndpointElements.models[id] instanceof SequenceD.Models.LifeLine) {
                        var lifeLine = this.model.attributes.diagramEndpointElements.models[id];
                        var lifeLineView = new SequenceD.Views.LifeLineView({
                            model: lifeLine,
                            options: lifeLineOptions
                        });
                        diagramViewElements[diagramViewElements.length] = (lifeLineView);
                        var rectColour = this.model.attributes.diagramEndpointElements.models[id].attributes.colour;
                        lifeLineView.render("#" + this.options.diagram.wrapper.id, "processors", rectColour);
                    }
                }

                for (var id in this.model.attributes.diagramEndpointElements.models) {
                    if (this.model.attributes.diagramEndpointElements.models[id] instanceof SequenceD.Models.LifeLine) {
                        var lifeLine = this.model.attributes.diagramEndpointElements.models[id];
                        var lifeLineView = new SequenceD.Views.LifeLineView({
                            model: lifeLine,
                            options: lifeLineOptions
                        });
                        diagramViewElements[diagramViewElements.length] = (lifeLineView);
                        var rectColour = this.model.attributes.diagramEndpointElements.models[id].attributes.colour;
                        lifeLineView.render("#" + this.options.diagram.wrapper.id, "messages", rectColour);
                    }
                }

                return mainGroup;
            },

            renderMainElement: function (lifelineName, counter, colour) {
                var numberOfResourceElements = diagram.attributes.diagramResourceElements.length;
                var numberOfEndpointElements = diagram.attributes.diagramEndpointElements.length;
                var centerPoint;
                if(numberOfEndpointElements > 0) {
                    if(lifelineName == "Resource") {
                        centerPoint = createPoint(200, 50);
                        diagram.attributes.diagramEndpointElements.each(function(model) {
                            var xVal = model.get('centerPoint').attributes.x;
                            model.get('centerPoint').move(180, 0);
                            model.setX(xVal + 180);
                            model.rightLowerConer({x: xVal + 245, y: 0});
                        });
                    } else {
                        var lastLifeLine = diagram.attributes.diagramEndpointElements.models[numberOfEndpointElements - 1];
                        centerPoint = createPoint(lastLifeLine.rightLowerConer().x + 115, 50);
                    }
                } else {
                    if(numberOfResourceElements > 0) {
                        var lastLifeLine = diagram.attributes.diagramResourceElements.models[numberOfResourceElements - 1];
                        centerPoint = createPoint(lastLifeLine.rightLowerConer().x + 115, 50);
                    } else {
                        //initial life line position
                        centerPoint = createPoint(200, 50);
                    }
                }
                var lifeline = createLifeLine(lifelineName + counter, centerPoint, colour);
                lifeline.leftUpperConer({x: centerPoint.attributes.x - 65, y: centerPoint.attributes.y - 15});
                lifeline.rightLowerConer({
                    x: centerPoint.attributes.x + 65,
                    y: centerPoint.attributes.y + 15 + lifeLineOptions.middleRect.height + lifeLineOptions.rect.heigh
                });
                lifeLineOptions.colour = colour;
                diagram.addElement(lifeline, lifeLineOptions);
                diagramView.render();
            },

            renderDiagram: function () {
                diagramView.render();
            },

            onAddElement: function (element, opts) {
                this.renderViewForElement(element, opts);
            },

            onClickDiagram: function () {
                if (diagram.selected === true) {
                    diagram.selected = false;
                    $('#propertyPane').empty();
                    $('#propertySave').hide();
                } else if (!diagram.selectedNode) {

                    if (selected.classList && selected.classList.contains("lifeline_selected")) {
                        selected.classList.toggle("lifeline_selected");
                    }

                    udcontrol.set('visible', false);
                    udcontrol.set('lifeline', '');
                    selected = '';
                    diagram.selected = true;

                    $('#propertyPane').empty();
                    $('#propertySave').show();

                    propertyPane = ppView.createPropertyPane(diagram.getDefinitionSchema(),
                                                             diagram.getDefinitionEditableProperties(), diagram);
                }
            },

            renderViewForElement: function (element, renderOpts) {
                var view = Diagrams.Utils.createViewForModel(element, renderOpts);
                view.diagramView(this);
                view.render("#" + this.options.diagram.wrapper.id);
            },

            gridWidth: function () {
                return this.options.diagram.grid.width;
            },

            gridHeight: function () {
                return this.options.diagram.grid.height;
            },

            onMessageDrawEnd: function(sourceModel, sourcePoint, destinationPoint) {

                var destinationModel = null;
                if (this.model.destinationLifeLine) {
                    destinationModel = this.model.destinationLifeLine;
                    this.model.destinationLifeLine = null;
                } else if(this.model.destinationProcessor){
                    destinationModel = this.model.destinationProcessor;
                    this.model.destinationProcessor = null;
                }

                if(destinationModel){
                    if(!_.isEqual(sourceModel.cid, destinationModel.cid )){
                       var messageOptionsInbound = {'class': 'messagePoint', 'direction': 'inbound'};
                       var messageOptionsOutbound = {'class': 'messagePoint', 'direction': 'outbound'};
                       sourceModel.addChild(sourcePoint, messageOptionsOutbound);
                       destinationModel.addChild(destinationPoint, messageOptionsInbound);
                    }
                }
                this.render();
            },

            onMessageDrawStart: function(sourceModel, startPoint, calcNewStartPoint, onMessageDrawEndCallback){

                var diagView = this;

                var line = this.d3svg.append("line")
                    .attr("x1", startPoint.x())
                    .attr("y1", startPoint.y())
                    .attr("x2", startPoint.x())
                    .attr("y2", startPoint.y())
                    .attr("marker-end", "url(#markerArrow)")
                    .attr("class", "message");

                this.d3svg.on("mousemove", function () {
                    var m = d3.mouse(this);
                    line.attr("x2", m[0]);
                    line.attr("y2", m[1]).attr("marker-end", "url(#markerArrow)");
                    if(!_.isUndefined(calcNewStartPoint)){
                        var newSP = calcNewStartPoint(m[0], m[1]);
                        line.attr("x1", newSP.x);
                        line.attr("y1", newSP.y);
                    }
                });

                this.d3svg.on("mouseup", function () {
                    // unbind current listeners
                    diagView.d3svg.on("mousemove", null);
                    diagView.d3svg.on("mouseup", null);
                    var startPoint = new GeoCore.Models.Point({x:line.attr("x1"), y:line.attr("y1")}),
                        endpoint = new GeoCore.Models.Point({x:line.attr("x2"), y:line.attr("y2")});
                    line.remove();

                    var sourcePoint = new SequenceD.Models.MessagePoint({
                        model : {type: "messagePoint"},
                        x: startPoint.x(),
                        y: startPoint.y(),
                        direction: "outbound"
                    });
                    var destinationPoint = new SequenceD.Models.MessagePoint({
                        model : {type: "messagePoint"},
                        x: endpoint.x(),
                        y: endpoint.y(),
                        direction: "inbound"
                    });
                    var messageLink = new SequenceD.Models.MessageLink({
                        source: sourcePoint,
                        destination: destinationPoint
                    });
                    diagView.model.trigger("messageDrawEnd", sourceModel, sourcePoint, destinationPoint);

                });
            }
        });

    views.DiagramElementView = DiagramElementView;
    views.ShapeView = ShapeView;
    views.LinkView = LinkView;
    views.ConnectionView = ConnectionView;
    views.ConnectionPointView = ConnectionPointView;
    views.DiagramView = DiagramView;

    diagrams.Views = views;
    return diagrams;

}(Diagrams || {}));
