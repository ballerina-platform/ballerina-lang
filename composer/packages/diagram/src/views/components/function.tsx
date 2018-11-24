import { Function as BallerinaFunction } from "@ballerina/ast-model";
import * as React from "react";
import { FunctionViewState } from "../../view-model/index";
import { LifeLine } from "./life-line";
import { Panel } from "./panel";

export const Function = (props: {model: BallerinaFunction}) => {
    const { model } = props;
    const viewState: FunctionViewState = model.viewState;

    return (
        <Panel model={viewState} title={model.name.value} icon="function">
            <LifeLine title="Client" icon="client" model={viewState.client}/>
        </Panel>);
};
