export interface BallerinaAST {
    kind: string;
    topLevelNodes: BallerinaASTNode[];
}
export interface BallerinaASTNode {
    kind: string;
}
export interface BallerinaEndpoint {
    packageName: string;
    name: string;
}
export interface BallerinaSourceFragment {
    source: string;
    expectedNodeType: string;
}