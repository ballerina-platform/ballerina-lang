import { BallerinaAST } from "@ballerina/ast-model";
import { createStdioLangClient,
  getBBEs, StdioBallerinaLangServer } from "@ballerina/lang-service";
import { ChildProcess } from "child_process";
import { writeFileSync } from "fs";
import * as path from "path";

let bbeASTs;
const bbeASTJson = path.join(__dirname, "..", "..", "resources", "bbe-asts.json");
const server = new StdioBallerinaLangServer();
server.start();

// tslint:disable-next-line:no-empty
createStdioLangClient(server.lsProcess as ChildProcess, () => {}, () => {})
.then((client) => {
  if (client) {
    client.init()
    .then((result) => {
      const bbes = getBBEs();
      const promises: Array<Thenable<{ bbe: string, ast: BallerinaAST, title: string }>> = [];
      bbes.forEach((bbe, index) => {
        if (index < 10) {
          // tslint:disable-next-line:no-console
          console.log("getting ast for ", bbe);
          promises.push(
            client.getAST({
              documentIdentifier: {
                uri: bbe,
              },
            }).then((resp) => ({ bbe, ast: resp.ast, title: path.basename(bbe)})),
          );
          }
        });
      Promise.all(promises)
        .then((responses) => {
          bbeASTs = responses;
          // tslint:disable-next-line:no-console
          console.log("Writing data to ", bbeASTJson);
          writeFileSync(bbeASTJson, JSON.stringify(bbeASTs));
          client.close();
          server.shutdown();
        });

      });
    }
});
