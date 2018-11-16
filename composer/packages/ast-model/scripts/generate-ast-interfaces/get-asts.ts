import { createStdioLangClient, StdioBallerinaLangServer } from "@ballerina/lang-service";
import { ChildProcess } from "child_process";
import { existsSync } from "fs";
import { sync as globSync } from "glob";
import * as path from "path";
import URI from "vscode-uri";
import * as Ballerina from "../../src/ast-interfaces";

if (!existsSync(path.join(process.cwd(), "..", "..", "..", "examples"))) {
    // tslint:disable-next-line:no-console
    console.log("please run from ast-model directory. you are in", process.cwd());
    process.exit(1);
}

function fetchBalFiles(): string[] {
    const bbeFiles = globSync(path.join(process.cwd(), "..", "..", "..", "{examples,tests}", "**", "*.bal"), {});
    return bbeFiles.map((file) => URI.file(file).toString());
}

export async function forEachAST(callback: (node: Ballerina.ASTNode) => void) {
    const balFiles = fetchBalFiles();
    const server = new StdioBallerinaLangServer(process.env.BALLERINA_HOME);
    server.start();
    // tslint:disable-next-line:no-console
    console.log("server started");

    const client = await createStdioLangClient(server.lsProcess as ChildProcess, () => {/**/}, () => {/**/});
    if (!client) {
        // tslint:disable-next-line:no-console
        console.log("Could not initiate language client");
    }

    await client.init();
    const promises = balFiles.map(async (balFilePath, index) => {
        // tslint:disable-next-line:no-console
        console.log("getting ast for ", balFilePath);
        const astResp = await client.getAST({
            documentIdentifier: { uri: balFilePath }
        });
        if (astResp.ast) {
            callback(astResp.ast);
        } else {
            // tslint:disable-next-line:no-console
            console.log(`Could not parse: ${balFilePath}`);
        }
    });
    try {
        await Promise.all(promises);
    } catch (e) {
        // tslint:disable-next-line:no-console
        console.log(e);
    }
    client.close();
    server.shutdown();
}
