import { BallerinaEndpoint } from "@ballerina/lang-service";
import React from "react";
import { Dropdown, DropdownItemProps, Header, Modal, Transition } from "semantic-ui-react";
import { DiagramContext, IDiagramContext } from "../../diagram/diagram-context";

export class EndpointSearchDialog extends React.Component<{
    show: boolean,
    onClose: () => void,
    onEndpointSelect?: (endpointDef: any) => void,
},
{
    endpoints: BallerinaEndpoint[]
}> {

    public static contextType = DiagramContext;

    public state = {
        endpoints: []
    };

    private lastUsedEndpoint: string | undefined;

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
        const { show, onClose, onEndpointSelect } = this.props;
        const options: DropdownItemProps[] = this.state.endpoints.map((ep: BallerinaEndpoint) => ({
            text: ep.packageName + ":" + ep.name,
            value: ep.packageName + ":" + ep.name
        }));
        return <Transition visible={show} animation="fade" duration={500}>
                <Modal
                    open={show}
                    onClose={onClose}
                    basic
                    size="small"
                    className="endpoint-search-dialog"
                    dimmer
                >
                    <Header as="h4">Select from available endpoints</Header>
                    <Dropdown
                        clearable
                        fluid
                        search
                        selection
                        searchInput={{ autoFocus: true, autoComplete: "on" }}
                        placeholder="Select Endpoint Type"
                        defaultOpen
                        options={options}
                        onChange={(evt, { value }) => {
                            if (onEndpointSelect) {
                                onEndpointSelect(value);
                                this.lastUsedEndpoint = value as string;
                            }
                            onClose();
                        }}
                        defaultValue={this.lastUsedEndpoint}
                    />
                </Modal>
            </Transition>;
    }
}
