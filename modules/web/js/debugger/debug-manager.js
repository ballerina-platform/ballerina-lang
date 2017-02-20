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

define(['require', 'jquery', 'backbone', 'lodash', 'event_channel', './channel', './debug-point', 'mousetrap', 'log'],
    function (require, $, Backbone, _ ,EventChannel, Channel, DebugPoint, Mousetrap, log) {
	var instance;

    var DebugManager = function(args) {
        var self = this;
    	this.debugPoints = [];
        this.enable = false;
        this.channel = undefined;
        this.active = false;

    	this.on("breakpoint-added",_.bind(this.publishBreakPoints, this));
        this.on("breakpoint-removed",_.bind(this.publishBreakPoints, this));

        Mousetrap.bind('alt+o', _.bindKey(this, 'stepOver'));
        Mousetrap.bind('alt+r', _.bindKey(this, 'resume'));
        Mousetrap.bind('alt+i', _.bindKey(this, 'stepIn'));
        Mousetrap.bind('alt+u', _.bindKey(this, 'stepOut'));
        Mousetrap.bind('alt+p', _.bindKey(this, 'stop'));
    };

    DebugManager.prototype = Object.create(EventChannel.prototype);
    DebugManager.prototype.constructor = DebugManager;

    DebugManager.prototype.stepIn = function(){
        var message = { "command": "STEP_IN" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    };

    DebugManager.prototype.stepOut = function(){
        var message = { "command": "STEP_OUT" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    };

    DebugManager.prototype.stop = function(){
        var message = { "command": "STOP" };
        if(this.launchManager.active){
            this.launchManager.stopProgram();
            this.trigger("resume-execution");
            this.trigger("session-ended");
        }else{
            this.channel.sendMessage(message);
            this.trigger("resume-execution");
        }
    };

    DebugManager.prototype.stepOver = function(){
        var message = { "command": "STEP_OVER" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    };

    DebugManager.prototype.resume = function(){
        var message = { "command": "RESUME" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    };

    DebugManager.prototype.startDebug = function(){
        var message = { "command": "START" };
        this.channel.sendMessage(message);
        this.trigger("resume-execution");
    };

    DebugManager.prototype.processMesssage = function(message){
        if(message.code == "DEBUG_HIT"){
            this.trigger("debug-hit", message);
        }
        if(message.code == "EXIT"){
            this.active = false;
            this.trigger("session-ended");            
        }
    };

    DebugManager.prototype.connect = function(url){        
        if(url != undefined || url != ""){
            this.channel = new Channel({ endpoint: "ws://" + url + "/debug" , debugger: this});
            this.channel.connect();
        }
    };

    DebugManager.prototype.startDebugger = function(port){ 
        var url =  "localhost:" + port;
        this.connect(url);
    };    

    DebugManager.prototype.init = function(options){
        this.enable = true;    
        this.launchManager = options.launchManager;
        this.launchManager.on("debug-active", _.bindKey(this, 'startDebugger'))
    }; 

    DebugManager.prototype.addBreakPoint = function(lineNumber, fileName){
        log.debug('debug point added', lineNumber, fileName);
        var point = new DebugPoint({ "fileName": fileName , "lineNumber": lineNumber});
    	this.debugPoints.push(point);
    	this.trigger("breakpoint-added", fileName);
    };

    DebugManager.prototype.removeBreakPoint = function(lineNumber, fileName){
        log.debug('debug point removed', lineNumber, fileName);
        var point = new DebugPoint({ "fileName": fileName , "lineNumber": lineNumber});
        _.remove(this.debugPoints, function(item) {
            return item.fileName == point.fileName && item.lineNumber == point.lineNumber ;
        });
        this.trigger("breakpoint-removed", fileName);
    };    

    DebugManager.prototype.publishBreakPoints = function(){
        try{
            var message = { "command": "SET_POINTS", points: this.debugPoints };
            this.channel.sendMessage(message);
        }catch(e){
            //@todo log
        }
    };

    DebugManager.prototype.hasBreakPoint = function (lineNumber, fileName) {
        return !!_.find(this.debugPoints, {lineNumber: lineNumber, fileName: fileName});
    };

    DebugManager.prototype.isEnabled = function(){
        return this.enable;
    };

    DebugManager.prototype.getDebugPoints = function (fileName) {
        var breakpoints = _.map(this.debugPoints, function(breakpoint) {
            if(breakpoint.fileName === fileName)  {
                return breakpoint.lineNumber;
            }
        });
        return breakpoints;
    };

    DebugManager.prototype.removeAllBreakpoints = function(fileName) {
        _.remove(this.debugPoints, function(item) {
            return item.fileName == fileName;
        });
        log.debug('removed all debugpoints for fileName', fileName);
        this.publishBreakPoints();
    }

    DebugManager.prototype.createDebugPoint = function(lineNumber, fileName){
        return new DebugPoint({ "fileName": fileName , "lineNumber": lineNumber});
    };

    return (instance = (instance || new DebugManager()));
});
