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
import $ from 'jquery';
import * as d3 from 'd3';
import EventChannel from 'event_channel';

/**
 * Handle Message drawing by Message manager
 * @class MessageManager
 */
class MessageManager extends EventChannel {
    /**
     * Constructor for MessageManager
     * @param {args} args for constructor
     * @constructor
     */
    constructor() {
        super();
        log.debug('Initialising Message Manager');
        this._typeBeingDragged = undefined;
        this._isOnDrag = false;
        this._source = undefined;
        this._destination = undefined;
        this._messageEnd = {
            x: 0,
            y: 0,
        };
        this._messageStart = {
            x: 0,
            y: 0,
        };
        this._arrowDecorator = undefined;
        this._backwardArrowDecorator = undefined;
        this._container = undefined;
        this._targetValidationCallback = undefined;
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

    setBackwardArrowDecorator(arrowDecorator) {
        this._backwardArrowDecorator = arrowDecorator;
    }

    getArrowDecorator() {
        return this._arrowDecorator;
    }

    getBackwardArrowDecorator() {
        return this._backwardArrowDecorator;
    }

    setContainer(container) {
        this._container = container;
    }

    getContainer() {
        return this._container;
    }

    setTargetValidationCallback(validationCallback) {
        this._targetValidationCallback = validationCallback;
    }

    getTargetValidationCallback() {
        return this._targetValidationCallback;
    }

    reset() {
        /**
         * @event MessageManager#drag-stop
         * @type {ASTNode}
         */
        this.setSource(undefined);
        this.setDestination(undefined);
        this.getArrowDecorator().setState({ drawOnMouseMoveFlag: -1 });
        this.getBackwardArrowDecorator().setState({ drawOnMouseMoveFlag: -1 });
        this.setIsOnDrag(false);
    }

    startDrawMessage(mouseUpCallback, targetValidationCallback) {
        const self = this;
        const container = d3.select($(document).find('.svg-container').get(0));

        container.on('mousemove', function (e) {
            self.trigger('message-draw-start');
            const m = d3.mouse(this);
            self.setMessageEnd(m[0] - 5, self.getMessageStart().y);
            if (self.getMessageEnd().x > self.getMessageStart().x) {
                const currentDrawOnMouseMoveFlag = self.getArrowDecorator().state.drawOnMouseMoveFlag;
                self.getBackwardArrowDecorator().setState({ drawOnMouseMoveFlag: -1 });
                self.getArrowDecorator().setState({ drawOnMouseMoveFlag: (currentDrawOnMouseMoveFlag + 1) });
            } else {
                const currentDrawOnMouseMoveFlag = self.getBackwardArrowDecorator().state.drawOnMouseMoveFlag;
                self.getArrowDecorator().setState({ drawOnMouseMoveFlag: -1 });
                self.getBackwardArrowDecorator().setState({ drawOnMouseMoveFlag: (currentDrawOnMouseMoveFlag + 1) });
            }
        });

        container.on('mouseup', () => {
            // unbind current listeners
            container.on('mousemove', null);
            container.on('mouseup', null);
            const messageSource = self.getSource();
            const messageDestination = self.getDestination();
            const validDestination = self.isAtValidDestination();
            self.reset();
            self.trigger('message-draw-stop');
            if (validDestination) {
                mouseUpCallback(messageSource, messageDestination);
            }
        });
    }

    isAtValidDestination() {
        return this._targetValidationCallback(this.getDestination());
    }
}

export default MessageManager;
