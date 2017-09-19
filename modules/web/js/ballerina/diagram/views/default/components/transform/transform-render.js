
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

        this.draggingContainer = `transformOverlay-content-dragging-${this.viewId}`;

        const jsPlumbOptions = {
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
        };

        this.jsPlumbInstance = jsPlumb.getInstance(jsPlumbOptions);

        jsPlumbOptions.Container = this.draggingContainer;
        this.jsPlumbInstanceNewConnections = jsPlumb.getInstance(jsPlumbOptions);

        this.jsPlumbInstanceNewConnections.bind('connection', (params, ev) => {
            if (_.isUndefined(ev)) {
                return;
            }

            const input = params.connection.getParameters().input;
            const output = this.getDroppingTarget();
            const connection = this.getConnectionObject(input, output);
            this.midpoint += this.midpointVariance;
            this.jsPlumbInstance.importDefaults({ Connector: this.getConnectorConfig(this.midpoint) });
            this.onConnection(connection);
            this.setConnectionMenu(params.connection);
        });

        this.jsPlumbInstanceNewConnections.bind('connectionDrag', conn => {
            this.jsPlumbInstanceNewConnections.repaintEverything();
            this._draggingConnection = conn;
        });
    }

    /**
    * Disconnects the connection created.
    *
    * @param connection
    */
    disconnect(connection) {
        const propertyConnection = this.getConnectionObject(
            connection.getParameter('input'), connection.getParameter('output'));
        this.midpoint = this.midpoint - this.midpointVariance;
        this.jsPlumbInstance.importDefaults({ Connector: this.getConnectorConfig(this.midpoint) });
        this.jsPlumbInstance.detach(connection);
        this.disconnectCallback(propertyConnection);
    }

    /**
    * Disconnects all the connection created.
    * This does not remove the associated children from the model
    */
    disconnectAll() {
        this.midpoint = 0.1;
        this.jsPlumbInstance.deleteEveryEndpoint();
        this.jsPlumbInstanceNewConnections.deleteEveryEndpoint();
    }

    /**
    * Created the connection object from the sourceId and targetId of the connection elements
    * @param sourceId Id of the source element of the connection
    * @param targetId Id of the target element of the connection
    * @returns connectionObject
    */
    getConnectionObject(source, target) {
        if (source.isField) {
            source.endpointKind = source.root.endpointKind;
        }

        if (target.isField) {
            target.endpointKind = target.root.endpointKind;
        }

        return {
            source,
            target,
        };
    }

    remove(elementId) {
        this.jsPlumbInstanceNewConnections.remove(elementId);
    }

    addConnection(sourceId, targetId, folded = false) {
        this.midpoint += this.midpointVariance;
        this.jsPlumbInstance.importDefaults({ Connector: this.getConnectorConfig(this.midpoint) });
        const options = {
            source: sourceId,
            target: targetId,
            anchors: ['Right', 'Left'],
        };

        if (folded) {
            options.paintStyle = {
                strokeWidth: 1,
                stroke: '#666769',
                cssClass: 'plumbConnect',
                outlineStroke: '#F7F7F7',
                outlineWidth: 2,
                dashstyle: '4',
            };

            options.parameters = options.parameters || {};
            options.parameters.isFolded = true;
        }

        this.jsPlumbInstance.connect(options);
        this.hideConnectContextMenu(this.container.find('#' + this.contextMenu));
        this.setConnectionMenu(this.jsPlumbInstance.getConnections({ source: sourceId, target: targetId })[0]);
    }

    connectionsAdded() {
        this.jsPlumbInstance.unmakeEverySource();
        this.jsPlumbInstance.unmakeEveryTarget();
    }

    onConnectionAborted(callback) {
        this.jsPlumbInstanceNewConnections.bind('connectionAborted', callback)
    }

    getDraggingConnection() {
        return this._draggingConnection;
    }

    setConnectionMenu(connection) {
        if (!connection) {
            return;
        }
        connection.bind('mouseover', (conn, e) => {
            if (connection.getParameters().isFolded) {
                return;
            }

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
    addSource(element, input, existingConnectionsAdded) {
        const jsPlumbInstance = existingConnectionsAdded ? this.jsPlumbInstanceNewConnections : this.jsPlumbInstance;
        if(jsPlumbInstance.isSource(element)) {
            return;
        }

        const connectionConfig = {
            anchor: ['Right'],
            parameters: {
                input,
            },
        };
        jsPlumbInstance.makeSource(element, connectionConfig);
    }

    /**
     * Make target property
     * @param {any} element
     * @param {any} output
     */
    addTarget(element, output, existingConnectionsAdded, validateCallback) {
        const jsPlumbInstance = existingConnectionsAdded ? this.jsPlumbInstanceNewConnections : this.jsPlumbInstance;
        if(jsPlumbInstance.isTarget(element)) {
            return;
        }

        jsPlumbInstance.makeTarget(element, {
            maxConnections: 1,
            anchor: ['Left'],
            parameters: {
                output,
            },
            beforeDrop: (params) => {
                return validateCallback(
                    params.connection.getParameters().input.type, this.getDroppingTarget().type);
            },
        });
    }

/**
 * Reposition function nodes and redraw connections accordingly
 * @param {string} viewId type mapper view identifier
 * @param jsPlumbInstance jsPlumb instance of the type mapper to be repositioned
 */
    reposition(viewId) {
        this.viewId = viewId;
        const funcs = this.container
          .find('.middle-content > .transform-expanded-func, .middle-content > .operator-expanded-func');
        let yFunctionPointer = 20;
        const functionGap = 20;
        const svgLines = $('#' + this.placeHolderName + '-' + viewId + ' > svg');
        // Traverse through all the connection svg lines
        _.forEach(svgLines, (svgLine) => {
            // Get bottom and right values relative to the type mapper parent div
            const arrowBottom = svgLine.children[2].getBoundingClientRect().bottom
             - (this.container.find('.middle-content').position().top + 100);
            // Calculate the yFunctionPointer value  based on the bottom value of the direct connections
            if (arrowBottom > yFunctionPointer && svgLine.getBoundingClientRect().width > 400) {
                yFunctionPointer = arrowBottom;
            }
        });
        // Traverse through all the function divs
        _.forEach(funcs, (func) => {
            // Position functions and increase yFunctionPointer with gaps
            this.container.find(func).css('left',
                (this.container.find('.middle-content').width() - this.container.find(func).width())/2 + 'px');
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

    setDroppingTarget(endpoint) {
        this._droppingTarget = endpoint;
    }

    getDroppingTarget(endpoint) {
        return this._droppingTarget;
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

    repaintEverything() {
        this.jsPlumbInstance.repaintEverything();
    }

    recalculateOffsets(element) {
        this.jsPlumbInstanceNewConnections.recalculateOffsets(
            this.jsPlumbInstanceNewConnections.getSelector('.transform-dragging-connections'));
    }

    repaintDraggingArrows() {
        this.jsPlumbInstanceNewConnections.repaintEverything();
    }

    isConnectionDragging() {
        return this.jsPlumbInstanceNewConnections.isConnectionBeingDragged();
    }

}

export default TransformRender;
