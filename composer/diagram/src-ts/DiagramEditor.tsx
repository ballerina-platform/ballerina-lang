import { BallerinaAST, BallerinaASTNode, BallerinaEndpoint, BallerinaSourceFragment } from "@ballerina/ast-model";
import { BallerinaLangClient } from "@ballerina/lang-service";
import React from "react";

export interface DiagramEditorProps {
    docUri: string;
    langClient: BallerinaLangClient;
    height: number;
    width: number;
}

export interface DiagramEditorState {
    currentAST: BallerinaAST;
    editMode: boolean;
    diagramMode: "action" | "default";
}

export class DiagramEditor extends React.Component<DiagramEditorProps, DiagramEditorState> {
    public render() {
        return <React.Fragment>
            <div>{"Diagram Editor"}</div>
        </React.Fragment>;
    }
}

export const DiagramEditor2 = (props: DiagramEditorProps) => {
    return <React.Fragment>
            <div>{"Diagram Editor"}</div>
        </React.Fragment>;
};
