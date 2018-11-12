
import { BallerinaAST, BallerinaASTNode, BallerinaEndpoint, BallerinaSourceFragment } from "@ballerina/ast-model";
import { ASTDidChangeParams, ASTDidChangeResponse, GetASTParams, GetASTResponse } from "@ballerina/lang-service";
import { DiagramLangClient, DiagramMode } from "./../src";

export class MockLangClient implements DiagramLangClient {
    constructor(
        private ast: BallerinaAST,
    ) {}
    public getAST(params: GetASTParams): Thenable<GetASTResponse> {
      return Promise.resolve({ ast: this.ast, parseSuccess: true });
    }
    public astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse> {
      return Promise.resolve({});
    }
    public parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode> {
      return Promise.resolve({ kind: "Empty" });
    }
    public getEndpoints(): Thenable<BallerinaEndpoint[]> {
      return Promise.resolve([]);
    }
    public goToSource(line: number, column: number): void {
      // do nothing
    }
}

export const commonProps = {
  height: 1000,
  mode: DiagramMode.ACTION,
  width: 1000,
  zoom: 1,
};
