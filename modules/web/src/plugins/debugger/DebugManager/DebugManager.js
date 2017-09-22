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
import EventChannel from 'event_channel';
import log from 'log';
import DebugChannel from './DebugChannel';
import DebugPoint from './DebugPoint';
import LaunchManager from './../LaunchManager';

/**
 * DebugManager
 * @class DebugManager
 * @extends {EventChannel}
 */
class DebugManager extends EventChannel {
    /**
     * Creates an instance of DebugManager.
     *
     * @memberof DebugManager
     */
    constructor() {
        super();
        this.debugPoints = [];
        this.channel = undefined;

        this.on('breakpoint-added', () => { this.publishBreakpoints(); });
        this.on('breakpoint-removed', () => { this.publishBreakpoints(); });

        LaunchManager.on('debug-active', () => {
            this.startDebugger(this.endpoint);
        });
    }

    /**
     * Send call to backend to step in
     *
     * @memberof DebugManager
     */
    stepIn() {
        const message = { command: 'STEP_IN', threadId: this.currentThreadId };
        this.channel.sendMessage(message);
    }
    /**
     * Send call to backend to step out
     *
     * @memberof DebugManager
     */
    stepOut() {
        const message = { command: 'STEP_OUT', threadId: this.currentThreadId };
        this.channel.sendMessage(message);
    }
    /**
     * Send call to backend to stop debugging
     *
     * @memberof DebugManager
     */
    stop() {
        LaunchManager.stop();
        this.trigger('session-ended');
        this.channel.close();
    }
    /**
     * Send call to backend to step over
     *
     * @memberof DebugManager
     */
    stepOver() {
        const message = { command: 'STEP_OVER', threadId: this.currentThreadId };
        this.channel.sendMessage(message);
    }
    /**
     * Send call to backend to resume execution
     *
     * @memberof DebugManager
     */
    resume() {
        const message = { command: 'RESUME', threadId: this.currentThreadId };
        this.channel.sendMessage(message);
    }
    /**
     * Send call to backend to start debugging
     *
     * @memberof DebugManager
     */
    startDebug() {
        const message = { command: 'START' };
        this.channel.sendMessage(message);
        this.trigger('debugging-started');
    }
    /**
     * @param {Object} message - Process message from backend
     *
     * @memberof DebugManager
     */
    processMesssage(message) {
        if (message.code === 'DEBUG_HIT') {
            this.trigger('debug-hit', message);
            this.currentThreadId = message.threadId;
        }
        if (message.code === 'COMPLETE' || message.code === 'EXIT') {
            this.trigger('session-completed');
        }
    }

    /**
     * @param {String} url - Debugger backend url
     *
     * @memberof DebugManager
     */
    connect(url) {
        this.trigger('connecting');
        if (url !== undefined || url !== '') {
            this.channel = new DebugChannel(url);
            this.channel.connect();
        }
        this.channel.on('onmessage', (message) => {
            this.processMesssage(message);
        });
        this.channel.on('connected', () => {
            this.publishBreakpoints();
            this.startDebug();
        });
        this.channel.on('session-ended session-terminated', () => {
            this.trigger('execution-ended');
        });
    }
    /**
     * @param {String} url - Start debugging with a remote url
     *
     * @memberof DebugManager
     */
    startDebugger(url) {
        this.connect(url);
    }

    /**
     * @param {Object} options - Debug manager configs
     *
     * @memberof DebugManager
     */
    init(endpoint) {
        this.endpoint = endpoint;
    }
    /**
     * Add new breakpoint
     * @param {Integer} lineNumber - Line number of the breakpoint
     * @param {String} fileName - File name of the breakpoint
     *
     * @memberof DebugManager
     */
    addBreakPoint(lineNumber, fileName, packagePath) {
        log.debug('debug point added', lineNumber, fileName, packagePath);
        const point = new DebugPoint({ fileName, lineNumber, packagePath });
        this.debugPoints.push(point);
        this.trigger('breakpoint-added', point);
    }
    /**
     * Remove breakpoint
     * @param {Integer} lineNumber - Line number of the breakpoint
     * @param {String} fileName - File name of the breakpoint
     *
     * @memberof DebugManager
     */
    removeBreakPoint(lineNumber, fileName, packagePath) {
        log.debug('debug point removed', lineNumber, fileName);
        const point = new DebugPoint({ fileName, lineNumber, packagePath });
        _.remove(this.debugPoints, item => item.fileName === point.fileName && item.lineNumber === point.lineNumber);
        this.trigger('breakpoint-removed', point);
    }

    /**
     * Publishes current breakpoints to backend
     *
     * @memberof DebugManager
     */
    publishBreakpoints() {
        try {
            const message = { command: 'SET_POINTS', points: this.debugPoints };
            this.channel.sendMessage(message);
        } catch (e) {
            // @todo log
        }
    }
    /**
     * Check a given position for breakpoint or not
     * @param {Integer} lineNumber - Line number of the breakpoint
     * @param {String} fileName - File name of the breakpoint
     * @returns {Boolean} true if has a breakpoint in the position
     *
     * @memberof DebugManager
     */
    hasBreakPoint(lineNumber, fileName) {
        return !!this.debugPoints.find(point => point.lineNumber === lineNumber && point.fileName === fileName);
    }

    /**
     * Returns breakpoints for a given file
     * @param {String} fileName - Name of the ballerina file
     * @returns {[Integer]} line numbers of breakpoints
     *
     * @memberof DebugManager
     */
    getDebugPoints(fileName) {
        const breakpoints = this.debugPoints.filter(breakpoint => breakpoint.fileName === fileName);

        const breakpointsLineNumbers = breakpoints.map(breakpoint => breakpoint.lineNumber);
        return breakpointsLineNumbers;
    }
    /**
     * Removes all breakpoints for a ballerina file
     * @param {any} fileName - Name of the ballerina file
     *
     * @memberof DebugManager
     */
    removeAllBreakpoints(fileName) {
        _.remove(this.debugPoints, item => item.fileName === fileName);
        log.debug('removed all debugpoints for fileName', fileName);
        this.publishBreakpoints();
    }

    /**
     * Create and return breakpoint object
     * @param {Integer} lineNumber - Line number of the breakpoint
     * @param {String} fileName - File name of the breakpoint
     * @returns {DebugPoint}
     *
     * @memberof DebugManager
     */
    createDebugPoint(lineNumber, fileName) {
        return new DebugPoint({ fileName, lineNumber });
    }
}

export default new DebugManager();
