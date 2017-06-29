
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

import _ from 'lodash';
import $ from 'jquery';
import jsPlumbLib from 'jsplumb';
import './transform-statement.css';
/**
 * Renderer constructor for TypeMapper
 * @param {function} onConnectionCallback call back function when connection made
 * @param {function} onDisconnectCallback call back function when connection removed
 * @param {object} typeConverterView Type Mapper View reference object
 * @constructor
 */
const jsPlumb = jsPlumbLib.jsPlumb;


class TransformRender {
    constructor(onConnectionCallback, onDisconnectCallback) {
        this.references = [];
        this.viewId = 'transformer';
        this.contextMenu = 'transformContextMenu';
        this.jsTreePrefix = 'jstree-container';
        this.viewIdSeperator = '___';
        this.sourceTargetSeperator = '_--_';
        this.idNameSeperator = '_-_-_-';
        this.nameTypeSeperator = '---';
        this.placeHolderName = 'transformOverlay-content';
        this.onConnection = onConnectionCallback;
        this.midpoint = 0.1;
        this.midpointVariance = 0.01;
        this.disconnectCallback = onDisconnectCallback;
        this.connectCallback = onConnectionCallback;
        this.connectionPool = [];
        this.existingJsTrees = [];
        const self = this;

        this.jsPlumbInstance = jsPlumb.getInstance({
            Connector: self.getConnectorConfig(self.midpoint),
            Container: this.placeHolderName,
            PaintStyle: {
                strokeWidth: 1,
            // todo : load colors from css
                stroke: '#666769',
                cssClass: 'plumbConnect',
                outlineStroke: '#F7F7F7',
                outlineWidth: 2,
            },
            HoverPaintStyle: {
                strokeWidth: 2,
                stroke: '#ff9900',
                outlineWidth: 2,
                outlineStroke: '#ffe0b3',
            },
            EndpointStyle: { radius: 1 },
            ConnectionOverlays: [
                ['Arrow', {
                    location: 1,
                    visible: true,
                    width: 6,
                    length: 6,
                    foldback: 1,
                }],
                ['Custom', {
                    create(component) {
                        return $('<select id=\'typeMapperList' + self.viewIdSeperator + self.viewId + '\'></select>');
                    },
                    location: 0.5,
                    id: 'typeMapperDropdown',
                    cssClass: 'typeMapperList',
                }],
            ],
        });

        $('#' + self.contextMenu).hide();
        this.jsPlumbInstance.bind('contextmenu', (connection, e) => {
            const contextMenuDiv = $('#' + self.contextMenu);
            const anchorTag = $('<a>').attr('id', 'typeMapperConRemove').attr('class', 'type-mapper-con-remove');
            anchorTag.html($('<i>').addClass('fw fw-delete'));
            anchorTag.html(anchorTag.html() + ' Remove');
            contextMenuDiv.html(anchorTag);

            document.addEventListener('click', (eClick) => {
                if (eClick.explicitOriginalTarget == null || eClick.explicitOriginalTarget.nodeName != 'path') {
                    $('#' + self.contextMenu).hide();
                }
            }, false);

            $('.leftType, .middle-content, .rightType').scroll(() => {
                $('#' + self.contextMenu).hide();
            });

            $('#typeMapperConRemove').click(() => {
                self.disconnect(connection);
                $('#' + self.contextMenu).hide();
            });

            contextMenuDiv.css({
                top: e.pageY,
                left: e.pageX,
                zIndex: 1000,
            });

            contextMenuDiv.show();
            e.preventDefault();
        });


        this.jsPlumbInstance.bind('connection', (info, ev) => {
            self.reposition(self);
    // TODO: for multiple type mappers
    // self.processTypeMapperDropdown(info);
        });
    }

/**
 * Disconnects the connection created.
 *
 * @param connection
 */
    disconnect(connection) {
        const self = this;
        const propertyConnection = this.getConnectionObject(connection.getParameter('id'),
        connection.sourceId, connection.targetId);
        this.midpoint = this.midpoint - this.midpointVariance;
        this.jsPlumbInstance.importDefaults({ Connector: self.getConnectorConfig(self.midpoint) });
        this.jsPlumbInstance.detach(connection);
        this.reposition(this);
        this.disconnectCallback(propertyConnection);
    // this.enableParentsJsTree(connection.sourceId, this, this.jsPlumbInstance.getAllConnections(), true);
    // this.enableParentsJsTree(connection.targetId, this, this.jsPlumbInstance.getAllConnections(), false);
    }

/**
 * Created the connection object from the sourceId and targetId of the connection elements
 * @param sourceId Id of the source element of the connection
 * @param targetId Id of the target element of the connection
 * @returns connectionObject
 */
    getConnectionObject(id, sourceId, targetId) {
        const sourceName = this.getStructId(sourceId);
        const targetName = this.getStructId(targetId);

        let sourceRefObj;
        let targetRefObj;

        for (let i = 0; i < this.references.length; i++) {
            if (this.references[i].name == sourceName) {
                sourceRefObj = this.references[i].refObj;
            } else if (this.references[i].name == targetName) {
                targetRefObj = this.references[i].refObj;
            }
        }

        return {
            id,
            sourceStruct: this.getStructName(sourceName),
            sourceProperty: this.getPropertyNameStack(sourceId),
            sourceType: this.getPropertyType(sourceId),
            sourceReference: sourceRefObj,
            targetStruct: this.getStructName(targetName),
            targetProperty: this.getPropertyNameStack(targetId),
            targetType: this.getPropertyType(targetId),
            targetReference: targetRefObj,
            isComplexMapping: false,
            complexMapperName: null,
        };
    }

/**
 * Get the id of the struct from the propertyId
 * @param propertyId Id of the property
 * @returns {*}
 */
    getStructId(propertyId) {
        const id = propertyId.replace(this.jsTreePrefix + this.viewIdSeperator, '');
        return id.split(this.idNameSeperator)[0];
    }

/**
 * Get the struct name from the id of the struct
 * @param structId
 * @returns {*}
 */
    getStructName(structId) {
        return structId.split(this.viewIdSeperator)[0];
    }

/**
 * Get the type of the property from the property ID
 * @param propertyId
 * @returns {*}
 */
    getPropertyType(propertyId) {
        const parts = propertyId.split(this.idNameSeperator);
        return parts[parts.length - 1].split(this.nameTypeSeperator)[1].replace('_anchor', '');
    }

/**
 * Get the name of the property from the property id
 * @param propertyId
 * @returns {*}
 */
    getPropertyName(propertyId) {
        const parts = propertyId.split(this.idNameSeperator);
        return parts[parts.length - 1].split(this.nameTypeSeperator)[0];
    }

/**
 * Populate the attribute properties stack from the propertyId
 * @param propertyId
 * @returns {Array}
 */
    getPropertyNameStack(propertyId) {
        const id = propertyId.replace(this.jsTreePrefix + this.viewIdSeperator, '');
        const parts = id.split(this.idNameSeperator);
        const propertyNames = [];
        const self = this;
        let elementId = 0;
        _.forEach(parts, (aPart) => {
            if (elementId != 0) {
                propertyNames.push(aPart.split(self.nameTypeSeperator)[0]);
            } else {
                elementId++;
            }
        });
        return propertyNames;
    }

/**
 * Process the connection and determines whether to show/hide the dropdown from the connection.
 * @param info connection object.
 */
    processTypeMapperDropdown(info) {
        const sourceType = this.getPropertyType(info.sourceId);
        const targetType = this.getPropertyType(info.targetId);
        const isValidTypes = sourceType == targetType;
        const self = this;
        if (!isValidTypes) {
            const connection = info.connection;
            connection.getOverlay('typeMapperDropdown').show();
            const typeMapperId = '#typeMapperList' + this.viewIdSeperator + this.viewId;
            const updatedTypeMapperId = 'typeMapperList' + this.viewIdSeperator + connection.sourceId
            + this.sourceTargetSeperator + connection.targetId;
            const typeMappers = this.getExistingTypeMappers(this.typeConverterView, sourceType, targetType);
            $.each(typeMappers, (i, item) => {
                $(typeMapperId).append($('<option>', {
                    value: item,
                    text: item,
                }));
            });
            $(typeMapperId).attr('id', updatedTypeMapperId);
            $('#' + updatedTypeMapperId).change(() => {
                self.onChangeTypeMapper(updatedTypeMapperId);
            });
        } else {
            info.connection.getOverlay('typeMapperDropdown').hide();
        }
    }

/**
 * Finds the existing type mappers defined.
 * @param typeConverterObj
 * @param sourceType
 * @param targetType
 * @returns {Array}
 */
    getExistingTypeMappers(typeConverterObj, sourceType, targetType) {
        const compatibleTypeConverters = [];
        const typeConverters = typeConverterObj.getPackage().getTypeMapperDefinitions();
        _.forEach(typeConverters, (typeConverter) => {
            if (typeConverterObj.getModel().getTypeMapperName() !== typeConverter.getTypeMapperName()) {
                if (sourceType == typeConverter.getInputParamAndIdentifier().split(' ')[0] &&
            targetType == typeConverter.getReturnType()) {
                    compatibleTypeConverters.push(typeConverter.getTypeMapperName());
                }
            }
        });
        return compatibleTypeConverters;
    }

/**
 * The integrates the source manipluation trigger for the change in the
 * dropdown of the complex mapping.
 *
 * @param listId Id of the dropdown.
 */
    onChangeTypeMapper(listId) {
        const id = listId.replace('typeMapperList' + this.viewIdSeperator, '');
        const sourceId = id.split(this.sourceTargetSeperator)[0];
        const targetId = id.split(this.sourceTargetSeperator)[1];
        const connection = this.getConnectionObject(id, sourceId, targetId);
        this.disconnectCallback(connection);
        connection.isComplexMapping = true;
        connection.complexMapperName = $('#' + listId + ' option:selected').val();
        this.connectCallback(connection);
    }

/**
 * Remove a type from the mapper UI
 * @param {string} name identifier of the type
 */
    removeType(name) {
        const typeId = name + this.viewIdSeperator + this.viewId;
        if ($('#' + typeId).attr('class') != null) {
            let typeConns;
            let lookupClass = 'property';

            if ($('#' + typeId).attr('class').includes('struct')) {
                lookupClass = 'jstree-anchor';
                typeConns = $('div[id^="' + this.jsTreePrefix + this.viewIdSeperator + typeId + '"]')
                .find('.' + lookupClass);
            } else if ($('#' + typeId).attr('class').includes('variable')) {
                lookupClass = 'variable-content';
                typeConns = $('div[id^="' + typeId + '"]').find('.' + lookupClass);
            } else {
                typeConns = $('div[id^="' + typeId + '"]');
            }

            const self = this;
            _.forEach(typeConns, (structCon) => {
                if (_.includes(structCon.className, lookupClass)) {
                    self.jsPlumbInstance.remove(structCon.id);
                }
            });
            $('#' + typeId).remove();
            this.reposition(this);
        }
    }

/**
 * Add a connection arrow in the mapper UI
 * @param {object} connection connection object which specified source and target
 */
    addConnection(connection) {
        const anchorEnd = '_anchor';
        let sourceId = this.jsTreePrefix + this.viewIdSeperator + connection.sourceStruct
        + this.viewIdSeperator + this.viewId;
        let targetId = this.jsTreePrefix + this.viewIdSeperator + connection.targetStruct
        + this.viewIdSeperator + this.viewId;
        let isSourceExists;
        let isTargetExists;

        if (connection.sourceFunction) {
            sourceId = connection.sourceStruct + connection.sourceId + this.viewIdSeperator + this.viewId;
            isSourceExists = true;
        } else if (connection.sourceStruct == connection.sourceProperty[0]) {
            // Construct Variable property id
            sourceId = connection.sourceStruct;
            isSourceExists = true;
        } else {
            isSourceExists = _.includes(this.existingJsTrees,
                connection.sourceStruct + this.viewIdSeperator + this.viewId);
        }
        if (connection.targetFunction) {
            targetId = connection.targetStruct + connection.targetId + this.viewIdSeperator + this.viewId;
            isTargetExists = true;
        } else if (connection.targetStruct == connection.targetProperty[0]) {
            // Construct Variable property id
            targetId = connection.targetStruct;
            isTargetExists = true;
        } else {
            isTargetExists = _.includes(this.existingJsTrees,
                connection.targetStruct + this.viewIdSeperator + this.viewId);
        }

        if (isSourceExists && isTargetExists) {
            for (var i = 0; i < connection.sourceProperty.length; i++) {
                sourceId += this.idNameSeperator
                + connection.sourceProperty[i] + this.nameTypeSeperator + connection.sourceType[i];
            }
            if (!connection.sourceFunction && connection.sourceStruct != connection.sourceProperty[0]) {
                sourceId += anchorEnd;
            }

            for (var i = 0; i < connection.targetProperty.length; i++) {
                targetId += this.idNameSeperator
                + connection.targetProperty[i] + this.nameTypeSeperator + connection.targetType[i];
            }

            if (!connection.targetFunction && connection.targetStruct != connection.targetProperty[0]) {
                targetId += anchorEnd;
            }

            this.jsPlumbInstance.connect({
                source: sourceId,
                target: targetId,
                parameters: { id: connection.id },
            });
            this.reposition(this);
        } else {
            this.connectionPool.push({
                connection,
                isSourceExists,
                isTargetExists,
                connected: false,
            });
        }
    }


/**
 * Add a source type in the mapper UI
 * @param {object} type definition with parameters to be mapped
 * @param {object} reference AST model reference
 */
    addSourceType(struct, removeCallback) {
        const id = struct.name + this.viewIdSeperator + this.viewId;
        struct.id = id;
        this.makeType(struct, removeCallback, 50, 10, 'source');
        const jsTreeId = this.jsTreePrefix + this.viewIdSeperator + id;
        this.addComplexProperty(jsTreeId, struct);
        this.processJSTree(jsTreeId, id, this.addSource);
    }

/**
 * Manipulates the jstree structure and the jsPlumb connections
 *
 * @param jsTreeId
 * @param structId
 * @param createCallback
 */
    processJSTree(jsTreeId, structId, createCallback) {
        const self = this;
    // Disable jstree collapsing
        $.jstree.plugins.noclose = function () {
            this.close_node = $.noop;
        };
        $('#' + jsTreeId).jstree({ plugins: ['noclose'] }).on('ready.jstree', () => {
            const sourceElements = $('#' + structId).find('.jstree-anchor');
            _.forEach(sourceElements, (element) => {
                createCallback(element, self);
            });
            $('#' + jsTreeId).jstree('open_all');
            self.existingJsTrees.push(structId);
            self.reposition(self);
            _.forEach(self.connectionPool, (conPoolObj) => {
                if (!conPoolObj.connected && structId ===
                        conPoolObj.connection.sourceStruct + self.viewIdSeperator + self.viewId) {
                    conPoolObj.isSourceExists = true;
                } else if (!conPoolObj.connected && structId ===
                        conPoolObj.connection.targetStruct + self.viewIdSeperator + self.viewId) {
                    conPoolObj.isTargetExists = true;
                }
                if (!conPoolObj.connected && conPoolObj.isSourceExists && conPoolObj.isTargetExists) {
                    self.addConnection(conPoolObj.connection);
                    conPoolObj.connected = true;
                }
            });
        }).on('after_open.jstree', (event, data) => {
            self.reposition(self);
            const parentId = data.node.id;
            const sourceElements = $('#' + parentId).find('.jstree-anchor');
            _.forEach(sourceElements, (element) => {
                createCallback(element, self);
            });
            self.jsPlumbInstance.repaintEverything();
        }).on('after_close.jstree', (event, data) => {
            self.reposition(self);
            self.jsPlumbInstance.repaintEverything();
        }).on('select_node.jstree', (event, data) => {
            data.instance.deselect_node(data.node);
        });
    }

    repaintAll(jsTreeId) {
        const children = $('#' + jsTreeId).jstree().get_node('#').children_d;
        _.forEach(children, (child) => {
            self.jsPlumbInstance.repaint(child.id + '_anchor');
        });
    }

/**
 * Handles the complex struct properties.
 *
 * @param parentId Id of the parentElement where the element needs to be added.
 * @param struct  Object which specifies the id, name, and type of the struct.
 */
    addComplexProperty(parentId, struct) {
        const self = this;
        _.forEach(struct.properties, (property) => {
            if (property.innerType != null && property.innerType.properties.length > 0) {
                const complexStructEl = self.makeProperty($('#' + parentId), property.name, property.type);
                self.addComplexProperty(complexStructEl.attr('id'), property.innerType);
            } else {
                self.makeProperty($('#' + parentId), property.name, property.type);
            }
        });
    }

/**
 * Add a target struct in the mapper UI
 * @param {object} struct definition with parameters to be mapped
 * @param {object} reference AST model reference
 */
    addTargetType(struct, removeCallback) {
        const id = struct.name + this.viewIdSeperator + this.viewId;
        struct.id = id;
        this.makeType(struct, removeCallback, 50, 10, 'target');
        const jsTreeId = this.jsTreePrefix + this.viewIdSeperator + id;
        this.addComplexProperty(jsTreeId, struct);
        this.processJSTree(jsTreeId, id, this.addTarget);
    }

/**
 * Make generic struct div element in the UI
 * @param {object} struct definition with parameters to be mapped
 * @param {int} posX X position cordinate
 * @param {int} posY Y position cordinate
 * @param {object} reference
 */
    makeType(struct, removeCallback, posX, posY, type) {
        const newStruct = $('<div>').attr('id', struct.id).attr('type', type).addClass('struct');
        const structIcon = $('<i>').addClass('type-mapper-icon fw fw-struct');
        const structName = $('<div>');
        const closeButton = $('<span>').attr('id', struct.id + '-button').addClass('fw-stack fw-lg btn btn-remove');
        const del = $('<i>').addClass('fw fw-delete fw-stack-1x');
        structName.append(structIcon);
        structName.append($('<span>').text(struct.name + ' : ' + struct.typeName));
        newStruct.append(structName);
        closeButton.append(del);
        structName.append(closeButton);
        let subPlaceHolder;

        if (type == 'source') {
            subPlaceHolder = 'leftType';
        } else {
            subPlaceHolder = 'rightType';
        }

        newStruct.css({
            top: posX,
            left: posY,
        });
        const jsTreeContainer = $('<div>').attr('id', this.jsTreePrefix + this.viewIdSeperator + struct.id)
        .addClass('tree-container');
        newStruct.append(jsTreeContainer);
        $('#' + this.placeHolderName).find('.' + subPlaceHolder).append(newStruct);
        this.onRemove(struct.id, struct, removeCallback, struct.name);
    }


    addVariable(variable, type, removeCallback) {
        const id = variable.name + this.viewIdSeperator + this.viewId;
        const propId = variable.name + this.idNameSeperator + variable.name + this.nameTypeSeperator + variable.type;
        const newVar = $('<div>').attr('id', id).attr('type', type).addClass('variable');
        const varIcon = $('<i>').addClass('type-mapper-icon fw fw-variable');
        const property = $('<a>').attr('id', propId).addClass('variable-content');
        const propertyName = $('<span>').addClass('property-name').text(variable.name);
        const seperator = $('<span>').addClass('property-name').text(':');
        const propertyType = $('<span>').addClass('property-type').text(variable.type);
        const closeButton = $('<span>').attr('id', id + '-button').addClass('fw-stack fw-lg btn btn-remove');
        const del = $('<i>').addClass('fw fw-delete fw-stack-1x');
        newVar.append(varIcon);
        property.append(propertyName);
        property.append(seperator);
        property.append(propertyType);
        closeButton.append(del);
        newVar.append(property);
        newVar.append(closeButton);
        let subPlaceHolder;


        newVar.css({
            top: 0,
            left: 0,
        });

        if (type == 'source') {
            subPlaceHolder = 'leftType';
            $('#' + this.placeHolderName).find('.leftType').append(newVar);
            this.addSource(property, this, false);
        } else {
            subPlaceHolder = 'rightType';
            $('#' + this.placeHolderName).find('.rightType').append(newVar);
            this.addTarget(property, this);
        }
        this.onRemove(id, variable, removeCallback, variable.name);
        this.reposition(this);
    }


/**
 * Add a function in the mapper UI
 * @param {object} function definition with parameters to be mapped
 * @param {object} reference AST model reference
 * @param {function} onFunctionRemove call back function for function remove
 */
    addFunction(func, reference, onFunctionRemove) {
        const packageName = func.getPackageName().replace(' ', '');
        let funcName = _.isEmpty(packageName) ? func.getName() :
                                  packageName + ' : ' + func.getName();
        const funcText = func.getName();
        // Allow multiple functions to drag and drop without conflicting
        const functionInvocationModelId = reference.getID();
        func.name = (_.isEmpty(packageName) ? func.getName() : packageName + '-' + func.getName()) +
                   functionInvocationModelId;

        const id = func.name + this.viewIdSeperator + this.viewId;
        if ($('#' + id).length === 0) {
            this.references.push({ name: id, refObj: reference });
            const newFunc = $('<div>').attr('id', id).addClass('func');
            const self = this;
            funcName = $('<div>');
            const funcIcon = $('<i>').addClass('type-mapper-icon fw fw-function fw-inverse');
            const closeButton = $('<span>').attr('id', id + '-button').addClass('fw-stack fw-lg btn btn-remove');
            const outputContent = $('<div>').attr('id', id + 'func-output').addClass('func-output');

            const del = $('<i>').addClass('fw fw-delete fw-stack-1x fw-inverse');

            funcName.append(funcIcon);
            funcName.append($('<span>').text(funcText));
            closeButton.append(del);
            funcName.append(closeButton);
            newFunc.append(funcName);
            newFunc.append(outputContent);

            newFunc.css({
                top: 0,
                left: 0,
            });

            $('#' + this.placeHolderName).find('.middle-content').append(newFunc);
            this.onRemove(id, func, onFunctionRemove, reference);

            _.forEach(func.getParameters(), (parameter) => {
                const property = self.makeFunctionAttribute($('#' + id), parameter.name, parameter.type, true);
                self.addTarget(property, self);
            });

            _.forEach(func.getReturnParams(), (parameter) => {
                const property = self.makeFunctionAttribute($('#' + id + 'func-output'), parameter.name, parameter.type, false);
                self.addSource(property, self, true);
            });

            self.reposition(this);
        }
    }

    makeFunctionAttribute(parentId, name, type, input) {
        const id = parentId.selector.replace('#', '').replace('func-output', '')
        + this.idNameSeperator + name + this.nameTypeSeperator + type;
        let property;
        if (input) {
            property = $('<div>').attr('id', id).addClass('func-in-property');
        } else {
            property = $('<div>').attr('id', id).addClass('func-out-property');
        }
        const propertyName = $('<span>').addClass('property-name').text(name);
        const seperator = $('<span>').addClass('property-name').text(':');
        const propertyType = $('<span>').addClass('property-type').text(type);

        property.append(propertyName);
        property.append(seperator);
        property.append(propertyType);
        $(parentId).append(property);
        return property;
    }

/**
 * Make Property DIV element
 * @param {string} parentId identifier of the parent of the property
 * @param {string} name property name
 * @param {string} type property type
 * @returns {*|jQuery}
 */
    makeProperty(parentId, name, type) {
        const id = parentId.selector.replace('#', '') + this.idNameSeperator + name + this.nameTypeSeperator + type;
        const ul = $('<ul class="property">');
        const li = $('<li class="property">').attr('id', id).text(name + ' : ' + type);
        ul.append(li);
        $(parentId).append(ul);
        return li;
    }

/**
 * Make source property
 *
 * @param element
 * @param self
 */
    addSource(element, self, maxConnections) {
        const connectionConfig = {
            anchor: ['Right'],
        };
        if (maxConnections) {
            connectionConfig.maxConnections = 1;
        }
        self.jsPlumbInstance.makeSource(element, connectionConfig);
    }

/**
 * Remove the source element
 *
 * @param elements
 * @param self
 */
    removeSource(elements, self) {
        self.jsPlumbInstance.unmakeSource(elements);
    }

/**
 * Specifies connection has a function
 * @param {object} connection
 * @returns {boolean} has a connection or not
 */
    hasFunction(connection, self) {
        return $('#' + connection.sourceStruct + self.viewIdSeperator + self.viewId).attr('class').includes('func')
        || $('#' + connection.targetStruct + self.viewIdSeperator + self.viewId).attr('class').includes('func');
    }

/**
 * Make target property
 * @param element
 * @param self
 */
    addTarget(element, self) {
        self.jsPlumbInstance.makeTarget(element, {
            maxConnections: 1,
            anchor: ['Left'],
            beforeDrop(params) {
            // Checks property types are equal
                const isValidTypes = self.getPropertyType(params.sourceId).toLowerCase()
                                        == self.getPropertyType(params.targetId).toLowerCase();
                const connection = self.getConnectionObject(params.id, params.sourceId, params.targetId);
                if (isValidTypes) {
                    self.midpoint += self.midpointVariance;
                    self.jsPlumbInstance.importDefaults({ Connector: self.getConnectorConfig(self.midpoint) });
                    connection.id = self.onConnection(connection);
                    params.connection.setParameter('id', connection.id);
                }
                return isValidTypes;
            },
        })
    ;
    }

    disableParentsJsTree(connectionId, self) {
        const sourceJsTreeId = this.jsTreePrefix + self.viewIdSeperator + self.getStructId(connectionId);
        const sourceJsTree = $('#' + sourceJsTreeId).jstree(true);
        const node = sourceJsTree.get_node(connectionId.replace('_anchor', ''));
        _.forEach(node.parents, (parentNodeId) => {
            if (parentNodeId !== '#') {
                const parentNode = sourceJsTree.get_node(parentNodeId);
                parentNode.state = 'leaf';
            }
        });
    }

    enableParentsJsTree(connectionId, self, connections, isSource) {
        const sourceJsTreeId = this.jsTreePrefix + self.viewIdSeperator + self.getStructId(connectionId);
        const sourceJsTree = $('#' + sourceJsTreeId).jstree(true);
        const node = sourceJsTree.get_node(connectionId.replace('_anchor', ''));
        _.forEach(node.parents, (parentNodeId) => {
            if (parentNodeId !== '#' && !self.isChildConnectionExists(sourceJsTree, self, connections, isSource)) {
                const parentNode = sourceJsTree.get_node(parentNodeId);
                parentNode.state = {
                    disabled: false,
                    loaded: true,
                    opened: true,
                    selected: false,
                };
            }
        });
    }

    isChildConnectionExists(jsTreeNode, self, connections, isSource) {
        const childNodes = jsTreeNode.children_d;
        const child = _.find(childNodes, (childId) => {
            const connection = _.find(connections, (connection) => {
                if (isSource) {
                    return childId.includes(connection.sourceId);
                }
                return childId.includes(connection.targetId);
            });
            if (!_.isUndefined(connection)) {
                return true;
            }
        });
        return !_.isUndefined(child);
    }

    removeTarget(element, self) {
        self.jsPlumbInstance.unmakeTarget(element);
    }

/**
 * Get list of connections for provided property of a source struct
 * @param {string} structName
 * @param {Array} property name hierarchy of the property
 * @returns {Array} List of connections
 */
    getSourceConnectionsByProperty(structName, property, type) {
        const self = this;
        const connections = [];
        for (var i = 0; i < property.length; i++) {
            _.forEach(self.jsPlumbInstance.getAllConnections(), (connection) => {
                if (connection.sourceId.includes(structName + self.viewIdSeperator + self.viewId
                + self.idNameSeperator + property[i] + self.nameTypeSeperator + type[i])) {
                    connections.push(self.getConnectionObject(connection.getParameter('id'),
                connection.sourceId, connection.targetId));
                }
            });

            _.forEach(connections, (connection) => {
                self.jsPlumbInstance.detach(connection);
            });
        }

        return connections;
    }

/**
 * Get list of connections for provided property of a target struct
 * @param {string} structName
 * @param {Array} property name hierarchy of the property
 * @returns {Array} List of connections
 */
    getTargetConnectionsByProperty(structName, property, type) {
        const self = this;
        const connections = [];
        for (var i = 0; i < property.length; i++) {
            _.forEach(self.jsPlumbInstance.getAllConnections(), (connection) => {
                if (connection.targetId.includes(structName + self.viewIdSeperator + self.viewId
                + self.idNameSeperator + property[i] + self.nameTypeSeperator + type[i])) {
                    connections.push(self.getConnectionObject(connection.getParameter('id'),
                connection.sourceId, connection.targetId));
                }
            });

            _.forEach(connections, (connection) => {
                self.jsPlumbInstance.detach(connection);
            });
        }
        return connections;
    }

/**
 * Get list of connections for provided source struct
 * @param {string} structName
 * @returns {Array} List of connections
 */
    getSourceConnectionsByStruct(structName) {
        const self = this;
        const connections = [];
        _.forEach(self.jsPlumbInstance.getAllConnections(), (connection) => {
            if (connection.sourceId.includes(structName)) {
                connections.push(self.getConnectionObject(connection.getParameter('id'),
            connection.sourceId, connection.targetId));
            }
        });

        _.forEach(connections, (connection) => {
            self.jsPlumbInstance.detach(connection);
        });

        return connections;
    }

/**
 * Get list of connections for provided target struct
 * @param {string} structName
 * @returns {Array} List of connections
 */
    getTargetConnectionsByStruct(structName) {
        const self = this;
        const connections = [];
        _.forEach(self.jsPlumbInstance.getAllConnections(), (connection) => {
            if (connection.targetId.includes(structName)) {
                connections.push(self.getConnectionObject(connection.getParameter('id'),
            connection.sourceId, connection.targetId));
            }
        });

        _.forEach(connections, (connection) => {
            self.jsPlumbInstance.detach(connection);
        });

        return connections;
    }

/**
 * Reposition function nodes and redraw connections accordingly
 * @param {string} viewId type mapper view identifier
 * @param jsPlumbInstance jsPlumb instance of the type mapper to be repositioned
 */
    reposition(self) {
        const funcs = $('.middle-content  > .func');
        const sourceStructs = $('.leftType > .struct, .leftType > .variable');
        const targetStructs = $('.rightType > .struct, .rightType > .variable');
        const xFunctionPointer = ($('.middle-content').width() - 300) / 2;
        let yFunctionPointer = 120;
        const xSourcePointer = 0;
        let ySourcePointer = 0;
        const xTargetPointer = 0;
        let yTargetPointer = 0;
        const functionGap = 30;
        const svgLines = $('#' + self.placeHolderName + '> svg');

        // Traverse through all the connection svg lines
        _.forEach(svgLines, (svgLine) => {
            // Get bottom and right values relative to the type mapper parent div
            const arrowBotton = svgLine.children[2].getBoundingClientRect().bottom - $('.middle-content').position().top;
            const right = svgLine.getBoundingClientRect().right;

            // Calculate the yFunctionPointer value  based on the bottom value of the direct connections
            if (arrowBotton > yFunctionPointer && svgLine.getBoundingClientRect().width > 600) {
                yFunctionPointer = arrowBotton;
            }
        });

        // Traverse through all the function divs
        _.forEach(funcs, (func) => {
            // Position functions and increase yFunctionPointer with gaps
            $('#' + func.id).css('left', xFunctionPointer + 'px');
            $('#' + func.id).css('top', yFunctionPointer + 'px');
            yFunctionPointer += $('#' + func.id).height() + functionGap;
        });

        _.forEach(sourceStructs, (structType) => {
            // Position functions and increase yFunctionPointer with gaps
            $('#' + structType.id).css('left', xSourcePointer + 'px');
            $('#' + structType.id).css('top', ySourcePointer + 'px');
            ySourcePointer += $('#' + structType.id).height() + functionGap;
        });

        _.forEach(targetStructs, (structType) => {
            // Position functions and increase yFunctionPointer with gaps
            $('#' + structType.id).css('left', xTargetPointer + 'px');
            $('#' + structType.id).css('top', yTargetPointer + 'px');
            yTargetPointer += $('#' + structType.id).height() + functionGap;
        });
        self.jsPlumbInstance.repaintEverything();
    }

/**
 * Give the flow chart object array for given midpoint
 * @param {int} midPoint point which flow chart connection should bend
 * @returns {*[]} flow chart object array
 */
    getConnectorConfig(midPoint) {
        return ['Flowchart', {
            midpoint: midPoint,
            stub: [40, 60],
            cornerRadius: 5,
            alwaysRespectStubs: true,
        }];
    }

/**
 * Bind the onRemove click event and callback to given container
 * @param {string} unique identifier
 * @param {object} container information
 * @param {function} onRemoveFunction callback
 * @param {int} Reference AST Node id
 */
    onRemove(id, container, removeFunction, reference) {
        $('#' + id + '-button').on('click', () => {
            const removedFunction = { name: container.name };
            removedFunction.incomingConnections = [];
            removedFunction.outgoingConnections = [];

            _.forEach(this.jsPlumbInstance.getAllConnections(), (connection) => {
                if (connection.target.id.includes(id)) {
                    removedFunction.incomingConnections.push(
            this.getConnectionObject(connection.getParameter('id'), connection.sourceId, connection.targetId));
                } else if (connection.source.id.includes(id)) {
                    removedFunction.outgoingConnections.push(
                        this.getConnectionObject(connection.getParameter('id'), connection.sourceId, connection.targetId));
                }
            });

            for (let i = 0; i < this.references.length; i++) {
                if (this.references[i].name === id) {
                    removedFunction.reference = this.references[i].refObj;
                }
            }

            this.removeType(container.name);
            removeFunction(reference);
        });
    }

}

export default TransformRender;
