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
import log from 'log';
import $ from 'jquery';
import _ from 'lodash';
import Backbone from 'backbone';
import 'bootstrap';
import Tab from './tab';

const TabList = Backbone.View.extend(
    /** @lends TabList.prototype */
    {
        /**
         * @augments Backbone.View
         * @constructs
         * @class TabList represents a list of tabs.
         */
        initialize(options) {
            let errMsg;
            // check whether a custom Tab type is set
            if (_.has(options, 'tabModel')) {
                this.TabModel = _.get(options, 'tabModel');
                // check whether the custom type is of type Tab
                if (!(this.TabModel.prototype instanceof Tab)) {
                    errMsg = `custom tab model is not a sub type of Tab: ${Tab}`;
                    log.error(errMsg);
                    throw errMsg;
                }
            } else {
                this.TabModel = Tab;
            }
            this._tabs = [];

            if (!_.has(options, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            const container = $(_.get(options, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = `unable to find container for tab list with selector: ${_.get(options, 'container')}`;
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;
            if (!_.has(options, 'tabs.container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            const tabContainer = this._$parent_el.find(_.get(options, 'tabs.container'));
            // check whether container element exists in dom
            if (!tabContainer.length > 0) {
                errMsg = `unable to find container for tab list with selector: ${_.get(options, 'tabs.container')}`;
                log.error(errMsg);
                throw errMsg;
            }
            this._$tab_container = tabContainer;
            this.options = options;
        },
        getBrowserStorage() {
            return _.get(this, 'options.application.browserStorage');
        },
        render() {
            const tabHeaderContainer = this._$parent_el.children(_.get(this.options, 'headers.container'));
            const tabList = $('<ul></ul>');
            tabHeaderContainer.append(tabList);

            const tabListClass = _.get(this.options, 'headers.cssClass.list');
            tabList.addClass(tabListClass);
            this._$tabList = tabList;
            this.el = tabList.get();

            if (_.has(this.options, 'toolPalette')) {
                _.get(this.options, 'toolPalette').render();
            }
        },

        hideTabComponents() {
            const tabHeaderContainer = this._$parent_el.children(_.get(this.options, 'headers.container'));
            tabHeaderContainer.hide();
            // Hide the tool palette
            _.get(this.options, 'toolPalette').hideToolPalette();
        },

        showTabComponents() {
            const tabHeaderContainer = this._$parent_el.children(_.get(this.options, 'headers.container'));
            tabHeaderContainer.show();
            // Show the tool palette
            _.get(this.options, 'toolPalette').showToolPalette();
        },

        createHeaderForTab(tab) {
            const tabHeader = $('<li></li>');
            this._$tabList.append(tabHeader);

            const tabHeaderClass = _.get(this.options, 'headers.cssClass.item');
            tabHeader.addClass(tabHeaderClass);

            const tabHeaderLink = $('<a></a>');
            tabHeader.append(tabHeaderLink);
            tabHeader.link = tabHeaderLink;
            tabHeaderLink.attr('href', `#${tab.cid}`);
            tabHeaderLink.text(tab.getTitle());

            tabHeader.setText = function (text) {
                tabHeaderLink.text(text);
            };

            const self = this;
            tabHeaderLink.click((e) => {
                tabHeaderLink.tab('show');
                self.setActiveTab(tab);
                e.preventDefault();
                e.stopPropagation();
            });

            const tabCloseBtn = $('<button type="button" >×</button>');
            tabHeader.append(tabCloseBtn);
            tabCloseBtn.addClass(_.get(this.options, 'tabs.tab.cssClass.tab_close_btn'));
            tabCloseBtn.click((e) => {
                self.removeTab(tab);
                e.preventDefault();
                e.stopPropagation();
            });

            tab.on('title-changed', (title) => {
                tabHeaderLink.text(title);
            });

            tab.setHeader(tabHeader);
        },

        getTabContainer() {
            return this._$tab_container;
        },
        /**
         * add a tab to the tab list.
         *
         * @param {Tab} tab an object of tab
         * @fires TabList#tab-added
         */
        addTab(tab) {
            tab.setParent(this);
            this.createHeaderForTab(tab);
            this._tabs.push(tab);
            /**
             * tab added event.
             * @event TabList#tab-added
             * @type {Tab}
             */
            this.trigger('tab-added', tab);
        },
        /**
         * gets tab
         * @param {string} tab id
         * @returns {*}
         */
        getTab(tabId) {
            return _.find(this._tabs, ['id', tabId]);
        },
        getTabList() {
            return this._tabs;
        },
        /**
         * removes a tab
         * @param {Tab} tab the tab instance
         * @fires TabList#tab-removed
         */
        removeTab(tab) {
            if (!_.includes(this._tabs, tab)) {
                const errMsg = `tab : ${tab.id}is not part of this tab list.`;
                log.error(errMsg);
                throw errMsg;
            }
            const tabIndex = _.findIndex(this._tabs, tab);

            _.remove(this._tabs, tab);
            tab.getHeader().remove();
            tab.remove();
            /**
             * tab removed event.
             * @event TabList#tab-removed
             * @type {Tab}
             */
            this.trigger('tab-removed', tab);
            if (_.isEmpty(this._tabs)) {
                this.trigger('last-tab-removed', tab);
            }

            // switch to tab at last or next index
            // make sure there are remaining tabs
            if (this._tabs.length > 0 && !_.isEqual(tabIndex, -1)) {
                // if removing tab is 0th tab, next tab is also the 0th
                let nextTabIndex = 0;
                if (!_.isEqual(tabIndex, 0)) {
                    nextTabIndex = tabIndex - 1;
                }
                const nextTab = this._tabs[nextTabIndex];
                // if closed tab was the active tab, change active tab to next
                if (_.isEqual(this.getActiveTab(), tab)) {
                    this.setActiveTab(nextTab);
                }
            }
        },
        /**
         * set selected tab
         * @param {Tab} tab the tab instance
         * @fires TabList#active-tab-changed
         */
        setActiveTab(tab) {
            if (!_.isEqual(this.activeTab, tab)) {
                if (!_.includes(this._tabs, tab)) {
                    const errMsg = `tab : ${tab.cid}is not part of this tab list.`;
                    log.error(errMsg);
                    throw errMsg;
                }
                const lastActiveTab = this.activeTab;
                this.activeTab = tab;
                const activeTabHeaderClass = _.get(this.options, 'headers.cssClass.active');

                if (!_.isUndefined(lastActiveTab)) {
                    lastActiveTab.getHeader().removeClass(activeTabHeaderClass);
                    lastActiveTab.setActive(false);
                }
                this.activeTab.getHeader().addClass(activeTabHeaderClass);
                this.activeTab.setActive(true);

                const storage = this.getBrowserStorage();
                storage.put('activeTab', tab.cid);

                // this.activeTab.getHeader().tab('show');
                /**
                 * Active tab changed event.
                 * @event TabList#active-tab-changed
                 * @type {object}
                 * @property {Tab} lastActiveTab - last active tab.
                 * @property {Tab} newActiveTab - new active tab.
                 */
                const evt = { lastActiveTab, newActiveTab: tab };
                this.trigger('active-tab-changed', evt);
            }
        },
        /**
         * active tab
         * @returns {Tab}
         */
        getActiveTab() {
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
        newTab(opts) {
            const tabOptions = _.get(opts, 'tabOptions') || {};
            _.set(tabOptions, 'application', this.options.application);
            // merge view options from app config
            _.assign(tabOptions, _.get(this.options, 'tabs.tab'));
            _.set(tabOptions, 'tabs_container', _.get(this.options, 'tabs.container'));
            _.set(tabOptions, 'parent', this);
            let newTab;
            // user provided a custom tab type
            if (_.has(opts, 'tabModel')) {
                const TabModel = _.get(opts, 'tabModel');
                newTab = new TabModel(tabOptions);
            } else {
                newTab = new this.TabModel(tabOptions);
            }
            this.addTab(newTab);
            // check whether switch to new tab set to false
            if (_.has(opts, 'switchToNewTab')) {
                if (_.isBoolean(_.get(opts, 'switchToNewTab')) && _.get(opts, 'switchToNewTab')) {
                    this.setActiveTab(newTab);
                }
            } else {
                // activate by default
                this.setActiveTab(newTab);
            }
            newTab.render();
            return newTab;
        },

        forEach(callback) {
            this._tabs.forEach(callback);
        },
    });

export default TabList;
