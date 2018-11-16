// tslint:disable-next-line:no-submodule-imports
import { Function as BallerinaFunction } from "@ballerina/ast-model";
import * as React from "react";

export const Function = (props: {model: BallerinaFunction}) => {
    const { model } = props;

    return <div>{model.name.value}</div>;
};
