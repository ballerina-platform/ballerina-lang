import { storiesOf } from "@storybook/react";
import * as React from "react";
import { DiagramMode, EditableDiagram } from "./../src";

const FILE_URI = "file:///....";

// tslint:disable-next-line:no-submodule-imports
import { IBallerinaLangClient } from "@ballerina/lang-service/lib/src/client/model";
// tslint:disable-next-line:no-submodule-imports
import { createWSLangClient } from "@ballerina/lang-service/lib/src/client/ws";

class RenderDiagramAfterLSInit extends React.Component<
    {},
    { client?: IBallerinaLangClient }
  > {
  public state = {
    client: undefined,
  };
  public componentDidMount() {
    let client: IBallerinaLangClient | undefined;
    // tslint:disable-next-line:no-empty
    createWSLangClient(8081, () => {}, () => {})
      .then((lsCient) => {
        client = lsCient;
        return lsCient.init();
      })
      .then((result) => {
        this.setState({ client });
      });

  }
  public render(): React.ReactNode {
    const { client } = this.state;
    if (!client) {
      return <div />;
    }
    return (
      <EditableDiagram
        docUri={FILE_URI}
        fitToWidthOrHeight
        langClient={client}
        zoom={1}
        mode={DiagramMode.DEFAULT}
      />
    );
  }
}

// tslint:disable:no-console
const editableDiagramStories = storiesOf("Edtable Diagram", module);

editableDiagramStories.add("Diagram for a Bal File", () => (
  <RenderDiagramAfterLSInit />
));
