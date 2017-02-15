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

        var Channel = function(args){
            if(_.isNil(args.endpoint)){
                throw "Invalid Endpoint";
            }
            _.assign(this, args);

            // See http://tools.ietf.org/html/rfc6455#section-7.4.1
            this.ws_normal_code = 1000;
            this.ws_ssl_code = 1015;
        };

        Channel.prototype = Object.create(EventChannel.prototype);

        Channel.prototype.constructor = Channel;

        Channel.prototype.connect = function(){
            var websocket = new WebSocket(this.endpoint);
            //bind functions
            websocket.onmessage = _.bindKey(this, 'parseMessage');
            websocket.onopen = _.bindKey(this, 'onOpen');
            websocket.onclose = _.bindKey(this, 'onClose');
            websocket.onerror = _.bindKey(this, 'onError');
            this.websocket = websocket;
        }

        Channel.prototype.parseMessage = function (strMessage) {            
            var message = JSON.parse(strMessage.data);
            this.debugger.processMesssage(message);
        };

        Channel.prototype.sendMessage = function (message) {
            this.websocket.send(JSON.stringify(message))
        };

        Channel.prototype.onClose = function(event){
            this.debugger.active = false;
            this.debugger.trigger("session-terminated");
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
            log.error(reason);
        };

        Channel.prototype.onError = function(event){
            this.debugger.active = false;
            this.debugger.trigger("session-error");
        };

        Channel.prototype.onOpen = function(event){
            this.debugger.active = true;
            this.debugger.trigger("session-started");
        };        

        return Channel;
    });