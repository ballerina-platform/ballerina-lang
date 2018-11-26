import { BallerinaEndpoint } from "@ballerina/lang-service";
import React from "react";
import { Dropdown, DropdownItemProps, Modal } from "semantic-ui-react";
import { DiagramContext, IDiagramContext } from "../../diagram/index";

export class EndpointSearchDialog extends React.Component<{
    show: boolean,
    onClose: () => void,
    onEndpointSelect?: (endpointDef: any) => void,
},
{
    endpoints: BallerinaEndpoint[]
}> {

    public static context = DiagramContext;

    public state = {
        endpoints: [
            {
                name: "Client",
                packageName: "http"
            },
            {
                name: "Client",
                packageName: "jms"
            }
        ]
    };

    private dropDownRef = React.createRef<any>();

    public componentDidMount() {
        const context = this.context as IDiagramContext;
        if (context && context.langClient) {
            context.langClient.getEndpoints()
                .then((endpoints) => {
                    this.setState({
                        endpoints
                    });
                });
        }
    }

    public render() {
        const { show, onClose } = this.props;
        const options: DropdownItemProps[] = this.state.endpoints.map((ep: BallerinaEndpoint) => ({
            text: ep.packageName + ":" + ep.name,
            value: ep.packageName + ":" + ep.name
        }));
        return  <Modal
                open={show}
                onClose={onClose}
                basic
                size="small"
                className="endpoint-search-dialog"
            >
                <Dropdown
                    clearable
                    fluid
                    search
                    selection
                    searchInput={{ autoFocus: true, autoComplete: "on" }}
                    placeholder="Select Endpoint Type"
                    defaultOpen
                    options={options}
                    ref={this.dropDownRef}
                />
            </Modal>;
    }
}
