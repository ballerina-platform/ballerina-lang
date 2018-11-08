import { BallerinaAST } from "@ballerina/ast-model";
// tslint:disable-next-line:no-implicit-dependencies
import { storiesOf } from "@storybook/react";
import * as React from "react";
import bbeASTs from "../resources/bbe-asts.json";
import { Diagram } from "./../src-ts/";
import { MockLangClient } from "./../tests/utils";

const storybook = storiesOf("Ballerina Diagram", module);

const bbeASTsArray = bbeASTs as Array<{ bbe: string, ast: BallerinaAST, title: string }>;

bbeASTsArray.forEach((bbeAST) => {
    storybook.add(bbeAST.title, () => (
        <Diagram
          docUri={bbeAST.bbe}
          height={1000}
          width={1000}
          langClient={new MockLangClient(bbeAST.ast)}
        />
      ));
});
