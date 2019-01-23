import * as Swagger from "openapi3-ts";
import * as React from "react";

import { OpenApiContext, OpenApiContextConsumer } from "../context/open-api-context";

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
            <OpenApiContextConsumer>
                {(context: OpenApiContext | null) => {
                    if (contact) {
                        return (
                            <InlineEdit
                                text={contact}
                                changeModel={context!.openApiJson}
                                changeAttribute={{key: "info.contact", changeValue: ""}}
                                placeholderText="Please include apropriate contact info"
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                        );
                    } else {
                        return (
                            <InlineEdit
                                text={{
                                    email: "",
                                    name: "",
                                    url: "",
                                }}
                                changeModel={context!.openApiJson}
                                changeAttribute={{key: "info.contact", changeValue: ""}}
                                placeholderText="Please include apropriate contact info"
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                        );
                    }
                }}
            </OpenApiContextConsumer>
        );
    }
}

export default OpenApiContact;
