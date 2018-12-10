import * as fs from "fs";
import * as path from "path";
import { CompilationUnit } from "../src/ast-interfaces";
import { genSource } from "../src/source-gen";
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
        fs.writeFileSync(defaultImportPath, JSON.stringify(ast.topLevelNodes[0]));

        const defaultFunctionPath = path.join(DEFAULT_NODES_PATH, "function.json");
        fs.writeFileSync(defaultFunctionPath, JSON.stringify(ast.topLevelNodes[1]));

        const defaultServicePath = path.join(DEFAULT_NODES_PATH, "service.json");
        fs.writeFileSync(defaultServicePath, JSON.stringify(ast.topLevelNodes[2]));

        const defaultMainFunctionPath = path.join(DEFAULT_NODES_PATH, "main-function.json");
        fs.writeFileSync(defaultMainFunctionPath, JSON.stringify(ast.topLevelNodes[4]));

        fs.writeFileSync(path.join(".", "top-level-defs-gen.bal"), genSource(ast));

        shutdown();
    });
}
