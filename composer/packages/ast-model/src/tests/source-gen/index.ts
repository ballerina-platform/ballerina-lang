import * as fs from "fs";
import { forEachAST } from "../../../scripts/generate-ast-interfaces/get-asts";
import { genSource } from "../../source-gen";

test();

function test() {
    testGenSource();
}

function testGenSource() {
    forEachAST((ast, balFilePath) => {
        const actual = genSource(ast);
        const expected = fs.readFileSync(balFilePath, "utf8");
        if (actual !== expected) {
            // tslint:disable-next-line:no-console
            console.log(actual, expected);
        }
    });
}
