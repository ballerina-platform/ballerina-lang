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
        });

        this._generateUniqueIdentifiers = undefined;
    };

    ASTNode.prototype = Object.create(EventChannel.prototype);
    ASTNode.prototype.constructor = ASTNode;

    ASTNode.prototype.getParent = function () {
        return this.parent;
    };

    ASTNode.prototype.setParent = function (parent, options) {
        this.setAttribute('parent', parent, options);
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

        //setting the parent node - doing silently avoid subsequent change events
        child.setParent(this, {doSilently:true});
        child.generateUniqueIdentifiers();

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
     * finds the child from the AST tree by ID
     * @param id
     * @returns {*}
     */
    ASTNode.prototype.getChildById = function (id) {
        return _.find(this.children, ['id', id]);
    };

    /**
     * remove the child from the AST tree by ID
     * @param id
     * @param ignoreTreeModifiedEvent {boolean}
     */
    ASTNode.prototype.removeChildById = function (id, ignoreTreeModifiedEvent) {
        var child = this.getChildById(id);
        this.removeChild(child,ignoreTreeModifiedEvent);
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
                if(child){
                    // visit current child
                    visitor.visit(child);
                    self.trigger("child-visited", child);
                    // forward visitor down the hierarchy to visit children of current child
                    // if visitor doesn't support visiting children of current child, it will break
                    child.accept(visitor);
                }
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
            var title = _.has(options, 'changeTitle') ? _.get(options, 'changeTitle') : 'Modify ' + this.getType();
            /**
             * @event ASTNode#tree-modified
             */
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: title,
                context: this,
                data: {
                    attributeName: attributeName,
                    newValue: newValue,
                    oldValue: oldValue
                },
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

    /**
     * A generic method to be used for adding values for node attributes which are arrays while firing required change events
     *
     * @param arrAttrName {string} name of the array attribute that needs to be pushed to
     * @param newValue {*} new value to be pushed in
     * @param [options] {Object} options
     * @param [options.predicate=undefined] {*} A predicate valid for {@link https://lodash.com/docs/4.17.4#findIndex|Lodash.findIndex}
     * if defined, if a value that satisfy this predicate exists in the array, replaces it instead of adding a new value
     * @param [options.changeTitle=Modify $attributeName] {string} the title for change
     * @param [options.doSilently=false] {boolean} a flag to indicate whether events should not be fired
     */
    ASTNode.prototype.pushToArrayAttribute = function (arrAttrName, newValue, options) {
        var currentArray = _.get(this, arrAttrName);

        // Check if a value already exists for the given key
        var existingValueIndex = -1;
        if(_.has(options, 'predicate')) {
            existingValueIndex = _.findIndex(currentArray, options.predicate);
        }

        var existingValue;

        if (existingValueIndex === -1) {
            // A value with this key does not exists, then add a new one.
            currentArray.push(newValue);
        } else {
            // keep a reference to the old object so we can 'undo' this operation
            existingValue = currentArray[existingValueIndex];
            // Updating existing annotation.
            currentArray[existingValueIndex] = newValue;
        }

        // fire change event with necessary callbacks for undo/redo
        if(_.isNil(options) || !options.doSilently){
            var title = _.has(options, 'changeTitle') ? _.get(options, 'changeTitle') : 'Modify ' + this.getType();
            /**
             * @event ASTNode#tree-modified
             */
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: title,
                context: this,
                undo: function(){
                    var currentArray = _.get(this, arrAttrName);
                    if (existingValueIndex === -1) {
                        // A value with this key did not exist. So remove it.
                        _.remove(currentArray, options.predicate);
                    } else {
                        this.pushToArrayAttribute(arrAttrName, existingValue, {
                          predicate: options.predicate,
                          doSilently: true
                        });
                    }
                },
                redo: function() {
                    this.pushToArrayAttribute(arrAttrName, newValue, {
                      predicate: options.predicate,
                      doSilently: true
                    });
                }
            });
        }
    };

    /**
     * Checks if an string is valid as an identifier.
     * @param {string} identifier - The string value.
     * @return {boolean} - True if valid, else false.
     */
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

    ASTNode.prototype.setLineNumber = function (lineNumber, options) {
        this.setAttribute('_lineNumber', parseInt(lineNumber), options);
    };

    ASTNode.prototype.getLineNumber = function () {
        return this.getAttribute('_lineNumber');
    };

    /**
     * Function which should be used to generate unique values for attributes. Ex: newStruct, newStruct1, newStruct2.
     */
    ASTNode.prototype.generateUniqueIdentifiers = function() {};

    return ASTNode;

});
