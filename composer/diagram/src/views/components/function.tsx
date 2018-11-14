import * as React from "react";
import * as Ballerina from "../../models";

export const Function = (props: {model: Ballerina.Function}) => {
    const { model } = props;

    return <div>{model.name.value}</div>;
};
