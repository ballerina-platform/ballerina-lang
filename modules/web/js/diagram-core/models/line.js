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
define(['require', 'jquery', 'lodash', 'backbone', './point'], function (require, $, _, Backbone, Point) {

    var Line = Backbone.Model.extend(
        /** @lends Line.prototype */
        {
            /**
             * @augments Backbone.Model
             * @constructs
             * @class Line represents a line in paper.
             */
            initialize: function (attrs, options) {
            },

            /**
             * default line is a 0 length line at 0,0.
             */
            defaults: {
                start: new Point(),
                end: new Point()
            },

            /**
             * Get or set starting point of the line.
             * @param {Point} [point] Ignore if you only want to get the starting point.
             * @returns {Point|void} starting point of the line or void if setter is invoked.
             */
            start: function (point) {
                if (point === undefined) {
                    return this.get('start');
                }
                this.set('start', point);
            },
            /**
             * Get or set ending point of the line.
             * @param {Point} [point] Ignore if you only want to get the ending point.
             * @returns {Point|void} ending point of the line or void if setter is invoked.
             */
            end: function (point) {
                if (point === undefined) {
                    return this.get('start');
                }
                this.set('end', point);
            },

            /**
             *  Gives the length of the line.
             *  @returns {number} length of the line.
             */
            length: function () {
                return this.end().distFrom(this.start());
            }
        });

    return Line;
});