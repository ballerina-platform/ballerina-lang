import * as Swagger from "openapi3-ts";
import * as React from "react";

import OpenApiInfo from "./components/info/info";

export interface OpenApiProps {
    openApiJson: Swagger.OpenAPIObject;
}

class OpenApiVisualizer extends React.Component<OpenApiProps, any> {
    constructor(props: OpenApiProps) {
        super(props);
    }

    public render() {
        const { openApiJson } = this.props;
        return (
            <OpenApiInfo info={openApiJson.info} />
        );
    }
}

export default OpenApiVisualizer;
