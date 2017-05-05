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
import log from 'log';
import _ from 'lodash';
import * as d3 from 'd3';
import $ from 'jquery';

/**
 * Handle Message drawing by Message manager
 * @class MessageManager
 */
class MessageManager {
    /**
     * Constructor for MessageManager
     * @param {args} args for constructor
     * @constructor
     */
    constructor(args) {
        log.debug('Initialising Message Manager');
        this._typeBeingDragged = undefined;
        this._messageSource = undefined;
        this._messageTarget = undefined;
        this._isOnDrag = false;
        this._source = undefined;
        this._destination = undefined;
        this._messageEnd = {
            x: 0,
            y: 0
        };
        this._messageStart = {
            x: 0,
            y: 0
        };
        this._arrowDecorator = undefined;
        this._container = undefined;
    }

    setMessageSource(source) {
        if (!_.isUndefined(source)) {
            this._messageSource = source;
        }
    }


    getMessageSource() {
        return this._messageSource;
    }

    setMessageTarget(destination) {
        if (!_.isUndefined(destination)) {
            this._messageTarget = destination;
        }
    }

    getMessageTarget() {
        return this._messageTarget;
    }

    setActivatedTarget(target) {
        this._activatedDropTarget = target;
    }

    getActivatedTarget() {
        return this._activatedDropTarget;
    }

    setValidateCallBack(callBackMethod) {
        if (!_.isUndefined(callBackMethod)) {
            this.validateCallBack = callBackMethod;
        }
    }

    getValidateCallBack() {
        return this.validateCallBack;
    }

    /**
     * Set the type being dragged at a given moment.
     * @param source being dragged
     */
    setSource(source) {
        this._source = source;
    }

    getSource() {
        return this._source;
    }

    setDestination(destination) {
        this._destination = destination;
    }

    getDestination() {
        return this._destination;
    }

    /**
     * Gets the type which is being dragged at a given moment - if any.
     * @return type
     */
    getTypeBeingDragged() {
        return this._typeBeingDragged;
    }

    isOnDrag() {
        return this._isOnDrag;
    }

    setIsOnDrag(status) {
        this._isOnDrag = status;
    }

    setMessageEnd(x, y) {
        this._messageEnd.x = x;
        this._messageEnd.y = y;
    }

    getMessageEnd() {
        return this._messageEnd;
    }

    setMessageStart(x, y) {
        this._messageStart.x = x;
        this._messageStart.y = y;
    }

    getMessageStart() {
        return this._messageStart;
    }

    setArrowDecorator(arrowDecorator) {
        this._arrowDecorator = arrowDecorator;
    }

    getArrowDecorator() {
        return this._arrowDecorator;
    }

    setContainer(container) {
        this._container = container;
    }

    getContainer() {
        return this._container;
    }

    reset() {
        /**
         * @event MessageManager#drag-stop
         * @type {ASTNode}
         */
        this.setMessageSource(undefined);
        this.setValidateCallBack( undefined);
        this.setActivatedDropTarget(undefined);
        this._typeBeingDragged = undefined;
    }

    startDrawMessage() {

        var self = this,
            container = d3.select('.svg-container');

        container.on('mousemove', function (e) {
            var m = d3.mouse(this);
            self.setMessageEnd(m[0] - 5, self.getMessageStart().y);
            const currentDrawOnMouseMoveFlag = self.getArrowDecorator().state.drawOnMouseMoveFlag;
            self.getArrowDecorator().setState({drawOnMouseMoveFlag: (currentDrawOnMouseMoveFlag + 1)});
        });

        // container.on('mouseup', function () {
        //     // unbind current listeners
        //     container.on('mousemove', null);
        //     container.on('mouseup', null);
        //     var startPoint = new Point(tempLine.attr('x1'),tempLine.attr('y1'));
        //
        //     tempLine.remove();
        //     arrowPoint.remove();
        //     self.reset();
        // });
    }
}

export default MessageManager;
