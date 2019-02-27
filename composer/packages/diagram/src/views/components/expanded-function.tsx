import { ASTNode, ASTUtil, Function as BallerinaFunction, NodePosition } from "@ballerina/ast-model";
import { getCodePoint } from "@ballerina/font";
import { BallerinaAST, IBallerinaLangClient } from "@ballerina/lang-service";
import * as React from "react";
import { DiagramContext } from "../../diagram/index";
import { StmntViewState } from "../../view-model/index";
import { visitor as initVisitor } from "../../visitors/init-visitor";
import { Block } from "./block";

interface ExpandedFunctionProps {
    model: BallerinaFunction;
    docUri: string;
    bBox: any;
}

export const ExpandedFunction: React.SFC<ExpandedFunctionProps> = ({ model, docUri, bBox }) => {
    return (
        <DiagramContext.Consumer>
            {(context) => {
                if (!model.body) {
                    return null;
                }
                return (
                    // Override the docUri context value
                    <g>
                        <g className="life-line">
                            <line x1={bBox.x} x2={bBox.x} y1={bBox.y} y2={bBox.y + bBox.h} />
                        </g>
                        <DiagramContext.Provider value={{ ...context, docUri }}>
                            <Block model={model.body} />
                        </DiagramContext.Provider>
                    </g>
                );
            }}
        </DiagramContext.Consumer>
    );
};

interface FunctionExpanderProps {
    x: number;
    y: number;
    position: NodePosition;
    viewState: StmntViewState;
}

export const FunctionExpander: React.SFC<FunctionExpanderProps> = ({ x, y, position, viewState }) => {
    return (
        <DiagramContext.Consumer>
            {({ langClient, docUri, update }) => (
                <text x={x} y={y} onClick={getExpandFunctionHandler(langClient, docUri, position, viewState, update)}>
                    {getCodePoint("down")}
                </text>
            )}
        </DiagramContext.Consumer>
    );
};

function getExpandFunctionHandler(
    langClient: IBallerinaLangClient | undefined, docUri: string | undefined,
    position: NodePosition, viewState: StmntViewState, update: () => void) {

    return async (e: React.MouseEvent<SVGTextElement>) => {
        if (!langClient || !docUri) {
            return;
        }

        const res = await langClient.getDefinitionPosition({
            position: {
                character: position.startColumn,
                line: position.startLine - 1,
            },
            textDocument: {
                uri: docUri,
            },
        });

        const astRes = await langClient.getAST({
            documentIdentifier: {
                uri: res.uri
            }
        });

        const subTree = findDefinitionSubTree({
            endColumn: res.range.end.character + 1,
            endLine: res.range.end.line + 1,
            startColumn: res.range.start.character + 1,
            startLine: res.range.start.line + 1,
        }, astRes.ast);

        const defTree = subTree as BallerinaFunction;

        ASTUtil.traversNode(defTree, initVisitor);

        viewState.expanded = true;
        viewState.expandedSubTree = defTree;
        viewState.expandedSubTreeDocUri = res.uri;
        update();
    };
}

function findDefinitionSubTree(position: NodePosition, ast: BallerinaAST) {
    return ast.topLevelNodes.find((balNode) => {
        const node = balNode as ASTNode;
        if (!node.position) {
            return false;
        }

        if (node.position.startColumn === position.startColumn &&
            node.position.endColumn === position.endColumn &&
            node.position.startLine === position.startLine &&
            node.position.endLine === position.endLine) {

            return true;
        }

        return false;
    });
}
