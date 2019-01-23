import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Header } from "semantic-ui-react";

import { OpenApiContext, OpenApiContextConsumer } from "../context/open-api-context";

import InlineEdit from "../utils/inline-edit";
import OpenApiContact from "./contact";
import OpenApiLicense from "./license";

export interface OpenApiInfoProps {
    info: Swagger.InfoObject;
}

class OpenApiInfo extends React.Component<OpenApiInfoProps, any> {
    constructor(props: OpenApiInfoProps) {
        super(props);
    }

    public render() {
        const { info } = this.props;
        return(
            <OpenApiContextConsumer>
                {(context: OpenApiContext | null) => {
                    return (
                        <React.Fragment>
                            <Header as="h1">
                                {info.title}
                                <Header.Subheader>
                                    <InlineEdit
                                        changeAttribute={{key: "info.version", changeValue: ""}}
                                        text={info.version}
                                        changeModel={context!.openApiJson}
                                        placeholderText="Please include an appropriate description."
                                        onInlineValueChange={context!.onInlineValueChange}
                                    />
                                </Header.Subheader>
                            </Header>
                            <InlineEdit
                                changeAttribute={{key: "info.description", changeValue: ""}}
                                text={info.description}
                                isMarkdown
                                isParagraph
                                changeModel={context!.openApiJson}
                                placeholderText="Please include an appropriate description."
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                            <InlineEdit
                                text={{
                                    link: info.termsOfService,
                                    type: "tos",
                                    urlText: "Terms of service"
                                }}
                                changeAttribute={{key: "info.termsOfService", changeValue: ""}}
                                changeModel={context!.openApiJson}
                                placeholderText="Please include terms of service link."
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                            <OpenApiContact contact={info.contact} />
                            <OpenApiLicense license={info.license} />
                        </React.Fragment>
                    );
                }}
            </OpenApiContextConsumer>
        );
    }
}

export default OpenApiInfo;
