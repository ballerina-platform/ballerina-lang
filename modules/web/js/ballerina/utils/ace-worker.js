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
define(function(require, exports, module) {

    var oop = require("ace/lib/oop");
    var backends = require("bal_configs/backends");
    var Mirror = require("ace/worker/mirror").Mirror;

    var WorkerModule = exports.WorkerModule = function(sender) {
        Mirror.call(this, sender);
    };

    // Mirror is a simple class which keeps main and webWorker versions of the document in sync
    oop.inherits(WorkerModule, Mirror);

    (function() {
        this.onUpdate = function() {
            var value = this.doc.getValue();
            if(value.trim()){
                var errors = [];
                var content = { "content": value};
                var request = new XMLHttpRequest();
                var self = this;

                request.onreadystatechange = function() {
                    if(request.readyState === 4) {
                        if(request.status === 200) {
                            errors = (JSON.parse(request.responseText)).errors;
                            self.sender.emit("lint", errors);
                        }
                    }
                };

                request.open('POST', backends.services.validator.endpoint, true);
                request.setRequestHeader("Content-type", "application/json");
                request.send(JSON.stringify(content));
            } else {
                this.sender.emit("lint", []);
            }
        };
    }).call(WorkerModule.prototype);

});