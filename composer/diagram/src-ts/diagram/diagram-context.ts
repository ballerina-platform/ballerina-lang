import { BallerinaAST } from "@ballerina/ast-model";
import React from "react";

export enum DiagramMode { ACTION, DEFAULT }

export interface IDiagramContext {
    ast: BallerinaAST | undefined;
    changeMode: (newMode: DiagramMode) => void;
    editingEnabled: boolean;
    mode: DiagramMode;
    toggleEditing: () => void;
}

const defaultDiagramContext: IDiagramContext = {
    ast: undefined,
    changeMode: () => {
        // do nothing
    },
    editingEnabled: false,
    mode: DiagramMode.ACTION,
    toggleEditing: () => {
        // do nothing
    },
};

export const DiagramContext = React.createContext(defaultDiagramContext);
