import * as Swagger from "openapi3-ts";
import * as React from "react";

export interface OpenApiContactProps {
    contact?: Swagger.ContactObject;
}

class OpenApiContact extends React.Component<OpenApiContactProps, any> {
    constructor(props: OpenApiContactProps) {
        super(props);
    }

    public render() {
        const { contact } = this.props;
        return (
            <div></div>
        );
    }
}

export default OpenApiContact;
