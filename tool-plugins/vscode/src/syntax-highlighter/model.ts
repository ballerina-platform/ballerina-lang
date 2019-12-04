import { Range } from "vscode";

export interface SemanticHighlightingInformation{
    line: number;
    token: string;
}

  export interface HighlightToken {
    scope: number;
    range: Range;
}

export interface SemanticHighlightingCapabilities{
    semanticHighlighting: boolean;
}

export interface SemanticHighlighting{
    scopes: string[][];
}