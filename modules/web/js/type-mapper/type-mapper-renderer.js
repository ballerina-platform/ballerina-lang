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
        this.jsTreePrefix = "jstree-container";
        this.viewIdSeperator = "___";
        this.sourceTargetSeperator = "_--_";
        this.idNameSeperator = "_-_-_-";
        this.nameTypeSeperator = "---";
        this.placeHolderName = "data-mapper-container" + this.viewIdSeperator + this.viewId;
        this.onConnection = onConnectionCallback;
        this.typeConverterView = typeConverterView;
        this.midpoint = 0.1;
        this.midpointVariance = 0.02;
        this.disconnectCallback = onDisconnectCallback;
        this.connectCallback = onConnectionCallback;
        this.connectionPool = [];
        this.existingJsTrees = [];
        var self = this;

        this.jsPlumbInstance = jsPlumb.getInstance({
            Connector: self.getConnectorConfig(self.midpoint),
            Container: this.placeHolderName,
            PaintStyle: {
                strokeWidth: 2,
                //todo : load colors from css
                stroke: "#3d3d3d",
                cssClass: "plumbConnect",
                outlineStroke: "#F7F7F7",
                outlineWidth: 2
            },
            HoverPaintStyle: {
                strokeWidth: 3,
                stroke: "#ff9900",
                outlineWidth: 3,
                outlineStroke: "#ffe0b3"
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
            self.dagrePosition(self);
            //TODO: for multiple type mappers
            // self.processTypeMapperDropdown(info);
        });
    };

    TypeMapperRenderer.prototype.constructor = TypeMapperRenderer;

    /**
     * Disconnects the connection created.
     *
     * @param connection
     */
    TypeMapperRenderer.prototype.disconnect = function (connection) {
        var self = this;
        var propertyConnection = this.getConnectionObject(connection.getParameter("id"),
            connection.sourceId, connection.targetId);
        this.midpoint = this.midpoint - this.midpointVariance;
        this.jsPlumbInstance.importDefaults({Connector: self.getConnectorConfig(self.midpoint)});
        this.jsPlumbInstance.detach(connection);
        this.dagrePosition(this);
        this.disconnectCallback(propertyConnection);
        // this.enableParentsJsTree(connection.sourceId, this, this.jsPlumbInstance.getAllConnections(), true);
        // this.enableParentsJsTree(connection.targetId, this, this.jsPlumbInstance.getAllConnections(), false);
    };

    /**
     * Created the connection object from the sourceId and targetId of the connection elements
     * @param sourceId Id of the source element of the connection
     * @param targetId Id of the target element of the connection
     * @returns connectionObject
     */
    TypeMapperRenderer.prototype.getConnectionObject = function (id, sourceId, targetId) {
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
            id: id,
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
        var id = propertyId.replace(this.jsTreePrefix + this.viewIdSeperator, "");
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
            var updatedTypeMapperId = "typeMapperList" + this.viewIdSeperator + connection.sourceId
                + this.sourceTargetSeperator + connection.targetId;
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
                if (sourceType == typeConverter.getInputParamAndIdentifier().split(" ")[0] &&
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
        var connection = this.getConnectionObject(id, sourceId, targetId);
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
        var structConns;
        var lookupClass = "property";

        if ($("#" + structId).attr('class').includes("struct")) {
            lookupClass = "jstree-anchor";
            structConns = $('div[id^="' + this.jsTreePrefix + this.viewIdSeperator + structId + '"]')
                .find('.' + lookupClass);
        } else {
            structConns = $('div[id^="' + structId + '"]');
        }

        var self = this;
        _.forEach(structConns, function (structCon) {
            if (_.includes(structCon.className, lookupClass)) {
                self.jsPlumbInstance.remove(structCon.id);
            }
        });
        $("#" + structId).remove();
        this.dagrePosition(this);
    };

    /**
     * Add a connection arrow in the mapper UI
     * @param {object} connection connection object which specified source and target
     */
    TypeMapperRenderer.prototype.addConnection = function (connection) {
        var anchorEnd = '_anchor';
        var sourceId =  this.jsTreePrefix + this.viewIdSeperator +  connection.sourceStruct
            + this.viewIdSeperator + this.viewId;
        var targetId =  this.jsTreePrefix + this.viewIdSeperator +  connection.targetStruct
            + this.viewIdSeperator + this.viewId;
        var isSourceExists;
        var isTargetExists;

        if (connection.sourceFunction) {
            sourceId = connection.sourceStruct + connection.sourceId + this.viewIdSeperator + this.viewId;
            isSourceExists = true;
        } else {
            isSourceExists = _.includes(this.existingJsTrees,
                                        connection.sourceStruct + this.viewIdSeperator + this.viewId)
        }
        if (connection.targetFunction) {
            targetId = connection.targetStruct + connection.targetId + this.viewIdSeperator + this.viewId;
            isTargetExists = true;
        } else {
            isTargetExists = _.includes(this.existingJsTrees,
                                        connection.targetStruct+ this.viewIdSeperator + this.viewId)
        }

        if (isSourceExists && isTargetExists) {
            for (var i = 0; i < connection.sourceProperty.length; i++) {
                sourceId += this.idNameSeperator
                    + connection.sourceProperty[i] + this.nameTypeSeperator + connection.sourceType[i];
            }
            if (!connection.sourceFunction) {
                sourceId += anchorEnd;
            }

            for (var i = 0; i < connection.targetProperty.length; i++) {
                targetId += this.idNameSeperator
                    + connection.targetProperty[i] + this.nameTypeSeperator + connection.targetType[i];
            }

            if (!connection.targetFunction) {
                targetId += anchorEnd;
            }

            this.jsPlumbInstance.connect({
                anchor: ["Continuous", {faces: ["right", "left"]}],
                source: sourceId,
                target: targetId,
                parameters: {id: connection.id}
            });
            this.dagrePosition(this);

        } else {
            this.connectionPool.push({
                connection : connection,
                isSourceExists : isSourceExists,
                isTargetExists : isTargetExists,
                connected : false
            });
        }
    };

    /**
     * Add a source struct in the mapper UI
     * @param {object} struct definition with parameters to be mapped
     * @param {object} reference AST model reference
     */
    TypeMapperRenderer.prototype.addSourceStruct = function (struct, reference) {
        var id = struct.name + this.viewIdSeperator + this.viewId;
        struct.id = id;
        this.makeStruct(struct, 50, 50, reference, "source");
        var jsTreeId = this.jsTreePrefix + this.viewIdSeperator + id;
        this.addComplexProperty(jsTreeId, struct);
        this.processJSTree(jsTreeId, id, this.addSource)
    };


    /**
     * Manipulates the jstree structure and the jsPlumb connections
     *
     * @param jsTreeId
     * @param structId
     * @param createCallback
     */
    TypeMapperRenderer.prototype.processJSTree = function (jsTreeId, structId, createCallback) {
        var self = this;
        $("#" + jsTreeId).jstree().on('ready.jstree', function () {
            var sourceElements = $("#" + structId).find('.jstree-anchor');
            _.forEach(sourceElements, function (element) {
                createCallback(element, self);
            });
            $("#" + jsTreeId).jstree('open_all');
            self.existingJsTrees.push(structId);
            self.dagrePosition(self);
            _.forEach(self.connectionPool, function (conPoolObj) {
                if (!conPoolObj.connected && structId ==
                    conPoolObj.connection.sourceStruct + self.viewIdSeperator + self.viewId ) {
                    conPoolObj.isSourceExists = true;
                } else if (!conPoolObj.connected && structId ==
                    conPoolObj.connection.targetStruct + self.viewIdSeperator + self.viewId ) {
                    conPoolObj.isTargetExists = true;
                }
                if (!conPoolObj.connected && conPoolObj.isSourceExists && conPoolObj.isTargetExists) {
                    self.addConnection(conPoolObj.connection);
                    conPoolObj.connected = true;
                }
            });
        }).on('after_open.jstree', function (event, data) {
            self.dagrePosition(self);
            var parentId = data.node.id;
            var sourceElements = $("#" + parentId).find('.jstree-anchor');
            _.forEach(sourceElements, function (element) {
                createCallback(element, self);
            });
            self.jsPlumbInstance.repaintEverything();
        }).on('after_close.jstree', function (event, data) {
            self.dagrePosition(self);
            self.jsPlumbInstance.repaintEverything();
        }).on('select_node.jstree', function (event, data) {
            data.instance.deselect_node(data.node);
        });
    };

    TypeMapperRenderer.prototype.repaintAll = function (jsTreeId) {
        var children = $("#" + jsTreeId).jstree().get_node('#').children_d;
        _forEach(children, function (child) {
            self.jsPlumbInstance.repaint(child.id + "_anchor");
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
        var posY = placeHolderWidth - (placeHolderWidth / 3);
        this.makeStruct(struct, 50, posY, reference, "target");
        var jsTreeId = 'jstree-container' + this.viewIdSeperator + id;
        this.addComplexProperty(jsTreeId, struct);
        this.processJSTree(jsTreeId, id, this.addTarget);
    };

    /**
     * Make generic struct div element in the UI
     * @param {object} struct definition with parameters to be mapped
     * @param {int} posX X position cordinate
     * @param {int} posY Y position cordinate
     * @param {object} reference
     */
    TypeMapperRenderer.prototype.makeStruct = function (struct, posX, posY, reference, type) {
        this.references.push({name: struct.id, refObj: reference});
        var newStruct = $('<div>').attr('id', struct.id).attr('type', type).addClass('struct');
        var structIcon = $('<i>').addClass('type-mapper-icon fw fw-struct fw-inverse');
        var structName = $('<div>');

        structName.append(structIcon);
        structName.append($('<span>').text(struct.name));
        newStruct.append(structName);
        newStruct.css({
            'top': posX,
            'left': posY
        });
        var jsTreeContainer = $('<div>').attr('id', 'jstree-container' + this.viewIdSeperator + struct.id)
            .addClass('tree-container');
        newStruct.append(jsTreeContainer);
        $("#" + this.placeHolderName).append(newStruct);
    };

    /**
     * Add a function in the mapper UI
     * @param {object} function definition with parameters to be mapped
     * @param {object} reference AST model reference
     */
    TypeMapperRenderer.prototype.addFunction = function (func, reference, onFunctionRemove) {
        var funcText = func.name;
        //Allow multiple functions to drag and drop without conflicting
        var functionInvocationModelId = reference.model.getChildren()[1].getChildren()[0].getID();
        func.name = func.name + functionInvocationModelId;

        var id = func.name + this.viewIdSeperator + this.viewId;
        this.references.push({name: id, refObj: reference});
        var newFunc = $('<div>').attr('id', id).addClass('func');
        var self = this;
        var funcName = $('<div>');
        var funcIcon = $('<i>').addClass('type-mapper-icon fw fw-function fw-inverse');
        var closeButton = $('<span>').attr('id', id + "-button").addClass('fw-stack fw-lg btn btn-remove');

        var square = $('<i>').addClass('fw fw-square fw-stack-1x');
        var del = $('<i>').addClass('fw fw-delete fw-stack-1x fw-inverse');

        funcName.append(funcIcon);
        funcName.append($('<span>').text(funcText));
        closeButton.append(square);
        closeButton.append(del);
        funcName.append(closeButton);
        newFunc.append(funcName);

        newFunc.css({
            'top': 0,
            'left': 0
        });

        $("#" + this.placeHolderName).append(newFunc);

        //Remove button functionality
        $("#" + id + "-button").on("click", function () {
            var removedFunction = {name: func.name}
            removedFunction.incomingConnections = [];
            removedFunction.outgoingConnections = [];

            _.forEach(self.jsPlumbInstance.getAllConnections(), function (connection) {
                if (connection.target.id.includes(id)) {
                    removedFunction.incomingConnections.push(
                        self.getConnectionObject(connection.getParameter("id"),
                            connection.sourceId, connection.targetId));
                } else if (connection.source.id.includes(id)) {
                    removedFunction.outgoingConnections.push(
                        self.getConnectionObject(connection.getParameter("id"),
                            connection.sourceId, connection.targetId));
                }
            });

            for (var i = 0; i < self.references.length; i++) {
                if (self.references[i].name == id) {
                    removedFunction.reference = self.references[i].refObj;
                }
            }

            self.removeStruct(func.name);
            onFunctionRemove(removedFunction);
        });

        _.forEach(func.parameters, function (parameter) {
            var property = self.makeFunctionAttribute($('#' + id), parameter.name, parameter.type, true);
            self.addTarget(property, self);
        });

        _.forEach(func.returnType, function (parameter) {
            var property = self.makeFunctionAttribute($('#' + id), parameter.name, parameter.type, false);
            self.addSource(property, self, true);
        });

        self.dagrePosition(this);
    };

    TypeMapperRenderer.prototype.makeFunctionAttribute = function (parentId, name, type, input) {
        var id = parentId.selector.replace("#", "") + this.idNameSeperator + name + this.nameTypeSeperator + type;
        var property;
        if (input) {
            property = $('<div>').attr('id', id).addClass('func-in-property');
        } else {
            property = $('<div>').attr('id', id).addClass('func-out-property');
        }
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
     * Make source property
     *
     * @param element
     * @param self
     */
    TypeMapperRenderer.prototype.addSource = function (element, self, maxConnections) {
        var connectionConfig = {
            anchor: ["Continuous", {faces: ["right"]}]
        };
        if (maxConnections) {
            connectionConfig.maxConnections = 1;
        }
        self.jsPlumbInstance.makeSource(element, connectionConfig);
    };

    /**
     * Remove the source element
     *
     * @param elements
     * @param self
     */
    TypeMapperRenderer.prototype.removeSource = function (elements, self) {
        self.jsPlumbInstance.unmakeSource(elements);
    };

    /**
     * Specifies connection has a function
     * @param {object} connection
     * @returns {boolean} has a connection or not
     */
    TypeMapperRenderer.prototype.hasFunction = function(connection, self) {
        return  $("#" + connection.sourceStruct + self.viewIdSeperator + self.viewId).attr('class').includes("func")
            || $("#" + connection.targetStruct + self.viewIdSeperator + self.viewId).attr('class').includes("func");
    }

    /**
     * Make target property
     * @param element
     * @param self
     */
    TypeMapperRenderer.prototype.addTarget = function (element, self) {
        self.jsPlumbInstance.makeTarget(element, {
            maxConnections: 1,
            anchor: ["Continuous", {faces: ["left"]}],
            beforeDrop: function (params) {
                //Checks property types are equal
                var isValidTypes = self.getPropertyType(params.sourceId) == self.getPropertyType(params.targetId);
                var connection = self.getConnectionObject(params.id, params.sourceId, params.targetId);
                if (isValidTypes) {
                    self.midpoint = self.midpoint + self.midpointVariance;
                    self.jsPlumbInstance.importDefaults({Connector: self.getConnectorConfig(self.midpoint)});
                    self.onConnection(connection);
                    return self.hasFunction(connection, self);
                    // self.disableParentsJsTree(params.sourceId, self);
                    // self.disableParentsJsTree(params.targetId, self);
                } else {
                    var compatibleTypeConverters = self.getExistingTypeMappers(self.typeConverterView,
                        connection.sourceType, connection.targetType);
                    isValidTypes = compatibleTypeConverters.length > 0;
                    if (isValidTypes) {
                        connection.isComplexMapping = true;
                        connection.complexMapperName = compatibleTypeConverters[0];
                        self.jsPlumbInstance.importDefaults({Connector: self.getConnectorConfig(self.midpoint)});
                        self.onConnection(connection);
                        return self.hasFunction(connection, self);
                        // self.disableParentsJsTree(params.sourceId, self);
                        // self.disableParentsJsTree(params.targetId, self);
                    } else {
                        alerts.error("There is no valid type mapper existing to covert from : " + connection.sourceType
                            + " to: " + connection.targetType);
                    }
                }
                return isValidTypes;
            },
            onDrop: function () {
                self.dagrePosition(self);
            }
        })
        ;
    };

    TypeMapperRenderer.prototype.disableParentsJsTree = function (connectionId, self) {
        var jsTreeContainerPrefix = 'jstree-container';
        var sourceJsTreeId = jsTreeContainerPrefix + self.viewIdSeperator + self.getStructId(connectionId);
        var sourceJsTree = $("#" + sourceJsTreeId).jstree(true);
        var node = sourceJsTree.get_node(connectionId.replace('_anchor', ''));
        _.forEach(node.parents, function (parentNodeId) {
            if (parentNodeId !== '#') {
                var parentNode = sourceJsTree.get_node(parentNodeId);
                parentNode.state = 'leaf';
            }
        });
    };

    TypeMapperRenderer.prototype.enableParentsJsTree = function (connectionId, self, connections, isSource) {
        var jsTreeContainerPrefix = 'jstree-container';
        var sourceJsTreeId = jsTreeContainerPrefix + self.viewIdSeperator + self.getStructId(connectionId);
        var sourceJsTree = $("#" + sourceJsTreeId).jstree(true);
        var node = sourceJsTree.get_node(connectionId.replace('_anchor', ''));
        _.forEach(node.parents, function (parentNodeId) {
            if (parentNodeId !== '#' && !self.isChildConnectionExists(sourceJsTree, self, connections, isSource)) {
                var parentNode = sourceJsTree.get_node(parentNodeId);
                parentNode.state = {
                    disabled: false,
                    loaded: true,
                    opened: true,
                    selected: false
                };
            }
        });
    };

    TypeMapperRenderer.prototype.isChildConnectionExists = function (jsTreeNode, self, connections, isSource) {
        var childNodes = jsTreeNode.children_d;
        var child = _.find(childNodes, function (childId) {
            var connection = _.find(connections, function (connection) {
                if (isSource) {
                    return childId.includes(connection.sourceId);
                } else {
                    return childId.includes(connection.targetId);
                }
            });
            if (!_.isUndefined(connection)) {
                return true;
            }
        });
        return !_.isUndefined(child);
    };

    TypeMapperRenderer.prototype.removeTarget = function (element, self) {
        self.jsPlumbInstance.unmakeTarget(element);
    };


    /**
     * Position Nodes with dagre
     * @param {string} viewId type mapper view identifier
     * @param jsPlumbInstance jsPlumb instance of the type mapper to be repositioned
     */
    TypeMapperRenderer.prototype.dagrePosition = function (self) {
        // construct dagre graph from this.jsPlumbInstance graph
        var graph = new dagre.graphlib.Graph();
        var alignment = 'LR';
        var nodes = []
        var sourceIndex;
        var targetIndex;
        var structs = $("#" + self.placeHolderName + "> .struct");
        var funcs = $("#" + self.placeHolderName + "> .func");
        if (self.jsPlumbInstance.getAllConnections() == 0) {
            alignment = 'TD';
        }

        graph.setGraph({ranksep: '100', rankdir: alignment, edgesep: '100', marginx: '0'});
        graph.setDefaultEdgeLabel(function () {
            return {};
        });

        if (structs.length > 1) {
            if ($(structs[0]).attr("type") == "source") {
                sourceIndex = 0;
                targetIndex = 1;
            } else {
                sourceIndex = 1;
                targetIndex = 0;
            }

            nodes.push(structs[sourceIndex]);
            _.forEach(funcs, function (func) {
                nodes.push(func);
            });
            nodes.push(structs[targetIndex]);
        } else {
            if (structs != null) {
                nodes = structs;
            }
            _.forEach(funcs, function (func) {
                nodes.push(func);
            });
        }

        if (nodes.length > 0) {
            var maxTypeHeight = 0;
            var maxYPosition = 0;

            _.forEach(nodes, function (n) {
                var nodeContent = $("#" + n.id);
                if (maxTypeHeight < nodeContent.height()) {
                    maxTypeHeight = nodeContent.height();
                }
                graph.setNode(n.id, {width: nodeContent.width(), height: nodeContent.height()});
            });

            var edges = self.jsPlumbInstance.getAllConnections();

            _.forEach(edges, function (edge) {
                var source = edge.source.id.split(self.idNameSeperator)[0];
                var target = edge.target.id.split(self.idNameSeperator)[0];
                var sourceId;
                var targetId;

                //checks whether target and source is a generic type or a function
                if (source.includes(self.jsTreePrefix)) {
                    sourceId = source.split(self.viewIdSeperator)[1] + self.viewIdSeperator
                        + source.split(self.viewIdSeperator)[2];
                } else {
                    sourceId = source;
                }

                if (target.includes(self.jsTreePrefix)) {
                    targetId = target.split(self.viewIdSeperator)[1]
                        + self.viewIdSeperator + target.split(self.viewIdSeperator)[2];
                } else {
                    targetId = target;
                }

                graph.setEdge(sourceId, targetId);
            });
            // calculate the layout (i.e. node positions)
            dagre.layout(graph);

            // Applying the calculated layout
            _.forEach(graph.nodes(), function (dagreNode) {
                var node = $("#" + dagreNode);
                node.css("left", graph.node(dagreNode).x + "px");
                node.css("top", graph.node(dagreNode).y + "px");
                // }

                if (graph.node(dagreNode) != null && graph.node(dagreNode).y > maxYPosition) {
                    maxYPosition = graph.node(dagreNode).y;
                }
            });

            $("#" + self.placeHolderName).height(maxTypeHeight + maxYPosition + 55);
            self.jsPlumbInstance.repaintEverything();
        }
    };

    /**
     * Give the flow chart object array for given midpoint
     * @param {int} midPoint point which flow chart connection should bend
     * @returns {*[]} flow chart object array
     */
    TypeMapperRenderer.prototype.getConnectorConfig = function (midPoint) {
        return ["Flowchart", {
            midpoint: midPoint,
            stub: [40, 60], cornerRadius: 5, alwaysRespectStubs: true
        }]
    }

    return TypeMapperRenderer;
});