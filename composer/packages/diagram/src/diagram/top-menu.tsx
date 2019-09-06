import React from "react";
import { Dropdown, DropdownItemProps, Grid, Icon, Label, Popup} from "semantic-ui-react";
import { DiagramMode } from "./diagram-context";

export interface TopMenuProps {
    modes: Array<{
        text: string;
        type: DiagramMode;
    }>;
    handleModeChange: (e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) => void;
    handleBackClick: (e: React.MouseEvent) => void;
    handleFitClick: (e: React.MouseEvent) => void;
    handleZoomIn: (e: React.MouseEvent) => void;
    handleZoomOut: (e: React.MouseEvent) => void;
    selectedModeText: string;
    openedState: boolean;
    handleOpened: (e: React.MouseEvent) => void;
    handleClosed: (e: React.MouseEvent) => void;
    handleReset: (e: React.MouseEvent) => void;
    handleDepthSelect: (depth: number) => void;
    maxInvocationDepth: number;
    reachedInvocationDepth: number;
    zoomFactor: number;
}

export const TopMenu = (props: TopMenuProps) => {
    const {
        modes,
        handleModeChange,
        selectedModeText,
        handleFitClick,
        handleZoomIn,
        handleZoomOut,
        handleOpened,
        handleClosed,
        handleReset,
        handleDepthSelect,
        openedState,
        zoomFactor = 1,
        maxInvocationDepth,
        reachedInvocationDepth,
    } = props;

    return (
        <div>{
            !openedState ?
            <div>
                <div onClick={handleOpened} className="menu-icon">
                    <Icon className="fw fw-search" />
                </div>
                <div onClick={handleOpened} className="status-wrapper">
                    Zoom : <Label> {`${Math.floor(zoomFactor * 100)}%`} </Label>
                    Depth : <Label>{maxInvocationDepth === -1 ?
                        reachedInvocationDepth : maxInvocationDepth.toString()}</Label>
                    Design : <Label>{selectedModeText}</Label>
                </div>
            </div> :
            <Grid className="menu-container">
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Zoom
                        </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Popup
                            trigger={<Icon onClick={handleZoomOut} className="fw fw-minus"/>}
                            content="Zoom out"
                            position="top center"
                            size="small"
                            inverted
                            />
                        <Label className="menu-dropdown-small"> {`${Math.floor(zoomFactor * 100)}%`} </Label>
                        <Popup
                            trigger={<Icon onClick={handleZoomIn} className="fw fw-add"/>}
                            content="Zoom in"
                            position="top center"
                            size="small"
                            inverted
                            />
                        <Popup
                            trigger={<Icon onClick={handleFitClick} className="fw fw-fit-to-screen"/>}
                            content="Fit to Screen"
                            position="top center"
                            size="small"
                            inverted
                            />
                    </Grid.Column>
                    <Grid.Column className="menu-control" width={3}>
                        <Popup
                            trigger={<Icon onClick={handleReset} className="fw fw-refresh"/>}
                            content="Reset"
                            position="top center"
                            size="small"
                            inverted
                        />
                        <Popup
                            trigger={<Icon onClick={handleClosed} className="fw fw-uncheck"/>}
                            content="Close"
                            position="top center"
                            size="small"
                            inverted
                        />
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Depth
                    </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Dropdown
                            text={maxInvocationDepth === -1 ?
                                reachedInvocationDepth.toString() : maxInvocationDepth.toString()}
                            className="menu-dropdown-mid"
                        >
                            <Dropdown.Menu>
                                {
                                    (() => {
                                        const items = [];

                                        for (let i = 0; i < reachedInvocationDepth + 1; i++) {
                                            items.push(<Dropdown.Item onClick={() => handleDepthSelect(i)} text={i} />);
                                        }

                                        return items;
                                    })()
                                }
                            </Dropdown.Menu>
                        </Dropdown>
                    </Grid.Column>
                    <Grid.Column width={3} />
                </Grid.Row>
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Design
                        </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Dropdown className="menu-dropdown-mid " text={selectedModeText}>
                            <Dropdown.Menu>
                                {
                                    modes.map((mode) => (
                                        <Dropdown.Item
                                            text={mode.text}
                                            value={mode.type}
                                            onClick={handleModeChange}
                                            data={{
                                                text: mode.text,
                                                type: mode.type,
                                            }}
                                        />
                                    ))
                                }
                            </Dropdown.Menu>
                        </Dropdown>
                    </Grid.Column>
                    <Grid.Column width={3} />
                </Grid.Row>
            </Grid>
        }
        </div>
    );
};
