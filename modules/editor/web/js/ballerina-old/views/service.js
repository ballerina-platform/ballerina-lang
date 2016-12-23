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
define(['require', 'log', 'jquery', 'd3', 'd3utils', 'backbone', 'lodash', 'diagram_core', 'main_elements',
        './service-outline', 'processors', './life-line',
        'ballerina_models/containable-processor-element', 'ballerina_models/life-line',  'app/ballerina-old/models/message-point',
        'app/ballerina-old/models/message-link', 'ballerina_models/service', 'app/ballerina-old/utils/module',
        'app/ballerina-old/utils/processor-factory', 'svg_pan_zoom', 'jquery_ui'],

function (require, log, $, d3, D3Utils, Backbone,  _, DiagramCore, MainElements, DiagramPreview, Processors, LifeLineView,
          ContainableProcessorElement, LifeLine, MessagePoint, MessageLink, Service, utils, ProcessorFactory

) {
    var createLifeLine = function (title, center, cssClass, utils, parameters, textModel, type, definition) {
        return new LifeLine({
            title: title,
            centerPoint: center,
            cssClass: cssClass,
            utils: utils,
            parameters: parameters,
            textModel: textModel,
            type: type,
            definition: definition
        });
    };

    var ServiceView = Backbone.View.extend(
        /** @lends ServiceView.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class ServiceView Represents the view for the ballerina service
             * @param {Object} options Rendering options for the view
             */
            initialize: function (opts) {
                opts.diagram = opts.diagram || {};
                opts.diagram.height = opts.diagram.height || "100%";
                opts.diagram.width = opts.diagram.width || "100%";
                opts.diagram.padding =  opts.diagram.padding || 50;
                opts.diagram.viewBoxWidth =  opts.diagram.viewBoxWidth || 1000;
                opts.diagram.viewBoxHeight =  opts.diagram.viewBoxHeight || 1000;

                opts.diagram.class = opts.diagram.class || "diagram";
                opts.diagram.selector = opts.diagram.selector || ".diagram";
                opts.diagram.wrapper = opts.diagram.wrapper ||{};
                // CHANGED
                opts.diagram.wrapperId = opts.wrapperId || "diagramWrapper";
                opts.diagram.grid = opts.diagram.grid || {};
                opts.diagram.grid.height = opts.diagram.grid.height || 25;
                opts.diagram.grid.width = opts.diagram.grid.width || 25;
                this.options = opts;

                // set previewMode to false if not set
                if(_.isUndefined(this.isPreviewMode())){
                    this.setPreviewMode(false);
                }

                _.extend(this, _.pick(opts, ['toolPalette']));

                // create a new service model if not passed
                if(_.isUndefined(this.model)){
                    this.model = new Service();
                    this.addInitialElements();
                }

                this.model.on("messageDrawStart", this.onMessageDrawStart, this);
                this.model.on("messageDrawEnd", this.onMessageDrawEnd, this);

                          var container = d3.select(this.options.container);


                if (_.isUndefined(this.options.container)) {
                    log.error("options.container is not defined");
                }
                // wrap d3 with custom drawing apis
                container = D3Utils.decorate(container);
                var svg = container.draw.svg(this.options.diagram);

                var definitions = svg.append("defs");
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

                // add the delete icon pattern
                definitions.append("pattern")
                    .attr("id", "delIcon")
                    .attr("x", "0").
                    attr("y", "0").
                    attr("patternUnits", "objectBoundingBox").
                    attr("height", "12").
                    attr("width", "12").
                    append("svg:image").
                    attr("x", "6").
                    attr("y", "6").
                    attr("height", "12").
                    attr("width", "12").
                    attr("xlink:href", "images/delete.svg");

                // add the delete icon pattern
                definitions.append("pattern")
                    .attr("id", "editIcon")
                    .attr("x", "0").
                    attr("y", "0").
                    attr("patternUnits", "objectBoundingBox").
                    attr("height", "12").
                    attr("width", "12").
                    append("svg:image").
                    attr("x", "6").
                    attr("y", "6").
                    attr("height", "12").
                    attr("width", "12").
                    attr("xlink:href", "images/edit.svg");

                // add the delete icon pattern
                definitions.append("pattern")
                    .attr("id", "addIcon")
                    .attr("x", "0").
                    attr("y", "0").
                    attr("patternUnits", "objectBoundingBox").
                    attr("height", "12").
                    attr("width", "12").
                    append("svg:image").
                    attr("x", "6").
                    attr("y", "6").
                    attr("height", "12").
                    attr("width", "12").
                    attr("xlink:href", "images/add.svg");

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

                this.d3svg = svg;

                    this.panAndZoom = $(svg.node()).svgPanZoom({
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
                        animationTime: 100,

                        // how much to zoom-in or zoom-out
                        zoomFactor: 0.1,

                        // maximum zoom in, must be a number bigger than 1
                        maxZoom: 10,

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
                            width: this.options.diagram.viewBoxWidth,

                            // the height of the viewBox
                            height: this.options.diagram.viewBoxHeight
                        }
                    });

                $(svg.node()).dblclick({view: this}, function (evt) {
                    evt.data.view.panAndZoom.reset();
                });
                //update pan and zoom limits upon redraw
                this.on("renderCompleted", function(){
                    this.calculateViewBoxLimits();
                }, this);

                // override default setViewBox to trigger notifications.
                var view = this;
                var setViewBox = this.panAndZoom.setViewBox.bind(this.panAndZoom);
                this.panAndZoom.setViewBox = function(x, y, width, height, animationTime){
                    setViewBox.call(this.panAndZoom, x, y, width, height, animationTime);
                    view.trigger("viewBoxChange", this.getViewBox(), animationTime);
                };
                svg.attr("preserveAspectRatio", "xMinYMin meet");
                // disable zoom in/out handler from plugin to override default behaviour
                $(svg.node()).unbind("mousewheel DOMMouseScroll MozMousePixelScroll");
                $(svg.node()).on("wheel", null, this, this.toggleZoom);
            },


            isPreviewMode:function(){
                return this._previewMode;
            },
            setPreviewMode:function(mode){
                this._previewMode = mode;
            },

            createPreview: function(opts){
                opts.mainView = this;
                return new DiagramPreview(opts);
            },

            toggleZoom: function(ev){
                var diagView = ev.data, delta, minHeight, minWidth, newMousePosition, newViewBox, newcenter,
                    oldDistanceFromCenter, oldMousePosition, oldViewBox, oldcenter, reductionFactor;
                delta = parseInt(ev.originalEvent.wheelDelta);
                if (delta === 0) {
                    return;
                }
                ev.preventDefault();
                ev.stopPropagation();
                oldViewBox = diagView.panAndZoom.getViewBox();

                oldMousePosition = diagView.toViewBoxCoordinates(new DiagramCore.Models.Point({x: ev.originalEvent.clientX,
                    y: ev.originalEvent.clientY}));
                oldcenter = {
                    x: oldViewBox.x + oldViewBox.width / 2,
                    y: oldViewBox.y + oldViewBox.height / 2
                };
                oldDistanceFromCenter = {
                    x: oldcenter.x - oldMousePosition.x(),
                    y: oldcenter.y - oldMousePosition.y()
                };
                if (delta > 0) {
                    diagView.panAndZoom.zoomIn(void 0, 0);
                    var viewBox = diagView.panAndZoom.getViewBox();
                    minWidth = diagView.panAndZoom.initialViewBox.width / diagView.panAndZoom.maxZoom;
                    minHeight = diagView.panAndZoom.initialViewBox.height / diagView.panAndZoom.maxZoom;
                    if (viewBox.width < minWidth) {
                        reductionFactor = minWidth / viewBox.width;
                        viewBox.width = minWidth;
                        viewBox.height = viewBox.height * reductionFactor;
                    }
                    if (viewBox.height < minHeight) {
                        reductionFactor = minHeight / viewBox.height;
                        viewBox.height = minHeight;
                        viewBox.width = viewBox.width * reductionFactor;
                    }
                    diagView.panAndZoom.setViewBox(viewBox.x, viewBox.y, viewBox.width, viewBox.height, 0);
                } else {
                    diagView.panAndZoom.zoomOut(void 0, 0);
                }
                newMousePosition = diagView.toViewBoxCoordinates(new DiagramCore.Models.Point({x: ev.originalEvent.clientX,
                    y: ev.originalEvent.clientY}));
                newcenter = {
                    x: oldcenter.x + (oldMousePosition.x() - newMousePosition.x()),
                    y: oldcenter.y + (oldMousePosition.y() - newMousePosition.y())
                };
                diagView.panAndZoom.setCenter(newcenter.x, newcenter.y, 0);
                newViewBox = diagView.panAndZoom.getViewBox();
                diagView.panAndZoom.setViewBox(oldViewBox.x, oldViewBox.y, oldViewBox.width, oldViewBox.height, 0);
                diagView.panAndZoom.setViewBox(newViewBox.x - newMousePosition.x(),
                    newViewBox.y - newMousePosition.y(), newViewBox.width, newViewBox.height);
            },

            disableDragZoomOptions: function () {
                this.panAndZoom.events.drag = false;
                this.panAndZoom.events.mouseWheel = false;

                //Blocking the mousewheel event
                document.onmousewheel = function (e) {
                    this.stopWheel();
                };
                if (document.addEventListener) {
                    document.addEventListener('DOMMouseScroll', this.stopWheel, false);
                }
            },

            stopWheel: function () {
                if (!e) {
                    e = window.event;
                }
                if (e.preventDefault) {
                    e.preventDefault();
                }
                e.returnValue = false;
            },

            enableDragZoomOptions: function () {
                this.panAndZoom.events.drag = true;
                this.panAndZoom.events.mouseWheel = true;

                //Re-enabling the mousewheel event
                document.onmousewheel = null;
                if (document.addEventListener) {
                    document.removeEventListener('DOMMouseScroll', this.stopWheel, false);
                }
            },

            drawPropertiesPane: function (svg, options, parameters, propertyPaneSchema) {
                //remove the property pane svg, if it already exists
                var propertySVG = document.getElementById("property-pane-svg");
                if (propertySVG) {
                    propertySVG.parentNode.removeChild(propertySVG);
                }

                var svgOptions = {
                    id: "property-pane-svg",
                    height: "100%",
                    width: "100%",
                    class: "property",
                    x: options.x,
                    y: options.y
                };
                propertySVG = svg.draw.propertySVG(svgOptions);

                var rect = propertySVG.append("rect")
                    .attr("id", "property-pane")
                    .attr("x", 7)
                    .attr("y", 5)
                    .attr("rx", "0")
                    .attr("ry", "0")
                    .attr("width", "245")
                    .attr("fill", "#ffffff")
                    .attr("stroke", "#000000")
                    .attr("stroke", "#000000")
                    .attr("opacity", "0.9");

                this.disableDragZoomOptions();
                diagram.propertyWindow = true;
                propertySVG.draw.form(propertySVG, parameters, propertyPaneSchema, rect);
            },

            /**
             * Checks whether the diagram view is empty. (main group has no children.)
             *
             * @returns {boolean}
             */
            isEmpty: function(){
                if(!this.el){
                    return true;
                }
                return this.el.childNodes.length === 0;
            },

            getCurrentAspectRatio: function(){
                var svgStyle = getComputedStyle(this.d3svg.node()) ;
                return parseFloat(svgStyle.width)/parseFloat(svgStyle.height);
            },

            getInitialAspectRatio: function(){
                return (this.panAndZoom.initialViewBox.width/this.panAndZoom.initialViewBox.height);
            },

            calculateViewBoxLimits: function () {
                if(this.d3el){
                    var wrapperBBx = this.d3el.node().getBBox();
                    if(wrapperBBx){
                        var aspectRatio = 1;
                        var width = wrapperBBx.width ;
                        var height = wrapperBBx.height;
                        var max = Math.max(width, height);
                        if (max === width){
                            if(this.panAndZoom.initialViewBox.width > width){
                                width = this.panAndZoom.initialViewBox.width;
                                var useInitOrigin = true;
                            }
                            height = width * (1/this.getCurrentAspectRatio());
                        } else {
                            if(this.panAndZoom.initialViewBox.height > height){
                                height = this.panAndZoom.initialViewBox.height;
                                var useInitOrigin = true;
                            }
                            width = height * (this.getCurrentAspectRatio());
                        }
                        var newlimits = {
                            x:  ((useInitOrigin) ? this.panAndZoom.initialViewBox.x : wrapperBBx.x) - this.options.diagram.padding,
                            y:  ((useInitOrigin) ? this.panAndZoom.initialViewBox.y : wrapperBBx.y) - this.options.diagram.padding,
                            x2: width + wrapperBBx.x + this.options.diagram.padding,
                            y2: height + wrapperBBx.y + this.options.diagram.padding
                        };
                        this.panAndZoom.limits = newlimits;
                        this.trigger("viewBoxLimitsUpdated", newlimits);
                    }
                }

            },

            /**
             * Gets the SVG Viewport size.
             *
             * @returns {{}} Viewport for Diagram SVG.
             */
            getViewPort: function () {
                var viewPortHeight = $(this.d3svg.node()).height(),
                    viewPortWidth = $(this.d3svg.node()).width();
                return {w: viewPortWidth, h: viewPortHeight};
            },

            /**
             * Gets the SVG Viewbox attribute.
             *
             * @returns {} View Box for Diagram SVG.
             */
            getViewBox: function () {
                this.panAndZoom.getViewBox();
            },

            /**
             * Sets the SVG Viewbox attribute.
             *
             * @param {number} x min X axis of the viewbox.
             * @param {number} y min y axis of the viewbox.
             * @param {number} w width of the viewbox.
             * @param {number} h height of the viewbox
             * @param {string} [aspectRatio] value for preserveAspectRatio attribute
             */
            setViewBox: function (x, y, w, h, aspectRatio) {
                this.panAndZoom.setViewBox(x, y, w, h, 300);
                if(aspectRatio){
                    this.d3svg.attr("preserveAspectRatio", aspectRatio);
                }
            },

            /**
             * Scale the Diagram SVG.
             *
             * @param {number} sx scale to transform X axis.
             * @param {number} sy scale to transform Y axis.
             */
            scale: function (sx, sy) {
                this.d3el.attr("transform", "scale( " + sx + ", " + sy + " )");
            },

            /**
             * Covert a point in client viewport Coordinates to svg user space Coordinates.
             * @param {Point} point a point in client viewport Coordinates
             */
            toViewBoxCoordinates: function (point) {
                var pt = this.d3svg.node().createSVGPoint();
                pt.x = point.x();
                pt.y = point.y();
                pt = pt.matrixTransform(this.d3svg.node().getScreenCTM().inverse());
                return new DiagramCore.Models.Point({x: pt.x, y: pt.y});
            },

            addContainableProcessorElement: function (processor, center) {
                var containableProcessorElem = new ContainableProcessorElement();
                containableProcessorElem.type = 'ContainableProcessorElement';
                processor.containableProcessorElements().add(containableProcessorElem);
            },
            createDropHandler: function(serviceView){
                function dropHandler(event, ui) {
                    // Check for invalid drops on endpoints
                    if (true) {
                        var newDraggedElem = $(ui.draggable).clone();
                        var txt = serviceView.model;
                        var id = ui.draggable.context.lastChild.id;
                        var position = new DiagramCore.Models.Point({x: ui.offset.left, y: ui.offset.top});
                        //convert drop position to relative svg coordinates
                        position = serviceView.toViewBoxCoordinates(position);

                        if (Processors.manipulators[id] && txt.selectedNode) {
                            //manipulators are unit processors
                            var processor = txt.selectedNode.createProcessor(
                                Processors.manipulators[id].title,
                                position,
                                Processors.manipulators[id].id,
                                {
                                    type: Processors.manipulators[id].type || "UnitProcessor",
                                    initMethod: Processors.manipulators[id].init,
                                    editable: Processors.manipulators[id].editable,
                                    deletable: Processors.manipulators[id].deletable,
                                    hasOutputConnection : Processors.manipulators[id].hasOutputConnection,
                                    messageLinkType : Processors.manipulators[id].messageLinkType
                                },
                                {colour: Processors.manipulators[id].colour},
                                Processors.manipulators[id].parameters,
                                Processors.manipulators[id].utils,
                                undefined,
                                Processors.manipulators[id].width,
                                Processors.manipulators[id].height,
                                txt
                            );

                            if(typeof Processors.manipulators[id].init !== "undefined") {
                                Processors.manipulators[id].init(txt, processor);
                            }
                            txt.selectedNode.addChild(processor);

                            serviceView.render();
                        } else if (Processors.flowControllers[id] && txt.selectedNode) {
                            var processor = txt.selectedNode.createProcessor(
                                Processors.flowControllers[id].title,
                                position,
                                Processors.flowControllers[id].id,
                                {
                                    type: Processors.flowControllers[id].type,
                                    initMethod: Processors.flowControllers[id].init
                                },
                                {colour: Processors.flowControllers[id].colour},
                                Processors.flowControllers[id].parameters,
                                Processors.flowControllers[id].utils,
                                undefined,
                                undefined,
                                undefined,
                                txt
                            );
                            if (Processors.flowControllers[id].type == "ComplexProcessor") {
                                (Processors.flowControllers[id].containableElements).forEach(function (elm) {
                                    (elm.children).forEach(function (child) {
                                        var containableProcessorElem = new ContainableProcessorElement();
                                        containableProcessorElem.type = 'ContainableProcessorElement';
                                        containableProcessorElem.set('title', child.title);
                                        containableProcessorElem.set('utils', processor.get('utils'));
                                        containableProcessorElem.parent(processor);
                                        processor.containableProcessorElements().add(containableProcessorElem);

                                    });
                                });
                            }

                            txt.selectedNode.addChild(processor);

                            serviceView.render();
                        } else if (id == "EndPoint") {
                            var countOfEndpoints = txt.endpointLifeLineCounter();
                            ++countOfEndpoints;
                            serviceView.renderMainElement(id, countOfEndpoints, _.get(MainElements, 'lifelines.EndPoint'));
                            txt.endpointLifeLineCounter(countOfEndpoints);
                        } else if (id == "Resource") {
                            var countOfResources = txt.resourceLifeLineCounter();
                            //if no resource elements added to this tab view, as only one resource element is allowed in a tab
                            if (countOfResources === 0) {
                                ++countOfResources;
                                serviceView.renderMainElement(id, countOfResources,  _.get(MainElements, 'lifelines.Resource'));
                                txt.resourceLifeLineCounter(countOfResources);
                            }

                        } else if (id == "Source") {
                            var countOfSources = txt.sourceLifeLineCounter();
                            if (countOfSources === 0) {
                                ++countOfSources;
                                serviceView.renderMainElement(id, countOfSources,  _.get(MainElements, 'lifelines.Source'));
                                txt.sourceLifeLineCounter(countOfSources);
                            }
                        } else if (id == "Worker") {
                            var countOfWorkers = txt.workerLifeLineCounter();
                            countOfWorkers += 1;
                            serviceView.renderMainElement(id, countOfWorkers,  _.get(MainElements, 'lifelines.Worker'),
                                {utils: MainElements.lifelines.get('Worker').utils});
                            txt.workerLifeLineCounter(countOfWorkers);
                            this.render();
                        }
                    } //for invalid check
                }

                return dropHandler;
            },

            render: function () {
                //Remove previous diagram
                if (this.d3el) {
                    this.d3el.remove();
                } else {
                    // When re-rendering the same event handler do not need to re-register.
                    // Otherwise same function will call for multiple times.
                    this.model.on("addElement", this.onAddElement, this);
                    this.model.on("renderDiagram", this.renderDiagram);
                }

                var mainGroup = this.d3svg.draw.group(this.d3svg).attr("id", this.options.diagram.wrapperId)
                    .attr("width", "100%")
                    .attr("height", "100%");
                this.d3el = mainGroup;
                this.el = mainGroup.node();
                if(!this.isPreviewMode()) {
                    this.calculateViewBoxLimits();
                }
                this.htmlDiv = $(this.options.container);
                this.htmlDiv.droppable({
                    drop: this.createDropHandler(this),
                    tolerance: "pointer"
                });

                var lifeLineViews = [];

                for (var id in this.model.attributes.diagramSourceElements.models) {
                    if (this.model.attributes.diagramSourceElements.models[id] instanceof LifeLine) {
                        var lifeLine = this.model.attributes.diagramSourceElements.models[id];
                        var lifelineOpts = {
                            model: lifeLine,
                            serviceView: this,
                            class:  _.get(MainElements, 'lifelines.Source.class')
                        };
                        var lifeLineView = new LifeLineView(lifelineOpts);
                        lifeLineViews.push(lifeLineView);
                    }
                }

                for (var id in this.model.attributes.diagramResourceElements.models) {
                    if (this.model.attributes.diagramResourceElements.models[id] instanceof LifeLine) {
                        var lifeLine = this.model.attributes.diagramResourceElements.models[id];
                        var lifelineOpts = {
                            model: lifeLine,
                            serviceView: this,
                            class:  _.get(MainElements, 'lifelines.Resource.class')
                        };
                        var lifeLineView = new LifeLineView(lifelineOpts);
                        lifeLineViews.push(lifeLineView);
                    }
                }

                for (var id in this.model.attributes.diagramEndpointElements.models) {
                    if (this.model.attributes.diagramEndpointElements.models[id] instanceof LifeLine) {
                        var lifeLine = this.model.attributes.diagramEndpointElements.models[id];
                        var lifelineOpts = {
                            model: lifeLine,
                            serviceView: this,
                            class:  _.get(MainElements, 'lifelines.Endpoint.class')
                        };
                        var lifeLineView = new LifeLineView(lifelineOpts);
                        lifeLineViews.push(lifeLineView);
                    }
                }

                if (!_.isUndefined(this.model.attributes.diagramWorkerElements)) {
                    for (var id in this.model.attributes.diagramWorkerElements.models) {
                        if (this.model.attributes.diagramWorkerElements.models[id] instanceof LifeLine) {
                            lifeLine = this.model.attributes.diagramWorkerElements.models[id];
                            var lifelineOpts = {
                                model: lifeLine,
                                serviceView: this,
                                class:  _.get(MainElements, 'lifelines.Worker.class')
                            };
                            var lifeLineView = new LifeLineView(lifelineOpts);
                            lifeLineViews.push(lifeLineView);
                        }
                    }
                }

                lifeLineViews.forEach(function(lifeLineView){
                    lifeLineView.render();
                });

                lifeLineViews.forEach(function(lifeLineView){
                    lifeLineView.renderProcessors();
                });

                lifeLineViews.forEach(function(lifeLineView){
                    lifeLineView.renderMessages();
                });

                this.trigger("renderCompleted", this.d3svg.node());
                return mainGroup;
            },

            addInitialElements: function(){

                var centerPoint = utils.createPoint(100, 50);
                var type = "Source";
                var lifeLineDef = _.get(MainElements, 'lifelines.Source');

                var resourceCenterPoint = utils.createPoint(380, 50);
                var resourceType = "Resource";
                var resourceLifeLineDef =_.get(MainElements, 'lifelines.Resource');

                var resourceLifeline = createLifeLine("Resource", resourceCenterPoint, resourceLifeLineDef.class, resourceLifeLineDef.utils,
                    resourceLifeLineDef.parameters, resourceLifeLineDef.textModel, resourceType, resourceLifeLineDef);

                this.model.addElement(resourceLifeline, {class: MainElements.lifelines.Resource.class });
                this.model.resourceLifeLineCounter(1);

                var lifeline = createLifeLine("Source", centerPoint, lifeLineDef.class, lifeLineDef.utils,
                    lifeLineDef.parameters, lifeLineDef.textModel, type, lifeLineDef);
                this.model.addElement(lifeline, {class: MainElements.lifelines.Source.class });
                this.model.sourceLifeLineCounter(1);

                var position =  utils.createPoint(0,0);
                var processor = new ProcessorFactory(Processors.actions.StartAction.title,
                    position,
                    Processors.actions.StartAction.type,
                    {
                        type: Processors.actions.StartAction.type,
                        initMethod: Processors.actions.StartAction.init
                    },
                    {
                        colour: Processors.actions.StartAction.colour
                    },
                    Processors.actions.StartAction.parameters,
                    Processors.actions.StartAction.utils,
                    Processors.actions.StartAction.textModel,
                    Processors.actions.StartAction.width,
                    Processors.actions.StartAction.height);

                resourceLifeline.addChild(processor);

                this.addInitArrow(lifeline,resourceLifeline);

            },

            shiftEndpointsRight: function () {
                var txt = this.model;
                var numberOfEndpointElements = txt.attributes.diagramEndpointElements.length;

                var halfWidth = 0;
                txt.attributes.diagramEndpointElements.models.forEach(function (endpoint) {
                    halfWidth = endpoint.rightLowerCorner().x - endpoint.get('centerPoint').x();
                    endpoint.setX(endpoint.get('centerPoint').x() + halfWidth + 115);
                    endpoint.rightLowerCorner().x = endpoint.rightLowerCorner().x + halfWidth + 115;
                });
            },

            renderMainElement: function (lifelineName, counter, lifeLineDef) {
                var numberOfResourceElements = this.model.attributes.diagramResourceElements.length;
                var numberOfEndpointElements = this.model.attributes.diagramEndpointElements.length;
                var numberOfWorkerElements = this.model.attributes.diagramWorkerElements.length;
                var centerPoint;
                var type;

                // All the lifelines are drawn based on the assumption of, first appear the source, then Resource,
                // Then Workers in order they are adding and at last endpoints in the order they are adding

                // In order to make the logic clear both ENDPOINT and the WORKER checks are enclosed seperately without
                // Merging both together in to one, for future reference
                if(lifelineName == "Source") {
                    centerPoint = utils.createPoint(200, 50);
                    type = "Source";
                } else if (lifelineName == "Resource") {
                    centerPoint = utils.createPoint(380, 50);
                    type = "Resource";
                } else if (lifelineName == "EndPoint") {
                    type = "EndPoint";
                    if (numberOfEndpointElements > 0) {
                        var lastEpLifeLine = this.model.attributes.diagramEndpointElements.models[numberOfEndpointElements - 1];
                        var rightCorner = lastEpLifeLine.get('centerPoint').x() + lastEpLifeLine.get('textModel').get('dynamicRectWidth')/2;
                        centerPoint = utils.createPoint(rightCorner + 115, 50);
                    } else if (numberOfWorkerElements > 0) {
                        var lastWorkerLifeLine = this.model.attributes.diagramWorkerElements.models[numberOfWorkerElements - 1];
                        var rightCorner = lastWorkerLifeLine.get('centerPoint').x() + lastWorkerLifeLine.get('textModel').get('dynamicRectWidth')/2;
                        centerPoint = utils.createPoint(rightCorner + 115, 50);
                    } else {
                        var resourceLifeLine = this.model.attributes.diagramResourceElements.models[numberOfResourceElements - 1];
                        var rightCorner = resourceLifeLine.get('centerPoint').x() + resourceLifeLine.get('textModel').get('dynamicRectWidth')/2;
                        centerPoint = utils.createPoint(rightCorner + 115, 50);
                    }
                } else if (lifelineName == "Worker") {
                    type = "Worker";
                    if (numberOfEndpointElements > 0) {
                        var firstEpLifeLine = this.model.attributes.diagramEndpointElements.models[0];
                        centerPoint = utils.createPoint(firstEpLifeLine.get('centerPoint').x(), 50);
                        // Shift the existing Endpoints
                        this.shiftEndpointsRight();
                    } else if (numberOfWorkerElements > 0) {
                        var lastWorkerLifeLine = this.model.attributes.diagramWorkerElements.models[numberOfWorkerElements - 1];
                        centerPoint = utils.createPoint(lastWorkerLifeLine.rightLowerCorner().x + 115, 50);
                        // Shift existing endpoints
                        this.shiftEndpointsRight();
                    } else {
                        var resourceLifeLine = this.model.attributes.diagramResourceElements.models[numberOfResourceElements - 1];
                        centerPoint = utils.createPoint(resourceLifeLine.rightLowerCorner().x + 115, 50);
                    }
                }

                var title = lifelineName;
                if(lifelineName == "EndPoint" || lifelineName == "Worker") {
                    title += counter;
                }
                var lifeline = createLifeLine(title, centerPoint, lifeLineDef.class, lifeLineDef.utils,
                    lifeLineDef.parameters, lifeLineDef.textModel, type, lifeLineDef);
                this.model.addElement(lifeline);
                this.render();
            },

            renderDiagram: function () {
                defaultView.render();
            },
            onRenderForCurrentDiagram: function (element, opts) {
                this.renderViewForElement(element, opts);
            },

            onAddElement: function (element, opts) {
                this.renderViewForElement(element, opts);
            },

            renderViewForElement: function (element, renderOpts) {
            },

            gridWidth: function () {
                return this.options.diagram.grid.width;
            },

            gridHeight: function () {
                return this.options.diagram.grid.height;
            },

            onMessageDrawEnd: function (sourceModel, sourcePoint, destinationPoint) {

                var destinationModel = null;
                if (this.model.destinationLifeLine) {
                    destinationModel = this.model.destinationLifeLine;
                    this.model.destinationLifeLine = null;
                } else if (this.model.destinationProcessor) {
                    destinationModel = this.model.destinationProcessor;
                    this.model.destinationProcessor = null;
                }

                if (destinationModel) {
                    if (sourceModel.canConnect(destinationModel)) {
                        var messageOptionsInbound = {'class': 'messagePoint', 'direction': 'inbound'};
                        var messageOptionsOutbound = {'class': 'messagePoint', 'direction': 'outbound'};
                        sourceModel.outputConnector(sourcePoint);
                        destinationModel.addChild(destinationPoint, messageOptionsInbound);
                    }
                }
                this.render();
            },

            onMessageDrawStart: function (sourceModel, startPoint, calcNewStartPoint, onMessageDrawEndCallback) {

                var serviceView = this;

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
                    if (!_.isUndefined(calcNewStartPoint)) {
                        var newSP = calcNewStartPoint(m[0], m[1]);
                        line.attr("x1", newSP.x);
                        line.attr("y1", newSP.y);
                    }
                });

                this.d3svg.on("mouseup", function () {
                    // unbind current listeners
                    serviceView.d3svg.on("mousemove", null);
                    serviceView.d3svg.on("mouseup", null);
                    var startPoint = new DiagramCore.Models.Point({x: line.attr("x1"), y: line.attr("y1")}),
                        endpoint = new DiagramCore.Models.Point({x: line.attr("x2"), y: line.attr("y2")});
                    line.remove();

                    var sourcePoint = new MessagePoint({
                        model: {type: "messagePoint"},
                        x: startPoint.x(),
                        y: startPoint.y(),
                        direction: "outbound"
                    });
                    var destinationPoint = new MessagePoint({
                        model: {type: "messagePoint"},
                        x: endpoint.x(),
                        y: endpoint.y(),
                        direction: "inbound"
                    });
                    var messageLink = new MessageLink({
                        source: sourcePoint,
                        destination: destinationPoint,
                        type : sourceModel.model.messageLinkType
                    });
                    serviceView.model.trigger("messageDrawEnd", sourceModel, sourcePoint, destinationPoint);
                });
            },

            addInitArrow:function(source,destination){
                var centerS = utils.createPoint(200, 125);
                var centerR = utils.createPoint(315, 125);
                var sourcePoint = new MessagePoint({
                    model: {type: "messagePoint"},
                    x: centerS.x(),
                    y: centerS.y(),
                    direction: "outbound"
                });
                var destinationPoint = new MessagePoint({
                    model: {type: "messagePoint"},
                    x: centerR.x(),
                    y: centerR.y(),
                    direction: "inbound"
                });
                var messageLink = new MessageLink({
                    source: sourcePoint,
                    destination: destinationPoint,
                    priority: destinationPoint,
                    // type one is out only
                    type : 1
                });
                var messageOptionsInbound = {'class': 'messagePoint', 'direction': 'inbound'};
                var messageOptionsOutbound = {'class': 'messagePoint', 'direction': 'outbound'};
                source.addChild(sourcePoint, messageOptionsOutbound);
                destination.inputConnector(destinationPoint);
            }
        });

    return ServiceView;
});

