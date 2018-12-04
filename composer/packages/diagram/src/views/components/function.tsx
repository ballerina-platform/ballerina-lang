import { Function as FunctionNode, VisibleEndpoint } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { FunctionViewState } from "../../view-model/index";
import { AddWorkerOrEndpointMenu } from "./add-worker-or-endpoint-menu";
import { Block } from "./block";
import { LifeLine } from "./life-line";
import { Panel } from "./panel";
import { StartInvocation } from "./start-invocation";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Function = (props: { model: FunctionNode }) => {
    const { model } = props;
    const viewState: FunctionViewState = model.viewState;

    return (
        <Panel model={viewState} title={model.name.value} icon={viewState.icon}>
            {!model.resource &&
                <LifeLine title="Client" icon="client" model={viewState.client.bBox} />}
            <LifeLine title="Default" icon="worker" model={viewState.defaultWorker.lifeline.bBox} />
            <StartInvocation client={viewState.client} worker={viewState.defaultWorker.lifeline}
                y={viewState.defaultWorker.bBox.y + config.lifeLine.header.height} label="" />
            {model.body && <Block model={model.body} />}
            {model.VisibleEndpoints && model.VisibleEndpoints
                .filter((element) => element.viewState.visible)
                .map((element: VisibleEndpoint) => {
                    return <LifeLine title={element.name} icon="endpoint" model={element.viewState.bBox} />;
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
