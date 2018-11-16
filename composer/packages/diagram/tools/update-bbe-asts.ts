import { createStdioLangClient,
  getBBEs, StdioBallerinaLangServer } from "@ballerina/lang-service";
import { ChildProcess } from "child_process";
import { existsSync, mkdirSync, writeFileSync } from "fs";
import * as path from "path";
import { sync as rimrafSync } from "rimraf";

const bbeASTsPath = path.join(__dirname, "..", "..", "resources", "bbe-asts");
const bbeASTsIndexPath = path.join(__dirname, "..", "..", "resources", "bbe-asts.json");

// tslint:disable-next-line:no-console
console.log("Removing existing bbe ast jsons if any.");
if (existsSync(bbeASTsPath)) {
  rimrafSync(bbeASTsPath);
}

mkdirSync(bbeASTsPath);

const server = new StdioBallerinaLangServer(process.env.BALLERINA_HOME);
server.start();

// tslint:disable-next-line:no-empty
createStdioLangClient(server.lsProcess as ChildProcess, () => {}, () => {})
.then((client) => {
  if (client) {
    client.init()
    .then((result) => {
      const bbes = getBBEs(process.env.BALLERINA_HOME);
      const promises: Array<Thenable<string>> = [];
      bbes.forEach((bbe, index) => {
        // tslint:disable-next-line:no-console
        console.log("getting ast for ", bbe);
        promises.push(
          client.getAST({
            documentIdentifier: {
              uri: bbe,
            },
          }).then((resp) => {
            const bbeData = { bbe, ast: resp.ast, title: path.basename(bbe) };
            const jsonFileName = `${path.parse(bbe).name}.json`;
            const astPath = path.join(bbeASTsPath, jsonFileName);
            // tslint:disable-next-line:no-console
            console.log("Writing data to ", astPath);
            writeFileSync(astPath, JSON.stringify(bbeData));
            return jsonFileName;
          })
        );
      });
      Promise.all(promises)
        .then((responses) => {
          writeFileSync(bbeASTsIndexPath, JSON.stringify(responses));
          client.close();
          server.shutdown();
        });
      });
    }
});
