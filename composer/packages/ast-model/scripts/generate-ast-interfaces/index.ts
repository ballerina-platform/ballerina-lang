import * as fs from "fs";
import { fix } from "prettier-tslint";
import { findModelInfo, genBaseVisitorFileCode, genInterfacesFileCode } from "./generate";
import { forEachAST } from "./get-asts";

const modelInfo: any = {};

const AST_INTERFACES_PATH = "./src/ast-interfaces.ts";
const BASE_VISITOR_PATH = "./src/base-visitor.ts";

forEachAST((ast) => {
    findModelInfo(ast, modelInfo);
}).then(() => {
    fs.writeFileSync(AST_INTERFACES_PATH, genInterfacesFileCode(modelInfo));
    fix(AST_INTERFACES_PATH);
    fs.writeFileSync(BASE_VISITOR_PATH, genBaseVisitorFileCode(Object.keys(modelInfo)));
    fix(BASE_VISITOR_PATH);
});
