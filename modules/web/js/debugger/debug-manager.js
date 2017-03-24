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

import $ from 'jquery';
import Backbone from 'backbone';
import _ from 'lodash';
import EventChannel from 'event_channel';
import Channel from './channel';
import DebugPoint from './debug-point';
import Mousetrap from 'mousetrap';
import log from 'log';
var instance;

class DebugManager extends EventChannel {
    constructor(args) {
        super();
    	this.debugPoints = [];
        this.enable = false;
        this.channel = undefined;
        this.active = false;

    	this.on("breakpoint-added",_.bind(this.publishBreakPoints, this));
        this.on("breakpoint-removed",_.bind(this.publishBreakPoints, this));
    }

    stepIn() {
        var message = { "command": "STEP_IN" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    }

    stepOut() {
        var message = { "command": "STEP_OUT" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    }

    stop() {
        var message = { "command": "STOP" };
        if(this.launchManager.active){
            this.launchManager.stopProgram();
            this.trigger("resume-execution");
            this.trigger("session-ended");
        }else{
            this.channel.sendMessage(message);
            this.trigger("resume-execution");
        }
    }

    stepOver() {
        var message = { "command": "STEP_OVER" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    }

    resume() {
        var message = { "command": "RESUME" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    }

    startDebug() {
        var message = { "command": "START" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    }

    processMesssage(message) {
        if(message.code == "DEBUG_HIT"){
            this.active = true;
            this.trigger("debug-hit", message);
        }
        if(message.code == "EXIT"){
            this.active = false;
            this.trigger("session-ended");
        }
        if(message.code == "COMPLETE") {
            this.active = false;
            this.trigger("session-completed");
        }
    }

    connect(url) {
        if(url != undefined || url != ""){
            this.channel = new Channel({ endpoint: "ws://" + url + "/debug" , debugger: this});
            this.channel.connect();
        }
    }

    startDebugger(port) {
        var url =  "localhost:" + port;
        this.connect(url);
    }

    init(options) {
        this.enable = true;
        this.launchManager = options.launchManager;
        this.launchManager.on("debug-active", _.bindKey(this, 'startDebugger'))
    }

    addBreakPoint(lineNumber, fileName) {
        log.debug('debug point added', lineNumber, fileName);
        var point = new DebugPoint({ "fileName": fileName , "lineNumber": lineNumber});
    	this.debugPoints.push(point);
    	this.trigger("breakpoint-added", fileName);
    }

    removeBreakPoint(lineNumber, fileName) {
        log.debug('debug point removed', lineNumber, fileName);
        var point = new DebugPoint({ "fileName": fileName , "lineNumber": lineNumber});
        _.remove(this.debugPoints, function(item) {
            return item.fileName == point.fileName && item.lineNumber == point.lineNumber ;
        });
        this.trigger("breakpoint-removed", fileName);
    }

    publishBreakPoints() {
        try{
            var message = { "command": "SET_POINTS", points: this.debugPoints };
            this.channel.sendMessage(message);
        }catch(e){
            //@todo log
        }
    }

    hasBreakPoint(lineNumber, fileName) {
        return !!_.find(this.debugPoints, {lineNumber: lineNumber, fileName: fileName});
    }

    isEnabled() {
        return this.enable;
    }

    getDebugPoints(fileName) {
        var breakpoints = _.filter(this.debugPoints, function(breakpoint) {
            return breakpoint.fileName === fileName;
        });

        var breakpointsLineNumbers = _.map(breakpoints, function(breakpoint) {
             return breakpoint.lineNumber;
        });
        return breakpointsLineNumbers;
    }

    removeAllBreakpoints(fileName) {
        _.remove(this.debugPoints, function(item) {
            return item.fileName == fileName;
        });
        log.debug('removed all debugpoints for fileName', fileName);
        this.publishBreakPoints();
    }

    createDebugPoint(lineNumber, fileName) {
        return new DebugPoint({ "fileName": fileName , "lineNumber": lineNumber});
    }
}

export default new DebugManager();
