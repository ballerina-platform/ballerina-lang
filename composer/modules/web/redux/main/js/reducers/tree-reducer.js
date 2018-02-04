import _ from 'lodash';
import InitNodes from '../../resources/init-nodes.json';

/**
 * @typedef {{ topLevelNodes:Node[] }} CompilationUnit
 */

/**
 * @typedef {{ type:string, payload:(Object|undefined) }} Action
 */

/**
 * Shallow clone except along a given path.
 * Along given path objects will be deep cloned.
 *
 * @param {*} obj Object to be cloned.
 * @param {string[]} path Path to be deep cloned.
 * @return {{ leaf:*, clone:* }} Cloned object as 'clone' and Deepest non-shallow sub-object of clone as 'leaf'.
 */
export function cloneDeepAlongPath(obj, path) {
    let leaf = _.clone(obj);
    const clone = leaf;
    for (const fragment of path) {
        leaf[fragment] = _.clone(leaf[fragment]);
        leaf = leaf[fragment];
    }
    return { leaf, clone };
}


/**
 * Returns a new tree with the given action performed on it.
 *
 * @param {CompilationUnit} tree Current state
 * @param {Action} action Action to be performed on the tree.
 * @return {CompilationUnit} New state
 */
export function treeReducer(tree = { topLevelNodes: [], kind: 'CompilationUnit' }, action) {
    if (action.type.lastIndexOf('TREE:', 0) !== 0) {
        return tree;
    }

    if (action.type === 'TREE:APPEND_CHILD') {
        const path = action.payload.to;
        const kind = action.payload.kind;
        const target = path.length ? _.get(tree, path) : tree;
        if (!target) {
            throw Error('Tree does not contain the target at "' + path.join('.') + '"');
        }

        if (target.body && _.isArray(target.body.statements)) {
            const pathToChildArray = [...path, 'body', 'statements'];
            const { clone, leaf } = cloneDeepAlongPath(tree, pathToChildArray);
            leaf.push(InitNodes[kind]);
            return clone;
        } else if (_.isArray(target.topLevelNodes)) {
            const pathToChildArray = ['topLevelNodes'];
            const { clone, leaf } = cloneDeepAlongPath(tree, pathToChildArray);
            leaf.push(InitNodes[kind]);
            return clone;
        }

        throw Error('Don\'t know how to add ' + kind + ' to ' + target.kind);
        // } else if (action.type === 'TREE:CHANGE_PROP') {
    }

    throw new Error('Unknown action ' + action);
}
