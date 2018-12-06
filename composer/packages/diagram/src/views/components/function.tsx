import {
    ASTUtil, Function as FunctionNode, Lambda, Variable,
    VariableDef, VisibleEndpoint
} from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { FunctionViewState } from "../../view-model/index";
import { WorkerViewState } from "../../view-model/worker";
import { AddWorkerOrEndpointMenu } from "./add-worker-or-endpoint-menu";
import { Block } from "./block";
import { LifeLine } from "./life-line";
import { Panel } from "./panel";
import { StartInvocation } from "./start-invocation";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Function = (props: { model: FunctionNode }) => {
    const { model } = props;
    const viewState: FunctionViewState = model.viewState;
    if (model.lambda || model.body === undefined) {return <g/>; }

    return (
        <Panel model={viewState} title={model.name.value} icon={viewState.icon}>
            {!model.resource &&
                <LifeLine title="Client" icon="client" model={viewState.client.bBox} />}
            <LifeLine title="Default" icon="worker" model={viewState.defaultWorker.lifeline.bBox}
                astModel={model} />
            {model.body!.statements.filter((statement) => ASTUtil.isWorker(statement)).map((worker) => {
                const workerViewState: WorkerViewState = worker.viewState;
                const variable: Variable = ((worker as VariableDef).variable as Variable);
                const lambda: Lambda = (variable.initialExpression as Lambda);
                const functionNode = lambda.functionNode;
                return <g>
                    <LifeLine title={workerViewState.name} icon="worker"
                        model={workerViewState.lifeline.bBox} astModel={worker} />
                    {functionNode.body && <Block model={functionNode.body} />}
                </g>;
            })}
            <StartInvocation client={viewState.client} worker={viewState.defaultWorker.lifeline}
                y={viewState.defaultWorker.bBox.y + config.lifeLine.header.height} label="" />
            {model.body && <Block model={model.body} />}
            {model.VisibleEndpoints && model.VisibleEndpoints
                .filter((element) => element.viewState.visible)
                .map((element: VisibleEndpoint) => {
                    return <LifeLine title={element.name} icon="endpoint"
                                model={element.viewState.bBox} astModel={element} />;
                })
            }
            <AddWorkerOrEndpointMenu
                triggerPosition={viewState.menuTrigger}
                onAddEndpoint={(epDef: any) => {
                    // todo
                    // tslint:disable-next-line:no-console
                    console.log("Selected EP: " + JSON.stringify(epDef));
                }}
                onAddWorker={() => {
                    // todo
                }}
            />
        </Panel>);
};
