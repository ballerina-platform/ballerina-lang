import {
    Assignment, ASTKindChecker, ASTUtil, Break,
    CompoundAssignment, ExpressionStatement, Function as BallerinaFunction, Panic, VariableDef
} from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { StmntViewState } from "../../view-model";
import { ActionInvocation } from "./action-invocation";
import { ExpandedFunction, FunctionExpander } from "./expanded-function";
import { HiddenBlock } from "./hidden-block";
import { ReturnActionInvocation } from "./return-action-invocation";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Statement: React.StatelessComponent<{
    model: ExpressionStatement | VariableDef | Assignment | CompoundAssignment | Panic | Break
}> = ({
    model
}) => {
        const viewState: StmntViewState = model.viewState;
        const label = (/^worker /.test(viewState.bBox.label)) ? "" : viewState.bBox.label;
        let fullText = (model) ? ASTUtil.genSource(model) : label;
        fullText = fullText.replace(/\/\/.*$/gm, "");

        let shouldDrawExpander = false;
        let expandedPosition;

        if (ASTKindChecker.isExpressionStatement(model) && ASTKindChecker.isInvocation(model.expression)) {
            shouldDrawExpander = true;
        }

        const expanderProps = {
            position: model.position,
            viewState,
            x: viewState.bBox.x + config.statement.padding.left,
            y: viewState.bBox.y + (viewState.bBox.h / 2) + 3,
        };

        const statementProps = {
            className: "statement",
            fullText,
            target: model,
            text: label,
            x: expanderProps.x + 20,
            y: viewState.bBox.y + (viewState.bBox.h / 2),
        };

        const expandedProps = {
            bBox: {
                h: viewState.bBox.h,
                w: viewState.bBox.w,
                x: viewState.bBox.x + config.statement.expanded.offset,
                y: viewState.bBox.y,
            }
        };

        return (
            <g>
                {viewState.hiddenBlock &&
                    <HiddenBlock model={model} />
                }
                {!viewState.hiddenBlock &&
                    <g className="statement">
                        {viewState.isAction && !viewState.isReturn
                            && <ActionInvocation
                                model={viewState}
                                action={viewState.bBox.label}
                                astModel={model}
                            />}
                        {viewState.isAction && viewState.isReturn
                            && <ReturnActionInvocation
                                model={viewState}
                                action={viewState.bBox.label}
                                astModel={model}
                            />}
                        {!viewState.isAction && !viewState.hiddenBlock && !viewState.expanded &&
                            <SourceLinkedLabel {...statementProps} />
                        }
                        {!viewState.isAction && !viewState.hiddenBlock && !viewState.expanded &&
                            <FunctionExpander {...expanderProps} />
                        }
                        {!viewState.isAction && !viewState.hiddenBlock &&
                            viewState.expanded && viewState.expandedSubTree &&
                            <ExpandedFunction model={viewState.expandedSubTree as BallerinaFunction}
                                docUri={viewState.expandedSubTreeDocUri} bBox={expandedProps.bBox} />
                        }
                    </g>
                }
            </g>
        );
    };
