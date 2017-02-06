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
define(['log', 'require', 'event_channel', 'lodash'], function(log, require, EventChannel, _){

    /**
     * Constructor for the ASTNode
     * @class ASTNode
     * @augments EventChannel
     * @param {string} type - An identifier for the type of the object.
     * Example for a service definition : "ServiceDefinition". This is useful when debugging.
     * @constructor
     */
    var ASTNode = function(type) {
        this.object = undefined;
        this.parent = undefined;
        this.children = [];
        // TODO : Rename this to bType.
        this.type = type;
        this.id = uuid();
        this.on('tree-modified', function(event){
            if(!_.isNil(this.parent)){
                this.parent.trigger('tree-modified', event);
            } else {
              log.debug("Cannot find the parent node to propagate tree modified event up. Node: " + this.getType()
                  + ", EventType: " + event.type + ", EventTitle: " + event.title)
            }
        })
    };

    ASTNode.prototype = Object.create(EventChannel.prototype);
    ASTNode.prototype.constructor = ASTNode;

    ASTNode.prototype.getParent = function () {
        return this.parent;
    };

    ASTNode.prototype.setParent = function (parent) {
        this.parent = parent;
    };

    ASTNode.prototype.getChildren = function () {
        return this.children;
    };

    ASTNode.prototype.getType = function () {
        return this.type;
    };

    ASTNode.prototype.getID = function() {
        return this.id;
    };

    ASTNode.prototype.getLength = function () {
        return this.length;
    };

    ASTNode.prototype.getStartIndex = function () {
        return this.startIndex;
    };

    /**
     * Insert a given child to the children array for a given index or otherwise to the array normally
     * @param child
     * @param index
     * @param ignoreTreeModifiedEvent {boolean}
     * @fires  ASTNode#child-added
     * @fires  ASTNode#tree-modified
     */
    ASTNode.prototype.addChild = function (child, index, ignoreTreeModifiedEvent) {
        if (_.isUndefined(index)) {
            this.children.push(child);
        } else {
            this.children.splice(index, 0, child);
        }
        /**
         * @event ASTNode#child-added
         */
        this.trigger('child-added', child, index);

        if(!ignoreTreeModifiedEvent) {
            /**
             * @event ASTNode#tree-modified
             */
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                data: {
                    child: child,
                    index: index
                }
            });
        }
        //setting the parent node
        child.setParent(this);
    };

    /**
     * Remove a given child from the AST tree
     * @param child
     * @param ignoreTreeModifiedEvent {boolean}
     * @fires  ASTNode#child-removed
     * @fires  ASTNode#tree-modified
     */
    ASTNode.prototype.removeChild = function (child, ignoreTreeModifiedEvent) {
        var parentModelChildren = this.children;
        for (var itr = 0; itr < parentModelChildren.length; itr++) {
            if (parentModelChildren[itr].id === child.id) {
                parentModelChildren.splice(itr, 1);

                /**
                 * @event ASTNode#child-removed
                 */
                this.trigger("child-removed", child);

                if(!ignoreTreeModifiedEvent){
                    /**
                     * @event ASTNode#tree-modified
                     */
                    this.trigger('tree-modified', {
                        origin: this,
                        type: 'child-removed',
                        data: {
                            child: child,
                            index: itr
                        }
                    });
                }
                break;
            }
        }
    };

    /**
     * Accept function in visitor pattern
     * @param visitor {ASTVisitor}
     */
    ASTNode.prototype.accept = function (visitor) {
        if(visitor.canVisit(this)) {
            visitor.beginVisit(this);
            var self = this;
            _.forEach(this.children, function (child) {
                // visit current child
                visitor.visit(child);
                self.trigger("child-visited", child);
                // forward visitor down the hierarchy to visit children of current child
                // if visitor doesn't support visiting children of current child, it will break
                child.accept(visitor);
            });
            visitor.endVisit(this);
        }
    };

    /**
     * Indicates whether this can be the parent node for the give node
     * Used for drag and drop validations. Override as required.
     * @param node {ASTNode}
     * @return {boolean} Default is true
     */
    ASTNode.prototype.canBeParentOf = function (node) {
        return true;
    };

    /**
     * Indicates whether this can be a child node of given node.
     * Used for drag and drop validations. Override as required
     * @param node {ASTNode}
     * @return {boolean} Default is true
     */
    ASTNode.prototype.canBeAChildOf = function (node) {
        return true;
    };

    /**
     * Get factory.
     * @return {BallerinaASTFactory}
     */
    ASTNode.prototype.getFactory  = function() {
        return require('./ballerina-ast-factory');
    };

    /**
     * Get index of child.
     * @param {ASTNode}
     * @return {number}
     */
    ASTNode.prototype.getIndexOfChild  = function(child) {
        return _.findIndex(this.children, ['id', child.id]);
    };

    ASTNode.prototype.initFromJson = function(jsonNode) {
        throw "InitFromJson not implemented";
    };

    // Auto generated Id for service definitions (for accordion views)
    var uuid =  function (){
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };

    /**
     * A generic method to be used for setting node attributes while firing required change events
     *
     * @param attributeName {String} name of the attribute that needs to be updated
     * @param newValue {*} new value
     * @param [options] {Object} options
     * @param [options.changeTitle=change $attributeName] {String} the title for change
     * @param [options.doSilently=false] {boolean} a flag to indicate whether events should not be fired
     */
    ASTNode.prototype.setAttribute = function (attributeName, newValue, options) {

        var oldValue = _.get(this, attributeName);

        _.set(this, attributeName, newValue);

        // fire change event with necessary callbacks for undo/redo
        if(_.isNil(options) || !options.doSilently){
            var title = _.has(options, 'changeTitle') ? _.get(options, 'changeTitle') : 'set '
                            + attributeName + ' to ' + newValue;
            /**
             * @event ASTNode#tree-modified
             */
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: title,
                context: this,
                undo: function(){
                    this.setAttribute(attributeName, oldValue, {doSilently: true});
                },
                redo: function(){
                    this.setAttribute(attributeName, newValue, {doSilently: true});
                }
            });
        }
    };

    /**
     * A generic getter for all attributes of a node
     * @param attributeName
     * @return {*}
     */
    ASTNode.prototype.getAttribute = function (attributeName) {
        return _.get(this, attributeName);
    };

    ASTNode.isValidIdentifier = function (identifier) {
        return _.isUndefined(identifier) ? false : /^[a-zA-Z$_][a-zA-Z0-9$_]*$/.test(identifier);
    };

    /**
     * Removes node from the tree.
     * @param [options] {object}
     * @param [options.ignoreTreeModifiedEvent=false] {boolean} a flag to prevent tree-modified event being fired
     */
    ASTNode.prototype.remove = function(options) {
        if(!_.isNil(this.getParent())){
            this.trigger('before-remove');
            this.getParent().removeChild(this, _.get(options, 'ignoreTreeModifiedEvent'));
            this.trigger('after-remove');
        }
    };

    return ASTNode;

});