import "@ballerina/font";
import "@ballerina/theme";
import { storiesOf } from "@storybook/react";
import * as React from "react";
import { DiagramMode, EditableDiagram } from "./../src";

// tslint:disable-next-line:no-submodule-imports
import { createWSLangClient } from "@ballerina/lang-service/lib/src/client/ws";

// tslint:disable:no-console
const editableDiagramStories = storiesOf("Edtable Diagram", module);

async function createConnection(): Promise<void> {
  // tslint:disable-next-line:no-empty
  const client = await createWSLangClient(8081, () => {}, () => {});
  client.init()
    .then((result) => {
      editableDiagramStories.add("Edit Bal File", () => (
        <EditableDiagram
          docUri="file://~/Desktop/sample/test.bal"
          langClient={client}
          zoom={1}
          mode={DiagramMode.DEFAULT}
        />
      ));
    });
}

createConnection();
