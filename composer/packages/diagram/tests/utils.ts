import { ASTDidChangeParams, ASTDidChangeResponse, BallerinaAST, BallerinaASTNode, BallerinaEndpoint,
  BallerinaSourceFragment, GetASTParams, GetASTResponse } from "@ballerina/lang-service";
import { DiagramLangClient, DiagramMode } from "./../src";

export class MockLangClient implements DiagramLangClient {
    constructor(
        private ast: BallerinaAST
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
  mode: DiagramMode.ACTION,
  zoom: 1,
};
