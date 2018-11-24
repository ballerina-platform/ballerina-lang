import { createStdioLangClient, StdioBallerinaLangServer } from "@ballerina/lang-service";
import { ChildProcess } from "child_process";
import * as fs from "fs";
import { sync as globSync } from "glob";
import * as path from "path";
import URI from "vscode-uri";
import { attachNode, genSource } from "..";

if (!fs.existsSync(path.join(process.cwd(), "..", "..", "..", "examples"))) {
    // tslint:disable-next-line:no-console
    console.log("please run from ast-model directory. you are in", process.cwd());
    process.exit(1);
}

let exampleBals: string[] = [];
let beforeAttachBals: string[] = [];
let afterAttachBals: string[] = [];
function fetchBalFiles() {
    exampleBals = globSync(path.join(process.cwd(), "..", "..", "..", "examples", "**", "*.bal"), {});
    beforeAttachBals = globSync(path.join(__dirname, "resources", "**", "before.bal"), {}).sort();
    afterAttachBals = globSync(path.join(__dirname, "resources", "**", "after.bal"), {}).sort();
}

fetchBalFiles();

let client: any;
let server: any;

describe("AST utils", () => {
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

    describe("generates sources", () => {
        exampleBals.forEach((file) => {
            test(path.basename(file), async (done) => {
                const astResp = await client.getAST({
                    documentIdentifier: { uri: URI.file(file).toString() }
                });
                if (!astResp.ast) {
                    throw new Error("Could not parse");
                }
                fs.readFile(file, {encoding: "utf8"}, (err, content) => {
                    expect(genSource(astResp.ast)).toEqual(content);
                });
                done();
            });
        });
    });

    describe("attach node", () => {
        const functionAST = require("./resources/function-ast.json");
        beforeAttachBals.forEach((file, i) => {
            const fileParts = file.split(path.sep);
            test(fileParts[fileParts.length - 2], async (done) => {
                const astResp = await client.getAST({
                    documentIdentifier: { uri: URI.file(file).toString() }
                });
                if (!astResp.ast) {
                    throw new Error("Could not parse");
                }
                const tree = astResp.ast;
                attachNode(functionAST, tree, "topLevelNodes", tree.topLevelNodes.length);
                fs.readFile(afterAttachBals[i], {encoding: "utf8"}, (err, content) => {
                    expect(genSource(tree)).toEqual(content);
                    done();
                });
            });
        });
    });

    afterAll((done) => {
        client.close();
        server.shutdown();
    });
});
