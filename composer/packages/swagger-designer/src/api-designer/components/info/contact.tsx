import * as Swagger from "openapi3-ts";
import * as React from "react";

import InlineEdit from "../utils/inline-edit";

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
            <InlineEdit text={contact} placeholderText="Please include apropriate contact info"/>
        );
    }
}

export default OpenApiContact;
