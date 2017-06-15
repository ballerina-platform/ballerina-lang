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
import _ from 'lodash';
import $ from 'jquery';
import Backbone from 'backbone';

/**
 * Backbone view for a Tab.
 */
const Tab = Backbone.View.extend(
    /** @lends Tab.prototype */
    {
        /**
         * Initializes a tab.
         * 
         * @param {Object} options The object for creating a tab.
         * @param {string} options.title The title of the tab.
         * @param {HTMLElement} options.template The html element object for the tab.
         */
        initialize(options) {
            let errMsg;
            // FIXME
            _.set(this, 'id', this.cid);
            _.set(this, '_title', _.get(options, 'title'));
            if (!_.has(options, 'template')) {
                errMsg = `unable to find config template ${_.toString(options)}`;
                log.error(errMsg);
                throw errMsg;
            }
            const template = $(_.get(options, 'template'));
            if (!template.length > 0) {
                errMsg = `unable to find template with id ${_.get(options, 'template')}`;
                log.error(errMsg);
                throw errMsg;
            }
            this._template = template;
            this.options = options;
            this._isActive = false;

            if (_.has(options, 'parent')) {
                this.setParent(_.get(options, 'parent'));
            }

            // create the tab template
            const tab = this._template.children('div').clone();
            this.getParent().getTabContainer().append(tab);
            const tabClass = _.get(this.options, 'cssClass.tab');
            tab.addClass(tabClass);
            tab.attr('id', this.cid);
            this.$el = tab;
        },
        remove() {
            this.trigger('removed');
            Backbone.View.prototype.remove.call(this);
        },
        setActive(isActive) {
            if (_.isBoolean(isActive)) {
                this._isActive = isActive;
                if (isActive) {
                    this.$el.addClass(_.get(this.options, 'cssClass.tab_active'));
                } else {
                    this.$el.removeClass(_.get(this.options, 'cssClass.tab_active'));
                }
            }
        },
        isActive() {
            return this._isActive;
        },
        setHeader(header) {
            this._tabHeader = header;
        },
        getHeader() {
            return this._tabHeader;
        },
        getContentContainer() {
            return this.$el.get(0);
        },
        getParent() {
            return this._parentTabList;
        },
        setParent(parentTabList) {
            this._parentTabList = parentTabList;
        },
        getTitle() {
            return _.isNil(this._title) ? 'untitled' : this._title;
        },
        setTitle(title) {
            this._title = title;
            this.trigger('title-changed', title);
        },
    });

export default Tab;

