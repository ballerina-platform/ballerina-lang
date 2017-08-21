
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
 * Renderer constructor for TransformRender
 * @param {function} onConnectionCallback call back function when connection made
 * @param {function} onDisconnectCallback call back function when connection removed
 * @param {object} typeConverterView Type Mapper View reference object
 * @constructor
 */
const jsPlumb = jsPlumbLib.jsPlumb;

class TransformRender {
    constructor(onConnectionCallback, onDisconnectCallback, container) {
        this.container = container;
        this.placeHolderName = 'transformOverlay-content';
        this.viewId = container.attr('id').replace(this.placeHolderName + '-', '');
        this.contextMenu = 'transformContextMenu';
        this.viewIdSeperator = ':';
        this.idNameSeperator = '.';
        this.onConnection = onConnectionCallback;
        this.midpoint = 0.1;
        this.midpointVariance = 0.01;
        this.disconnectCallback = onDisconnectCallback;
        this.connectCallback = onConnectionCallback;

        this.jsPlumbInstance = jsPlumb.getInstance({
            Connector: this.getConnectorConfig(this.midpoint),
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
            ],
        });
        this.jsPlumbInstance.bind('connection', (params,ev) => {
            if (!_.isUndefined(ev)) {
                const input = params.connection.getParameters().input;
                const output = params.connection.getParameters().output;
                const sourceType = input.type;
                const targetType = output.type;
                const connection = this.getConnectionObject(params.id, input, output);
                this.midpoint += this.midpointVariance;
                this.jsPlumbInstance.importDefaults({ Connector: this.getConnectorConfig(this.midpoint) });
                this.onConnection(connection);
                this.setConnectionMenu(params.connection);
            }
        });
    }

    /**
    * Disconnects the connection created.
    *
    * @param connection
    */
    disconnect(connection) {
        const propertyConnection = this.getConnectionObject(connection.getParameter('id'),
        connection.getParameter('input'), connection.getParameter('output'));
        this.midpoint = this.midpoint - this.midpointVariance;
        this.jsPlumbInstance.importDefaults({ Connector: this.getConnectorConfig(this.midpoint) });
        this.jsPlumbInstance.detach(connection);
        this.disconnectCallback(propertyConnection);
        this.unmarkConnected(connection.targetId);
        this.unmarkConnected(connection.sourceId);
        _.forEach(this.jsPlumbInstance.getConnections(), (con) => {
            if (con.sourceId === connection.sourceId) {
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
        this.hideConnectContextMenu(this.container.find('#' + this.contextMenu));
        this.setConnectionMenu(this.jsPlumbInstance.getConnections({ source: sourceId, target: targetId })[0]);
    }

    setConnectionMenu(connection) {
        if (!connection) {
            return;
        }
        connection.bind('mouseover', (conn, e) => {
            if (!this.container.find('#' + this.contextMenu).is(':visible')) {
                const contextMenuDiv = this.container.find('#' + this.contextMenu);
                const anchorTag = $('<a>').attr('id', 'transformConRemove').attr('class', 'transform-con-remove');
                anchorTag.html($('<i>').addClass('fw fw-delete'));
                anchorTag.html(anchorTag.html() + ' Remove');
                contextMenuDiv.html(anchorTag);
                this.container.find('.leftType, .middle-content, .rightType').scroll(() => {
                    this.hideConnectContextMenu(this.container.find('#' + this.contextMenu));
                });
                this.container.find('#transformConRemove').click(() => {
                    this.disconnect(connection);
                    this.hideConnectContextMenu(this.container.find('#' + this.contextMenu));
                });
                contextMenuDiv.css({
                    top: e.pageY,
                    left: e.pageX,
                    zIndex: 1000,
                });
                this.showConnectContextMenu(contextMenuDiv);
                this.container.mousemove((event) => {
                    const xDif = e.pageX - event.pageX;
                    const yDif = e.pageY - event.pageY;
                    if (xDif > 5 || xDif < -75 || yDif > 5 || yDif < -30) {
                        this.hideConnectContextMenu(this.container.find('#' + this.contextMenu));
                        this.container.unbind('mousemove');
                    }
                });
            }
        });
    }

    /**
     * Make source property
     * @param {any} element element
     * @param {any} input input
     * @memberof TransformRender
     */
    addSource(element, input) {
        const connectionConfig = {
            anchor: ['Right'],
            parameters: {
                input,
            },
        };
        this.jsPlumbInstance.makeSource(element, connectionConfig);
    }

    /**
     * Make target property
     * @param {any} element
     * @param {any} output
     */
    addTarget(element, output) {
        this.jsPlumbInstance.makeTarget(element, {
            maxConnections: 1,
            anchor: ['Left'],
            parameters: {
                output,
            },
        });
    }

    /**
     * Checks given types are valid to connect
     * @param  {string} sourceType type name of the source
     * @param  {string} targetType type name of the target
     * @return Boolean             is valid
     */
    isValidTypes(sourceType, targetType) {
        let isValid;
        if (sourceType === 'struct' || targetType === 'struct') {
            isValid = input.typeName == output.typeName;
        } else {
            isValid = sourceType === targetType || sourceType === 'any' || targetType === 'any'
                || sourceType === 'json' || targetType === 'json';
        }
        return isValid;
    }

/**
 * Reposition function nodes and redraw connections accordingly
 * @param {string} viewId type mapper view identifier
 * @param jsPlumbInstance jsPlumb instance of the type mapper to be repositioned
 */
    reposition(viewId) {
        this.viewId = viewId;
        const funcs = this.container.find('.middle-content  > .func');
        let yFunctionPointer = 20;
        let ySourcePointer = 0;
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

  /**
   * show with fade in effect
   * @param  {object} contextMenuDiv menu div to be shown
   */
  showConnectContextMenu(contextMenuDiv) {
      contextMenuDiv.fadeIn(200);
  }

  /**
   * hide with fade out effect
   * @param  {object} contextMenuDiv menu div to be hidden
   */
  hideConnectContextMenu(contextMenuDiv) {
      contextMenuDiv.fadeOut(200);
  }

}

export default TransformRender;
