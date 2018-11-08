import { BallerinaAST } from "@ballerina/ast-model";
// tslint:disable-next-line:no-implicit-dependencies
import { storiesOf } from "@storybook/react";
import * as React from "react";
import bbeASTs from "../resources/bbe-asts.json";
// import { DiagramEditor } from "./../src-ts/DiagramEditor";

const storybook = storiesOf("Ballerina Diagram", module);

const bbeASTsArray = bbeASTs as Array<{ bbe: string, ast: BallerinaAST }>;

bbeASTsArray.forEach((bbeAST) => {
    storybook.add(JSON.stringify(bbeAST.bbe), () => (
          <div
            children={JSON.stringify(bbeAST.ast)} />
      ));
});

// bbes.forEach((sampleCat) => {
//   sampleCat.samples.forEach((sample) => {
//     storybook.add(sample.title, () => (
//         <DiagramEditor
//           docUri={sample.url}
//           height={1000}
//           width={1000}
//           langClient={client} />
//     ));
//   });
// });
