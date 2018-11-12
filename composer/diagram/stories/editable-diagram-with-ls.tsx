import { BallerinaAST } from "@ballerina/ast-model";
// tslint:disable-next-line:no-submodule-imports
import { createWSLangClient } from "@ballerina/lang-service/lib/client/ws";
import { storiesOf } from "@storybook/react";
import * as React from "react";
import { Diagram, EditableDiagram } from "./../src";
import { commonProps, MockLangClient } from "./../tests/utils";

const editableDiagramStories = storiesOf("Edtable Diagram with LS", module);

async function test(): Promise<number> {
  // tslint:disable-next-line:no-empty
  const client = await createWSLangClient(8081, () => {}, () => {});
  const initResult = await client.init();
  const samples = await client.fetchExamples({});
  // tslint:disable-next-line:no-console
  console.log(samples);
  return 0;
}

test();
