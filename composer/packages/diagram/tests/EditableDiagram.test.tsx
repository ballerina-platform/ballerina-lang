import * as React from "react";
import { create } from "react-test-renderer";
import bbeASTs from "../resources/bbe-asts.json";
import { EditableDiagram } from "../src";
import { commonProps, MockLangClient } from "./utils";

const bbeASTsArray = bbeASTs as string[];

bbeASTsArray.forEach((bbeASTPath) => {
    const bbeAST = require(`../resources/bbe-asts/${bbeASTPath}`);
    test(`Diagram for ${bbeAST.title} renders properly`, () => {
        const component = create(
            <EditableDiagram
              docUri={bbeAST.bbe}
              langClient={new MockLangClient(bbeAST.ast)}
              {...commonProps}
            />
        );
        const tree = component.toJSON();
        expect(tree).toMatchSnapshot();
    });
});
