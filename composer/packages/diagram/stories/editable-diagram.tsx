import { storiesOf } from "@storybook/react";
import * as React from "react";
import bbeASTs from "../resources/bbe-asts.json";
import { Diagram, EditableDiagram } from "./../src";
import { commonProps, MockLangClient } from "./../tests/utils";

const editableDiagramStories = storiesOf("Edtable Diagram", module);

const bbeASTsArray = bbeASTs as string[];

bbeASTsArray.forEach((bbeASTPath) => {
    const bbeAST = require(`../resources/bbe-asts/${bbeASTPath}`);
    editableDiagramStories.add(bbeAST.title, () => (
        <EditableDiagram
          fitToWidthOrHeight
          docUri={bbeAST.bbe}
          langClient={new MockLangClient(bbeAST.ast)}
          {...commonProps}
        />
      ));
});

const staticDiagramStories = storiesOf("Static Diagram", module);

bbeASTsArray.forEach((bbeASTPath) => {
  const bbeAST = require(`../resources/bbe-asts/${bbeASTPath}`);
  staticDiagramStories.add(bbeAST.title, () => (
      <Diagram
        docUri=""
        fitToWidthOrHeight
        langClient={new MockLangClient(bbeAST.ast)}
        ast={bbeAST.ast}
        {...commonProps}
      />
    ));
});
