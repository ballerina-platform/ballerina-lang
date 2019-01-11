import { BallerinaEndpoint } from "@ballerina/lang-service";
import React from "react";
import { Dropdown, DropdownItemProps, Header, Modal, Transition } from "semantic-ui-react";
import { DiagramContext, IDiagramContext } from "../../diagram/diagram-context";

const getEPLabel = (ep: BallerinaEndpoint) => ep.packageName + ":" + ep.name;
export class EndpointSearchDialog extends React.Component<{
    show: boolean,
    onClose: () => void,
    onEndpointSelect?: (endpointDef: BallerinaEndpoint) => void,
},
{
    endpoints: BallerinaEndpoint[]
}> {

    public static contextType = DiagramContext;

    public state = {
        endpoints: []
    };

    private lastUsedEndpoint: BallerinaEndpoint | undefined;

    public componentDidMount() {
        const context = this.context as IDiagramContext;
        if (context && context.langClient) {
            context.langClient.getEndpoints()
                .then((endpoints) => {
                    if (!this.lastUsedEndpoint) {
                        // set http client selected by default
                        const httpClientEP = endpoints.find((ep) => ep.packageName === "http"
                                        && ep.name === "Client");
                        this.lastUsedEndpoint = httpClientEP;
                    }
                    this.setState({
                        endpoints
                    });
                });
        }
    }

    public render() {
        const { show, onClose, onEndpointSelect } = this.props;
        const options: DropdownItemProps[] = this.state.endpoints.map((ep: BallerinaEndpoint) => ({
            active: ep === this.lastUsedEndpoint,
            data: ep,
            onClick: (event: React.MouseEvent<HTMLDivElement>, props: DropdownItemProps) => {
                if (onEndpointSelect) {
                    onEndpointSelect(props.data);
                    this.lastUsedEndpoint = props.data;
                }
                onClose();
            },
            selected: ep === this.lastUsedEndpoint,
            text: getEPLabel(ep)
        }));
        const containerRef = (this.context as IDiagramContext).containerRef;
        return <Transition visible={show} animation="fade" duration={500}>
                <Modal
                    open={show}
                    onClose={onClose}
                    basic
                    size="small"
                    className="endpoint-search-dialog"
                    dimmer
                    mountNode={containerRef ? containerRef.current : undefined}
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
                    />
                </Modal>
            </Transition>;
    }
}
