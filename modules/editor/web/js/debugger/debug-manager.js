/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'jquery', 'backbone', 'lodash', 'event_channel', './channel', './debug-point', 'mousetrap'], 
    function (require, $, Backbone, _ ,EventChannel, Channel, DebugPoint, Mousetrap) {
	var instance;

    var DebugManager = function(args) {
    	this.debugPoints = [];
        this.enable = false;
        this.channel = undefined;

    	this.on("breakpoint-added",_.bind(this.publishBreakPoints, this));
        this.on("breakpoint-removed",_.bind(this.publishBreakPoints, this));
        this.on("connected",_.bind(this.hideModal, this));

        this.connectionDialog = $("#modalDebugConnection");
        Mousetrap.bind('d+c', _.bindKey(this, 'showConnectionDialog'));
        Mousetrap.bind('s+o', _.bindKey(this, 'stepOver'));
        Mousetrap.bind('s+r', _.bindKey(this, 'resume'));

        $('.debug-connect-button').on("click", _.bindKey(this, 'connect'));
    };

    DebugManager.prototype = Object.create(EventChannel.prototype);
    DebugManager.prototype.constructor = DebugManager;

    DebugManager.prototype.hideModal = function(){
        this.connectionDialog.modal('hide');
        this.publishBreakPoints();
        this.startDebug();
    },

    DebugManager.prototype.startDebug = function(){
        var message = { "command": "START" };
        this.channel.sendMessage(message);
    },

    DebugManager.prototype.processMesssage = function(message){
        console.log(message);
    },

    DebugManager.prototype.connect = function(){
        var url = $("#debugUrl").val();
        if(url != undefined || url != ""){
            this.channel = new Channel({ endpoint: "ws://" + url + "/debug" , debugger: this});
        }
    },    

    DebugManager.prototype.showConnectionDialog = function(){
        this.connectionDialog.modal(); 
    },

    DebugManager.prototype.stepOver = function(){
        alert('stepOver');
    },

    DebugManager.prototype.resume = function(){
        alert('resume');
    },

    DebugManager.prototype.init = function(options){
        this.enable = true;              
    }; 

    DebugManager.prototype.addBreakPoint = function(line, fileName){
        var point = new DebugPoint({ "fileName": fileName , "line": line});
    	this.debugPoints.push(point);
    	this.trigger("breakpoint-added");
    };

    DebugManager.prototype.removeBreakPoint = function(line, fileName){
        var point = new DebugPoint({ "fileName": fileName , "line": line});
        this.debugPoints = _.filter(this.debugPoints, function(item){
            return item.fileName == fileName && item.line == line ;
        });
        this.trigger("breakpoint-removed", point);        
    };    

    DebugManager.prototype.publishBreakPoints = function(){
        try{
            var message = { "command": "SET_POINTS", points: this.debugPoints };
            this.channel.sendMessage(message);
        }catch(e){
            //@todo log
        }
    };

    DebugManager.prototype.isEnabled = function(){
        return this.enable;
    };

    return (instance = (instance || new DebugManager()));
});