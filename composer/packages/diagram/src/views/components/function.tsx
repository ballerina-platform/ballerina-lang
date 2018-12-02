import { Function as FunctionNode, VisibleEndpoint } from "@ballerina/ast-model";
import * as React from "react";
import { FunctionViewState } from "../../view-model/index";
import { AddWorkerOrEndpointMenu } from "./add-worker-or-endpoint-menu";
import { Block } from "./block";
import { LifeLine } from "./life-line";
import { Panel } from "./panel";

export const Function = (props: {model: FunctionNode}) => {
    const { model } = props;
    const viewState: FunctionViewState = model.viewState;

    return (
        <Panel model={viewState} title={model.name.value} icon="function">
            <LifeLine title="Client" icon="client" model={viewState.client}/>
            <LifeLine title="Default" icon="worker" model={viewState.defaultWorker.lifeline}/>
            { model.body && <Block model={model.body} />}
            { model.VisibleEndpoints && model.VisibleEndpoints.map((element: VisibleEndpoint) => {
                if (element.viewState.visible) {
                    return <LifeLine title={element.name} icon="endpoint" model={element.viewState.bBox} />;
                }
            })}
            <AddWorkerOrEndpointMenu
                triggerPosition={viewState.menuTrigger}
                onAddEndpoint={(epDef: any) => {
                    // todo
                }}
                onAddWorker={() => {
                    // todo
                }}
            />
        </Panel>);
};
