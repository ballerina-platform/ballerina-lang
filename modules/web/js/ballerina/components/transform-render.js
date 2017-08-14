
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
    constructor(onConnectionCallback, onDisconnectCallback, container) {
        this.container = container;
        this.references = [];
        this.placeHolderName = 'transformOverlay-content';
        this.viewId = container.attr('id').replace(this.placeHolderName + '-', '');
        this.contextMenu = 'transformContextMenu';
        this.jsTreePrefix = 'jstree-container';
        this.viewIdSeperator = ':';
        this.sourceTargetSeperator = '_--_';
        this.idNameSeperator = '.';
        this.nameTypeSeperator = ':';
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
            Container: container.attr('id'),
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
        this.container.find('#' + self.contextMenu).hide();
        this.jsPlumbInstance.bind('connection',function(params,ev){
            if (!_.isUndefined(ev)) {
              const input = params.connection.getParameters().input;
              const output = params.connection.getParameters().output;
              const sourceType = input.type;
              const targetType = output.type;
              let isValidTypes;

              if (sourceType === 'struct' || targetType === 'struct') {
                  isValidTypes = input.typeName == output.typeName;
              } else {
                  isValidTypes = sourceType === targetType || sourceType === 'any' || targetType === 'any'
                    || sourceType === 'json' || targetType === 'json';
              }

              const connection = self.getConnectionObject(params.id, input, output);
              if (isValidTypes) {
                  self.midpoint += self.midpointVariance;
                  self.jsPlumbInstance.importDefaults({ Connector: self.getConnectorConfig(self.midpoint) });
                  self.onConnection(connection);
              }
            }
        });
        this.jsPlumbInstance.bind('contextmenu', (connection, e) => {
            const contextMenuDiv = this.container.find('#' + self.contextMenu);
            const anchorTag = $('<a>').attr('id', 'typeMapperConRemove').attr('class', 'type-mapper-con-remove');
            anchorTag.html($('<i>').addClass('fw fw-delete'));
            anchorTag.html(anchorTag.html() + ' Remove');
            contextMenuDiv.html(anchorTag);

            document.addEventListener('click', (eClick) => {
                if (eClick.explicitOriginalTarget == null || eClick.explicitOriginalTarget.nodeName != 'path') {
                    this.container.find('#' + self.contextMenu).hide();
                }
            }, false);

            this.container.find('.leftType, .middle-content, .rightType').scroll(() => {
                this.container.find('#' + self.contextMenu).hide();
            });

            this.container.find('#typeMapperConRemove').click(() => {
                self.disconnect(connection);
                this.container.find('#' + self.contextMenu).hide();
            });

            contextMenuDiv.css({
                top: e.pageY,
                left: e.pageX,
                zIndex: 1000,
            });

            contextMenuDiv.show();
            e.preventDefault();
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
        connection.getParameter('input'), connection.getParameter('output'));
        this.midpoint = this.midpoint - this.midpointVariance;
        this.jsPlumbInstance.importDefaults({ Connector: self.getConnectorConfig(self.midpoint) });
        this.jsPlumbInstance.detach(connection);
        this.disconnectCallback(propertyConnection);
        this.unmarkConnected(connection.targetId);
        this.unmarkConnected(connection.sourceId);
        _.forEach(this.jsPlumbInstance.getConnections(), (con) => {
            if (con.sourceId == connection.sourceId) {
                this.markConnected(con.sourceId);
            }
        });
    }


/**
 * Disconnects all the connection created.
 * This does not remove the associated children from the model
 */
    disconnectAll(connection) {
        this.midpoint = 0.1;
        this.jsPlumbInstance.detachEveryConnection();
    }

/**
 * Created the connection object from the sourceId and targetId of the connection elements
 * @param sourceId Id of the source element of the connection
 * @param targetId Id of the target element of the connection
 * @returns connectionObject
 */
    getConnectionObject(id, source, target) {
        const sourceName = source.name;
        const targetName = target.name;

        let sourceReference;
        let rootSourceStructName;
        if (source.parentFunc || source.enclosingAssignmentStatement) {
            sourceReference = source.parentFunc || source.enclosingAssignmentStatement;
        }
        if (source.root) {
            sourceReference = source.root.enclosingAssignmentStatement;
            rootSourceStructName = source.root.name;
        }

        let targetReference;
        let rootTargetStructName;
        if (target.parentFunc || target.enclosingAssignmentStatement) {
            targetReference = target.parentFunc || target.enclosingAssignmentStatement;
        }
        if (target.root) {
            targetReference = target.root.enclosingAssignmentStatement;
            rootTargetStructName = target.root.name;
        }

        return {
            id,
            sourceStruct: rootSourceStructName || source.structName || sourceName,
            sourceProperty: source.fieldName,
            sourceIndex: source.index,
            sourceType: source.type,
            sourceFuncInv: source.funcInv,
            sourceReference,
            isSourceFunction: source.endpointKind.startsWith('function-'),
            targetStruct: rootTargetStructName || target.structName || targetName,
            targetProperty: target.fieldName,
            targetIndex: target.index,
            targetType: target.type,
            targetFuncInv: target.funcInv,
            targetReference,
            isTargetFunction: target.endpointKind.startsWith('function-'),
            isComplexMapping: false,
            complexMapperName: null,
        };
    }

/**
 * Remove a type from the mapper UI
 * @param {string} name identifier of the type
 */
    removeType(name) {
        _.forEach(this.jsPlumbInstance.getConnections(), (con) => {
            if (con.sourceId.split(this.viewIdSeperator)[0].split(this.idNameSeperator)[0] == name) {
                _.forEach($('.jtk-droppable'), (element) => {
                    if (element.id.split(this.viewIdSeperator)[0].split(this.idNameSeperator)[0] == name) {
                        this.jsPlumbInstance.remove(element.id);
                    }
                });
                this.unmarkConnected(con.targetId);
            } else if (con.targetId.split(this.viewIdSeperator)[0].split(this.idNameSeperator)[0] == name) {
                _.forEach($('.jtk-droppable'), (element) => {
                    if (element.id.split(this.viewIdSeperator)[0].split(this.idNameSeperator)[0] == name) {
                        this.jsPlumbInstance.remove(element.id);
                    }
                });
                this.jsPlumbInstance.remove(con.targetId);
                this.unmarkConnected(con.sourceId);
            }
        });
    }

    remove(elementId) {
        jsPlumb.detachAllConnections(elementId);
        jsPlumb.removeAllEndpoints(elementId);
        jsPlumb.detach(elementId);
    }

  addConnection(sourceId, targetId) {
        this.midpoint += this.midpointVariance;
        this.jsPlumbInstance.importDefaults({ Connector: this.getConnectorConfig(this.midpoint) });
        this.jsPlumbInstance.connect({
            source: sourceId,
            target: targetId,
        });
        this.markConnected(sourceId);
        this.markConnected(targetId);
    }
/**
 * Make source property
 *
 * @param element
 * @param self
 */
    addSource(element, self, maxConnections, input) {
        const connectionConfig = {
            anchor: ['Right'],
            parameters: {
                input,
            },
        };
        if (maxConnections) {
            connectionConfig.maxConnections = 1;
        }
        this.jsPlumbInstance.makeSource(element, connectionConfig);
    }
/**
 * Make target property
 * @param element
 * @param self
 */
    addTarget(element, self, output) {
        this.jsPlumbInstance.makeTarget(element, {
            maxConnections: 1,
            anchor: ['Left'],
            parameters: {
                output,
            },
            beforeDrop: (params) => {
                // Checks property types are equal or type is any
                const input = params.connection.getParameters().input;
                const sourceType = input.type;
                const targetType = output.type;
                let isValidTypes;
                if (sourceType === 'struct' || targetType === 'struct') {
                    isValidTypes = input.typeName == output.typeName;
                } else {
                    isValidTypes = sourceType === targetType || sourceType === 'any' || targetType === 'any'
                      || sourceType === 'json' || targetType === 'json';
                }
                return isValidTypes;
            },
        });
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
    reposition(viewId) {
        this.viewId = viewId;
        const funcs = this.container.find('.middle-content  > .func');
        const sourceStructs = this.container.find('.leftType').find('.jtk-droppable');
        const targetStructs = this.container.find('.rightType').find('.jtk-droppable');
        const xFunctionPointer = (this.container.find('.middle-content').width() - 300) / 2;
        let yFunctionPointer = 20;
        const xSourcePointer = 0;
        let ySourcePointer = 0;
        const xTargetPointer = 0;
        let yTargetPointer = 0;
        const functionGap = 0;
        const svgLines =   $('#'+this.placeHolderName+'-'+viewId+' > svg');
        // Traverse through all the connection svg lines
        _.forEach(svgLines, (svgLine) => {
            // Get bottom and right values relative to the type mapper parent div
            const arrowBotton = svgLine.children[2].getBoundingClientRect().bottom -
                (this.container.find('.middle-content').position().top + 120);
            const right = svgLine.getBoundingClientRect().right;
            // Calculate the yFunctionPointer value  based on the bottom value of the direct connections
            if (arrowBotton > yFunctionPointer && svgLine.getBoundingClientRect().width > 400) {
                yFunctionPointer = arrowBotton;
            }
        });
        // Traverse through all the function divs
        _.forEach(funcs, (func) => {
            // Position functions and increase yFunctionPointer with gaps
            this.container.find(func).css('top', yFunctionPointer + 'px');
            yFunctionPointer += this.container.find(func).height() + functionGap;
        });
        this.jsPlumbInstance.repaintEverything();
    }

/**
 * Give the flow chart object array for given midpoint
 * @param {int} midPoint point which flow chart connection should bend
 * @returns {*[]} flow chart object array
 */
    getConnectorConfig(midPoint) {
        return ['Flowchart', {
            midpoint: midPoint,
            stub: [30, 40],
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
        this.container.find('#' + id + '-button').on('click', () => {
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

  /**
   * mark specified endpoint in the UI
   * @param  {string} endpointId endpoint identifier to be marked
   */
  markConnected(endpointId){
    this.container.find(document.getElementById(endpointId)).removeClass("fw-circle-outline").addClass("fw-circle");
  }

  /**
   * unmark specified endpoint in the UI
   * @param  {string} endpointId endpoint identifier to be unmarked
   */
  unmarkConnected(endpointId){
    this.container.find(document.getElementById(endpointId)).removeClass("fw-circle").addClass("fw-circle-outline");
  }

}

export default TransformRender;
