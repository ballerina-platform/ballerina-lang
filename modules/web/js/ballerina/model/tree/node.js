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
import _ from 'lodash';
import uuid from 'uuid/v1';
import EventChannel from 'event_channel';
import SimpleBBox from '../view/simple-bounding-box';
import getSourceOf from '../source-gen';

/**
 * Base of all tree nodes.
 *
 * @class Node
 */
class Node extends EventChannel {

    constructor() {
        super();
        // Set an id @TODO move to a path base ID.
        this.id = uuid();

        // Following will propergate tree modified event to the top.
        this.on('tree-modified', function (event) {
            if (!_.isNil(this.parent)) {
                this.parent.trigger('tree-modified', event);
            }
        });

        /**
         * View State Object to keep track of the model's view properties
         * @type {{bBox: SimpleBBox, components: {}, dimensionsSynced: boolean}}
         */
        this.viewState = {
            bBox: new SimpleBBox(),
            components: {},
            dimensionsSynced: false,
            hidden: false,
        };

        this.isStatement = false;
        this.isExpression = false;
    }

    /**
     * If there are child nodes to be aliased, this should carry the implementation accordingly
     */
    setChildrenAlias() {
        // Will implement by the particular node
    }

    /**
     *
     * @param {NodeVisitor} visitor
     */
    accept(visitor) {
        visitor.beginVisit(this);
        // eslint-disable-next-line guard-for-in
        for (const childName in this) {
            if (childName !== 'parent' && childName !== 'position' && childName !== 'ws') {
                const child = this[childName];
                if (child instanceof Node && child.kind) {
                    child.accept(visitor);
                } else if (child instanceof Array) {
                    for (let i = 0; i < child.length; i++) {
                        const childItem = child[i];
                        if (childItem instanceof Node && childItem.kind) {
                            childItem.accept(visitor);
                        }
                    }
                }
            }
        }
        visitor.endVisit(this);
    }

    sync(visitor, newTree) {
        visitor.beginVisit(this, newTree);
        // eslint-disable-next-line guard-for-in
        for (const childName in this) {
            if (childName !== 'parent' && childName !== 'position' && childName !== 'ws') {
                const child = this[childName];
                const child2 = newTree[childName];
                if (child instanceof Node && child.kind) {
                    child.sync(visitor, child2);
                } else if (child instanceof Array) {
                    for (let i = 0; i < child.length; i++) {
                        const childItem = child[i];
                        const childItem2 = child2[i];
                        if (childItem instanceof Node && childItem.kind) {
                            childItem.sync(visitor, childItem2);
                        }
                    }
                }
            }
        }
        visitor.endVisit(this, newTree);
    }

    getKind() {
        return this.kind;
    }

    getWS() {
        return this.wS;
    }

    getPosition() {
        return this.position;
    }

    /**
     * Get the source of the current Node.
     * @param {boolean?} pretty ignore WS and put default WS.
     * @return {string} source.
     */
    getSource(pretty) {
        return getSourceOf(this, pretty);
    }

    [Symbol.iterator]() {
        const children = [];
        for (const key of Object.keys(this)) {
            const prop = this[key];
            if (prop instanceof Node && key !== 'parent') {
                children.push([key, prop]);
            } else if (_.isArray(prop) && prop.length > 0 && prop[0] instanceof Node) {
                let i = 0;
                for (const propI of prop) {
                    children.push([key + '[' + i++ + ']', propI]);
                }
            }
        }
        let nextIndex = 0;

        return {
            next() {
                return nextIndex < children.length ?
                    { value: children[nextIndex++], done: false } :
                    { done: true };
            },
        };
    }

    entries() {
        const props = [];
        for (const key of _.pull(Object.keys(this), 'ws', 'viewState', 'id', '_events', 'parent', 'position', 'kind')) {
            const prop = this[key];
            if (_.isArray(prop)) {
                if (prop.length > 0 && !(prop[0] instanceof Node)) {
                    props.push([key, prop]);
                }
            } else if (!(prop instanceof Node)) {
                props.push([key, prop]);
            }
        }

        return props;
    }


    getID() {
        return this.id;
    }

    /**
     * Get the current file
     * @returns {object} - current file
     */
    getFile() {
        return this.file;
    }

    /**
     * Set the current file
     * @param {object} file - current file
     */
    setFile(file) {
        this.file = file;
    }

    /**
     * Get root of the node which is the compilation unit
     * @returns {Node} root node
     * @memberof Node
     */
    getRoot() {
        if (this.kind === 'CompilationUnit' || !this.parent) {
            return this;
        } else {
            return this.parent.getRoot();
        }
    }

    /**
     * Checks if an string is valid as an identifier.
     * @param {string} identifier - The string value.
     * @return {boolean} - True if valid, else false.
     */
    static isValidIdentifier(identifier) {
        if (_.isUndefined(identifier)) {
            return false;
        } else if (/^[a-zA-Z_$][a-zA-Z0-9_]*$/.test(identifier)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a string is valid as a type.
     * @param {string} type - The string value.
     * @return {boolean} - True if valid, else false.
     */
    static isValidType(type) {
        if (_.isUndefined(type)) {
            return false;
        } else if (/^[a-zA-Z0-9:_]*\s*[[*\s\]<*\s*>a-zA-Z0-9_]*$/.test(type)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the node is a statement node.
     *
     * @returns {boolean} return true if node is a statement.
     * @memberof Node
     */
    isStatement() {
        return this.isStatement;
    }

    /**
     * Check if the node is a expression node.
     *
     * @returns {boolean} return true if node is an expression.
     * @memberof Node
     */
    isExpression() {
        return this.isExpression;
    }

    /**
     * Remove the node from the tree. Currently works only for nodes in a list.
     *
     * @memberof Node
     */
    remove() {
        const parent = this.parent;
        if (this.parent === undefined) {
            // Unable to remove, this is a root node.
            return;
        }
        // get the parent and iterate till you find the node.
        for (const key of Object.keys(this.parent)) {
            const prop = this.parent[key];
            if (prop instanceof Node && key !== 'parent') {
                // TODO need to see how we can remove node attributes.
            } else if (_.isArray(prop) && prop.length > 0 && prop[0] instanceof Node) {
                let i = 0;
                for (const propI of prop) {
                    if (propI.getID() === this.getID()) {
                        break;
                    }
                    i += 1;
                }
                prop.splice(i, 1);
                parent.trigger('tree-modified', {
                    origin: this,
                    type: 'child-removed',
                    title: `Removed ${this.kind}`,
                    data: {
                        'node': this,
                    },
                });
                return;
            }
        }
    }

    /**
     * Clear the whitespace of the node.
     *
     * @memberof Node
     */
    clearWS() {
        this.accept({
            beginVisit: (node) => {
                node.ws = [];
            },
            endVisit: (node) => {
                //do nothing.
            },
        });
    }
}

export default Node;
