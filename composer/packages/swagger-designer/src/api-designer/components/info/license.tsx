import * as Swagger from "openapi3-ts";
import * as React from "react";

import { OpenApiContext, OpenApiContextConsumer } from "../context/open-api-context";

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
            <OpenApiContextConsumer>
                {(context: OpenApiContext | null) => {
                    if (license) {
                        return (
                            <InlineEdit
                                text={license}
                                changeModel={context!.openApiJson}
                                changeAttribute={{key: "info.license", changeValue: ""}}
                                placeholderText="+ License info"
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                        );
                    } else {
                        return (
                            <InlineEdit
                                text={{
                                    name: "",
                                    url: "",
                                }}
                                changeModel={context!.openApiJson}
                                changeAttribute={{key: "info.license", changeValue: ""}}
                                placeholderText="+ License info"
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                        );
                    }
                }}
            </OpenApiContextConsumer>
        );
    }
}

export default OpenApiLicense;
