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
import log from 'log';
import EventChannel from 'event_channel';
import _ from 'lodash';
import BallerinaAstFactory from './ballerina-ast-factory';
import SimpleBBox from './simple-bounding-box';

/**
 * Constructor for the ASTNode
 * @class ASTNode
 * @augments EventChannel
 * @param {string} type - An identifier for the type of the object.
 * Example for a service definition : "ServiceDefinition". This is useful when debugging.
 * @constructor
 */
class ASTNode extends EventChannel {
    constructor(type) {
        super();
        this.object = undefined;
        this.parent = undefined;
        this.children = [];
        // TODO : Rename this to bType.
        this.type = type;
        this.id = uuid();
        this.on('tree-modified', function (event) {
            if (!_.isNil(this.parent)) {
                this.parent.trigger('tree-modified', event);
            } else {
                log.debug("Cannot find the parent node to propagate tree modified event up. Node: " + this.getType() +
                    ", EventType: " + event.type + ", EventTitle: " + event.title);
            }
        });

        this._generateUniqueIdentifiers = undefined;
        this.whiteSpaceDescriptor = {};
        this.shouldCalculateIndentation = true;

        /**
         * View State Object to keep track of the model's view properties
         * @type {{bBox: SimpleBBox, components: {}, dimensionsSynced: boolean}}
         */
        this.viewState = {
            bBox: new SimpleBBox(),
            components: {},
            dimensionsSynced: false
        };
    }

    /**
     * Get the node's view state
     * @return {{bBox: BBox}} node's view state
     */
    getViewState(){
        return this.viewState;
    }

    getParent() {
        return this.parent;
    }

    setParent(parent, options) {
        this.setAttribute('parent', parent, options);
    }

    getChildren() {
        return this.children;
    }

    getType() {
        return this.type;
    }

    getID() {
        return this.id;
    }

    getLength() {
        return this.length;
    }

    getStartIndex() {
        return this.startIndex;
    }

    /**
     * Insert a given child to the children array for a given index or otherwise to the array normally
     * @param child
     * @param index
     * @param ignoreTreeModifiedEvent {boolean}
     * @param ignoreChildAddedEvent {boolean}
     * @fires  ASTNode#child-added
     * @fires  ASTNode#tree-modified
     */
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent = false) {
        if (_.isUndefined(index)) {
            this.children.push(child);
        } else {
            this.children.splice(index, 0, child);
        }

        //setting the parent node - doing silently avoid subsequent change events
        child.setParent(this, {
            doSilently: true
        });
        child.generateUniqueIdentifiers();

        if (!ignoreChildAddedEvent) {
            /**
             * @event ASTNode#child-added
             */
            this.trigger('child-added', child, index);
        }

        if (!ignoreTreeModifiedEvent) {
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
    }

    /**
     * Remove a given child from the AST tree
     * @param child
     * @param ignoreTreeModifiedEvent {boolean}
     * @fires  ASTNode#child-removed
     * @fires  ASTNode#tree-modified
     */
    removeChild(child, ignoreTreeModifiedEvent) {
        var parentModelChildren = this.children;
        for (var itr = 0; itr < parentModelChildren.length; itr++) {
            if (parentModelChildren[itr].id === child.id) {
                parentModelChildren.splice(itr, 1);

                /**
                 * @event ASTNode#child-removed
                 */
                this.trigger("child-removed", child);

                if (!ignoreTreeModifiedEvent) {
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
    }

    /**
     * finds the child from the AST tree by ID
     * @param id
     * @returns {*}
     */
    getChildById(id) {
        return _.find(this.children, ['id', id]);
    }

    /**
     * remove the child from the AST tree by ID
     * @param id
     * @param ignoreTreeModifiedEvent {boolean}
     */
    removeChildById(id, ignoreTreeModifiedEvent) {
        var child = this.getChildById(id);
        this.removeChild(child, ignoreTreeModifiedEvent);
    }

    /**
     * Accept function in visitor pattern
     * @param visitor {ASTVisitor}
     */
    accept(visitor) {
        if (visitor.canVisit(this)) {
            visitor.beginVisit(this);
            visitor.visit(this);
            _.forEach(this.children, function (child) {
                if (child) {
                    visitor.visit(child);
                    // forward visitor down the hierarchy to visit children of current child
                    // if visitor doesn't support visiting children of current child, it will break
                    if(visitor.canVisit(child)) {
                        child.accept(visitor);
                    }
                }
            });
            visitor.endVisit(this);
        }
    }

    /**
     * Indicates whether this can be the parent node for the give node
     * Used for drag and drop validations. Override as required.
     * @param node {ASTNode}
     * @return {boolean} Default is true
     */
    canBeParentOf(node) {
        return true;
    }

    /**
     * Indicates whether this can be a child node of given node.
     * Used for drag and drop validations. Override as required
     * @param node {ASTNode}
     * @return {boolean} Default is true
     */
    canBeAChildOf(node) {
        return true;
    }

    /**
     * Get factory.
     * @return {BallerinaASTFactory}
     */
    getFactory() {
        return BallerinaAstFactory;
    }

    /**
     * Get index of child.
     * @param {ASTNode}
     * @return {number}
     */
    getIndexOfChild(child) {
        return _.findIndex(this.children, ['id', child.id]);
    }

    /**
     * Filter matching children from the predicate function
     * @param predicateFunction a function returning a boolean to match filter condition from children
     * @return {[ASTNode]} array of matching AST nodes
     */
    filterChildren(predicateFunction) {
        return _.filter(this.getChildren(), predicateFunction);
    }

    /**
     * Find matching child from the predicate function+
     * @param predicateFunction a function returning a boolean to match find condition from children
     */
    findChild(predicateFunction) {
        return _.find(this.getChildren(), predicateFunction);
    }

    /**
     * Remove matching child from the predicate function
     * @param predicateFunction a function returning a boolean to match remove condition from children
     * @param name of child to remove
     */
    removeChildByName(predicateFunction, name) {
        _.remove(this.getChildren(), function (child) {
            return predicateFunction && (child.getName() === name);
        });
    }

    /**
     * Find last index of matching children from the predicate function
     * @param predicateFunction a function returning a boolean to match find condition from children
     */
    findLastIndexOfChild(predicateFunction) {
        return _.findLastIndex(this.getChildren(), predicateFunction);
    }

    initFromJson(jsonNode) {
        throw "InitFromJson not implemented";
    }

    /**
     * A generic method to be used for setting node attributes while firing required change events
     *
     * @param attributeName {String} name of the attribute that needs to be updated
     * @param newValue {*} new value
     * @param [options] {Object} options
     * @param [options.changeTitle=change $attributeName] {String} the title for change
     * @param [options.doSilently=false] {boolean} a flag to indicate whether events should not be fired
     */
    setAttribute(attributeName, newValue, options) {

        var oldValue = _.get(this, attributeName);

        _.set(this, attributeName, newValue);

        // fire change event with necessary callbacks for undo/redo
        if (_.isNil(options) || !options.doSilently) {
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
                undo: function () {
                    this.setAttribute(attributeName, oldValue, {
                        doSilently: true
                    });
                    if (_.has(options, 'undoCallBack') && _.isFunction(options.undoCallBack)) {
                        var undoCallBack = _.get(options, 'undoCallBack');
                        undoCallBack();
                    }
                },
                redo: function () {
                    this.setAttribute(attributeName, newValue, {
                        doSilently: true
                    });
                    if (_.has(options, 'redoCallBack') && _.isFunction(options.redoCallBack)) {
                        var redoCallBack = _.get(options, 'redoCallBack');
                        redoCallBack();
                    }
                }
            });
        }
    }

    /**
     * A generic getter for all attributes of a node
     * @param attributeName
     * @return {*}
     */
    getAttribute(attributeName) {
        return _.get(this, attributeName);
    }

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
    pushToArrayAttribute(arrAttrName, newValue, options) {
        var currentArray = _.get(this, arrAttrName);

        // Check if a value already exists for the given key
        var existingValueIndex = -1;
        if (_.has(options, 'predicate')) {
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
        if (_.isNil(options) || !options.doSilently) {
            var title = _.has(options, 'changeTitle') ? _.get(options, 'changeTitle') : 'Modify ' + this.getType();
            /**
             * @event ASTNode#tree-modified
             */
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: title,
                context: this,
                undo: function () {
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
                redo: function () {
                    this.pushToArrayAttribute(arrAttrName, newValue, {
                        predicate: options.predicate,
                        doSilently: true
                    });
                }
            });
        }
    }

    /**
     * Checks if an string is valid as an identifier.
     * @param {string} identifier - The string value.
     * @return {boolean} - True if valid, else false.
     */
    static isValidIdentifier(identifier) {
        if(_.isUndefined(identifier)) {
            return false;
        } else if(/^[a-zA-Z0-9_]*$/.test(identifier)){
            return true;
        }
        return false;
    }

    /**
     * Removes node from the tree.
     * @param [options] {object}
     * @param [options.ignoreTreeModifiedEvent=false] {boolean} a flag to prevent tree-modified event being fired
     */
    remove(options) {
        if (!_.isNil(this.getParent())) {
            this.trigger('before-remove');
            this.getParent().removeChild(this, _.get(options, 'ignoreTreeModifiedEvent'));
            this.trigger('after-remove');
        }
    }

    addBreakpoint() {
        this.isBreakpoint = true;
    }

    removeBreakpoint() {
        this.isBreakpoint = false;
    }

    addDebugHit() {
        this.setAttribute('isDebugHit', true);
    }

    removeDebugHit() {
        this.setAttribute('isDebugHit', false);
    }

    setLineNumber(lineNumber, options) {
        this.setAttribute('_lineNumber', parseInt(lineNumber), options);
    }

    setIsIdentifierLiteral(isLiteral, options) {
        this.setAttribute('_is_identifier_literal', isLiteral, options);
    }

    getIdentifierLiteral() {
        return this.getAttribute('_is_identifier_literal');
    }

    getLineNumber() {
        return this.getAttribute('_lineNumber');
    }

    /**
     * Function which should be used to generate unique values for attributes. Ex: newStruct, newStruct1, newStruct2.
     */
    generateUniqueIdentifiers() {}

    getWhiteSpaceDescriptor() {
        return this.whiteSpaceDescriptor;
    }

    setWhiteSpaceDescriptor(whiteSpaceDescriptor, options) {
        this.setAttribute('whiteSpaceDescriptor', whiteSpaceDescriptor, options);
    }

    /** Gets the children of a specific type.
     * @param  {function} typeCheckFunction The function thats used for type checking. Example: BallerinaASTFactory.isConnectorDeclaration
     * @return {ASTNode[]}                   An array of children.
     */
    getChildrenOfType(typeCheckFunction) {
        return _.filter(this.getChildren(), function (child) {
            return typeCheckFunction.call(child.getFactory(), child);
        });
    }

    /**
     * Fills in the pathVector array with location/position of node by traversing through it's children. Important: The
     * return array is inverted.
     * @param {ASTNode} node The node of which the path to be found.
     * @param {number[]} pathVector An array
     */
    getPathToNode(node, pathVector) {
        let nodeParent = node.getParent();
        if (!_.isNil(nodeParent)) {
            let nodeIndex = _.findIndex(nodeParent.getChildren(), node);
            pathVector.push(nodeIndex);
            this.getPathToNode(nodeParent, pathVector);
        }
    }

    /**
     * Gets the node by vector which travers through node's children.
     * @param {ASTNode} root The node to be traversed.
     * @param {number[]} pathVector A reversed array of the position of the node to be found.
     * @return {ASTNode|undefined}
     */
    getNodeByVector(root, pathVector) {
        let returnNode = root;
        let reverseVector = _.reverse(pathVector);

        _.forEach(reverseVector, function (index) {
            returnNode = returnNode.getChildren()[index];
        });
        return returnNode;
    }

    /**
     * This returns the top level parent (should be Resource, action definition, function definition or worker declaration
     * @returns {ASTNode} Parent Node
     */
    getTopLevelParent() {
        const parent = this.getParent();
        if (BallerinaAstFactory.isResourceDefinition(parent) || BallerinaAstFactory.isResourceDefinition(parent) ||
            BallerinaAstFactory.isConnectorAction(parent) || BallerinaAstFactory.isFunctionDefinition(parent) ||
            BallerinaAstFactory.isWorkerDeclaration(parent)) {

            return parent;
        } else {
            return parent.getTopLevelParent();
        }
    }
}

// Auto generated Id for service definitions (for accordion views)
var uuid = function () {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
};

export default ASTNode;
