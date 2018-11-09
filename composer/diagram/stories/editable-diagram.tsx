import { BallerinaAST } from "@ballerina/ast-model";
// tslint:disable-next-line:no-implicit-dependencies
import { storiesOf } from "@storybook/react";
import * as React from "react";
import bbeASTs from "../resources/bbe-asts.json";
import { Diagram, EditableDiagram } from "./../src-ts/";
import { commonProps, MockLangClient } from "./../tests/utils";

const editableDiagramStories = storiesOf("Edtable Diagram", module);

const bbeASTsArray = bbeASTs as Array<{ bbe: string, ast: BallerinaAST, title: string }>;

bbeASTsArray.forEach((bbeAST) => {
    editableDiagramStories.add(bbeAST.title, () => (
        <EditableDiagram
          docUri={bbeAST.bbe}
          langClient={new MockLangClient(bbeAST.ast)}
          {...commonProps}
        />
      ));
});

const staticDiagramStories = storiesOf("Static Diagram", module);

bbeASTsArray.forEach((bbeAST) => {
  staticDiagramStories.add(bbeAST.title, () => (
      <Diagram
        ast={bbeAST.ast}
        {...commonProps}
      />
    ));
});
