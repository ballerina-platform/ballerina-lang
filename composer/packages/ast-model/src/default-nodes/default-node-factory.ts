import * as Ballerina from "../ast-interfaces";
// Instead of requiring we keep a json string so we don't have to clone the default nodes to reuse
// tslint:disable:no-var-requires
const defaultImport = JSON.stringify(require("./resources/function.json"));
const defaultFunction = JSON.stringify(require("./resources/function.json"));
const defaultMainFunction = JSON.stringify(require("./resources/function.json"));
// tslint:enable:no-var-requires

export function createImportNode(): Ballerina.Import {
    return JSON.parse(defaultImport);
}

export function createFunctionNode(): Ballerina.Function {
    return JSON.parse(defaultFunction);
}

export function createMainFunctionNode(): Ballerina.Function {
    return JSON.parse(defaultMainFunction);
}
