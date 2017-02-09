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
define(['jquery', 'lodash', 'backbone', 'log'], function ($, _, Backbone, log) {

    var File = Backbone.Model.extend(
        {
            defaults: {
                path: 'temp',
                name: 'untitled',
                content: undefined,
                isPersisted: false,
                lastPersisted: _.now(),
                isDirty: true
            },

            initialize: function (attrs, options) {
                var errMsg;
                if (!this.get('isPersisted')){
                    if(!_.has(options, 'storage')){
                        errMsg = 'unable to find storage' + _.toString(attrs);
                        log.error(errMsg);
                        throw errMsg;
                    }
                    this._storage = _.get(options, 'storage');
                    this._storage .create(this);
                }
            },

            save: function(){
                if(!_.isNil(this._storage.get(this.id))){
                    this._storage.update(this);
                } else {
                    this._storage.create(this);
                }
                return this;
            },

            setPath: function(path){
                this.set('path', path);
                return this;
            },

            setStorage: function(storage){
                this._storage = storage;
                return this;
            },

            setPersisted: function(isPersisted){
                this.set('isPersisted', isPersisted);
                return this;
            },

            setLastPersisted: function(lsatPersisted){
                this.set('lastPersisted', lsatPersisted);
                return this;
            },

            setDirty: function(isDirty){
                this.set('isDirty', isDirty);
                this.trigger('dirty-state-change', isDirty);
                return this;
            },

            setName: function(name){
                this.set('name', name);
                return this;
            },

            setContent: function(name){
                this.set('content', name);
                return this;
            },

            getPath: function(){
                return this.get('path')
            },

            getName: function(){
                return this.get('name')
            },

            getContent: function(){
                return this.get('content')
            },

            getLastPersisted: function(){
                return this.get('lastPersisted');
            },


            isPersisted: function(){
                return this.get('isPersisted')
            },

            isDirty: function(){
                return this.get('isDirty')
            }

        });

    return File;
});