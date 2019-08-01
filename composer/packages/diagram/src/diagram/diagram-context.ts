import { ASTNode } from "@ballerina/ast-model";
import { IBallerinaLangClient } from "@ballerina/lang-service";
import React from "react";

export enum DiagramMode { INTERACTION, STATEMENT, ACTION, DEFAULT }

export interface IDiagramContext {
    ast?: ASTNode;
    hasSyntaxErrors: boolean;
    editingEnabled: boolean;
    mode: DiagramMode;
    zoomLevel: number;
    langClient?: IBallerinaLangClient;
    overlayGroupRef?: React.RefObject<SVGGElement>;
    containerRef?: React.RefObject<HTMLDivElement>;
    docUri?: string;
    update: () => void;
}

const defaultDiagramContext: IDiagramContext = {
    editingEnabled: false,
    hasSyntaxErrors: false,
    mode: DiagramMode.ACTION,
    update: () => { /** no op */},
    zoomLevel: 1,
};

export const DiagramContext = React.createContext(defaultDiagramContext);
