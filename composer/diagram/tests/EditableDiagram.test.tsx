import { BallerinaAST } from "@ballerina/ast-model";
import * as React from "react";
import { create } from "react-test-renderer";
import bbeASTs from "../resources/bbe-asts.json";
import { EditableDiagram } from "../src-ts";
import { commonProps, MockLangClient } from "./utils";

const bbeASTsArray = bbeASTs as Array<{ bbe: string, ast: BallerinaAST, title: string }>;

bbeASTsArray.forEach((bbeAST) => {
    test(`Diagram for ${bbeAST.title} renders properly`, () => {
        const component = create(
            <EditableDiagram
              docUri={bbeAST.bbe}
              langClient={new MockLangClient(bbeAST.ast)}
              {...commonProps}
            />,
        );
        const tree = component.toJSON();
        expect(tree).toMatchSnapshot();
    });
});
