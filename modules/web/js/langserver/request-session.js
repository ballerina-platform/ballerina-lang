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

import log from 'log';
import _ from 'lodash';

class RequestSession {
    constructor() {
        this._callback = undefined;
        this.message = undefined;
        this._id = uuid();
    }

    getCallback() {
        return this._callback;
    }

    getId() {
        return this._id;
    }

    getMessage() {
        return this._message;
    }

    setMessage(message) {
        this._message = message;
    }

    setCallback(callback) {
        this._callback = callback;
    }

    executeCallback(message) {
        this._callback(message);
    }
}

// Auto generated Id for the session
var uuid =  function (){
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
};

export default  RequestSession;