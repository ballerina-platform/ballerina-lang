import * as Swagger from "openapi3-ts";
import * as React from "react";

export interface OpenApiParameterListProps {
    parameterList: Swagger.ParameterObject[];
}

class OpenApiParameterList extends React.Component<OpenApiParameterListProps, any> {
    constructor(props: OpenApiParameterListProps) {
        super(props);
    }

    public render() {
        const { parameterList } = this.props;

        return (
            <div>
                    {parameterList.map((param: Swagger.ParameterObject, index: number) => {
                        return (
                            <div>{param.name}</div>
                        );
                    })}
            </div>
        );
    }
}

export default OpenApiParameterList;
