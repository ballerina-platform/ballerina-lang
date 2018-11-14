// tslint:disable-next-line:no-submodule-imports
import * as Ballerina from "@ballerina/ast-model/lib/models";
import * as React from "react";

export const Function = (props: {model: Ballerina.Function}) => {
    const { model } = props;

    return <div>{model.name.value}</div>;
};
