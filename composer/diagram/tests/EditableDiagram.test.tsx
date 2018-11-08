import { BallerinaAST } from "@ballerina/ast-model";
import * as React from "react";
import { create } from "react-test-renderer";
import bbeASTs from "../resources/bbe-asts.json";
import { Diagram } from "../src-ts";
import { MockLangClient } from "./utils";

const bbeASTsArray = bbeASTs as Array<{ bbe: string, ast: BallerinaAST, title: string }>;

bbeASTsArray.forEach((bbeAST) => {
    test(`Diagram for ${bbeAST.title} renders properly`, () => {
        const component = create(
            <Diagram
              docUri={bbeAST.bbe}
              height={1000}
              width={1000}
              langClient={new MockLangClient(bbeAST.ast)}
            />,
        );
        const tree = component.toJSON();
        expect(tree).toMatchSnapshot();
    });
});
