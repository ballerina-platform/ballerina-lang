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
define(['log', 'lodash', 'backbone'], function (log, _, Backbone) {
    var DragDropManager = Backbone.Model.extend(
    /** @lends DragDropManager.prototype */
    {
        idAttribute: this.cid,
        modelName: "DragDropManager",
        /**
         * @augments Backbone.Model
         * @constructs
         * @class Handles validations for drag and drop
         */
        initialize: function (attrs, options) {
        },

        setTypeBeingDragged: function (type, validateDropTargetCallback) {
            if (!_.isUndefined(type)) {
                this.set('typeBeingDragged', type);
                log.debug('started dragging ' + JSON.stringify(type));
            }
            if (!_.isUndefined(validateDropTargetCallback)) {
                this.set('validateDropTargetCallback', validateDropTargetCallback);
            }
        },

        getTypeBeingDragged: function (type) {
            return  this.get('typeBeingDragged');
        },

        reset: function(){
            this.trigger('drag-stop', this.get('typeBeingDragged'));
            this.set('typeBeingDragged', undefined);
            this.set('validateDropTargetCallback', undefined);
            this.set('activatedDropTarget', undefined);
            this.set('validateDropSourceCallback', undefined);

        },

        setActivatedDropTarget: function (activatedDropTarget, validateDropSourceCallback) {
            if (!_.isUndefined(activatedDropTarget)) {
                if (!_.isEqual(activatedDropTarget, this.get('activatedDropTarget'))){
                    this.trigger('drop-target-changed', activatedDropTarget);
                }
                this.set('activatedDropTarget', activatedDropTarget);
             }
            if (!_.isUndefined(validateDropSourceCallback)) {
                this.set('validateDropSourceCallback', validateDropSourceCallback);
            }
        },

        getActivatedDropTarget: function () {
             return this.get('activatedDropTarget');
        },

        clearActivatedDropTarget: function () {
                this.set('activatedDropTarget', undefined);
                this.set('validateDropSourceCallback', undefined);
        },

        isAtValidDropTarget: function(){
            var allowedBySource = true,
                allowedByTarget = true;
            if(!_.isUndefined(this.getActivatedDropTarget())){
                var validateDropTargetCallback = this.get('validateDropTargetCallback');
                if(!_.isUndefined(validateDropTargetCallback)){
                    if(_.isFunction(validateDropTargetCallback)){
                        allowedBySource = validateDropTargetCallback(this.getActivatedDropTarget());
                    }
                }
                var validateDropSourceCallback = this.get('validateDropSourceCallback');
                if(!_.isUndefined(validateDropSourceCallback)){
                    if(_.isFunction(validateDropSourceCallback)){
                        allowedByTarget = validateDropSourceCallback(this.getTypeBeingDragged());
                    }
                }
                return allowedBySource && allowedByTarget;
            }
            return true;
        },

        isOnDrag: function(){
            return !_.isNil(this.get('typeBeingDragged'));
        }
    });

    return DragDropManager;

});

