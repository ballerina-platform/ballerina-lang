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

        /**
         * Set the ASTNode being dragged at a given moment.
         * @param type {ASTNode} node being dragged
         * @param validateDropTargetCallback {DragDropManager~validateDropTargetCallback} - call back to do additional validations on drop target
         *
         */
        setNodeBeingDragged: function (type, validateDropTargetCallback) {
            if (!_.isUndefined(type)) {
                this.set('nodeBeingDragged', type);
            }
            if (!_.isUndefined(validateDropTargetCallback)) {
                this.set('validateDropTargetCallback', validateDropTargetCallback);
            }
            this.trigger('drag-start', this.get('nodeBeingDragged'));
        },

        /**
         * This callback will be passed a ref of currently activated drop target - so they can do additional validations
         * apart from built in canBeParentOf and canBeAChildOf validators in ASTNode class.
         * @callback DragDropManager~validateDropTargetCallback
         * @param {ASTNode} current drop target
         * @return {boolean} flag indicating whether it passed the validations or not.
         */

        /**
         * Gets the node which is being dragged at a given moment - if any.
         * @return {ASTNode}
         */
        getNodeBeingDragged: function () {
            return  this.get('nodeBeingDragged');
        },

        /**
         * Reset drag-drop manager. Call this once dragging stops.
         * @fires DragDropManager#drag-stop
         */
        reset: function(){
            /**
             * @event DragDropManager#drag-stop
             * @type {ASTNode}
             */
            this.trigger('drag-stop', this.get('nodeBeingDragged'));
            this.trigger('drop-target-changed', undefined);
            this.set('nodeBeingDragged', undefined);
            this.set('validateDropTargetCallback', undefined);
            this.set('activatedDropTarget', undefined);
            this.set('validateDropSourceCallback', undefined);
            this.set('getDroppedNodeIndexCallBack', undefined);
        },

        /**
         * Register a drop target once activated.
         *
         * @param activatedDropTarget {ASTNode} Node which is currently accepting drops.
         * @param [validateDropSourceCallback] {DragDropManager~validateDropSourceCallback}
         * @param [getDroppedNodeIndexCallBack] {DragDropManager~getDroppedNodeIndex}
         * @fires DragDropManager#drop-target-changed
         */
        setActivatedDropTarget: function (activatedDropTarget, validateDropSourceCallback, getDroppedNodeIndexCallBack) {
            var lastActivatedDropTarget = this.get('activatedDropTarget');
            if (!_.isUndefined(activatedDropTarget)) {
                this.set('activatedDropTarget', activatedDropTarget);
            }
            if (!_.isUndefined(validateDropSourceCallback)) {
                this.set('validateDropSourceCallback', validateDropSourceCallback);
            }
            if (!_.isUndefined(getDroppedNodeIndexCallBack)) {
                this.set('getDroppedNodeIndexCallBack', getDroppedNodeIndexCallBack);
            }
            if (!_.isEqual(activatedDropTarget, lastActivatedDropTarget)){
                /**
                 * @event DragDropManager#drop-target-changed
                 * @type ASTNode
                 */
                this.trigger('drop-target-changed', activatedDropTarget);
            }
        },

        /**
         * This callback will be passed a ref of the node which is currently being dragged - so it can do additional validations
         * apart from built in canBeParentOf and canBeAChildOf validators in ASTNode class.
         * @callback DragDropManager~validateDropSourceCallback
         * @param {ASTNode} - Node currently being dragged
         * @return {boolean} flag indicating whether it passed the validations or not.
         */

        /**
         * This callback will tell drag drop manager a index where node was dropped and should be added to in child array
         * @callback DragDropManager~getDroppedNodeIndex
         * @param {ASTNode} - Node which was dropped
         * @return {int} flag indicating whether it passed the validations or not.
         */

        /**
         * Gets currently the node currently being dragged.
         * @return {ASTNode}
         */
        getActivatedDropTarget: function () {
             return this.get('activatedDropTarget');
        },

        /**
         * Gets index of currently dropped element.
         * @return {number} index
         */
        getDroppedNodeIndex: function () {
             if(this.isAtValidDropTarget() && _.isFunction(this.get('getDroppedNodeIndexCallBack'))){
                 return this.get('getDroppedNodeIndexCallBack')(this.getNodeBeingDragged());
             }
             return -1;
        },

        /**
         * Clears currently activated drop target. Call this when the item being dragged go away from your drop zone.
         * @fires DragDropManager#drop-target-changed
         */
        clearActivatedDropTarget: function () {
                this.set('activatedDropTarget', undefined);
                this.set('validateDropSourceCallback', undefined);
                this.trigger('drop-target-changed', undefined);
        },

        /**
         * Indicates whether the dragged element is currently at a valid drop target.
         *
         * @return {boolean} Default is false
         */
        isAtValidDropTarget: function(){
            var allowedBySource = true,
                allowedByTarget = true,
                allowedBySourceValidateCallBack = true,
                allowedByTargetValidateCallBack = true;

            if(!_.isUndefined(this.getActivatedDropTarget())){

                allowedBySource = this.getNodeBeingDragged().canBeAChildOf(this.getActivatedDropTarget());
                allowedByTarget = this.getActivatedDropTarget().canBeParentOf(this.getNodeBeingDragged());

                var validateDropTargetCallback = this.get('validateDropTargetCallback');
                if(!_.isUndefined(validateDropTargetCallback)){
                    if(_.isFunction(validateDropTargetCallback)){
                        allowedByTargetValidateCallBack = validateDropTargetCallback(this.getActivatedDropTarget());
                    }
                }

                var validateDropSourceCallback = this.get('validateDropSourceCallback');
                if(!_.isUndefined(validateDropSourceCallback)){
                    if(_.isFunction(validateDropSourceCallback)){
                        allowedBySourceValidateCallBack = validateDropSourceCallback(this.getNodeBeingDragged());
                    }
                }

                return allowedBySource && allowedByTarget
                    && allowedBySourceValidateCallBack
                    && allowedByTargetValidateCallBack;
            }
            return false;
        },

        /**
         * Indicates whether there is a node being dragged at the moment.
         * @return {boolean}
         */
        isOnDrag: function(){
            return !_.isNil(this.get('nodeBeingDragged'));
        }
    });

    return DragDropManager;

});

