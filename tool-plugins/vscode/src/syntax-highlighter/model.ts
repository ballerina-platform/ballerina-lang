import { Range } from "vscode";
import { VersionedTextDocumentIdentifier } from 'vscode-languageserver-types';

export interface SemanticHighlightingInformation{
    line: number;
    token: string;
}

export interface SemanticHighlightingParams{
    textDocument: VersionedTextDocumentIdentifier;
    lines: SemanticHighlightingInformation[];
}

interface SemanticHighlightingToken {
    character: number;
    length: number;
    scope: number;
  }

  export interface SemanticHighlightingLine {
    line: number;
    tokens: SemanticHighlightingToken[];
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