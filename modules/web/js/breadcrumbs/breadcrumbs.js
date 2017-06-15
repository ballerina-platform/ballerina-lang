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
import 'jquery-ui';

const BreadcrumbControl = Backbone.View.extend(
        /** @lends BreadcrumbControl.prototype */
    {
            /**
             * @augments Backbone.View
             * @constructs
             * @class BreadcrumbControl
             * @param {Object} config configuration options for the BreadcrumbControl
             */
        initialize(config) {
            if (!_.has(config, 'container')) {
                log.error('Cannot init breadcrumbs. config container not found.');
            }
            const container = $(_.get(config, 'container'));
            if (!container.length > 0) {
                log.error(`Cannot init breadcrumbs. selector ${_.get(config, 'container')} returned no elements.`);
            } else {
                this._$parent_el = container;
            }
            this._options = config;
        },
        render() {
            const list = $('<ol></ol>');
            this._$parent_el.append(list);
            this.$el = list;
            list.addClass(_.get(this._options, 'cssClass.list'));

            const tabController = _.get(this._options, 'application.tabController');
            this.listenTo(tabController, 'active-tab-changed', function (evt) {
                const activeTab = evt.newActiveTab;
                if (_.isFunction(activeTab.getFile)) {
                    this.setPath(activeTab.getFile().getPath(), activeTab.getFile().getName());
                } else {
                    this.setPath('Ballerina Composer', activeTab.getTitle());
                }
            });

            this.listenTo(tabController, 'last-tab-removed', function () {
                this.setPath('', '');
            });
        },
        setPath(path, file) {
            path = _.replace(path, /\\/gi, '/');
            const pathArr = _.split(path, '/');

            this.$el.empty();
            pathArr.forEach((segment) => {
                if (!_.isEmpty(segment)) {
                    const li = $(`<li>${segment}</li>`);
                    li.addClass(_.get(this._options, 'cssClass.item'));
                    this.$el.append(li);
                }
            });
            const fileLi = $(`<li>${file}</li>`);
            fileLi.addClass(_.get(this._options, 'cssClass.item'));
            fileLi.addClass(_.get(this._options, 'cssClass.active'));
            this.$el.append(fileLi);
        },
    });

export default BreadcrumbControl;
