import React from "react";
import { Button, Dropdown, DropdownItemProps, Icon, Menu } from "semantic-ui-react";
import { DiagramMode } from "./diagram-context";

export interface TopMenuProps {
    modes: Array<{
        text: string;
        type: DiagramMode;
    }>;
    constructNames: string[];
    moduleNames: string[];
    handleModeChange: (e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) => void;
    handleBackClick: () => void;
    handleFitClick: () => void;
    handleZoomIn: () => void;
    handleZoomOut: () => void;
    selectedModuleName: string;
    selectedConstructName: string;
    handleConstructNameSelect: (e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) => void;
    handleModuleNameSelect: (e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) => void;
    selectedModeText: string;
    fitActive: boolean;
}

export const TopMenu = (props: TopMenuProps) => {
    const {
        modes,
        constructNames,
        moduleNames,
        handleModeChange,
        selectedModeText,
        handleBackClick,
        handleFitClick,
        handleZoomIn,
        handleZoomOut,
        selectedModuleName,
        selectedConstructName,
        handleConstructNameSelect,
        handleModuleNameSelect,
        fitActive
    } = props;

    return (
        <Menu fixed={"top"}>
        <Menu.Item onClick={handleBackClick} fitted="vertically">
            <Icon className="fw fw-left-arrow"/>
        </Menu.Item>
        <Menu.Item>
            <Dropdown text={selectedModuleName}>
                <Dropdown.Menu>
                {
                    moduleNames.map((moduleName) => (
                        <Dropdown.Item
                            text={moduleName}
                            value={moduleName}
                            onClick={handleModuleNameSelect}
                            data={{
                                name: moduleName,
                            }}
                        />
                    ))
                }
                </Dropdown.Menu>
            </Dropdown>
        </Menu.Item>
        <Menu.Item>
            <Dropdown text={selectedConstructName}>
                <Dropdown.Menu>
                {
                    constructNames.map((constructName) => (
                        <Dropdown.Item
                            text={constructName}
                            value={constructName}
                            onClick={handleConstructNameSelect}
                            data={{
                                name: constructName,
                            }}
                        />
                    ))
                }
                </Dropdown.Menu>
            </Dropdown>
        </Menu.Item>
        <Menu.Item fitted={"vertically"}>
            <Button.Group icon size={"mini"}>
            <Button onClick={handleZoomIn}>
            <Icon className="fw fw-zoom-in"/>
            </Button>
            <Button onClick={handleZoomOut}>
            <Icon className="fw fw-zoom-out"/>
            </Button>
            </Button.Group>
        </Menu.Item>
        <Menu.Item fitted={"vertically"}>
            <Button.Group icon size={"mini"}>
            <Button onClick={handleFitClick} active={fitActive} color={fitActive ? "green" : "grey"}>
            <Icon className="fw fw-fit"/>
            </Button>
            </Button.Group>
        </Menu.Item>
        <Menu.Item position={"right"}>
            <Dropdown text={selectedModeText}>
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
        </Menu.Item>
        </Menu>
    );
};
