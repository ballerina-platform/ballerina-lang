import { BallerinaAST, BallerinaASTNode, BallerinaEndpoint, BallerinaSourceFragment } from "@ballerina/ast-model";
import { ASTDidChangeParams, ASTDidChangeResponse, GetASTParams,
    GetASTResponse } from "@ballerina/lang-service";
import React from "react";

export interface DiagramLangClient {
    getAST(params: GetASTParams): Thenable<GetASTResponse>;
    astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse>;
    parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode>;
    getEndpoints(): Thenable<BallerinaEndpoint[]>;
    goToSource(line: number, column: number): void;
}

export interface DiagramProps {
    docUri: string;
    langClient: DiagramLangClient;
    height: number;
    width: number;
}

export interface DiagramState {
    currentAST: BallerinaAST;
    editMode: boolean;
    diagramMode: "action" | "default";
}

export class Diagram extends React.Component<DiagramProps, DiagramState> {
    public render() {
        return <React.Fragment>
            <div>{"Diagram Editor"}</div>
        </React.Fragment>;
    }
}
