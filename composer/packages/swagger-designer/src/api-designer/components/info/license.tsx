import * as Swagger from "openapi3-ts";
import * as React from "react";

import InlineEdit from "../utils/inline-edit";

export interface OpenApiLicenseProps {
    license?: Swagger.LicenseObject;
}

class OpenApiLicense extends React.Component<OpenApiLicenseProps, any> {
    constructor(props: OpenApiLicenseProps) {
        super(props);
    }

    public render() {
        const { license } = this.props;

        return (
            <InlineEdit text={license} placeholderText="Please include appropriate license details" />
        );
    }
}

export default OpenApiLicense;
