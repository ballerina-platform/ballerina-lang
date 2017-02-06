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

define(['lodash', 'event_channel', 'log', './tools'],
    function (_, EventChannel, log, Tools){

        var Channel = function(args){
            _.assign(this, args);
            var self = this;
            if(args.endpoint != undefined){
                var websocket = new WebSocket(args.endpoint);
                websocket.onopen = function () {
                    self.debugger.trigger("connected");
                };
                websocket.onerror = function (e) {
                    log.error('debugger error', e);
                };
                websocket.onmessage = _.bindKey(this, 'parseMessage');
                this.websocket = websocket;
            }else{
                //@todo need to handle error.
            }
            var self = this;

            Tools.on('execute-action', function (action) {
                self.sendMessage({command: action});
            });
        };

        Channel.prototype = Object.create(EventChannel.prototype);

        Channel.prototype.constructor = Channel;

        Channel.prototype.parseMessage = function (strMessage) {            
            var message = JSON.parse(strMessage.data);
            this.debugger.processMesssage(message);
        };

        Channel.prototype.sendMessage = function (message) {
            this.websocket.send(JSON.stringify(message))
        };

        return Channel;
    });