import { ASTKindChecker, BlockFunctionBody, Lambda, VariableDef, VisibleEndpoint } from "@ballerina/ast-model";
import * as React from "react";
import { ViewState } from "../../view-model/index";
import { WorkerViewState } from "../../view-model/worker";
import { Block } from "./block";
import { LifeLine } from "./life-line";
import { StartInvocation } from "./start-invocation";

interface WorkerProps {
    model: VariableDef;
    startY: number;
    client: ViewState;
}

export const Worker: React.SFC<WorkerProps> = ({ model, startY, client }) => {
    const workerViewState: WorkerViewState = model.viewState;
    const variable = model.variable;
    const lambda: Lambda = variable.initialExpression as Lambda;
    const functionNode = lambda.functionNode;
    return <g>
        <StartInvocation client={client} worker={workerViewState.lifeline}
            y={startY}/>
        <LifeLine title={workerViewState.name} icon="worker"
            model={workerViewState.lifeline.bBox} astModel={model} />
        {functionNode.body && ASTKindChecker.isBlockFunctionBody(functionNode.body)
            && <Block model={functionNode.body as BlockFunctionBody}
            visibleEPFilter={(ep: VisibleEndpoint) => ep.isLocal} />}
    </g>;
};
