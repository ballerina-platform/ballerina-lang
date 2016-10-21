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
                var newX = Math.round(point.x() / defaultView.gridWidth()) * defaultView.gridWidth();
                var newY = Math.round(point.y() / defaultView.gridHeight()) * defaultView.gridHeight();
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
// Holds the view for all tab elements
    var TabListView = Backbone.View.extend({
        //<ul> element for tabs
        el: '#tabList',
        initialize: function () {


        },


        events: {
            "click .add-resource": "addResourceTab"
        },
        render: function (model) {
            var tabView = new TabView({model: model});
            tabView.render();
            var t = $(this.el)[0].childNodes[1].childNodes[0];
            $(t).closest("li").before(tabView.el);
           // $(this.el).append(tabView.el);

        },
        //function to fire when a new resource tab button is clicked
        addResourceTab: function (e) {
            // required to clean previous views
            this.undelegateEvents();
            e.preventDefault();
            //create Unique id for each tab
            var id =  Math.random().toString(36).substr(2, 9);
            var hrefId = '#seq_' + id;
            var resourceId = 'seq_' + id;
            //create new Tab based resource model:todo: change resource title
            var resourceModel = new Diagrams.Models.Tab({
                resourceId: resourceId,
                hrefId: hrefId,
                resourceTitle: "New Resource" ,
                createdTab: false
            });

            //remove propety pane when adding a new tab
            if (diagram.previousDeleteIconGroup) {
                diagram.previousDeleteIconGroup.classed("circle-hide", true);
                diagram.previousDeleteIconGroup.classed("circle-show", false);
                diagram.previousPropertyIconGroup.classed("circle-hide", true);
                diagram.previousPropertyIconGroup.classed("circle-show", false);
            }
            diagram.previousDeleteIconGroup = null;
            diagram.currentDeleteIconGroup = null;
            diagram.previousPropertyIconGroup = null;
            diagram.currentPropertyIconGroup = null;
            if (propertyPane) {
                propertyPane.destroy();
            }

            var nextTabListView = new Diagrams.Views.TabListView({model: resourceModel});

            nextTabListView.render(resourceModel);
            //create new diagram object for the tab
            var diagramObj = new Diagrams.Models.Diagram({});
            resourceModel.addDiagramForTab(diagramObj);
            //Activating tab on creation itself
                $('.tabList a[href="#' + resourceId + '"]').tab('show');
            var dgModel = resourceModel.getDiagramOfTab(resourceModel.attributes.diagramForTab.models[0].cid);
            dgModel.CurrentDiagram(dgModel);
            var svgUId = resourceId + "4";
            var options = {selector: hrefId, wrapperId: svgUId};
            // get the current diagram view for the tab
            var currentView = dgModel.createDiagramView(dgModel, options);
            // add diagramModel
            var preview = new DiagramOutlineView({mainView: currentView});
            preview.render();
            resourceModel.preview(preview);
            // set current tab's diagram view as default view
            currentView.currentDiagramView(currentView);
            resourceModel.setDiagramViewForTab(currentView);
            // mark tab as visited
            resourceModel.setSelectedTab();

        }

    });
// View for each tab item (<li> item)
    var TabView = Backbone.View.extend({
        //create <li> element to hold each resource tab
        tagName: "li",
        className: "series ",
        template: _.template($('#resourceTabsTemplate').html()),
        initialize: function () {


        },
        events: {
            "click a": "clickedResourceTab",
            'click .remove-resource': 'removeResourceTab',
        },
        // on click of added tab
        clickedResourceTab: function (e) {
            e.preventDefault();
            currentTabId = this.model.get("hrefId");
            var currentTab = this.model;
            //Unique Id created for the svg element where elements can be drawn
            var svgUId = this.model.get("resourceId") + "4";
            //empty property pane when clicked on a tab
            if (diagram.previousDeleteIconGroup) {
                diagram.previousDeleteIconGroup.classed("circle-hide", true);
                diagram.previousDeleteIconGroup.classed("circle-show", false);
                diagram.previousPropertyIconGroup.classed("circle-hide", true);
                diagram.previousPropertyIconGroup.classed("circle-show", false);
            }
            diagram.previousDeleteIconGroup = null;
            diagram.currentDeleteIconGroup = null;
            diagram.previousPropertyIconGroup = null;
            diagram.currentPropertyIconGroup = null;
            if (propertyPane) {
                propertyPane.destroy();
            }
            //first time click on the tab
            if (this.model.attributes.createdTab === false) {
                // get the diagram model for this tab
                // var dgModel = this.model.getDiagramOfTab(currentTab.attributes.diagramForTab.models[0].cid);
                // dgModel.CurrentDiagram(dgModel);
                // var options = {selector: currentTabId, wrapperId: svgUId};
                // // get the current diagram view for the tab
                // var currentView = dgModel.createDiagramView(dgModel, options);
                // // set current tab's diagram view as default view
                // currentView.currentDiagramView(currentView);
                // this.model.setDiagramViewForTab(currentView);
                // // mark tab as visited
                // this.model.setSelectedTab();
            }
            else {
                // not the first time click on the given tab
                var dgViewToRender = this.model.viewObj;
                dgViewToRender.currentDiagramView(dgViewToRender);
                //Setting diagram model for lifeline message drawing context
                lifeLineOptions.diagram = defaultView.model;
                currentTab.preview().render();
            }

        },
        //Remove tab and tab content on 'remove' button
        removeResourceTab: function (e) {
            e.preventDefault();
            var anchor = $(e.currentTarget).siblings('a');
            $(anchor.attr('href')).remove();
            $(e.currentTarget).parent().remove();
        },

        render: function () {
            var html = this.template(this.model.attributes);
            $(this.el).append(html);
            var tabContent = new TabContentView({model: this.model});
            tabContent.render();

        }
    });
//View to create matching tab content div for a tab
    var TabContentView = Backbone.View.extend({
        //Adding created template to editor div
        el: ".resource-content",
        template: _.template($('#resourceContentTemplate').html()),
        initialize: function () {


        },
        render: function () {
            var html = this.template(this.model.attributes);
            $(this.el).append(html);
        }

    });

    // View to represent Diagram Preview.
    var DiagramOutlineView = Backbone.View.extend(
    /** @lends DiagramOutlineView.prototype */
    {
        /**
         * @augments Backbone.View
         * @constructs
         * @class DiagramOutlineView Represents an outline view (a small preview) of the diagram
         * @param {Object} options Rendering options for the view - Selector for container and Main DiagramView
         */
        initialize: function (options) {
            if(_.isUndefined(options.mainView)){
                throw "mainView is not set."
            }
            options.containerSelector = options.containerSelector || ".preview-container";
            this.$el = $(options.containerSelector);
            this.mainView = options.mainView;
            this.mainView.on("renderCompleted", this.render, this);
            this.width = options.width || "100%";
            this.height = options.height || "160px";
            this.fitToCanvasOpts =  options.fitToCanvasOpts || {};
        },

        /**
         * Renders preview for the given diagram.
         *
         * @param {SVGSVGElement} [mainSVG] SVG element to be cloned for preview.
         */
        render: function (mainSVG) {
            if(!mainSVG){
                var mainSVG = this.mainView.d3svg.node();
            }

            // clone SVG for preview
            var previewSVG = $(mainSVG).clone();
            // get
            var limits = this.mainView.panAndZoom.limits;
            var padding = this.mainView.options.diagram.padding;
            // set viewbox to fit in all content
            d3.select(previewSVG.get((0)))
                .attr("width", this.width)
                .attr("height", this.height)
                // get current limits and add a little offset to make red box visible at zoom out upper boundary
                .attr("viewBox", (limits.x) + " " + (limits.y ) + " "
                    + ((limits.x2 - limits.x)) + " " + ((limits.y2 - limits.y)))
                .attr("preserveAspectRatio", "xMinYMin meet");

            // unset current preview rendered on div
            this.$el.empty();

            // set new preview
            var previewContainer = $("<div></div>");
            previewContainer.attr("class", "diagram-preview");
            this.$el.append(previewContainer);
            previewContainer.append(previewSVG);

            // create controls
            var controlsContainer = $("<div></div>");
            controlsContainer.attr("class", "controls-container");
            this.$el.append(controlsContainer);

            var preview = this;

            var fitToCanvasControl =  $("<span class='glyphicon glyphicon-fullscreen fit-to-area-btn' aria-hidden=true'></span>");
            controlsContainer.append(fitToCanvasControl);
            fitToCanvasControl.click(function(evt){
                preview.mainView.setViewBox(
                    preview.mainView.panAndZoom.limits.x,
                    preview.mainView.panAndZoom.limits.y,
                    preview.mainView.panAndZoom.limits.x2 - preview.mainView.panAndZoom.limits.x,
                    preview.mainView.panAndZoom.limits.y2 - preview.mainView.panAndZoom.limits.y);
            });

            // create zoom range controller
            var zoomRangeController = $("<div></div>");
            controlsContainer.append(zoomRangeController);
            zoomRangeController.attr("class", "zoom-slider");

            this.slider = zoomRangeController.slider({
                min: 1,
                max: 100,
                step: 1,
                slide: function( event, ui ) {
                    var limitWidth = limits.x2 -limits.x;
                    var newWidth =  limitWidth * ((100 - ui.value)/100);
                    var newHeight = newWidth * (1/preview.mainView.getCurrentAspectRatio());
                    var currentVB = preview.mainView.panAndZoom.getViewBox();
                    var newX = currentVB.x + ((currentVB.width - newWidth)/2);
                    var newY = currentVB.y + ((currentVB.height - newHeight)/2);
                    preview.mainView.setViewBox(newX, newY, newWidth, newHeight);
                }
            });

            //init pan and zoom area marker box inside preview
            var previewWrapperGroup = d3.select(previewSVG.find("g").first()[0]);
            var panZoomMarkerRect = previewWrapperGroup.append('rect');

            var checkLimits = function(viewBox, limits) {
                var limitsHeight, limitsWidth, reductionFactor, vb;
                vb = $.extend({}, viewBox);
                limitsWidth = Math.abs(limits.x2 - limits.x);
                limitsHeight = Math.abs(limits.y2 - limits.y);
                if (vb.width > limitsWidth) {
                    if (vb.height > limitsHeight) {
                        if (limitsWidth > limitsHeight) {
                            reductionFactor = limitsHeight / vb.height;
                            vb.height = limitsHeight;
                            vb.width = vb.width * reductionFactor;
                        } else {
                            reductionFactor = limitsWidth / vb.width;
                            vb.width = limitsWidth;
                            vb.height = vb.height * reductionFactor;
                        }
                    } else {
                        reductionFactor = limitsWidth / vb.width;
                        vb.width = limitsWidth;
                        vb.height = vb.height * reductionFactor;
                    }
                } else if (vb.height > limitsHeight) {
                    reductionFactor = limitsHeight / vb.height;
                    vb.height = limitsHeight;
                    vb.width = vb.width * reductionFactor;
                }
                if (vb.x < limits.x) {
                    vb.x = limits.x;
                }
                if (vb.y < limits.y) {
                    vb.y = limits.y;
                }
                if (vb.x + vb.width > limits.x2) {
                    vb.x = limits.x2 - vb.width;
                }
                if (vb.y + vb.height > limits.y2) {
                    vb.y = limits.y2 - vb.height;
                }
                return vb;
            };
            var mainViewBox = preview.mainView.panAndZoom.getViewBox();
            panZoomMarkerRect
                .attr("x", mainViewBox.x)
                .attr("y", mainViewBox.y)
                .attr("width", mainViewBox.width)
                .attr("height", mainViewBox.height)
                .attr("class", "pan-zoom-marker");

            var panZoomMarkerRectDrag = d3.drag()
                .on("start", function () {

                })
                .on("drag", function () {
                    var evt = d3.event;
                    var newX = parseFloat(panZoomMarkerRect.attr("x")) + evt.dx;
                    var newY = parseFloat(panZoomMarkerRect.attr("y")) + evt.dy;
                    var oldW = parseFloat(panZoomMarkerRect.attr("width")) ;
                    var oldH = parseFloat(panZoomMarkerRect.attr("height"));

                    var approvedVB = checkLimits({x: newX, y: newY, width: oldW, height: oldH}, limits);
                    panZoomMarkerRect.attr("x", approvedVB.x);
                    panZoomMarkerRect.attr("y", approvedVB.y);
                    preview.mainView.setViewBox(approvedVB.x, approvedVB.y, oldW, oldH);
                })
                .on("end", function () {

                });
            panZoomMarkerRect.call(panZoomMarkerRectDrag);

            if(this.mainView.isEmpty()){
                panZoomMarkerRect.attr("class", "pan-zoom-marker hidden");
            }

            this.mainView.on("viewBoxChange", function(newViewBox, animationTime){
                panZoomMarkerRect.attr("x", newViewBox.x)
                    .attr("y", newViewBox.y)
                    .attr("width",  newViewBox.width)
                    .attr("height", newViewBox.height);

                var sliderVal = 100 - ((newViewBox.width/((limits.x2 - limits.x) - padding)) * 100);
                this.slider.slider("option", "value", sliderVal);

            }, this);

            var resetZoomToDefaultControl =  $("<span class='glyphicon glyphicon-screenshot reset-zoom-btn' aria-hidden=true'></span>");
            controlsContainer.append(resetZoomToDefaultControl);
            resetZoomToDefaultControl.click(function(evt){
                var defaultViewBox = preview.mainView.panAndZoom.initialViewBox;
                preview.mainView.setViewBox(defaultViewBox.x, defaultViewBox.y,
                    defaultViewBox.width, defaultViewBox.height);
            });

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
                opts.diagram.padding =  opts.diagram.padding || 50;
                opts.diagram.viewBoxWidth =  opts.diagram.viewBoxWidth || 1000;
                opts.diagram.viewBoxHeight =  opts.diagram.viewBoxHeight || 1000;

                opts.diagram.class = opts.diagram.class || "diagram";
                opts.diagram.selector = opts.diagram.selector || ".diagram";
                opts.diagram.wrapper = opts.diagram.wrapper || {};
                // CHANGED
                opts.diagram.wrapperId = opts.wrapperId || "diagramWrapper";
                opts.diagram.grid = opts.diagram.grid || {};
                opts.diagram.grid.height = opts.diagram.grid.height || 25;
                opts.diagram.grid.width = opts.diagram.grid.width || 25;
                this.options = opts;
                var defaultView = {};
                this.model.on("messageDrawStart", this.onMessageDrawStart, this);
                this.model.on("messageDrawEnd", this.onMessageDrawEnd, this);

                var container = d3.select(this.options.selector);
                if (_.isUndefined(container)) {
                    throw this.options.selector + " is not a valid query selector for container";
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
                svg.on("click", this.onClickDiagram);

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
                        width: parseFloat(getComputedStyle(this.d3svg.node()).width) || this.options.diagram.viewBoxWidth,

                        // the height of the viewBox
                        height: parseFloat(getComputedStyle(this.d3svg.node()).height) || this.options.diagram.viewBoxHeight
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

                diagram.propertyWindow = true;
                propertySVG.draw.form(propertySVG, parameters, propertyPaneSchema, rect, options.y);
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
               return (this.el.childNodes.length === 0 ) ? true : false;
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
                        console.log(newlimits);
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

                return new GeoCore.Models.Point({x: pt.x, y: pt.y});
            },


            addContainableProcessorElement: function (processor, center) {
                var containableProcessorElem = new SequenceD.Models.ContainableProcessorElement(lifeLineOptions);
                processor.containableProcessorElements().add(containableProcessorElem);
            },
            currentDiagramView: function (view1) {
                if (_.isUndefined(view1)) {
                    return defaultView;
                } else {
                    defaultView = view1;

                }
            },
            handleDropEvent: function (event, ui) {
                var newDraggedElem = $(ui.draggable).clone();
                var txt = defaultView.model;
                var id = ui.draggable.context.lastChild.id;
                var position = new GeoCore.Models.Point({x: ui.offset.left.x, y: ui.offset.top});
                //convert drop position to relative svg coordinates
                position = defaultView.toViewBoxCoordinates(position);
                if (propertyPane) {
                    propertyPane.destroy();
                }

                if (Processors.manipulators[id] && txt.selectedNode) {
                    //manipulators are unit processors
                    var processor = txt.selectedNode.createProcessor(
                        Processors.manipulators[id].title,
                        position,
                        Processors.manipulators[id].id,
                        {
                            type: Processors.manipulators[id].type || "UnitProcessor",
                            initMethod: Processors.manipulators[id].init
                        },
                        {colour: Processors.manipulators[id].colour},
                        {parameters: Processors.manipulators[id].parameters},
                        {getMySubTree: Processors.manipulators[id].getMySubTree}
                    );
                    txt.selectedNode.addChild(processor);
                    defaultView.render();
                } else if (Processors.flowControllers[id] && txt.selectedNode) {
                    var processor = txt.selectedNode.createProcessor(
                        Processors.flowControllers[id].title,
                        position,
                        Processors.flowControllers[id].id,
                        {type: Processors.flowControllers[id].type, initMethod: Processors.flowControllers[id].init},
                        {colour: Processors.flowControllers[id].colour},
                        {parameters: Processors.flowControllers[id].parameters},
                        {getMySubTree: Processors.flowControllers[id].getMySubTree}
                    );
                    txt.selectedNode.addChild(processor);

                    if (Processors.flowControllers[id].type == "ComplexProcessor") {
                        (Processors.flowControllers[id].containableElements).forEach(function(elm) {
                            var containableProcessorElem = new SequenceD.Models.ContainableProcessorElement(lifeLineOptions);
                            containableProcessorElem.set('title', elm);
                            containableProcessorElem.parent(processor);
                            processor.containableProcessorElements().add(containableProcessorElem);
                        });
                    }


                    defaultView.render();
                } else if (id == "EndPoint") {
                    var countOfEndpoints = txt.endpointLifeLineCounter();
                    ++countOfEndpoints;
                    defaultView.renderMainElement(id, countOfEndpoints, MainElements.lifelines.EndPointLifeline);
                    txt.endpointLifeLineCounter(countOfEndpoints);

                } else if (id == "Resource") {
                    var countOfResources = txt.resourceLifeLineCounter();
                    //if no resource elements added to this tab view, as only one resource element is allowed in a tab
                    if (countOfResources === 0) {
                        ++countOfResources;
                        defaultView.renderMainElement(id, countOfResources, MainElements.lifelines.ResourceLifeline);
                        txt.resourceLifeLineCounter(countOfResources);
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

                var mainGroup = this.d3svg.draw.group(this.d3svg).attr("id", this.options.diagram.wrapperId)
                    .attr("width", "100%")
                    .attr("height", "100%");
                this.d3el = mainGroup;
                this.el = mainGroup.node();
                this.calculateViewBoxLimits();
                this.htmlDiv = $(this.options.selector);
                this.htmlDiv.droppable({
                    drop: this.handleDropEvent,
                    tolerance: "pointer"
                });


                for (var id in this.model.attributes.diagramResourceElements.models) {
                    if (this.model.attributes.diagramResourceElements.models[id] instanceof SequenceD.Models.LifeLine) {
                        var lifeLine = this.model.attributes.diagramResourceElements.models[id];
                        var lifeLineView = new SequenceD.Views.LifeLineView({
                            model: lifeLine,
                            options: lifeLineOptions
                        });
                        diagramViewElements[diagramViewElements.length] = (lifeLineView);
                        var rectColour = this.model.attributes.diagramResourceElements.models[id].attributes.colour;
                        lifeLineView.render("#" + this.options.diagram.wrapperId, "processors", rectColour);
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
                        lifeLineView.render("#" + this.options.diagram.wrapperId, "processors", rectColour);
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
                        lifeLineView.render("#" + this.options.diagram.wrapperId, "messages", rectColour);
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
                        lifeLineView.render("#" + this.options.diagram.wrapperId, "messages", rectColour);
                    }
                }

                this.trigger("renderCompleted", this.d3svg.node());
                return mainGroup;
            },

            renderMainElement: function (lifelineName, counter, lifeLineDef) {
                var txt = this.model;
                var numberOfResourceElements = txt.attributes.diagramResourceElements.length;
                var numberOfEndpointElements = txt.attributes.diagramEndpointElements.length;
                var centerPoint;
                if (numberOfEndpointElements > 0) {
                    if (lifelineName == "Resource") {
                        centerPoint = createPoint(200, 50);
                        txt.attributes.diagramEndpointElements.each(function (model) {
                            var xVal = model.get('centerPoint').attributes.x;
                            model.get('centerPoint').move(180, 0);
                            model.setX(xVal + 180);
                            model.rightLowerConer({x: xVal + 245, y: 0});
                        });
                    } else {
                        var lastLifeLine = txt.attributes.diagramEndpointElements.models[numberOfEndpointElements - 1];
                        centerPoint = createPoint(lastLifeLine.rightLowerConer().x + 115, 50);
                    }
                } else {
                    if (numberOfResourceElements > 0) {
                        var lastLifeLine = txt.attributes.diagramResourceElements.models[numberOfResourceElements - 1];
                        centerPoint = createPoint(lastLifeLine.rightLowerConer().x + 115, 50);
                    } else {
                        //initial life line position
                        centerPoint = createPoint(200, 50);
                    }
                }
                var lifeline = createLifeLine(lifelineName + counter, centerPoint, lifeLineDef.class);
                lifeline.leftUpperConer({x: centerPoint.attributes.x - 65, y: centerPoint.attributes.y - 15});
                lifeline.rightLowerConer({
                    x: centerPoint.attributes.x + 65,
                    y: centerPoint.attributes.y + 15 + lifeLineOptions.middleRect.height + lifeLineOptions.rect.heigh
                });
                lifeLineOptions.class = lifeLineDef.class;
                //SETTING TOP SVG ELEMENT IN OPTIONS To Draw messages
                lifeLineOptions.diagram = defaultView.model;
                txt.addElement(lifeline, lifeLineOptions);
                defaultView.render();
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

            onClickDiagram: function () {
                var txt = defaultView;
                if (txt.model.selected === true) {
                    diagram.previousDeleteIconGroup = null;
                    diagram.previousPropertyIconGroup = null;
                    txt.model.selected = false;
                    if(propertyPane) {
                        propertyPane.destroy();
                    }
                    
                } else if (!txt.model.selectedNode) {
                    if (selected.classList && selected.classList.contains("lifeline_selected")) {
                        selected.classList.toggle("lifeline_selected");
                    }
                    if (diagram.previousDeleteIconGroup) {
                        diagram.previousDeleteIconGroup.classed("circle-hide", true);
                        diagram.previousDeleteIconGroup.classed("circle-show", false);
                        diagram.previousPropertyIconGroup.classed("circle-hide", true);
                        diagram.previousPropertyIconGroup.classed("circle-show", false);
                        defaultView.render();
                    }

                    diagram.previousDeleteIconGroup = null;
                    diagram.currentDeleteIconGroup = null;
                    diagram.previousPropertyIconGroup = null;
                    diagram.currentPropertyIconGroup = null;
                    selected = '';
                    txt.model.selected = true;
                    if (propertyPane) {
                        propertyPane.destroy();
                    }
                    propertyPane = ppView.createPropertyPane(txt.model.getDefinitionSchema(),
                        txt.model.getDefinitionEditableProperties(), txt.model);
                }
            },

            renderViewForElement: function (element, renderOpts) {
                var view = Diagrams.Utils.createViewForModel(element, renderOpts);
                view.diagramView(this);
                view.render("#" + this.options.diagram.wrapperId);
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
                    if (!_.isEqual(sourceModel.cid, destinationModel.cid)) {
                        var messageOptionsInbound = {'class': 'messagePoint', 'direction': 'inbound'};
                        var messageOptionsOutbound = {'class': 'messagePoint', 'direction': 'outbound'};
                        sourceModel.addChild(sourcePoint, messageOptionsOutbound);
                        destinationModel.addChild(destinationPoint, messageOptionsInbound);
                    }
                }
                this.render();
            },

            onMessageDrawStart: function (sourceModel, startPoint, calcNewStartPoint, onMessageDrawEndCallback) {

                var diagView = defaultView;

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
                    diagView.d3svg.on("mousemove", null);
                    diagView.d3svg.on("mouseup", null);
                    var startPoint = new GeoCore.Models.Point({x: line.attr("x1"), y: line.attr("y1")}),
                        endpoint = new GeoCore.Models.Point({x: line.attr("x2"), y: line.attr("y2")});
                    line.remove();

                    var sourcePoint = new SequenceD.Models.MessagePoint({
                        model: {type: "messagePoint"},
                        x: startPoint.x(),
                        y: startPoint.y(),
                        direction: "outbound"
                    });
                    var destinationPoint = new SequenceD.Models.MessagePoint({
                        model: {type: "messagePoint"},
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
    views.TabListView = TabListView;
    views.TabView = TabView;
    views.DiagramOutlineView = DiagramOutlineView;

    diagrams.Views = views;
    return diagrams;

}(Diagrams || {}));
