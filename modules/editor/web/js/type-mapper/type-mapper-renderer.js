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
define(['require', 'lodash', 'jquery', 'jsPlumb', 'dagre'], function (require, _, $, jsPlumb, dagre) {

    var TypeMapperRenderer = function (onConnectionCallback, onDisconnectCallback, typeConverterView) {
        this.references = [];
        this.placeHolderName = "data-mapper-container-" + typeConverterView._model.id;
        this.idNameSeperator = "-";
        this.onConnection = onConnectionCallback;
        this.typeConverterView = typeConverterView;
        this.jsPlumbInstance = jsPlumb.getInstance();

        var strokeColor = "#414e66";
        var strokeWidth = 1;
        var pointColor = "#414e66";
        var pointSize = 0.5;

        this.jsPlumbInstance.Defaults.Container = $("#" + this.placeHolderName);

        this.jsPlumbInstance.Defaults.PaintStyle = {
            strokeStyle: strokeColor,
            lineWidth: strokeWidth
        };

        this.jsPlumbInstance.Defaults.EndpointStyle = {
            radius: pointSize,
            fillStyle: pointColor
        };
        this.jsPlumbInstance.Defaults.Overlays = [
            ["Arrow", {location: 1, width: 10, length: 10}]
        ];

        this.jsPlumbInstance.importDefaults({
            Connector: ["Flowchart",
                {
                    cornerRadius: 20,
                    stub: 1, alwaysRespectStubs: false, midpoint: 0.2
                }]
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

            for (var i = 0; i < refObjects.length; i++) {
                if (refObjects[i].name == sourceId) {
                    sourceRefObj = refObjects[i].refObj;
                } else if (refObjects[i].name == targetId) {
                    targetRefObj = refObjects[i].refObj;
                }
            }

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

            jsPlumbInst.detach(connection);
            positionFunc(viewId, jsPlumbInst);
            onDisconnectCallback(PropertyConnection);
        });

        this.jsPlumbInstance.bind('connection', function (info, ev) {
            positionFunc(viewId, jsPlumbInst);
        });
    };

    TypeMapperRenderer.prototype.constructor = TypeMapperRenderer;

    TypeMapperRenderer.prototype.removeStruct = function (name) {
        var structId = name + this.idNameSeperator + this.typeConverterView._model.id;
        var structConns = $('div[id^="' + structId + '"]');
        for (var i = 0; i < structConns.length; i++) {
            var div = structConns[i];
            if (_.includes(div.className, 'property')) {
                this.jsPlumbInstance.remove(div.id);
            }
        }
        $("#" + structId).remove();
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };

    TypeMapperRenderer.prototype.addConnection = function (connection) {
        this.jsPlumbInstance.connect({
            source: connection.sourceStruct + this.idNameSeperator + connection.sourceProperty
            + this.idNameSeperator + connection.sourceType,
            target: connection.targetStruct + this.idNameSeperator + connection.targetProperty
            + this.idNameSeperator + connection.targetType
        });
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };


    TypeMapperRenderer.prototype.getConnections = function () {
        var connections = [];
        for (var i = 0; i < this.jsPlumbInstance.getConnections().length; i++) {
            var sourceParts = this.jsPlumbInstance.getConnections()[i].sourceId.split(this.idNameSeperator);
            var targetParts = this.jsPlumbInstance.getConnections()[i].targetId.split(this.idNameSeperator);
            var connection = {
                sourceStruct: sourceParts[0],
                sourceProperty: sourceParts[6],
                sourceType: sourceParts[7],
                targetStruct: targetParts[0],
                targetProperty: targetParts[6],
                targetType: targetParts[7]
            };
            connections.push(connection);
        }
        return connections;
    };

    TypeMapperRenderer.prototype.addSourceStruct = function (struct, reference) {
        struct.id = struct.name + this.idNameSeperator + this.typeConverterView._model.id;
        this.makeStruct(struct, 50, 50, reference);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addSourceProperty($('#' + struct.id), struct.properties[i].name, struct.properties[i].type);
        }
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };

    TypeMapperRenderer.prototype.addTargetStruct = function (struct, reference) {
        struct.id = struct.name + this.idNameSeperator + this.typeConverterView._model.id;
        var placeHolderWidth = document.getElementById(this.placeHolderName).offsetWidth;
        var posY = placeHolderWidth - (placeHolderWidth / 4);
        this.makeStruct(struct, 50, posY, reference);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addTargetProperty($('#' + struct.id), struct.properties[i].name, struct.properties[i].type);
        }
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
    };

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
        for (var i = 0; i < func.parameters.length; i++) {
            this.addTargetProperty(funcContent, func.parameters[i].name, func.parameters[i].type);
        }

        this.addSourceProperty(funcContent, "output", func.returnType);
        this.dagrePosition(this.placeHolderName,  this.jsPlumbInstance);

    };


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

    TypeMapperRenderer.prototype.addSourceProperty = function (parentId, name, type) {
        this.jsPlumbInstance.makeSource(this.makeProperty(parentId, name, type), {
            anchor: ["Continuous", {faces: ["right"]}]
        });
    };

    TypeMapperRenderer.prototype.addTargetProperty = function (parentId, name, type) {
        var callback = this.onConnection;
        var refObjects = this.references;
        var seperator = this.idNameSeperator;
        var typeConverterObj = this.typeConverterView;
        var placeHolderName = this.placeHolderName;
        var jsPlumbInst = this.jsPlumbInstance;
        var positionFunction = this.dagrePosition;

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

                var sourceRefObj;
                var targetRefObj;

                for (var i = 0; i < refObjects.length; i++) {
                    if (refObjects[i].name == sourceId) {
                        sourceRefObj = refObjects[i].refObj;
                    } else if (refObjects[i].name == targetId) {
                        targetRefObj = refObjects[i].refObj;
                    }
                }

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
                    callback(connection);
                } else {
                    var compatibleTypeConverters = [];
                    var typeConverters = typeConverterObj._package.getTypeMapperDefinitions();
                    for (var i = 0; i < typeConverters.length; i++) {
                        var aTypeConverter = typeConverters[i];
                        if (typeConverterObj._model.getTypeMapperName() !== aTypeConverter.getTypeMapperName()) {
                            if (connection.sourceType == aTypeConverter.getSourceAndIdentifier().split(" ")[0] &&
                                connection.targetType == aTypeConverter.getReturnType()) {
                                compatibleTypeConverters.push(aTypeConverter.getTypeMapperName());
                            }
                        }
                    }
                    isValidTypes = compatibleTypeConverters.length > 0;
                    if (isValidTypes) {
                        connection.isComplexMapping = true;
                        connection.complexMapperName = compatibleTypeConverters[0];
                        callback(connection);
                    }
                }
                return isValidTypes;
            },

            onDrop: function () {
                positionFunction(placeHolderName, jsPlumbInst);
            }
        });
    };


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

            for (var i = 0; i < nodes.length; i++) {
                var n = nodes[i];
                var nodeContent =  $("#" + n.id);

                if (maxTypeHeight < nodeContent.width()) {
                    maxTypeHeight = nodeContent.width();
                }

                graph.setNode(n.id, {width: nodeContent.width() , height: nodeContent.height()});
            }
            var edges = jsPlumbInstance.getAllConnections();
            for (var i = 0; i < edges.length; i++) {
                var c = edges[i];
                graph.setEdge(c.source.id.split("-")[0], c.target.id.split("-")[0]);
            }

            // calculate the layout (i.e. node positions)
            dagre.layout(graph);

            var maxYPosition = 0;

            // Applying the calculated layout
            graph.nodes().forEach(function(v) {

                var node = $("#" + v);

                if (node.attr('class') == "func") {
                    node.css("left", graph.node(v).x + "px");
                    node.css("top", graph.node(v).y + "px");
                }

                if (graph.node(v) != null && graph.node(v).y > maxYPosition) {
                    maxYPosition = graph.node(v).y;
                }
            });

            $("#" + viewId).height(maxTypeHeight + maxYPosition);
        }
    };

    return TypeMapperRenderer;
});


