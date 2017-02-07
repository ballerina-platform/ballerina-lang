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
define(['require', 'lodash', 'jquery', 'jsPlumb', 'dagre', 'alerts'], function (require, _, $, jsPlumb, dagre, alerts) {

    /**
     * Renderer constructor for TypeMapper
     * @param {object} onConnectionCallback call back function when connection made
     * @param {object} onDisconnectCallback call back function when connection removed
     * @param {object} typeConverterView Type Mapper View reference object
     * @constructor
     */
    var TypeMapperRenderer = function (onConnectionCallback, onDisconnectCallback, typeConverterView) {
        this.references = [];
        this.placeHolderName = "data-mapper-container-" + typeConverterView.getModel().id;
        this.idNameSeperator = "-";
        this.onConnection = onConnectionCallback;
        this.typeConverterView = typeConverterView;
        this.midpoint= 0.01;
        this.midpointVariance = 0.02;
        var self  = this;


        this.jsPlumbInstance = jsPlumb.getInstance({
            Connector :  [ "Flowchart", { midpoint: self.midpoint } ],
            Container :this.placeHolderName ,
            PaintStyle : {
                strokeWidth: 2,
                //todo : load colors from css
                stroke : "#3d3d3d",
                cssClass:"plumbConnect",
                outlineStroke: "#F7F7F7",
                outlineWidth: 2
            },
            EndpointStyle : { radius:1 },
            ConnectionOverlays: [
                [ "Arrow", {
                    location: 1,
                    visible:true,
                    width:11,
                    length:11
                } ]
            ]
        });

        var positionFunc = this.dagrePosition;
        var separator = this.idNameSeperator;
        var refObjects = this.references;
        var viewId = this.placeHolderName;
        var jsPlumbInst = this.jsPlumbInstance;

        this.jsPlumbInstance.bind('dblclick', function (connection, e) {
            var sourceParts = connection.sourceId.split(separator);
            var targetParts = connection.targetId.split(separator);
            var sourceId = sourceParts.slice(0, 6).join(separator);
            var targetId = targetParts.slice(0, 6).join(separator);

            var sourceRefObj;
            var targetRefObj;


            _.forEach(refObjects, function(obj) {
                if (obj.name == sourceId) {
                    sourceRefObj = obj.refObj;
                } else if (obj.name == targetId) {
                    targetRefObj = obj.refObj;
                }
            });

            var PropertyConnection = {
                sourceStruct: sourceParts[0],
                sourceProperty: sourceParts[6],
                sourceType: sourceParts[7],
                sourceReference: sourceRefObj,
                targetStruct: targetParts[0],
                targetProperty: targetParts[6],
                targetType: targetParts[7],
                targetReference: targetRefObj
            };

            self.midpoint= self.midpoint - self.midpointVariance;
            self.jsPlumbInstance.importDefaults({ Connector :  [ "Flowchart", { midpoint: self.midpoint }]});

            jsPlumbInst.detach(connection);
            positionFunc(viewId, jsPlumbInst);
            onDisconnectCallback(PropertyConnection);
        });

        this.jsPlumbInstance.bind('connection', function (info, ev) {
            positionFunc(viewId, jsPlumbInst);
        });
    };

    TypeMapperRenderer.prototype.constructor = TypeMapperRenderer;

    /**
     * Remove a struct from the mapper UI
     * @param {string} name identifier of the struct
     */
    TypeMapperRenderer.prototype.removeStruct = function (name) {
        var structId = name + this.idNameSeperator + this.typeConverterView.getModel().id;
        var structConns = $('div[id^="' + structId + '"]');
        var self = this;

        _.forEach(structConns, function(structCon) {
            if (_.includes(structCon.className, 'property')) {
                self.jsPlumbInstance.remove(structCon.id);
            }
        });

        $("#" + structId).remove();
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };

    /**
     * Add a connection arrow in the mapper UI
     * @param {object} connection connection object which specified source and target
     */
    TypeMapperRenderer.prototype.addConnection = function (connection) {
        this.jsPlumbInstance.connect({
            source: connection.sourceStruct + this.idNameSeperator + connection.sourceProperty
            + this.idNameSeperator + connection.sourceType,
            target: connection.targetStruct + this.idNameSeperator + connection.targetProperty
            + this.idNameSeperator + connection.targetType
        });
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };


    /**
     * Provides all the connections in mapped in the UI
     * @returns {Array}
     */
    TypeMapperRenderer.prototype.getConnections = function () {
        var connections = [];


        _.forEach(this.jsPlumbInstance.getConnections(), function(con) {
            var sourceParts = con.sourceId.split(this.idNameSeperator);
            var targetParts = con.targetId.split(this.idNameSeperator);
            var connection = {
                sourceStruct: sourceParts[0],
                sourceProperty: sourceParts[6],
                sourceType: sourceParts[7],
                targetStruct: targetParts[0],
                targetProperty: targetParts[6],
                targetType: targetParts[7]
            };
            connections.push(connection);
        });

        return connections;
    };

    /**
     * Add a source struct in the mapper UI
     * @param {object} struct definition with parameters to be mapped
     * @param {object} reference AST model reference
     */
    TypeMapperRenderer.prototype.addSourceStruct = function (struct, reference) {
        var self = this;
        struct.id = struct.name + this.idNameSeperator + this.typeConverterView.getModel().id;
        this.makeStruct(struct, 50, 50, reference);

        _.forEach(struct.properties, function(property) {
            self.addSourceProperty($('#' + struct.id), property.name, property.type);
        });

        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };

    /**
     * Add a target struct in the mapper UI
     * @param {object} struct definition with parameters to be mapped
     * @param {object} reference AST model reference
     */
    TypeMapperRenderer.prototype.addTargetStruct = function (struct, reference) {
        var self = this;
        struct.id = struct.name + this.idNameSeperator + this.typeConverterView.getModel().id;
        var placeHolderWidth = document.getElementById(this.placeHolderName).offsetWidth;
        var posY = placeHolderWidth - (placeHolderWidth / 4);
        this.makeStruct(struct, 50, posY, reference);

        _.forEach(struct.properties, function(property) {
            self.addTargetProperty($('#' + struct.id), property.name, property.type);
        });

        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };

    /**
     * Make generic struct div element in the UI
     * @param {object} struct definition with parameters to be mapped
     * @param {int} posX X position cordinate
     * @param {int} posY Y position cordinate
     * @param {object} reference
     */
    TypeMapperRenderer.prototype.makeStruct = function (struct, posX, posY, reference) {
        this.references.push({name: struct.id, refObj: reference});
        var newStruct = $('<div>').attr('id', struct.id).addClass('struct');

        var structName = $('<div>').addClass('struct-name').text(struct.name);
        newStruct.append(structName);

        newStruct.css({
            'top': posX,
            'left': posY
        });

        $("#" + this.placeHolderName).append(newStruct);
    };

    /**
     * Add a function in the mapper UI
     * @param {object} function definition with parameters to be mapped
     * @param {object} reference AST model reference
     */
    TypeMapperRenderer.prototype.addFunction = function (func, reference) {
        this.references.push({name: func.name, refObj: reference});
        var newFunc = $('<div>').attr('id', func.name).addClass('func');

        var funcName = $('<div>').addClass('struct-name').text(func.name);
        newFunc.append(funcName);

        newFunc.css({
            'top': 0,
            'left': 0
        });

        $("#" + this.placeHolderName).append(newFunc);

        var funcContent = $('#' + func.name);

        _.forEach(func.parameters, function(parameter) {
            this.addTargetProperty(funcContent, parameter.name,parameter.type);
        });

        this.addSourceProperty(funcContent, "output", func.returnType);
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);

    };

    /**
     * Make Property DIV element
     * @param {string} parentId identifier of the parent of the property
     * @param {string} name property name
     * @param {string} type property type
     * @returns {*|jQuery}
     */
    TypeMapperRenderer.prototype.makeProperty = function (parentId, name, type) {
        var id = parentId.selector.replace("#", "") + this.idNameSeperator + name + this.idNameSeperator + type;
        var property = $('<div>').attr('id', id).addClass('property');
        var propertyName = $('<span>').addClass('property-name').text(name);
        var seperator = $('<span>').addClass('property-name').text(":");
        var propertyType = $('<span>').addClass('property-type').text(type);

        property.append(propertyName);
        property.append(seperator);
        property.append(propertyType);
        $(parentId).append(property);
        return property;
    };

    /**
     * Make Source property
     * @param {string} parentId identifier of the parent of the property
     * @param {string} name property name
     * @param {string} type property type
     */
    TypeMapperRenderer.prototype.addSourceProperty = function (parentId, name, type) {
        this.jsPlumbInstance.makeSource(this.makeProperty(parentId, name, type), {
            anchor: ["Continuous", {faces: ["right"]}]
        });
    };

    /**
     * Make Target property
     * @param {string} parentId identifier of the parent of the property
     * @param {string} name property name
     * @param {string} type property type
     */
    TypeMapperRenderer.prototype.addTargetProperty = function (parentId, name, type) {
        var callback = this.onConnection;
        var refObjects = this.references;
        var seperator = this.idNameSeperator;
        var typeConverterObj = this.typeConverterView;
        var placeHolderName = this.placeHolderName;
        var jsPlumbInst = this.jsPlumbInstance;
        var positionFunction = this.dagrePosition;
        var self = this;

        this.jsPlumbInstance.makeTarget(this.makeProperty(parentId, name, type), {
            maxConnections: 1,
            anchor: ["Continuous", {faces: ["left"]}],
            beforeDrop: function (params) {
                //Checks property types are equal
                var sourceParts = params.sourceId.split(seperator);
                var targetParts = params.targetId.split(seperator);
                var isValidTypes = sourceParts[7] == targetParts[7];
                var sourceId = sourceParts.slice(0, 6).join(seperator);
                var targetId = targetParts.slice(0, 6).join(seperator);

                var sourceRefObj = null;
                var targetRefObj = null;

                _.forEach(refObjects, function(ref) {
                    if (ref.name == sourceId) {
                        sourceRefObj = ref.refObj;
                    } else if (ref.name == targetId) {
                        targetRefObj = ref.refObj;
                    }
                });

                var connection = {
                    sourceStruct: sourceParts[0],
                    sourceProperty: sourceParts[6],
                    sourceType: sourceParts [7],
                    sourceReference: sourceRefObj,
                    targetStruct: targetParts[0],
                    targetProperty: targetParts [6],
                    targetType: targetParts[7],
                    targetReference: targetRefObj,
                    isComplexMapping: false,
                    complexMapperName: null
                };

                if (isValidTypes) {
                    self.midpoint= self.midpoint + self.midpointVariance;
                    self.jsPlumbInstance.importDefaults({ Connector :  [ "Flowchart", { midpoint: self.midpoint }]});
                    callback(connection);
                } else {
                    var compatibleTypeConverters = [];
                    var typeConverters = typeConverterObj._package.getTypeMapperDefinitions();

                    _.forEach(typeConverters, function(typeConverter) {
                        if (typeConverterObj.getModel().getTypeMapperName() !== typeConverter.getTypeMapperName()) {
                            if (connection.sourceType == typeConverter.getSourceAndIdentifier().split(" ")[0] &&
                                connection.targetType == typeConverter.getReturnType()) {
                                compatibleTypeConverters.push(typeConverter.getTypeMapperName());
                            }
                        }
                    });

                    isValidTypes = compatibleTypeConverters.length > 0;
                    if (isValidTypes) {
                        connection.isComplexMapping = true;
                        connection.complexMapperName = compatibleTypeConverters[0];
                        callback(connection);
                    } else {
                        alerts.error("There is no valid type mapper existing to covert from : " + connection.sourceType
                            + " to: " + connection.targetType);
                    }
                }
                return isValidTypes;
            },

            onDrop: function () {




                positionFunction(placeHolderName, jsPlumbInst);
            }
        });
    };


    /**
     * Position Nodes with dagre
     * @param {string} viewId type mapper view identifier
     * @param jsPlumbInstance jsPlumb instance of the type mapper to be repositioned
     */
    TypeMapperRenderer.prototype.dagrePosition = function (viewId, jsPlumbInstance) {
        // construct dagre graph from this.jsPlumbInstance graph
        var graph = new dagre.graphlib.Graph();

        var alignment = 'LR';

        if (jsPlumbInstance.getAllConnections() == 0) {
            alignment = 'TD';
        }

        graph.setGraph({ranksep: '10', rankdir: alignment, edgesep: '10', marginx: '20'});
        graph.setDefaultEdgeLabel(function () {
            return {};
        });
        var nodes = $("#" + viewId + "> .struct, #" + viewId + "> .func");

        if (nodes.length > 0) {
            var maxTypeHeight = 0;


            _.forEach(nodes, function(n) {
                var nodeContent = $("#" + n.id);
                if (maxTypeHeight < nodeContent.width()) {
                    maxTypeHeight = nodeContent.width();
                }
                graph.setNode(n.id, {width: nodeContent.width() , height: nodeContent.height()});
            });

            var edges = jsPlumbInstance.getAllConnections();


            _.forEach(edges, function(edge) {
                graph.setEdge(edge.source.id.split("-")[0], edge.target.id.split("-")[0]);
            });

            // calculate the layout (i.e. node positions)
            dagre.layout(graph);

            var maxYPosition = 0;

            // Applying the calculated layout
            _.forEach(graph.nodes(), function(dagreNode) {
                var node = $("#" + dagreNode);

                if (node.attr('class') == "func") {
                    node.css("left", graph.node(dagreNode).x + "px");
                    node.css("top", graph.node(dagreNode).y + "px");
                }

                if (graph.node(dagreNode) != null && graph.node(dagreNode).y > maxYPosition) {
                    maxYPosition = graph.node(dagreNode).y;
                }
            });

            $("#" + viewId).height(maxTypeHeight + maxYPosition);
        }
    };

    return TypeMapperRenderer;
});


