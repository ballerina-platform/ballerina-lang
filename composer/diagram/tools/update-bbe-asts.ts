import { BallerinaAST } from "@ballerina/ast-model";
import { BallerinaLangClient, detectBallerinaHome, GetASTResponse, getBBEs,
  startBallerinaLangServer } from "@ballerina/lang-service";
import { writeFileSync } from "fs";
import * as path from "path";

let bbeASTs;

const ballerinaHome = detectBallerinaHome();
const bbeASTJson = path.join(__dirname, "..", "..", "resources", "bbe-asts.json");

startBallerinaLangServer(ballerinaHome)
.then((client: BallerinaLangClient | undefined) => {
  if (client) {
    const bbes = getBBEs(ballerinaHome);
    const promises: Array<Thenable<{ bbe: string, ast: BallerinaAST }>> = [];
    bbes.forEach((bbe, index) => {
      // tslint:disable-next-line:no-console
      console.log("getting ast for ", bbe);
      if (index < 10) {
        promises.push(
          client.getAST({
            documentIdentifier: {
              uri: bbe,
            },
          }).then((resp) => ({ bbe, ast: resp.ast })),
        );
      }
    });
    Promise.all(promises)
      .then((responses) => {
        bbeASTs = responses;
        // tslint:disable-next-line:no-console
        console.log("Writing data to ", bbeASTJson);
        writeFileSync(bbeASTJson, JSON.stringify(bbeASTs));
        client.kill();
      },
      (err) => {
          // tslint:disable-next-line:no-console
          console.error(err);
          client.kill();
      });
  }
});

// .then((astResp) => {
//   if (astResp.ast) {
//     // tslint:disable-next-line:no-console
//     console.log("received ast ", bbe);
//     bbeASTs.push({
//       ast: astResp.ast,
//       bbe,
//     });
//   } else {
//     // tslint:disable-next-line:no-console
//     console.error("Parse error" + bbe);
//   }
//   if (index === bbe.length - 1) {
//     client.kill();
//   }
// }
// });
