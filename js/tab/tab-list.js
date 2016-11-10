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
define(['logger', 'jquery', 'lodash', 'backbone', './tab'], function (log, $, _, Backbone, Tab) {

    var TabList = Backbone.View.extend(
        /** @lends TabList.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class TabList represents a list of tabs.
             */
            initialize: function (options) {
                var TabCollection, errMsg;
                // check whether a custom Tab type is set
                if (_.has(options, 'tabModel')) {
                    this.TabModel = _.get(options, 'tabModel');
                    // check whether the custom type is of type Tab
                    if (!this.TabModel instanceof Tab) {
                        errMsg = 'custom tab model is not sub type of Tab: ' + TabModel;
                        log.error(errMsg);
                        throw errMsg;
                    }
                } else {
                    this.TabModel = Tab;
                }
                TabCollection = Backbone.Collection.extend({
                    model: this.TabModel
                });
                this._tabs = new TabCollection();
                if (!_.has(options, 'container')) {
                    // set default selector
                    _.set(options, 'container', '.tabs-container');
                }
                // check whether container element exists in dom
                if (!$(options.containerSelector).length > 0) {
                    errMsg = 'unable to find container for tab list with selector: ' + options.containerSelector;
                    log.error(errMsg);
                    throw errMsg;
                }
                this.options = options;
            },
            render: function () {
            },
            /**
             * add a tab to the tab list.
             *
             * @param {Tab} tab an object of tab
             * @fires TabList#tab-added
             */
            addTab: function (tab) {
                tab.setParentTabList(this);
                this._tabs.add(tab);
                /**
                 * tab added event.
                 * @event TabList#tab-added
                 * @type {Tab}
                 */
                this.trigger("tab-added", tab);
            },
            /**
             * gets tab
             * @param {string|Tab} tab id, cid or the object
             * @returns {*}
             */
            getTab: function (tab) {
                return this._tabs.get(tab);
            },
            /**
             * removes a tab
             * @param {Tab} tab the tab instance
             * @fires TabList#tab-removed
             */
            removeTab: function (tab) {
                if (!this._tabs.contains(tab)) {
                    var errMsg = 'tab : ' + tab.id + 'is not part of this tab list.';
                    log.error(errMsg);
                    throw errMsg;
                }
                this._tabs.remove(tab);
                /**
                 * tab removed event.
                 * @event TabList#tab-removed
                 * @type {Tab}
                 */
                this.trigger("tab-removed", tab);
            },
            /**
             * set selected tab
             * @param {Tab} tab the tab instance
             * @fires TabList#active-tab-changed
             */
            setActiveTab: function (tab) {
                if (!_.isEqual(this.activeTab, tab)) {
                    if (!this._tabs.contains(tab)) {
                        var errMsg = 'tab : ' + tab.id + 'is not part of this tab list.';
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var lastActiveTab = this.activeTab;
                    this.activeTab = tab;

                    /**
                     * Active tab changed event.
                     * @event TabList#active-tab-changed
                     * @type {object}
                     * @property {Tab} lastActiveTab - last active tab.
                     * @property {Tab} newActiveTab - new active tab.
                     */
                    var evt = {lastActiveTab: lastActiveTab, newActiveTab: tab};
                    this.trigger("active-tab-changed", evt);
                }
            },
            /**
             * active tab
             * @returns {Tab}
             */
            getActiveTab: function () {
                return this.activeTab;
            },
            /**
             * Creates a new tab.
             * @param opts
             *          switchToNewTab: indicate whether to switch to new tab after creation
             *          tabOptions: constructor args for the tab
             * @returns {Tab} created tab instance
             * @event TabList#tab-added
             * @fires TabList#active-tab-changed
             */
            newTab: function (opts) {
                var newTab;
                if (_.has(opts, 'tabOptions')) {
                    newTab = new this.TabModel(_.get(opts, 'tabOptions'));
                } else {
                    newTab = new this.TabModel();
                }
                this.addTab(newTab);
                if (_.has(opts, 'switchToNewTab')) {
                    if (_.isBoolean(_.get(opts, 'switchToNewTab')) && _.get(opts, 'switchToNewTab')) {
                        this.setActiveTab(newTab);
                    }
                }
                return newTab;
            },

            forEach: this._tabs.forEach
        });

    return TabList;
});
