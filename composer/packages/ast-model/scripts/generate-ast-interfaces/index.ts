import * as fs from "fs";
import { fix } from "prettier-tslint";
import { findModelInfo, genBaseVisitorFileCode, genCheckKindUtilCode,
    genInterfacesFileCode } from "./generate";
import { forEachAST } from "./get-asts";

const modelInfo: any = {};

const AST_INTERFACES_PATH = "./src/ast-interfaces.ts";
const BASE_VISITOR_PATH = "./src/base-visitor.ts";
const CHECK_KIND_UTIL_PATH = "./src/check-kind-util.ts";

forEachAST((ast) => {
    findModelInfo(ast, modelInfo);
}).then(() => {
    fs.writeFileSync(AST_INTERFACES_PATH, genInterfacesFileCode(modelInfo));
    fix(AST_INTERFACES_PATH);
    const modelNames = Object.keys(modelInfo).sort();
    fs.writeFileSync(BASE_VISITOR_PATH, genBaseVisitorFileCode(modelNames));
    fix(BASE_VISITOR_PATH);
    fs.writeFileSync(CHECK_KIND_UTIL_PATH, genCheckKindUtilCode(modelNames));
    fix(CHECK_KIND_UTIL_PATH);
});
