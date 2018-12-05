import * as fs from "fs";
import * as path from "path";
import { CompilationUnit, Function as BallerinaFunction } from "../src/ast-interfaces";
import { genAST, shutdown } from "./lang-client";
const { log } = console;

const DEFAULT_NODES_PATH = "./src/default-nodes/resources";

genDefaultNodes();

function genDefaultNodes() {
    const defaultNodesBal = path.join(__dirname, "resources", "top-level-defs.bal");
    genAST(defaultNodesBal).then((ast: CompilationUnit) => {
        if (!ast) {
            log("Could not parse!");
            shutdown();
            return;
        }

        const defaultImportPath = path.join(DEFAULT_NODES_PATH, "import.json");
        fs.writeFileSync(defaultImportPath, JSON.stringify(ast.topLevelNodes[0], null, 2));

        const defaultFunctionPath = path.join(DEFAULT_NODES_PATH, "function.json");
        fs.writeFileSync(defaultFunctionPath, JSON.stringify(ast.topLevelNodes[1], null, 2));

        const defaultServicePath = path.join(DEFAULT_NODES_PATH, "service.json");
        fs.writeFileSync(defaultServicePath, JSON.stringify(ast.topLevelNodes[2], null, 2));

        const defaultMainFunctionPath = path.join(DEFAULT_NODES_PATH, "main-function.json");
        fs.writeFileSync(defaultMainFunctionPath, JSON.stringify(ast.topLevelNodes[5], null, 2));

        const defaultIfPath = path.join(DEFAULT_NODES_PATH, "if.json");
        const defaultWhilePath = path.join(DEFAULT_NODES_PATH, "while.json");

        let ifAST = {};
        let whileAST = {};
        const fBody = (ast.topLevelNodes[6] as BallerinaFunction).body;
        if (fBody) {
            ifAST = fBody.statements[0];
            whileAST = fBody.statements[1];
        }

        fs.writeFileSync(defaultIfPath, JSON.stringify(ifAST, null, 2));
        fs.writeFileSync(defaultWhilePath, JSON.stringify(whileAST, null, 2));

        shutdown();
    });
}
