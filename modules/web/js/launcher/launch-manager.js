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
define(['require', 'jquery', 'backbone', 'lodash', 'event_channel', './launch-manager', './launch-channel', 'console' ],
    function (require, $, Backbone, _ ,EventChannel, LaunchManager, LaunchChannel, Console) {
	var instance;

    var LaunchManager = function(args) {
        var self = this;
        this.enable = false;
        this.channel = undefined;
        this.active = false;
    };

    LaunchManager.prototype = Object.create(EventChannel.prototype);
    LaunchManager.prototype.constructor = LaunchManager;

    LaunchManager.prototype.runApplication = function(file){
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected',_.bindKey(this,'sendRunApplicationMessage',file));        
    };

    LaunchManager.prototype.runService = function(file){
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected',_.bindKey(this,'sendRunServiceMessage',file));
    };

    LaunchManager.prototype.debugApplication = function(file){
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected',_.bindKey(this,'sendDebugApplicationMessage',file));
    };

    LaunchManager.prototype.debugService = function(file){
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected',_.bindKey(this,'sendDebugServiceMessage',file));
    };    

    LaunchManager.prototype.sendRunApplicationMessage = function(file){
        var message = { 
            "command": "RUN_PROGRAM",
            "fileName" : file.getName(),
            "filePath" : file.getPath()
        };
        this.channel.sendMessage(message);
    };

    LaunchManager.prototype.sendRunServiceMessage = function(file){
        var message = { 
            "command": "RUN_SERVICE",
            "fileName" : file.getName(),
            "filePath" : file.getPath()
        };
        this.channel.sendMessage(message);
    }; 

    LaunchManager.prototype.sendDebugApplicationMessage = function(file){
        var message = { 
            "command": "DEBUG_PROGRAM",
            "fileName" : file.getName(),
            "filePath" : file.getPath()
        };
        this.channel.sendMessage(message);
    };

    LaunchManager.prototype.sendDebugServiceMessage = function(file){
        var message = { 
            "command": "DEBUG_SERVICE",
            "fileName" : file.getName(),
            "filePath" : file.getPath()
        };
        this.channel.sendMessage(message);
    };  

    LaunchManager.prototype.processMesssage = function(message){
        if(message.code == "OUTPUT"){
            if(_.endsWith(message.message, this.debugPort)){
                this.trigger("debug-active",this.debugPort);
                return;
            }
        }
        if(message.code == "EXECUTION_STARTED"){
            this.active = true;
            this.trigger("execution-started");
        }
        if(message.code == "EXECUTION_STOPED" || message.code == "EXECUTION_TERMINATED"){
            this.active = false;
            this.trigger("execution-ended");
        }                    
        if(message.code == "DEBUG_PORT"){
            this.debugPort = message.port;
            return;           
        }        
        if(message.code == "EXIT"){
            this.active = false;
            this.trigger("session-ended");
        }
        Console.println(message);
    };

    LaunchManager.prototype.openConsole = function(){
        Console.clear();
        Console.show();
    };

    LaunchManager.prototype.init = function(options){        
        this.endpoint = _.get(options, 'application.config.services.launcher.endpoint');
        this.enable = true; 
    };

    LaunchManager.prototype.stopProgram = function(){        
        var message = { 
            "command": "TERMINATE",
        };
        this.channel.sendMessage(message);
    }; 

    return (instance = (instance || new LaunchManager()));
});