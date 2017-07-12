import React from 'react';
import TransformRender from '../../ballerina/components/transform-render';
import SuggestionsDropdown from './transform-endpoints-dropdown';

class TransformExpanded extends React.Component {
    constructor() {
        super();
        this.state = {
            selectedSource: '-1',
            selectedTarget: '-1',
        }

        this.onSourceSelect = this.onSourceSelect.bind(this);
        this.onTargetSelect = this.onTargetSelect.bind(this);
        this.onSourceAdd = this.onSourceAdd.bind(this);
        this.onTargetAdd = this.onTargetAdd.bind(this);
    }

    componentDidMount() {
        this.props.editor.setTransformState(true);
    }

    componentWillUnmount() {
        this.props.editor.setTransformState(false);
        this.props.onUnmount();
    }

    onSourceSelect(e) {
        this.setState({
            selectedSource: e.target.value,
        });
    }

    onTargetSelect(e) {
        this.setState({
            selectedTarget: e.target.value,
        });
    }

    onSourceAdd() {
        this.props.onSourceAdd(this.state.selectedSource);
    }

    onTargetAdd() {
        this.props.onTargetAdd(this.state.selectedTarget);
    }

    render() {
        const sourceId = 'sourceStructs' + this.props.model.id;
        const targetId = 'targetStructs' + this.props.model.id;

        return (
            <div id='transformOverlay' className='transformOverlay'>
                <div id = 'transformOverlay-content' className='transformOverlay-content'
                    ref={div => this.transformOverlayContentDiv=div }
                    onMouseOver={this.props.onTransformDropZoneActivate}
                    onMouseOut={this.props.onTransformDropZoneDeactivate}>
                    <div id ="transformHeader" className="transform-header">
                        <i onClick={this.props.onClose} className="fw fw-left-arrow icon close-transform"></i>
                        <p className="transform-header-text ">
                            <i className="transform-header-icon fw fw-type-converter"></i>
                            Transform
                        </p>
                    </div>
                    <div id ="transformHeaderPadding" className="transform-header-padding"></div>
                    <div className="source-view">
                        <SuggestionsDropdown
                            suggestionsPool={this.props.sourcesAndTargets}
                            placeholder='Select Source'
                        />
                        <span
                            id="btn-add-source"
                            className="btn-add-type fw-stack fw-lg btn btn-add"
                            onClick={this.onSourceAdd}
                        >
                            <i className="fw fw-add fw-stack-1x"></i>
                        </span>
                    </div>
                    <div className="leftType"></div>
                    <div className="middle-content"></div>
                    <div className="target-view">
                        <SuggestionsDropdown
                            suggestionsPool={this.props.sourcesAndTargets}
                            placeholder='Select Target'
                        />
                        <span
                            id="btn-add-target"
                            className="btn-add-type fw-stack fw-lg btn btn-add"
                            onClick={this.onTargetAdd}
                        >
                            <i className="fw fw-add fw-stack-1x"></i>
                        </span>
                    </div>
                    <div className="rightType"></div>
                    <div id ="transformContextMenu" className="transformContextMenu"></div>
                    <div id ="transformFooter" className="transform-footer"></div>
                </div>
            </div>
        );
    }
}

export default TransformExpanded;
