// tslint:disable:no-submodule-imports
import { BallerinaAST, BallerinaEndpoint,
  EmptyLanguageClient, GetASTParams, GetASTResponse} from "@ballerina/lang-service/lib/src/client";
import { DiagramMode } from "./../src";

export const commonProps = {
  mode: DiagramMode.ACTION,
  zoom: 1,
};

export class MockLangClient extends EmptyLanguageClient {
  public isInitialized = true;
  constructor(public ast: BallerinaAST) {
    super();
  }
  public getAST(params: GetASTParams): Thenable<GetASTResponse> {
    return Promise.resolve({ ast: this.ast, parseSuccess: true });
  }
  public getEndpoints(): Thenable<BallerinaEndpoint[]> {
    return Promise.resolve([
      {
        name: "Client",
        packageName: "http"
      },
      {
        name: "Client",
        packageName: "jms"
      }
    ]);
  }
}
