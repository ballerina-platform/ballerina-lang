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

define(['lodash', 'event_channel', 'log'],
    function (_, EventChannel, log){

        var LaunchChannel = function(args){
            if(_.isNil(args.endpoint)){
                throw "Invalid Endpoint";
            }
            _.assign(this, args);            

            // See http://tools.ietf.org/html/rfc6455#section-7.4.1
            this.ws_normal_code = 1000;
            this.ws_ssl_code = 1015;

            this.connect();
        };

        LaunchChannel.prototype = Object.create(EventChannel.prototype);

        LaunchChannel.prototype.constructor = LaunchChannel;

        LaunchChannel.prototype.connect = function(){
            var websocket = new WebSocket(this.endpoint);
            //bind functions
            websocket.onmessage = _.bindKey(this, 'parseMessage');
            websocket.onopen = _.bindKey(this, 'onOpen');
            websocket.onclose = _.bindKey(this, 'onClose');
            websocket.onerror = _.bindKey(this, 'onError');
            this.websocket = websocket;
        }

        LaunchChannel.prototype.parseMessage = function (strMessage) {            
            var message = JSON.parse(strMessage.data);
            this.launcher.processMesssage(message);
        };

        LaunchChannel.prototype.sendMessage = function (message) {            
            this.websocket.send(JSON.stringify(message));
        };

        LaunchChannel.prototype.onClose = function(event){
            this.launcher.active = false;
            this.launcher.trigger("session-terminated");
            var reason;
            if (event.code == this.ws_normal_code){
                reason = "Normal closure";
                this.trigger("session-ended");
                this.debugger.active = false;
                return;
            }
            else if(event.code == this.ws_ssl_code){
                reason = "Certificate Issue";
            }
            else{
                reason = "Unknown reason :" + event.code;
            }
        };

        LaunchChannel.prototype.onError = function(event){
            this.launcher.active = false;
            this.launcher.trigger("session-error");
        };

        LaunchChannel.prototype.onOpen = function(event){            
            this.launcher.active = true;
            this.trigger("connected");
        };        

        return LaunchChannel;
    });