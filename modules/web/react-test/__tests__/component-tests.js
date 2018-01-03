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
 *
 */

import React from "react";
import chai, {expect} from 'chai';
import Enzyme from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import {render, shallow} from 'enzyme';
import chaiJestSnapshot from "chai-jest-snapshot";
import toJson from 'enzyme-to-json';
import {fetchConfigs, parseContent} from 'api-client/api-client';
import ASTFactory from '../../js/ballerina/ast/ast-factory';
import DefaultASTFactory from '../../js/ballerina/ast/default-ast-factory';
import AssignmentStatement from '../../js/ballerina/diagram/views/default/components/assignment-statement';
import SimpleBBox from '../../js/ballerina/ast/simple-bounding-box';
import DragDropManager from '../../js/ballerina/tool-palette/drag-drop-manager';
import VariableDefinitionStatement from './../../js/ballerina/diagram/views/default/components/variable-definition-statement';
import ReturnStatement from '../../js/ballerina/diagram/views/default/components/return-statement';
import BreakStatement from '../../js/ballerina/diagram/views/default/components/break-statement';
import IfElseStatement from '../../js/ballerina/diagram/views/default/components/if-else-statement';

Enzyme.configure({ adapter: new Adapter() });

chai.use(chaiJestSnapshot);

describe('component tests', function () {
    before(function (beforeAllDone) {
        fetchConfigs()
            .then(() => beforeAllDone())
            .catch(beforeAllDone);
        chaiJestSnapshot.resetSnapshotRegistry();
    });

    beforeEach(function () {
        chaiJestSnapshot.configureUsingMochaContext(this);
        chaiJestSnapshot.setFilename('react-test/__snapshots__/component-tests.snap');
    });

    it('Assignment statement test', () => {
        chaiJestSnapshot.setTestName('Assignment statement test');

        const model = ASTFactory.createAssignmentStatement();
        model.setStatementFromString('a = b');

        model.viewState = {};
        model.viewState.bBox = new SimpleBBox(120, 220, 120, 55, 0, 0);
        model.viewState.components = {};
        model.viewState.components['drop-zone'] = new SimpleBBox(120, 220, 120, 25, 0, 0);
        model.viewState.components['statement-box'] = new SimpleBBox(120, 245, 120, 30, 0, 0);

        model.viewState.dimensionsSynced = false;
        model.viewState.source = 'a = b;\n';
        model.viewState.offSet = 0;
        model.viewState.expression = 'a = b';
        model.viewState.fullExpression = model.getStatementString().replace(';', '');
        const treex = render(<AssignmentStatement model={model} />, {
            dragDropManager: new DragDropManager(),
        });
        expect(toJson(treex)).to.matchSnapshot();
    });

    it('Variable definition statement test', () => {
        chaiJestSnapshot.setTestName('Variable definition statement test');

        const model = ASTFactory.createVariableDefinitionStatement();
        model.setStatementFromString('int i1 = 0');

        model.viewState = {};
        model.viewState.bBox = new SimpleBBox(120, 220, 120, 55, 0, 0);
        model.viewState.components = {};
        model.viewState.components['drop-zone'] = new SimpleBBox(120, 220, 120, 55, 0, 0);
        model.viewState.components['statement-box'] = new SimpleBBox(120, 245, 120, 30, 0, 0);
        model.viewState.dimensionsSynced = false;
        model.viewState.expression = 'int i1 = 0';
        model.viewState.fullExpression = model.getStatementString().replace(';', '');
        model.viewState.hidden = false;
        model.viewState.offSet = 0;
        model.viewState.source = 'int i1 = 0;↵    ';

        const treex = render(<VariableDefinitionStatement model={model} />, {
            dragDropManager: new DragDropManager(),
        });
        expect(toJson(treex)).to.matchSnapshot();
    });

    it('Return statement test', () => {
        chaiJestSnapshot.setTestName('Return statement test');

        const model = ASTFactory.createReturnStatement();
        model.setStatementFromString('return 0');

        model.viewState = {};
        model.viewState.bBox = new SimpleBBox(120, 220, 120, 55, 0, 0);
        model.viewState.components = {};
        model.viewState.components['drop-zone'] = new SimpleBBox(120, 220, 120, 25, 0, 0);
        model.viewState.components['statement-box'] = new SimpleBBox(120, 245, 120, 30, 0, 0);
        model.viewState.dimensionsSynced = false;
        model.viewState.expression = 'return 0';
        model.viewState.fullExpression = model.getStatementString().replace(';', '');
        model.viewState.hidden = false;
        model.viewState.offSet = 0;
        model.viewState.source = `${model.getStatementString()}↵    `;

        const treex = render(<ReturnStatement model={model} />, {
            dragDropManager: new DragDropManager(),
        });
        expect(toJson(treex)).to.matchSnapshot();
    });

    it('Break statement test', () => {
        chaiJestSnapshot.setTestName('Break statement test');

        const model = ASTFactory.createBreakStatement();
        model.viewState = {};
        model.viewState.bBox = new SimpleBBox(145, 275, 120, 55, 0, 0);
        model.viewState.components = {};
        model.viewState.components['drop-zone'] = new SimpleBBox(145, 275, 120, 25, 0, 0);
        model.viewState.components['statement-box'] = new SimpleBBox(145, 300, 120, 30, 0, 0);
        model.viewState.dimensionsSynced = false;
        model.viewState.expression = model.getStatementString().replace(';', '');
        model.viewState.fullExpression = model.getStatementString().replace(';', '');
        model.viewState.hidden = false;
        model.viewState.offSet = 0;

        const treex = render(<BreakStatement model={model} />, {
            dragDropManager: new DragDropManager(),
        });
        expect(toJson(treex)).to.matchSnapshot();
    });

    it('IF statement test', () => {
        chaiJestSnapshot.setTestName('IF statement test');

        const model = ASTFactory.createIfElseStatement();
        model.viewState = {};
        model.viewState.bBox = new SimpleBBox(120, 220, 140, 155, 0, 0);
        model.viewState.components = {};
        model.viewState.components['drop-zone'] = new SimpleBBox(120, 245, 140, 100, 0, 0);
        model.viewState.components['statement-box'] = new SimpleBBox(120, 220, 140, 25, 0, 0);
        model.viewState.dimensionsSynced = false;
        model.viewState.hidden = false;
        model.viewState.offSet = 0;

        const treex = render(<IfElseStatement model={model} />, {
            dragDropManager: new DragDropManager(),
        });
        expect(toJson(treex)).to.matchSnapshot();
    });
});

