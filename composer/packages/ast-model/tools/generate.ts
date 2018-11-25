import * as fs from "fs";
import { sync as globSync } from "glob";
import * as path from "path";
import { fix } from "prettier-tslint";
import { CompilationUnit } from "../src/ast-interfaces";
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
const DEFAULT_NODES_PATH = "./src/default-nodes/resources";

const balFiles = globSync(path.join(
    process.cwd(), "..", "..", "..", "{examples,tests}", "**", "*.bal"), {});

const astPromises: any[] = [];
balFiles.forEach((file) => {
    const promise = genAST(file).then((ast) => {
        if (!ast) {
            // could not parse
            return;
        }
        findModelInfo(ast, modelInfo);
    });
    astPromises.push(promise);
});

const defaultNodesBal = path.join(__dirname, "resources", "top-level-defs.bal");
const defaultNodesASTPromise = genAST(defaultNodesBal).then((ast: CompilationUnit) => {
    const defaultImportPath = path.join(DEFAULT_NODES_PATH, "import.json");
    fs.writeFileSync(defaultImportPath, JSON.stringify(ast.topLevelNodes[0]));

    const defaultFunctionPath = path.join(DEFAULT_NODES_PATH, "function.json");
    fs.writeFileSync(defaultFunctionPath, JSON.stringify(ast.topLevelNodes[1]));

    const defaultMainFunctionPath = path.join(DEFAULT_NODES_PATH, "main-function.json");
    fs.writeFileSync(defaultMainFunctionPath, JSON.stringify(ast.topLevelNodes[6]));
});
astPromises.push(defaultNodesASTPromise);

Promise.all(astPromises).then(() => {
    fs.writeFileSync(AST_INTERFACES_PATH, genInterfacesFileCode(modelInfo));
    fix(AST_INTERFACES_PATH);
    const modelNames = Object.keys(modelInfo).sort();
    fs.writeFileSync(BASE_VISITOR_PATH, genBaseVisitorFileCode(modelNames));
    fix(BASE_VISITOR_PATH);
    fs.writeFileSync(CHECK_KIND_UTIL_PATH, genCheckKindUtilCode(modelNames));
    fix(CHECK_KIND_UTIL_PATH);

    shutdown();
});
