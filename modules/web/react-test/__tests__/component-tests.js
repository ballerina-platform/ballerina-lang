import React from "react";
import chai, {expect} from 'chai';
import {render, shallow} from 'enzyme';
import chaiJestSnapshot from "chai-jest-snapshot";
import toJson from 'enzyme-to-json';
import ASTFactory from '../../js/ballerina/ast/ast-factory';
import AssignmentStatement from '../../js/ballerina/components/assignment-statement';
import SimpleBBox from '../../js/ballerina/ast/simple-bounding-box';
import DragDropManager from '../../js/ballerina/tool-palette/drag-drop-manager';

chai.use(chaiJestSnapshot);

describe("component tests", function () {
    before(function () {
        chaiJestSnapshot.resetSnapshotRegistry();
    });

    beforeEach(function () {
        chaiJestSnapshot.configureUsingMochaContext(this);
        chaiJestSnapshot.setFilename("react-test/__snapshots__/component-tests.snap");
    });

    it("Assignment statement test", function() {
        chaiJestSnapshot.setTestName("Assignment statement test");
        const jsonNode = {
            "line_number": "2",
            "position_info": {"start_line": 2, "start_offset": 0, "stop_line": 2, "stop_offset": 0},
            "whitespace_descriptor": {"regions": {"0": "\n", "2": " ", "3": " ", "4": "\n"}},
            "type": "assignment_statement",
            "is_declared_with_var": false,
            "children": [{
                "type": "variable_reference_list",
                "children": [{
                    "line_number": "2",
                    "position_info": {"start_line": 2, "start_offset": 0, "stop_line": 2, "stop_offset": 0},
                    "whitespace_descriptor": {"regions": {"0": "\n", "3": " "}},
                    "type": "simple_variable_reference_expression",
                    "variable_reference_name": "b",
                    "variable_name": "b",
                    "package_name": null,
                }],
            }, {
                "line_number": "2",
                "position_info": {"start_line": 2, "start_offset": 4, "stop_line": 2, "stop_offset": 4},
                "whitespace_descriptor": {"regions": {"0": " ", "3": ""}},
                "type": "simple_variable_reference_expression",
                "variable_reference_name": "b",
                "variable_name": "b",
                "package_name": null,
            }],
        };

        const model = ASTFactory.createFromJson(jsonNode);
        model.initFromJson(jsonNode);
        model.viewState = {};
        model.viewState.bBox = new SimpleBBox(120, 220, 120, 55, 0, 0);
        model.viewState.components = {};
        model.viewState.components["drop-zone"] = new SimpleBBox(120, 220, 120, 25, 0, 0);
        model.viewState.components["statement-box"] = new SimpleBBox(120, 245, 120, 30, 0, 0);

        model.viewState.dimensionsSynced = false;
        model.viewState.source = "c = b;\n";
        model.viewState.offSet = 0;
        model.viewState.expression = "c = b";
        model.viewState.fullExpression = "c = b";
        const treex = render(<AssignmentStatement model={model}/>, {
            DragDropManager: new DragDropManager(),
        });
        expect(toJson(treex)).to.matchSnapshot();
    });
});

