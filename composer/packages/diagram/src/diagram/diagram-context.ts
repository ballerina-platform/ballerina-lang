import { ASTNode } from "@ballerina/ast-model";
import React from "react";

export enum DiagramMode { ACTION, DEFAULT }

export interface IDiagramContext {
    ast: ASTNode | undefined;
    changeMode: (newMode: DiagramMode) => void;
    editingEnabled: boolean;
    mode: DiagramMode;
    toggleEditing: () => void;
    zoomIn: () => void;
    zoomOut: () => void;
    zoomFit: () => void;
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
    zoomFit: () => {
        // do nothing
    },
    zoomIn: () => {
        // do nothing
    },
    zoomOut: () => {
        // do nothing
    }
};

export const DiagramContext = React.createContext(defaultDiagramContext);
