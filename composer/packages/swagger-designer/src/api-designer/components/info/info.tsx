import * as Swagger from "openapi3-ts";
import * as React from "react";

import { Header, Segment } from "semantic-ui-react";

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
            <React.Fragment>
                <Header as="h1">
                    {info.title}
                    <Header.Subheader>{info.version}</Header.Subheader>
                </Header>
                {info.description &&
                    <InlineEdit
                        text={info.description}
                        isMarkdown
                        isParagraph
                        placeholderText="Please include an appropriate description."
                    />
                }
                {info.termsOfService &&
                    <InlineEdit
                        text={{
                            link: info.termsOfService,
                            urlText: "Terms of service"
                        }}
                    />
                }
                {info.contact &&
                    <OpenApiContact contact={info.contact} />
                }
                {info.license &&
                    <OpenApiLicense license={info.license} />
                }
            </React.Fragment>
        );
    }
}

export default OpenApiInfo;
