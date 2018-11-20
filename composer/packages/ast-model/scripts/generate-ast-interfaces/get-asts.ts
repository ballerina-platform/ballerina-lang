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
    return globSync(path.join(process.cwd(), "..", "..", "..", "{examples,tests}", "**", "*.bal"), {});
}

export async function forEachAST(callback: (node: Ballerina.ASTNode, filePath: string) => void) {
    const balFiles = fetchBalFiles();
    const server = new StdioBallerinaLangServer(process.env.BALLERINA_HOME);
    server.start();

    const client = await createStdioLangClient(server.lsProcess as ChildProcess, () => {/**/}, () => {/**/});
    if (!client) {
        // tslint:disable-next-line:no-console
        console.error("Could not initiate language client");
    }

    await client.init();
    const promises = balFiles.map(async (balFilePath, index) => {
        const astResp = await client.getAST({
            documentIdentifier: { uri: URI.file(balFilePath).toString() }
        });
        if (astResp.ast) {
            callback(astResp.ast, balFilePath);
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
