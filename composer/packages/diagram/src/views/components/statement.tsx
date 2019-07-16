import {
    Assignment, ASTUtil, Break,
    CompoundAssignment, ExpressionStatement, Panic, VariableDef
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

        const statementProps = {
            className: "statement",
            fullText,
            target: model,
            text: label,
            x: viewState.bBox.x + config.statement.padding.left,
            y: viewState.bBox.y + (viewState.bBox.h / 2),
        };

        let expander;
        let expandedFunction;

        if (viewState.expandContext && viewState.expandContext.collapsed) {
            const expanderProps = {
                expandContext: viewState.expandContext,
                position: viewState.expandContext.expandableNode.position!,
                statement: viewState.expandContext.expandableNode,
                statementViewState: viewState,
            };

            expander = <FunctionExpander {...expanderProps}/>;
        }

        if (viewState.expandContext && !viewState.expandContext.collapsed) {
            if (!viewState.isAction && !viewState.hiddenBlock) {
                const expandedBBox = {
                    h: viewState.bBox.h,
                    statement: {
                        text: viewState.expandContext.labelText,
                        textWidth: viewState.expandContext.labelWidth,
                        x: viewState.bBox.x + config.statement.padding.left,
                        y: viewState.bBox.y,
                    },
                    w: viewState.bBox.w,
                    x: viewState.bBox.x,
                    y: viewState.bBox.y,
                };

                const onClose = () => {
                    if (viewState.expandContext) {
                        viewState.expandContext.collapsed = true;
                        viewState.expandContext.skipDepthCheck = true;
                    }
                };

                expandedFunction = <ExpandedFunction
                    model={viewState.expandContext.expandedSubTree}
                    docUri={viewState.expandContext.expandedSubTreeDocUri}
                    bBox={expandedBBox}
                    onClose={onClose}
                />;
            }
        }

        return (
            <g>
                {viewState.hiddenBlock &&
                    <HiddenBlock model={model} />
                }
                {(!viewState.hiddenBlock && !viewState.hidden) &&
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
                        {!viewState.isAction && !expandedFunction &&
                            <SourceLinkedLabel {...statementProps} />
                        }
                        { expander }
                        { expandedFunction }
                    </g>
                }
            </g>
        );
    };
