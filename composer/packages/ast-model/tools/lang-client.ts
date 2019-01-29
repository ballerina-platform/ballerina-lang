import { createStdioLangClient, StdioBallerinaLangServer } from "@ballerina/lang-service";
import { ChildProcess } from "child_process";
import URI from "vscode-uri";

let server: any;
let client: any;

const clientPromise = init();

export async function init() {
    server = new StdioBallerinaLangServer(process.env.BALLERINA_HOME);
    server.start();

    client = await createStdioLangClient(server.lsProcess as ChildProcess, () => {/**/}, () => {/**/});
    if (!client) {
        // tslint:disable-next-line:no-console
        console.error("Could not initiate language client");
    }

    await client.init();
}

export function shutdown() {
    client.close();
    server.shutdown();
}

export async function genAST(balFilePath: string) {
    let ast;
    try {
        await clientPromise;
        const astResp = await client.getAST({
            documentIdentifier: { uri: URI.file(balFilePath).toString() }
        });
        ast = astResp.ast;
    } catch (e) {
        // tslint:disable-next-line:no-console
        console.log(`Error when parsing ${balFilePath} \n ${e}`);
    }
    return ast;
}
