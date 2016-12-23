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
define(['log', 'jquery', 'lodash', './tab-list', './file-tab',  'workspace'],

    function (log, $, _, TabList, ServiceTab, Workspace) {

    var FileTabList = TabList.extend(
    /** @lends FileTabList.prototype */
    {
        /**
         * @augments FileTabList
         * @constructs
         * @class FileTabList represents service tab list.
         */
        initialize: function (options) {
            _.set(options, 'tabModel', ServiceTab);
            TabList.prototype.initialize.call(this, options);
            var lastWorkedFiles = this.getBrowserStorage().get('workingFileSet');
            this._workingFileSet = [];
            var self = this;
            if(!_.isNil(lastWorkedFiles)){
                lastWorkedFiles.forEach(function(fileData){
                    var file = new Workspace.File(fileData);
                    self._workingFileSet.push(file);
                });
            }
        },
        render: function() {
            TabList.prototype.render.call(this);
            if(!_.isEmpty(this._workingFileSet)){
                var self = this;
                this._workingFileSet.forEach(function(file){
                    self.newTab(_.set({}, 'tabOptions.file', file));
                });
            }
        },
        addTab: function(tab) {
            TabList.prototype.addTab.call(this, tab);
            // avoid re-addition of init time files
            if(!_.includes(this._workingFileSet, tab.getFile())){
                this._workingFileSet.push(tab.getFile());
                this.getBrowserStorage().put('workingFileSet', this._workingFileSet);
            }
        },
        removeTab: function (tab) {
            TabList.prototype.removeTab.call(this, tab);
            _.remove(this._workingFileSet, tab.getFile());
            if(tab.getFile().get('isTemp')){
                this.getBrowserStorage().destroy(tab.getFile());
            }
            this.getBrowserStorage().put('workingFileSet', this._workingFileSet);
        },
        newTab: function(opts) {
            var options = opts || {};
            TabList.prototype.newTab.call(this, options);
        },
        getBrowserStorage: function(){
            return _.get(this, 'options.application.browserStorage');
        },
        hasFilesInWorkingSet: function(){
            return !_.isEmpty(this._workingFileSet);
        }
    });

    return FileTabList;
});
