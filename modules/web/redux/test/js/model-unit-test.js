/* global describe, it */

import { expect } from 'chai';
import _ from 'lodash';
import store from '../../main/js/store';
import { treeReducer, cloneDeepAlongPath } from '../../main/js/reducers/tree-reducer';

describe('Test composer Redux logic', () => {
    describe('Redux store', () => {
        it('Init state should have an ast tree', () => {
            const state = store.getState();
            expect(state).have.property('tree');
        });
    });

    describe('Tree Reducer', () => {
        it('Non-tree actions does not change tree', () => {
            const tree = {};
            const action = { type: 'PANEL:OPEN' };
            const newTree = treeReducer(tree, action);
            expect(newTree).to.equal(tree);
        });

        it('Init tree should have an empty top level', () => {
            const action = { type: '@INIT@' };
            const initTree = treeReducer(undefined, action);
            expect(initTree.kind).to.equal('CompilationUnit');
            expect(initTree.topLevelNodes).to.be.an('array');
            expect(initTree.topLevelNodes.length).to.equal(0);
        });

        it('Cloning an object returns a different object with same props', () => {
            const obj = { a: { b: 'c' } };
            const { clone } = cloneDeepAlongPath(obj, []);
            expect(clone).to.deep.equal(obj);

            expect(clone).to.not.equal(obj);
            expect(clone.a).to.equal(obj.a);
        });

        it('Cloning an object deep-copies (non-shallow) along the given path, and shallow copy rest', () => {
            const obj = { a: { b: { c: 'd' }, x: { y: 'z' } } };
            const { clone } = cloneDeepAlongPath(obj, ['a', 'b']);
            expect(clone).to.deep.equal(obj);
            expect(clone.x).to.equal(obj.x);

            expect(clone).to.not.equal(obj);
            expect(clone.a).to.not.equal(obj.a);
            expect(clone.a.b).to.not.equal(obj.a.b);
        });

        it('Cloning should deep copy arrays if it is in the path', () => {
            const obj = { a: [{ b: { c: 'd' } }, { x: 'y' }] };
            const { clone } = cloneDeepAlongPath(obj, ['a', 0, 'b']);
            expect(clone).to.deep.equal(obj);
            expect(clone.a[1]).to.equal(obj.a[1]);

            expect(clone).to.not.equal(obj);
            expect(clone.a).to.not.equal(obj.a);
            expect(clone.a[0]).to.not.equal(obj.a[0]);
            expect(clone.a[0].b).to.not.equal(obj.a[0].b);
        });

        it('Adding a child to an empty tree populates the top level', () => {
            const action = {
                type: 'TREE:APPEND_CHILD',
                payload: { kind: 'Function', to: [] },
            };
            const newTree = treeReducer(undefined, action);
            expect(newTree.topLevelNodes.length).to.equal(1);
            expect(newTree.topLevelNodes[0].kind).to.equal('Function');
        });

        it('Adding to a func should populate the body', () => {
            const treeWithFunc = {
                topLevelNodes: [{
                    name: {
                        value: 'myTest',
                        kind: 'Identifier',
                    },
                    body: {
                        statements: [],
                        kind: 'Block',
                    },
                    kind: 'Function',
                }],
                kind: 'CompilationUnit',
            };
            const path = ['topLevelNodes', 0];
            const action = {
                type: 'TREE:APPEND_CHILD',
                payload: { kind: 'Assignment', to: path },
            };

            const newTree = treeReducer(treeWithFunc, action);

            expect(_.get(newTree, 'topLevelNodes.0.body.statements')).is.an('array');
            expect(newTree.topLevelNodes[0].body.statements[0].kind).to.equal('Assignment');
        });
    });
});
