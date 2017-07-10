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
import _ from 'lodash';
import Backbone from 'backbone';
import log from 'log';

let File = Backbone.Model.extend(
    {
        defaults: {
            path: 'temp',
            name: 'untitled',
            packageName: '',
            content: '',
            isPersisted: false,
            lastPersisted: _.now(),
            isDirty: true,
            langserverCallbacks: undefined,
        },

        initialize (attrs, options) {
            let errMsg;
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

        save(){
            if(!_.isNil(this._storage.get(this.id))){
                this._storage.update(this);
            } else {
                this._storage.create(this);
            }

            if (this.isPersisted()) {
                /**
                 * We send the fileDidSave notification to the language server on the file save
                 */
                const docUri = this.getPath() + '/' + this.getName();
                // Send document closed notification to the language server
                let didSaveOptions = {
                    didSaveParams: {
                        textDocument: {
                            documentUri: docUri,
                            documentId: this.id
                        },
                        text: this.getContent()
                    }
                };
                //this.getLangserverCallbacks().documentDidSaveNotification(didSaveOptions);
            }
            return this;
        },

        setPath(path){
            this.set('path', path);
            return this;
        },

        setStorage(storage){
            this._storage = storage;
            return this;
        },

        setPersisted(isPersisted){
            this.set('isPersisted', isPersisted);
            return this;
        },

        setLastPersisted(lsatPersisted){
            this.set('lastPersisted', lsatPersisted);
            return this;
        },

        setDirty(isDirty){
            this.set('isDirty', isDirty);
            this.trigger('dirty-state-change', isDirty);
            return this;
        },

        setName(name){
            this.set('name', name);
            return this;
        },

        setContent(content, changeInfo){
            const oldContent = this.get('content');
            this.set('content', content);
            this.trigger('content-modified', content, changeInfo);
            // if the new content is not equal to old content
            // set file dirty
            if (!_.isEqual(oldContent, content)) {
                this.setDirty(true);
            }
            // save to local storage
            this.save();
            return this;
        },

        setPackageName(pkgName){
            this.set('packageName', pkgName);
            return this;
        },

        getPath(){
            return this.get('path');
        },

        getName(){
            return this.get('name');
        },

        getContent(){
            return this.get('content');
        },

        getLastPersisted(){
            return this.get('lastPersisted');
        },

        getPackageName(){
            return this.get('packageName');
        },

        isPersisted(){
            return this.get('isPersisted');
        },

        isDirty(){
            return this.get('isDirty');
        },

        setLangserverCallbacks(callbacks) {
            this.set('langserverCallbacks', callbacks);
        },

        getLangserverCallbacks() {
            return this.get('langserverCallbacks');
        },
    },
);

export default File;
