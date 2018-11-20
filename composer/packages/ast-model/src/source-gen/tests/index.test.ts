import { createStdioLangClient, StdioBallerinaLangServer } from "@ballerina/lang-service";
import { ChildProcess } from "child_process";
import * as fs from "fs";
import { sync as globSync } from "glob";
import * as path from "path";
import URI from "vscode-uri";

if (!fs.existsSync(path.join(process.cwd(), "..", "..", "..", "examples"))) {
    // tslint:disable-next-line:no-console
    console.log("please run from ast-model directory. you are in", process.cwd());
    process.exit(1);
}

function fetchBalFiles(): string[] {
    return globSync(path.join(process.cwd(), "..", "..", "..", "examples", "**", "*.bal"), {});
}

const balFiles = fetchBalFiles();
let client: any;
let server: any;

describe("generates sources", () => {
    beforeAll(async (done) => {
        server = new StdioBallerinaLangServer(process.env.BALLERINA_HOME);
        server.start();

        client = await createStdioLangClient(server.lsProcess as ChildProcess, () => {/**/}, () => {/**/});
        if (!client) {
            done("Could not initiate language client");
        }

        await client.init();
        done();
    });

    balFiles.forEach((file) => {
        test(path.basename(file), async (done) => {
            const astResp = await client.getAST({
                documentIdentifier: { uri: URI.file(file).toString() }
            });
            if (!astResp.ast) {
                throw new Error("Could not parse");
            }
            done();
        });
    });

    afterAll((done) => {
        client.close();
        server.shutdown();
    });
});
