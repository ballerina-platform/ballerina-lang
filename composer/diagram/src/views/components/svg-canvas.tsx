import * as React from "react";

export const SvgCanvas = (props: any) => {
    const { children } = props;

    return <svg
        className="ballerina-diagram"
        preserveAspectRatio="xMinYMin"
    >
        {children}
    </svg >;
};
