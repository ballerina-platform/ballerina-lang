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

class BrowserStorage {

    constructor(name) {
        this.name = name;
    }

    // Generate four random hex digits.
    S4() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }

    // Generate a pseudo-GUID by concatenating random hexadecimal.
    guid() {
        return (`${this.S4() + this.S4()}-${this.S4()}-${this.S4()}-${this.S4()}-${this.S4()}${this.S4()}${this.S4()}`);
    }


    put(key, value) {
        this.localStorage().setItem(`${this.name}-${key}`, JSON.stringify(value));
    }

    get(key) {
        return this.jsonData(this.localStorage().getItem(`${this.name}-${key}`));
    }

    create(model) {
        if (!model.id) {
            model.id = this.guid();
            model.set(model.idAttribute, model.id);
        }
        this.localStorage().setItem(`${this.name}-${model.id}`, JSON.stringify(model));
        return this.find(model);
    }

    update(model) {
        this.localStorage().setItem(`${this.name}-${model.id}`, JSON.stringify(model));
        return this.find(model);
    }

    find(model) {
        return this.jsonData(this.localStorage().getItem(`${this.name}-${model.id}`));
    }

    destroy(model) {
        if (model.isNew()) { return false; }
        this.localStorage().removeItem(`${this.name}-${model.id}`);
        return model;
    }

    localStorage() {
        return localStorage;
    }

    jsonData(data) {
        return data && JSON.parse(data);
    }

}

export default BrowserStorage;
