import * as Ballerina from "../ast-interfaces";
// Instead of requiring we keep a json string so we don't have to clone the default nodes to reuse
// tslint:disable:no-var-requires
const defaultImport = JSON.stringify(require("./resources/function.json"));
const defaultFunction = JSON.stringify(require("./resources/function.json"));
const defaultMainFunction = JSON.stringify(require("./resources/main-function.json"));
const defaultService = JSON.stringify(require("./resources/service.json"));
const defaultIf = JSON.stringify(require("./resources/if.json"));
const defaultWhile = JSON.stringify(require("./resources/while.json"));
const defaultForeach = JSON.stringify(require("./resources/foreach.json"));
const defaultEndpoint = JSON.stringify(require("./resources/endpoint.json"));
const defaultWorker = JSON.stringify(require("./resources/worker.json"));
const implicitReturn = JSON.stringify(require("./resources/implicitReturn.json"));
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

export function createServiceNode(): Ballerina.Service {
    return JSON.parse(defaultService);
}

export function createIfNode(): Ballerina.If {
    return JSON.parse(defaultIf);
}

export function createWhileNode(): Ballerina.While {
    return JSON.parse(defaultWhile);
}

export function createForeachNode(): Ballerina.Foreach {
    return JSON.parse(defaultForeach);
}

export function createEndpointNode(): Ballerina.VariableDef {
    return JSON.parse(defaultEndpoint);
}

export function createWorkerNode(): Ballerina.VariableDef {
    return JSON.parse(defaultWorker);
}

export function createReturnNode(): Ballerina.Return {
    return JSON.parse(implicitReturn);
}
