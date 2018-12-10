
import { ASTNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { StmntViewState } from "../../view-model";
import { ActionInvocation } from "./action-invocation";
import { HiddenBlock } from "./hidden-block";
import { ReturnActionInvocation } from "./return-action-invocation";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Statement: React.StatelessComponent<{
    model: ASTNode
}> = ({
    model
}) => {
        const viewState: StmntViewState = model.viewState;
        const label = (/^worker /.test(viewState.bBox.label)) ? "" : viewState.bBox.label;

        const statementProps = {
            className: "statement",
            target: model,
            text: label,
            x: viewState.bBox.x + config.statement.padding.left,
            y: viewState.bBox.y + (viewState.bBox.h / 2)
        };

        return (
            <g>
                { viewState.hiddenBlock &&
                    <HiddenBlock model={model} />
                }
                { !viewState.hiddenBlock &&
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
                    {!viewState.isAction && !viewState.hiddenBlock  &&
                        <SourceLinkedLabel {...statementProps}  />
                    }
                </g>
                }
            </g>
            );
    };
