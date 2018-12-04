import * as fs from "fs";
import { sync as globSync } from "glob";
import * as path from "path";
import { fix } from "prettier-tslint";
import { findModelInfo, genBaseVisitorFileCode, genCheckKindUtilCode,
    genInterfacesFileCode } from "./generators";
import { genAST, shutdown } from "./lang-client";

if (!fs.existsSync(path.join(process.cwd(), "..", "..", "..", "examples"))) {
    // tslint:disable-next-line:no-console
    console.log("please run from ast-model directory. you are in", process.cwd());
    process.exit(1);
}

const modelInfo: any = {};

const AST_INTERFACES_PATH = "./src/ast-interfaces.ts";
const BASE_VISITOR_PATH = "./src/base-visitor.ts";
const CHECK_KIND_UTIL_PATH = "./src/check-kind-util.ts";

const balFiles = globSync(path.join(
    process.cwd(), "..", "..", "..", "{examples,tests}", "**", "*.bal"), {});

const triedBalFiles: string[] = [];
const notParsedBalFiles: string[] = [];
const usedBalFiles: string[] = [];

processPart(0, 100);

function printSummary() {
    const { log } = console;
    const found = balFiles.length;
    const notParsed = notParsedBalFiles.length;
    const used = usedBalFiles.length;
    log(`${found} Files found`);
    log(`${notParsed} Could not be parsed`);
    log(`${used} Used for util generation`);
}

function processPart(start: number, count: number) {
    const astPromises: any[] = [];
    const filesPart = balFiles.slice(start, start + count);
    filesPart.forEach((file) => {
        triedBalFiles.push(file);
        const promise = genAST(file).then((ast) => {
            if (!ast) {
                // could not parse
                notParsedBalFiles.push(file);
                return;
            }
            usedBalFiles.push(file);
            findModelInfo(ast, modelInfo);
        });

        const timeout = new Promise((resolve, reject) => {
            setTimeout(() => {
                resolve();
            }, 20000);
        });
        astPromises.push(Promise.race([promise, timeout]));
    });

    Promise.all(astPromises).then(() => {
        if (astPromises.length < count) {
            genFiles();
            shutdown();
            printSummary();
            return;
        }

        processPart(start + count, count);
    });
}

function genFiles() {
    fs.writeFileSync(AST_INTERFACES_PATH, genInterfacesFileCode(modelInfo));
    fix(AST_INTERFACES_PATH);
    const modelNames = Object.keys(modelInfo).sort();
    fs.writeFileSync(BASE_VISITOR_PATH, genBaseVisitorFileCode(modelNames));
    fix(BASE_VISITOR_PATH);
    fs.writeFileSync(CHECK_KIND_UTIL_PATH, genCheckKindUtilCode(modelNames));
    fix(CHECK_KIND_UTIL_PATH);
}
