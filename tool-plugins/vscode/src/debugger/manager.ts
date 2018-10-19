/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { EventEmitter } from 'events';
import { DebugChannel } from './channel';
import { BreakPoint } from './breakpoint';

/**
 * DebugManager
 * @class DebugManager
 * @extends {EventEmitter}
 */
export class DebugManager extends EventEmitter {

    private _breakPoints : Array<BreakPoint> = [];
    private _channel: DebugChannel | undefined;
    private _active: boolean = false;
    private _endpoint: string | undefined;

    /**
     * Creates an instance of DebugManager.
     *
     * @memberof DebugManager
     */
    constructor() {
        super();
        this.on('breakpoint-added', () => { this.publishBreakpoints(); });
        this.on('breakpoint-removed', () => { this.publishBreakpoints(); });

        this.on('execution-ended', () => {
            this.active = false;
        });
        this.on('session-error', () => {
            this.active = false;
        });
    }

    get active() {
        return this._active;
    }

    set active(active) {
        this._active = active;
    }

    sendMessage(message: Object) {
        if (this._channel) {
            this._channel.sendMessage(message);
        }
    }

    /**
     * Send call to backend to step in
     *
     * @memberof DebugManager
     */
    stepIn(threadId: string) {
        const message = { command: 'STEP_IN', threadId };
        this.sendMessage(message);
    }
    /**
     * Send call to backend to step out
     *
     * @memberof DebugManager
     */
    stepOut(threadId: string) {
        const message = { command: 'STEP_OUT', threadId };
        this.sendMessage(message);
    }
    /**
     * Send call to backend to stop debugging
     *
     * @memberof DebugManager
     */
    stop() {
        this.emit('session-ended');
        if (this._channel) {    
            this._channel.close();
        }
    }
    /**
     * Send call to backend to step over
     *
     * @memberof DebugManager
     */
    stepOver(threadId: string) {
        const message = { command: 'STEP_OVER', threadId };
        this.sendMessage(message);
    }
    /**
     * Send call to backend to resume execution
     *
     * @memberof DebugManager
     */
    resume(threadId: string) {
        const message = { command: 'RESUME', threadId };
        this.sendMessage(message);
    }
    /**
     * Send call to backend to start debugging
     *
     * @memberof DebugManager
     */
    startDebug() {
        const message = { command: 'START' };
        this.sendMessage(message);
        this.emit('debugging-started');
    }

    /**
     * Send call to backend to kill debug server
     *
     * @memberof DebugManager
     */
    kill() {
        const message = { command: 'KILL' };
        this.sendMessage(message);
    }

    /**
     * @param {any} message - Process message from backend
     * 
     *
     * @memberof DebugManager
     */
    processMesssage(message: any) { //TODO change type to a concrete type
        if (message.code === 'DEBUG_HIT') {
            this.emit('debug-hit', message);
        }
        if (message.code === 'COMPLETE' || message.code === 'EXIT') {
            this.emit('execution-ended');
        }
    }

    /**
     * @param {String} url - Debugger backend url
     *
     * @memberof DebugManager
     */
    connect(url: string | undefined, callback = () => {}) {
        this.emit('connecting');
        if (url) {
            this._endpoint = url;
            this._channel = new DebugChannel(url);
            this._channel.connect();
            this._channel.on('onmessage', (message) => {
                this.processMesssage(message);
            });
            this._channel.on('connected', () => {
                this.active = true;
                callback();
            });
            this._channel.on('session-ended', e => {
                this.emit('execution-ended', e);
            });
            this._channel.on('session-terminated', () => {
                this.emit('execution-ended');
            });
            this._channel.on('connection-closed', () => {
                this.emit('execution-ended');
            });
            this._channel.on('session-error', e => {
                this.emit('session-error', e);
            });
        }
    }
    /**
     * Reconnect to backend
     * @memberof DebugManager
     */
    reConnect() {
        this.connect(this._endpoint);
    }

    /**
     * @param {string | undefined} options - url
     *
     * @memberof DebugManager
     */
    init(url: string) {
        this._endpoint = url;
    }
    /**
     * Add new breakpoint
     * @param {Integer} lineNumber - Line number of the breakpoint
     * @param {String} fileName - File name of the breakpoint
     *
     * @memberof DebugManager
     */
    addBreakPoint(lineNumber: number, fileName: string, packagePath: string) {
        // log.debug('debug point added', lineNumber, fileName, packagePath);
        const point = new BreakPoint({ fileName, lineNumber, packagePath });
        this._breakPoints.push(point);
        this.emit('breakpoint-added', point);
    }
    /**
     * Remove breakpoint
     * @param {Integer} lineNumber - Line number of the breakpoint
     * @param {String} fileName - File name of the breakpoint
     *
     * @memberof DebugManager
     */
    removeBreakPoint(lineNumber: number, fileName: string, packagePath: string) {
        // log.debug('debug point removed', lineNumber, fileName);
        const point = new BreakPoint({ fileName, lineNumber, packagePath });
        // _.remove(this.debugPoints, item => item.fileName === point.fileName && item.lineNumber === point.lineNumber);
        this.emit('breakpoint-removed', point);
    }

    /**
     * Publishes current breakpoints to backend
     *
     * @memberof DebugManager
     */
    publishBreakpoints() {
        try {
            const message = { 
                command: 'SET_POINTS',
                points: this._breakPoints 
            };
            this.sendMessage(message);
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
    hasBreakPoint(lineNumber: number, fileName: string) {
        return !!this._breakPoints.find(point => point.lineNumber === lineNumber && point.fileName === fileName);
    }

    /**
     * Returns breakpoints for a given file
     * @param {String} fileName - Name of the ballerina file
     * @returns {[Integer]} line numbers of breakpoints
     *
     * @memberof DebugManager
     */
    getDebugPoints(fileName: string) {
        const breakpoints = this._breakPoints.filter(breakpoint => breakpoint.fileName === fileName);

        const breakpointsLineNumbers = breakpoints.map(breakpoint => breakpoint.lineNumber);
        return breakpointsLineNumbers;
    }
    /**
     * Removes all breakpoints for a ballerina file
     * @param {any} fileName - Name of the ballerina file
     *
     * @memberof DebugManager
     */
    removeAllBreakpoints(fileName: string) {
        this._breakPoints = this._breakPoints.filter(item => item.fileName !== fileName);
        this.publishBreakpoints();
    }

    /**
     * Create and return breakpoint object
     * @param {Integer} lineNumber - Line number of the breakpoint
     * @param {String} fileName - File name of the breakpoint
     * @returns {BreakPoint}
     *
     * @memberof DebugManager
     */
    createDebugPoint(lineNumber: number, fileName: string) {
        return new BreakPoint({ fileName, lineNumber });
    }
}

