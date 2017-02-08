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
        this.viewId = typeConverterView._model.id;
        this.viewIdSeperator = "___";
        this.sourceTargetSeperator = "_--_";
        this.idNameSeperator = "_-_-_-";
        this.nameTypeSeperator = "---";
        this.placeHolderName = "data-mapper-container" + this.viewIdSeperator + this.viewId;
        this.onConnection = onConnectionCallback;
        this.typeConverterView = typeConverterView;
        this.midpoint = 0.01;
        this.midpointVariance = 0.02;
        this.disconnectCallback = onDisconnectCallback;
        this.connectCallback = onConnectionCallback;
        var self = this;


        this.jsPlumbInstance = jsPlumb.getInstance({
            Connector: ["Flowchart", {midpoint: self.midpoint}],
            Container: this.placeHolderName,
            PaintStyle: {
                strokeWidth: 2,
                //todo : load colors from css
                stroke: "#3d3d3d",
                cssClass: "plumbConnect",
                outlineStroke: "#F7F7F7",
                outlineWidth: 2
            },
            EndpointStyle: {radius: 1},
            ConnectionOverlays: [
                ["Arrow", {
                    location: 1,
                    visible: true,
                    width: 11,
                    length: 11
                }],
                ["Custom", {
                    create: function (component) {
                        return $("<select id='typeMapperList" + self.viewIdSeperator + self.viewId + "'></select>");
                    },
                    location: 0.5,
                    id: "typeMapperDropdown",
                    cssClass: "typeMapperList"
                }]
            ]
        });

        this.jsPlumbInstance.bind('dblclick', function (connection, e) {
            self.disconnect(connection);
        });

        this.jsPlumbInstance.bind('connection', function (info, ev) {
            self.dagrePosition(self.placeHolderName, self.jsPlumbInstance);
            self.processTypeMapperDropdown(info);
        });
    };


    TypeMapperRenderer.prototype.constructor = TypeMapperRenderer;

    /**
     * Disconnects the connection created.
     *
     * @param connection
     */
    TypeMapperRenderer.prototype.disconnect = function (connection) {
        var propertyConnection = this.getConnectionObject(connection.sourceId, connection.targetId);
        this.midpoint = this.midpoint - this.midpointVariance;
        this.jsPlumbInstance.importDefaults({Connector: ["Flowchart", {midpoint: this.midpoint}]});
        this.jsPlumbInstance.detach(connection);
        this.dagrePosition(this.placeHolderName, this.jsPlumbInstance);
        this.disconnectCallback(propertyConnection);
    };

    /**
     * Created the connection object from the sourceId and targetId of the connection elements
     * @param sourceId Id of the source element of the connection
     * @param targetId Id of the target element of the connection
     * @returns connectionObject
     */
    TypeMapperRenderer.prototype.getConnectionObject = function (sourceId, targetId) {
        var sourceName = this.getStructId(sourceId);
        var targetName = this.getStructId(targetId);

        var sourceRefObj;
        var targetRefObj;

        for (var i = 0; i < this.references.length; i++) {
            if (this.references[i].name == sourceName) {
                sourceRefObj = this.references[i].refObj;
            } else if (this.references[i].name == targetName) {
                targetRefObj = this.references[i].refObj;
            }
        }

        return {
            sourceStruct: this.getStructName(sourceName),
            sourceProperty: this.getPropertyNameStack(sourceId),
            sourceType: this.getPropertyType(sourceId),
            sourceReference: sourceRefObj,
            targetStruct: this.getStructName(targetName),
            targetProperty: this.getPropertyNameStack(targetId),
            targetType: this.getPropertyType(targetId),
            targetReference: targetRefObj,
            isComplexMapping: false,
            complexMapperName: null
        };
    };

    /**
     * Get the id of the struct from the propertyId
     * @param propertyId Id of the property
     * @returns {*}
     */
    TypeMapperRenderer.prototype.getStructId = function (propertyId) {
        var id = propertyId.replace("jstree-container" + this.viewIdSeperator, "");
        return id.split(this.idNameSeperator)[0]
    };

    /**
     * Get the struct name from the id of the struct
     * @param structId
     * @returns {*}
     */
    TypeMapperRenderer.prototype.getStructName = function (structId) {
        return structId.split(this.viewIdSeperator)[0];
    };

    /**
     * Get the type of the property from the property ID
     * @param propertyId
     * @returns {*}
     */
    TypeMapperRenderer.prototype.getPropertyType = function (propertyId) {
        var parts = propertyId.split(this.idNameSeperator);
        return parts[parts.length - 1].split(this.nameTypeSeperator)[1].replace('_anchor', "");
    };

    /**
     * Get the name of the property from the property id
     * @param propertyId
     * @returns {*}
     */
    TypeMapperRenderer.prototype.getPropertyName = function (propertyId) {
        var parts = propertyId.split(this.idNameSeperator);
        return parts[parts.length - 1].split(this.nameTypeSeperator)[0];
    };

    /**
     * Populate the attribute properties stack from the propertyId
     * @param propertyId
     * @returns {Array}
     */
    TypeMapperRenderer.prototype.getPropertyNameStack = function (propertyId) {
        var id = propertyId.replace("jstree-container" + this.viewIdSeperator, "");
        var parts = id.split(this.idNameSeperator);
        var propertyNames = [];
        var self = this;
        var elementId = 0;
        _.forEach(parts, function (aPart) {
            if (elementId != 0) {
                propertyNames.push(aPart.split(self.nameTypeSeperator)[0])
            } else {
                elementId++;
            }
        });
        return propertyNames;
    };

    /**
     * Process the connection and determines whether to show/hide the dropdown from the connection.
     * @param info connection object.
     */
    TypeMapperRenderer.prototype.processTypeMapperDropdown = function (info) {
        var sourceType = this.getPropertyType(info.sourceId);
        var targetType = this.getPropertyType(info.targetId);
        var isValidTypes = sourceType == targetType;
        var self = this;
        if (!isValidTypes) {
            var connection = info.connection;
            connection.getOverlay("typeMapperDropdown").show();
            var typeMapperId = '#typeMapperList' + this.viewIdSeperator + this.viewId;
            var updatedTypeMapperId = "typeMapperList" + this.viewIdSeperator + connection.sourceId + this.sourceTargetSeperator + connection.targetId;
            var typeMappers = this.getExistingTypeMappers(this.typeConverterView, sourceType, targetType);
            $.each(typeMappers, function (i, item) {
                $(typeMapperId).append($('<option>', {
                    value: item,
                    text: item
                }));
            });
            $(typeMapperId).attr("id", updatedTypeMapperId);
            $("#" + updatedTypeMapperId).change(function () {
                self.onChangeTypeMapper(updatedTypeMapperId)
            });
        } else {
            info.connection.getOverlay("typeMapperDropdown").hide();
        }
    };


    /**
     * Finds the existing type mappers defined.
     * @param typeConverterObj
     * @param sourceType
     * @param targetType
     * @returns {Array}
     */
    TypeMapperRenderer.prototype.getExistingTypeMappers = function (typeConverterObj, sourceType, targetType) {
        var compatibleTypeConverters = [];
        var typeConverters = typeConverterObj.getPackage().getTypeMapperDefinitions();
        _.forEach(typeConverters, function (typeConverter) {
            if (typeConverterObj.getModel().getTypeMapperName() !== typeConverter.getTypeMapperName()) {
                if (sourceType == typeConverter.getSourceAndIdentifier().split(" ")[0] &&
                    targetType == typeConverter.getReturnType()) {
                    compatibleTypeConverters.push(typeConverter.getTypeMapperName());
                }
            }
        });
        return compatibleTypeConverters;
    };

    /**
     * The integrates the source manipluation trigger for the change in the
     * dropdown of the complex mapping.
     *
     * @param listId Id of the dropdown.
     */
    TypeMapperRenderer.prototype.onChangeTypeMapper = function (listId) {
        var id = listId.replace("typeMapperList" + this.viewIdSeperator, "");
        var sourceId = id.split(this.sourceTargetSeperator)[0];
        var targetId = id.split(this.sourceTargetSeperator)[1];
        var connection = this.getConnectionObject(sourceId, targetId);
        this.disconnectCallback(connection);
        connection.isComplexMapping = true;
        connection.complexMapperName = $("#" + listId + " option:selected").val();
        this.connectCallback(connection);
    };

    /**
     * Remove a struct from the mapper UI
     * @param {string} name identifier of the struct
     */
    TypeMapperRenderer.prototype.removeStruct = function (name) {
        var structId = name + this.viewIdSeperator + this.viewId;
        var structConns = $('div[id^="' + structId + '"]');
        var self = this;
        _.forEach(structConns, function (structCon) {
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
     * Add a source struct in the mapper UI
     * @param {object} struct definition with parameters to be mapped
     * @param {object} reference AST model reference
     */
    TypeMapperRenderer.prototype.addSourceStruct = function (struct, reference) {
        var id = struct.name + this.viewIdSeperator + this.viewId;
        struct.id = id;
        this.makeStruct(struct, 50, 50, reference);
        var jsTreeId = 'jstree-container' + this.viewIdSeperator + id;
        this.addComplexProperty(jsTreeId, struct);
        var self = this;
        $("#" + jsTreeId).jstree().on('ready.jstree', function () {
            var sourceElements = $("#" + id).find('.jstree-anchor');
            _.forEach(sourceElements, function (element) {
                self.addSource(element);
            });
            // self.dagrePosition(self.placeHolderName, self.jsPlumbInstance);
            $("#" + jsTreeId).jstree('open_all');
            self.dagrePosition(self.placeHolderName, self.jsPlumbInstance);
        }).on('after_open.jstree', function (event, data) {
            var parentId = data.node.id;
            var sourceElements = $("#" + parentId).find('.jstree-anchor');
            _.forEach(sourceElements, function (element) {
                self.addSource(element);
            });
            self.dagrePosition(self.placeHolderName, self.jsPlumbInstance);
        }).on('close_node.jstree', function (event, data) {
            //TODO: need to rethink what need to happen when close
            // var parentId = data.node.id;
            // var sourceElements = $("#" + parentId).find('.jstree-anchor');
            // var sourceIds = [];
            // _.forEach(sourceElements, function (element) {
            //     sourceIds.push(sourceElements.attr('id').replace('_anchor', ""));
            // });
            // var connections = self.jsPlumbInstance.getConnections({source: "jstree-container___newStruct4___ee7433db-99b0-3ed4-c346-1a019d10e350_-_-_-xsxaxax---newStruct2_-_-_-sqsq---string_anchor"});
            // _.forEach(connections, function (connection) {
            //     self.disconnect(connection);
            // })
        });
    };

    /**
     * Handles the complex struct properties.
     *
     * @param parentId Id of the parentElement where the element needs to be added.
     * @param struct  Object which specifies the id, name, and type of the struct.
     */
    TypeMapperRenderer.prototype.addComplexProperty = function (parentId, struct) {
        var self = this;
        _.forEach(struct.properties, function (property) {
            var buildInTypes = self.typeConverterView.getTypes();
            if (buildInTypes.includes(property.type)) {
                self.makeProperty($('#' + parentId), property.name, property.type);
            } else {
                var complexStructEl = self.makeProperty($('#' + parentId), property.name, property.type);
                var allStructDefn = self.typeConverterView.getPackage().getStructDefinitions();
                var structIndex = _.findIndex(allStructDefn, function (aStruct) {
                    return aStruct.getStructName() === property.type;
                });
                self.addComplexProperty(complexStructEl.attr('id'), allStructDefn[structIndex].getAttributesArray())
            }
        });
    };

    /**
     * Add a target struct in the mapper UI
     * @param {object} struct definition with parameters to be mapped
     * @param {object} reference AST model reference
     */
    TypeMapperRenderer.prototype.addTargetStruct = function (struct, reference) {
        var id = struct.name + this.viewIdSeperator + this.viewId;
        struct.id = id;
        var placeHolderWidth = document.getElementById(this.placeHolderName).offsetWidth;
        var posY = placeHolderWidth - (placeHolderWidth / 4);
        this.makeStruct(struct, 50, posY, reference);
        var jsTreeId = 'jstree-container' + this.viewIdSeperator + id;
        this.addComplexProperty(jsTreeId, struct);
        var self = this;
        $("#" + jsTreeId).jstree().on('ready.jstree', function () {
            var sourceElements = $("#" + id).find('.jstree-anchor');
            _.forEach(sourceElements, function (element) {
                self.addTarget(element);
            });
            $("#" + jsTreeId).jstree('open_all');
            self.dagrePosition(self.placeHolderName, self.jsPlumbInstance);
        }).on('after_open.jstree', function (event, data) {
            var parentId = data.node.id;
            var sourceElements = $("#" + parentId).find('.jstree-anchor');
            _.forEach(sourceElements, function (element) {
                self.addTarget(element);
            });
            self.dagrePosition(self.placeHolderName, self.jsPlumbInstance);
        }).on('close_node.jstree', function (event, data) {
            // //TODO: need to rethink what need to happen when close
            // var parentId = data.node.id;
            // var sourceElements = $("#" + parentId).find('.jstree-anchor');
            // var targetIds = [];
            // _.forEach(sourceElements, function (element) {
            //     targetIds.push(sourceElements.attr('id'));
            // });
            // var connections = self.jsPlumbInstance.getConnections({target: targetIds});
            // _.forEach(connections, function (connection) {
            //     self.disconnect(connection);
            // })
        });
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
        var jsTreeContainer = $('<div>').attr('id', 'jstree-container' + this.viewIdSeperator + struct.id).addClass('tree-container');
        newStruct.append(jsTreeContainer);
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
        var self = this;
        var funcName = $('<div>').addClass('struct-name').text(func.name);
        newFunc.append(funcName);

        newFunc.css({
            'top': 0,
            'left': 0
        });

        $("#" + this.placeHolderName).append(newFunc);

        var funcContent = $('#' + func.name);

        _.forEach(func.parameters, function (parameter) {
            self.addTargetProperty(funcContent, parameter.name, parameter.type);
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
        var id = parentId.selector.replace("#", "") + this.idNameSeperator + name + this.nameTypeSeperator + type;
        var ul = $('<ul class="property">');
        var li = $('<li class="property">').attr('id', id).text(name + " : " + type);
        ul.append(li);
        $(parentId).append(ul);
        return li;
    };

    /**
     * Make Source property
     * @param {string} parentId identifier of the parent of the property
     * @param {string} name property name
     * @param {string} type property type
     */
    TypeMapperRenderer.prototype.addSource = function (element) {
        this.jsPlumbInstance.makeSource(element, {
            anchor: ["Continuous", {faces: ["right"]}]
        });
    };

    /**
     * Make Target property
     * @param {string} parentId identifier of the parent of the property
     * @param {string} name property name
     * @param {string} type property type
     */
    TypeMapperRenderer.prototype.addTarget = function (element) {
        var callback = this.onConnection;
        var refObjects = this.references;
        var seperator = this.idNameSeperator;
        var typeConverterObj = this.typeConverterView;
        var placeHolderName = this.placeHolderName;
        var jsPlumbInst = this.jsPlumbInstance;
        var positionFunction = this.dagrePosition;
        var self = this;


        this.jsPlumbInstance.makeTarget(element, {
            maxConnections: 1,
            anchor: ["Continuous", {faces: ["left"]}],

            beforeDrop: function (params) {
                //Checks property types are equal
                var isValidTypes = self.getPropertyType(params.sourceId) == self.getPropertyType(params.targetId);
                var connection = self.getConnectionObject(params.sourceId, params.targetId);
                if (isValidTypes) {
                    self.midpoint = self.midpoint + self.midpointVariance;
                    self.jsPlumbInstance.importDefaults({Connector: ["Flowchart", {midpoint: self.midpoint}]});
                    callback(connection);
                } else {
                    var compatibleTypeConverters = self.getExistingTypeMappers(typeConverterObj,
                        connection.sourceType, connection.targetType);
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
        })
        ;
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


            _.forEach(nodes, function (n) {
                var nodeContent = $("#" + n.id);
                if (maxTypeHeight < nodeContent.width()) {
                    maxTypeHeight = nodeContent.width();
                }
                graph.setNode(n.id, {width: nodeContent.width(), height: nodeContent.height()});
            });

            var edges = jsPlumbInstance.getAllConnections();


            _.forEach(edges, function (edge) {
                graph.setEdge(edge.source.id.split("-")[0], edge.target.id.split("-")[0]);
            });

            // calculate the layout (i.e. node positions)
            dagre.layout(graph);

            var maxYPosition = 0;

            // Applying the calculated layout
            _.forEach(graph.nodes(), function (dagreNode) {
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