import { ASTKindChecker, ASTNode, ASTUtil,
    Function as BallerinaFunction, NodePosition, VariableDef, VisibleEndpoint } from "@ballerina/ast-model";
import { BallerinaAST, IBallerinaLangClient } from "@ballerina/lang-service";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { DiagramContext } from "../../diagram/index";
import { getCodePoint } from "../../utils";
import { BlockViewState } from "../../view-model/block";
import { ExpandContext } from "../../view-model/expand-context";
import { FunctionViewState, StmntViewState, ViewState } from "../../view-model/index";
import { Block } from "./block";
import { LifeLine } from "./life-line";
import { Worker } from "./worker";

const config: DiagramConfig = DiagramUtils.getConfig();

interface ExpandedFunctionProps {
    model: BallerinaFunction;
    docUri: string;
    bBox: any;
    onClose: () => void;
}

export const ExpandedFunction: React.SFC<ExpandedFunctionProps> = ({ model, docUri, bBox, onClose }) => {
    if (!model.body) {
        return null;
    }

    const expandedFnViewState = model.body.viewState as BlockViewState;
    const expandedFnBbox = expandedFnViewState.bBox;

    return (
        <DiagramContext.Consumer>
            {(context) => {
                if (!model.body) {
                    return null;
                }

                const onClickClose = () => {
                    onClose();
                    context.update();
                };

                const workers = model.body.statements.filter((statement) => ASTUtil.isWorker(statement));

                const lifeLine = {
                    x1: expandedFnBbox.x,
                    x2: expandedFnBbox.x,
                    y1: expandedFnBbox.y,
                    y2: expandedFnBbox.y + expandedFnBbox.h,
                };

                // If workers present we want to increase the lifeline length
                // but if the body is empty no need to do it as by default the body height at least one
                // statement height
                // A worker is actually 2 statements so have to multiply by 2
                if (!(model.viewState as FunctionViewState).implicitReturn.hidden
                    && workers.length > 0 && (model.body.statements.length - 2 * workers.length) > 0) {
                    lifeLine.y2 += (1 * config.statement.height);
                }

                return (
                    <g className="expanded-func">
                        <rect className="frame"
                            x={bBox.x - config.statement.expanded.margin}
                            y={bBox.y + config.statement.height / 2}
                            width={bBox.w + config.statement.expanded.margin}
                            height={bBox.h - config.statement.expanded.footer - (config.statement.height / 2)}/>
                        <rect className="name-background"
                            x={bBox.statement.x - config.statement.expanded.labelGutter}
                            y={bBox.y}
                            width={bBox.statement.textWidth + (2 * config.statement.expanded.labelGutter)}
                            height={config.statement.height}/>
                        <text className="func-name"
                            x={bBox.statement.x}
                            y={bBox.statement.y + (config.statement.height / 2)}>
                            {bBox.statement.text}
                        </text>
                        <text className="collapser"
                            x={bBox.statement.x + bBox.w - 30}
                            y={bBox.statement.y + config.statement.height + 5}
                            onClick={onClickClose}>
                            {getCodePoint("up")}
                        </text>
                        <line className="life-line" x1={bBox.x}
                            x2={expandedFnBbox.x}
                            y1={expandedFnBbox.y}
                            y2={expandedFnBbox.y} />
                        <line className="life-line" { ...lifeLine } />
                        { /* Override the docUri context value and always disable editing */ }
                        <DiagramContext.Provider value={{ ...context, docUri, editingEnabled: false }}>
                            {workers.map((worker) => {
                                const client = new ViewState();
                                client.bBox.w = 0;
                                client.bBox.x = expandedFnBbox.x;
                                return <Worker
                                    model={ worker as VariableDef }
                                    startY={ expandedFnBbox.y +
                                        (model.viewState as FunctionViewState).defaultWorker.initHeight -
                                        2 * config.statement.height }
                                    client={client} />;
                            })}
                            <Block model={model.body} />
                            {model.VisibleEndpoints && model.VisibleEndpoints
                                .filter((element) => element.viewState.visible)
                                .map((element: VisibleEndpoint) => {
                                    return <LifeLine title={element.name} icon="endpoint"
                                        model={element.viewState.bBox}
                                        astModel={element.caller ? model.parameters[0] : element } />;
                                })
                            }
                        </DiagramContext.Provider>
                    </g>
                );
            }}
        </DiagramContext.Consumer>
    );
};

interface FunctionExpanderProps {
    statementViewState: StmntViewState;
    position: NodePosition;
    expandContext: ExpandContext;
}

export const FunctionExpander: React.SFC<FunctionExpanderProps> = (
    { statementViewState, position, expandContext }) => {

    const x = statementViewState.bBox.x + statementViewState.bBox.labelWidth + 10;
    const y = statementViewState.bBox.y + (statementViewState.bBox.h / 2) + 2;
    return (
        <DiagramContext.Consumer>
            {({ langClient, docUri, update }) => (
                <g className="expander">
                    <circle className="circle" cx={x + 7} cy={y - 2} r={10}/>
                    <text x={x} y={y}
                        onClick={getExpandFunctionHandler(
                            langClient, docUri, position, expandContext, update)}>
                        {getCodePoint("down")}
                    </text>
                </g>
            )}
        </DiagramContext.Consumer>
    );
};

function getExpandFunctionHandler(
    langClient: IBallerinaLangClient | undefined, docUri: string | undefined,
    position: NodePosition, expandContext: ExpandContext, update: () => void) {

    return async () => {
        if (!langClient || !docUri) {
            return;
        }
        const res = await langClient.getDefinitionPosition({
            position: {
                character: position.startColumn - 1,
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

        expandContext.expandedSubTree = defTree;
        expandContext.expandedSubTreeDocUri = res.uri;
        update();
    };
}

function findDefinitionSubTree(position: NodePosition, ast: BallerinaAST) {
    return ast.topLevelNodes.find((balNode) => {
        const tlnode = balNode as ASTNode;
        if (!ASTKindChecker.isFunction(tlnode)) {
            return false;
        }

        const node = tlnode.name;

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
