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
import $ from 'jquery';
import _ from 'lodash';
import TabList from './tab-list';
import FileTab from './file-tab';
import File from '../workspace/file';

const FileTabList = TabList.extend(
    /** @lends FileTabList.prototype */
    {
        /**
         * Initialize the FileTabList
         * @param {object} options - list of options
         */
        initialize(options) {
            _.set(options, 'tabModel', FileTab);
            TabList.prototype.initialize.call(this, options);
            const lastWorkedFiles = this.getBrowserStorage().get('workingFileSet');
            this._workingFileSet = [];
            const self = this;
            if (!_.isNil(lastWorkedFiles)) {
                lastWorkedFiles.forEach((fileID) => {
                    self._workingFileSet.push(fileID);
                });
            }
            const commandManager = _.get(this, 'options.application.commandManager');
            const optionsNextTab = {
                shortcuts: {
                    mac: {
                        key: 'command+right',
                        label: '\u2318\u2192',
                    },
                    other: {
                        key: 'ctrl+right',
                        label: 'Ctrl+Right',
                    },
                },
            };
            commandManager.registerCommand('next-tab', optionsNextTab);
            commandManager.registerHandler('next-tab', this.goToNextTab, this);
            const optionsPrevTab = {
                shortcuts: {
                    mac: {
                        key: 'command+left',
                        label: '\u2318\u2190',
                    },
                    other: {
                        key: 'ctrl+left',
                        label: 'Ctrl+Left',
                    },
                },
            };
            commandManager.registerCommand('previous-tab', optionsPrevTab);
            commandManager.registerHandler('previous-tab', this.goToPreviousTab, this);
        },
        render() {
            TabList.prototype.render.call(this);
            if (!_.isEmpty(this._workingFileSet)) {
                const self = this;
                const activeTabId = this.getBrowserStorage().get('activeTab');
                this._workingFileSet.forEach((fileID) => {
                    const fileData = self.getBrowserStorage().get(fileID);
                    const file = new File(fileData, { storage: self.getBrowserStorage() });
                    const tab = self.newTab(_.set({}, 'tabOptions.file', file));
                    tab.updateHeader();
                });

                if (!_.isUndefined(activeTabId)) {
                    const activeTab = _.find(this.getTabList(), tab => tab.cid === activeTabId);
                    if (!_.isUndefined(activeTab)) {
                        this.setActiveTab(activeTab);
                    }
                }
            }
        },
        setActiveTab(tab) {
            $('#transformOverlay').remove();
            // This is a hack @todo need to refactor transformer code 
            if (tab._fileEditor !== undefined) {
                tab._fileEditor.setTransformState(false);
            }
            TabList.prototype.setActiveTab.call(this, tab);
        },
        addTab(tab) {
            TabList.prototype.addTab.call(this, tab);
            // avoid re-addition of init time files
            if (tab instanceof FileTab && !_.includes(this._workingFileSet, tab.getFile().id)) {
                this._workingFileSet.push(tab.getFile().id);
                this.getBrowserStorage().put('workingFileSet', this._workingFileSet);
            }
        },

        removeTab(tab) {
            const commandManager = _.get(this, 'options.application.commandManager');
            const self = this;
            const remove = function () {
                TabList.prototype.removeTab.call(self, tab);
                if (tab instanceof FileTab) {
                    _.remove(self._workingFileSet, fileID => _.isEqual(fileID, tab.getFile().id));
                    tab.trigger('tab-removed');
                    self.getBrowserStorage().destroy(tab.getFile());
                    self.getBrowserStorage().put('workingFileSet', self._workingFileSet);
                  // open welcome page upon last tab close
                    if (_.isEmpty(self.getTabList())) {
                        const commandManager = _.get(self, 'options.application.commandManager');
                        commandManager.dispatch('go-to-welcome-page');
                    }
                }
            };

            if (!_.isFunction(tab.getFile)) {
                remove();
                return;
            }

            const file = tab.getFile();
            if (file.isPersisted() && !file.isDirty()) {
                // if file is not dirty no need to ask for confirmation
                remove();
                return;
            }

            if (!file.isPersisted() && _.isEmpty(file.getContent())) {
                // if file is not dirty no need to ask for confirmation
                remove();
                return;
            }

            const handleConfirm = function (shouldSave) {
                if (shouldSave) {
                    const done = function (saved) {
                        if (saved) {
                            remove();
                        }
                        // saved is false if cancelled. Then don't close the tab
                    };
                    commandManager.dispatch('save', { callback: done });
                } else {
                    remove();
                }
            };

            commandManager.dispatch('open-close-file-confirm-dialog', {
                file,
                handleConfirm,
            });
        },

        newTab(opts) {
            const options = opts || {};
            if (_.has(options, 'tabOptions.file')) {
                const file = _.get(options, 'tabOptions.file');
                file.setStorage(this.getBrowserStorage());
            }
            const tab = TabList.prototype.newTab.call(this, options);
            if (tab instanceof FileTab) {
                tab.updateHeader();
            }
            return tab;
        },
        getBrowserStorage() {
            return _.get(this, 'options.application.browserStorage');
        },
        hasFilesInWorkingSet() {
            return !_.isEmpty(this._workingFileSet);
        },
        goToNextTab() {
            if (!_.isEmpty(this._tabs)) {
                let nextTabIndex = 0;
                const currentActiveIndex = _.findIndex(this._tabs, this.activeTab);
                if (currentActiveIndex >= 0) {
                    if (currentActiveIndex < (this._tabs.length - 1)) {
                        nextTabIndex = currentActiveIndex + 1;
                    }
                }
                const nextTab = _.nth(this._tabs, nextTabIndex);
                this.setActiveTab(nextTab);
            }
        },

        goToPreviousTab() {
            if (!_.isEmpty(this._tabs)) {
                const currentActiveIndex = _.findIndex(this._tabs, this.activeTab);
                let prevTabIndex = 0;
                if (currentActiveIndex === 0) {
                    prevTabIndex = this._tabs.length - 1;
                } else {
                    prevTabIndex = currentActiveIndex - 1;
                }
                const previousTab = _.nth(this._tabs, prevTabIndex);
                this.setActiveTab(previousTab);
            }
        },

        getTabForFile(file) {
            return _.find(this._tabs, (tab) => {
                if (tab instanceof FileTab) {
                    const tabFile = tab.getFile();
                    return _.isEqual(tabFile.getPath(), file.getPath()) && _.isEqual(tabFile.getName(), file.getName());
                }
                return undefined;
            });
        },
    });

export default FileTabList;
