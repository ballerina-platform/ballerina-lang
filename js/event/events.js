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
define(['lodash', 'backbone'], function (_, Backbone) {
    var EventManager = Backbone.Model.extend(
    /** @lends Eventmanager.prototype */
    {
        idAttribute: this.cid,
        modelName: "EventManager",
        /**
         * @augments Backbone.Model
         * @constructs
         * @class Handles validations of the diagram
         */
        initialize: function (attrs, options) {
            this.draggedElement();
            this.isActivated();
            this.currentType();
            this.invalid = false;
        },
        //keep the current dragged element type
        currentType: function (type) {
            if (_.isUndefined(type)) {
                return this.get('currentType');
            } else {
                this.set('currentType', type);
            }
        },
        draggedElement: function (element) {
            if (_.isUndefined(element)) {
                return this.get('draggedElement');
            } else {
                this.set('draggedElement', element);
            }
        },
        // check the current activated element's valid drops
        isActivated: function (activatedType) {
            if (activatedType != null) {
                if (this.currentType() != "Resource" && this.currentType() != "EndPoint" && this.currentType() != "Source") {
                    // validation for active endpoints
                    if (activatedType.includes("EndPoint") || activatedType.includes("Source")) {
                        this.invalid = true;
                    }
                    else {
                        this.invalid = false;
                    }
                }
            }
        },
        // Called when text controller changes occurs and if there is a parent element
        notifyParent: function(parentModel, currentTextModel){
            console.log("parent received it");
        }
    });

    return EventManager;

});

