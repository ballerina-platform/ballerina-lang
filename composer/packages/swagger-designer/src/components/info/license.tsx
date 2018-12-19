import * as Swagger from "openapi3-ts";
import * as React from "react";

export interface OpenApiLicenseProps {
    license?: Swagger.LicenseObject;
}

class OpenApiLicense extends React.Component<OpenApiLicenseProps, any> {
    constructor(props: OpenApiLicenseProps) {
        super(props);
    }

    public render() {
        return (
            <div></div>
        );
    }
}

export default OpenApiLicense;
