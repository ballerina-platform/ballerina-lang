/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from "react";
import Collapsible from 'react-collapsible';
import { Scrollbars } from 'react-custom-scrollbars';
import ReactDOM from 'react-dom';
import log from 'log';
import $ from 'jquery';
import * as d3 from 'd3';
import _ from 'lodash';
import D3Utils from 'd3utils';
import DragDropManager from '../tool-palette/drag-drop-manager';
import PropTypes from 'prop-types';
import InitialTools from '../item-provider/initial-definitions';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import DefaultBallerinaASTFactory from '../ast/default-ballerina-ast-factory';

class Tool extends React.Component {

	constructor(props){
		super(props);
		this.model = props.tool
	}

	componentDidMount(){
		let toolDiv = ReactDOM.findDOMNode(this);
		$(toolDiv).draggable({
            helper: _.isUndefined(this.createCloneCallback) ?  'clone' : this.createCloneCallback(self),
            cursor: 'move',
            cursorAt: { left: 30, top: -10 },
            containment: 'document',
            zIndex: 10001,
            stop: this.createHandleDragStopEvent(),
            start : this.createHandleDragStartEvent(),
            drag:this.createHandleOnDragEvent()
        });
	}

	createHandleDragStopEvent(){
        return (event, ui) => {
            if(this.context.dragDropManager.isAtValidDropTarget()){
                var indexForNewNode = this.context.dragDropManager.getDroppedNodeIndex();
                if(indexForNewNode >= 0){
                    this.context.dragDropManager.getActivatedDropTarget()
                            .addChild(this.context.dragDropManager.getNodeBeingDragged(), indexForNewNode);
                } else {
                    this.context.dragDropManager.getActivatedDropTarget()
                            .addChild(this.context.dragDropManager.getNodeBeingDragged());
                }
            }
            this.context.dragDropManager.reset();
            this._$disabledIcon = undefined;
            this._$draggedToolIcon = undefined;
        };
	}

	createHandleDragStartEvent(){
        return (event,ui) => {
            // Get the meta information/ arguments to create the particular tool
            var meta = this.model.get('meta') || {};
            this.context.dragDropManager.setNodeBeingDragged(this.model.nodeFactoryMethod(meta, true));
        };
	}
	
	createHandleOnDragEvent(){
        return (event, ui) => {
            if(!this.context.dragDropManager.isAtValidDropTarget()){
                this._$disabledIcon.show();
                this._$draggedToolIcon.css('opacity', 0.1);
            } else {
                this._$disabledIcon.hide();
                this._$draggedToolIcon.css('opacity', 1);
            }
        };
	}

    createCloneCallback(view) {
        var icon = this.props.tool.icon,
            self = this,
            iconSize = '50px';
        var self = this;
        d3.select(icon).attr("width", iconSize).attr("height", iconSize);
        return () => {
            var div = this.createContainerForDraggable();
            div.node().appendChild(icon);
            self._$draggedToolIcon = $(icon);
            return div.node();
        }
    }

    createContainerForDraggable(){
        var body = d3.select("body");
        var div = body.append("div").attr("id", "draggingToolClone")
                        .classed('tool-drag-container', true);
        //For validation feedback
        var disabledIconDiv = div.append('div').classed('disabled-icon-container', true);
        disabledIconDiv.append('i').classed('fw fw-lg fw-block tool-disabled-icon', true);
        this._$disabledIcon = $(disabledIconDiv.node());
        this._$disabledIcon.css('top', 30 + 20);
        this._$disabledIcon.css('left', 30 - 10);
        return div;
    }

	render() {
        let tool = this.props.tool;
		let icon = tool.icon;
		if(this.props.toolOrder == 'horizontal'){
			return (
				<div className="tool-block tool-container" data-original-title={tool.get('name')} data-placement="bottom" data-toggle="tooltip" id={tool.get('name')} title={tool.get('name')}>
					<i className={tool.get('cssClass')}></i> <span className="tool-title-wrap"></span>
					<span className="tool-title-wrap"><p className="tool-title">{tool.get('name')}</p></span>
				</div>				
			);
		}else{
			return (
				<div id={tool.id + '-tool'} className={ "tool-block tool-container-vertical " + tool.get('classNames') } >
					<div  className="tool-container-vertical-icon" data-placement="bottom" data-toggle="tooltip" title={tool.get('title')}>
						<img src={icon.getAttribute("src")} className="tool-image" />
					</div>
					<div className="tool-container-vertical-title" data-placement="bottom" data-toggle="tooltip" title={tool.get('title')}>{tool.get('title')}</div>
					<p className="tool-title">{tool.get('name')}</p>
				</div>
			);
		}
    }
}

class ToolGroup extends React.Component {

	constructor(props){
		super(props);
		this.state = {
			activeGridStyle: 'list'
		}
		this.changeGridStyle = this.changeGridStyle.bind(this);
	}

	changeGridStyle(event){
		this.setState({ activeGridStyle: event.currentTarget.dataset.style });
	}

	render() {
        let group = this.props.group;
        let children = [];
		group.tools.forEach((element, index) => {
			if(element.attributes.seperator){
				children.push(<div className='clear-fix ' key={ "clear" + index } />);
				children.push(<div className='tool-separator' key={ "sep" + index } />);
			}else{
				children.push(<Tool tool={element} key={index} toolOrder={group.get('toolOrder')} />);
			}
		}, this);

		let optionsDisplay = { display : "none" };
		if (this.props.iconOptions){
			optionsDisplay = { display : "block" };
		}

		let trigger = <div className="tool-group-header">
						<a className="tool-group-header-title">{group.get('toolGroupName')}</a>
						<span className="collapse-icon fw fw-down"></span>
					</div>;

		let triggerWhenOpen = <div className="tool-group-header">
						<a className="tool-group-header-title">{group.get('toolGroupName')}</a>
						<span className="collapse-icon fw fw-up"></span>
					</div>;					

		let disabled = false;
		let open = false;
		if(group.collapsed){
			open = true;
		}

		return (
            <div id="tool-group-constructs-tool-group" className="tool-group">
				<Collapsible trigger={trigger} triggerDisabled={disabled} open={open} triggerWhenOpen={triggerWhenOpen} >
					<div className={"tool-group-body tool-group-body-" + this.state.activeGridStyle }>
						{ this.props.showGridStyles &&
						<div className="tools-view-modes-controls clearfix">
							<a className="collapse-icon fw fw-tiles" onClick={this.changeGridStyle} data-style="tiles" ></a>
							<a className="collapse-icon fw fw-grid" onClick={this.changeGridStyle} data-style="grid" ></a>
							<a className="collapse-icon fw fw-list" onClick={this.changeGridStyle} data-style="list" ></a>
						</div>
						}
						{children}
					</div>
				</Collapsible>
            </div>
		);
    }
}


class ToolsPanel extends React.Component {

	constructor(props){
		super(props);
	}

	render() {
		let trigger = <div className="tool-palette-panel-header">
						<a className="tool-palette-panel-header-title">{ this.props.name }</a>
						<span className="collapse-icon fw fw-down"></span>
					</div>;

		let triggerWhenOpen = <div className="tool-palette-panel-header">
						<a className="tool-palette-panel-header-title">{ this.props.name }</a>
						<span className="collapse-icon fw fw-up"></span>
					</div>;					

		return (
			<div className="tool-palette-panel">
				{/*<Collapsible trigger={trigger}  open={this.props.open} triggerWhenOpen={triggerWhenOpen} >*/}
					<a className="tool-palette-panel-header-title">{ this.props.name }</a>
					<div className="tool-palette-panel-body" >
							{this.props.children}
					</div>
				{/*</Collapsible>*/}
			</div>			
		)
	}
}

class ToolsPane extends React.Component {

	constructor(props){
		super(props);
		this.changePane = this.changePane.bind(this);		
	}

	changePane(type) {
		this.props.changePane(type);
	}		

	render() {
		return (
			<div>
				{ this.props.constructs &&	<ToolGroup group={this.props.constructs} key="constructs" showGridStyles={true} /> }
				{ this.props.currentTools &&	<ToolGroup group={this.props.currentTools} key="Current Package" showGridStyles={false} /> }
				<ToolsPanel name="Connectors"  >
					{ this.props.connectors }
					<a className="tool-palette-add-button" onClick={() => this.changePane("connectors")}>
						<i className="fw fw-add fw-helper fw-helper-circle-outline icon"></i> Add
					</a>
				</ToolsPanel>								
				<ToolsPanel name="Libraries"  >
					{this.props.library}
					<a className="tool-palette-add-button"  onClick={() => this.changePane("library")} >				
						<i className="fw fw-add fw-helper fw-helper-circle-outline icon"></i> Add
					</a>
				</ToolsPanel>
			</div>	
		)
	}
}

class ConnectorPane extends React.Component {

	constructor(props){
		super(props);
		this.changePane = this.changePane.bind(this);
	}

	changePane(type) {
		this.props.changePane(type);
	}	

	render() {
		return (
			<div className="connector-panel">
				<div className="tool-pane-header">
					 Connectors
					<a className="back" onClick={() => this.changePane("tools")} >
						<i className="fw fw-cancel fw-helper fw-helper-circle-outline icon"></i>
					</a>					 
				</div>
				{this.props.connectors}
			</div>
		)
	}
}

class LibraryPane extends React.Component {

	constructor(props){
		super(props);
		this.changePane = this.changePane.bind(this);
	}

	changePane(type) {
		this.props.changePane(type);
	}

	render() {
		return (
			<div className="library-panel">
				<div className="tool-pane-header">
					Libraries
					<a className="back"  onClick={() => this.changePane("tools")} >
						<i className="fw fw-cancel fw-helper fw-helper-circle-outline icon"></i>
					</a>
				</div>
				{this.props.library}
			</div>			
		)
	}
}


class ToolSearch extends React.Component {

	constructor(props){
		super(props);
		this.handleChange = this.handleChange.bind(this);
		this.state = { text:''};
	}

	handleChange(e) {
		this.setState({text: e.target.value});
		this.props.onTextChange(e.target.value);
	}
	
	render() {
		return (
			<div className="non-user-selectable">
				<div className="search-bar">
					<i className="fw fw-search searchIcon"></i>
					<input className="search-input" id="search-field" placeholder="Search" type="text" onChange={this.handleChange} value={this.state.text} />
				</div>
			</div>
		);
    }
}

class ToolPaletteView extends React.Component {

	constructor(props){
		super(props);
		this.provider = props.provider;
		// bind the event handlers to this
		this.onSearchTextChange = this.onSearchTextChange.bind(this);
		this.changePane = this.changePane.bind(this);

		this.state = {
			tab: 'tools',
			search: ''
		}

		this.editor = props.editor;	
        this.editor.on("update-diagram", () => {
            this.forceUpdate();
        });		
	}

	onSearchTextChange(value){
		this.setState({ search: value});
	}

	changePane(type){
		this.setState({ tab: type });
	}

	render() {
		//get the model
		let model = this.props.editor.getModel();
		//get the environment
		let environment = this.props.editor.getEnvironment();
		//get the current package
		let currentPackage = environment.getPackageByName('Current Package');
		let currentTools = this.provider.getCombinedToolGroup(currentPackage);
		//get the constructs
		let constructs = _.cloneDeep(InitialTools[0]);
		//get imported packages
		let imports = model.getImportDeclarations();

		//convert imports to tool groups
		let connectors = [];
		let library = [];

		if(this.state.tab == 'tools'){
			imports.forEach( (item) => {
				let pkg = environment.getPackageByName(item.getPackageName());
				let group = this.provider.getConnectorToolGroup(pkg);
				group = this.searchTools(this.state.search, _.cloneDeep(group));
				if(group != undefined && !_.isEmpty(group.tools)){ 
					connectors.push(<ToolGroup group={group} key={"connector" + item.getPackageName() } showGridStyles={false} />);
				}

				group = this.provider.getLibraryToolGroup(pkg);
				group = this.searchTools(this.state.search, _.cloneDeep(group));
				if(group != undefined && !_.isEmpty(group.tools)){
					library.push(<ToolGroup group={group} key={"library" + item.getPackageName() } showGridStyles={false} />);
				}
			});
		}else{
			let filterOutList = imports.map((item)=>{
				return item.getPackageName();
			});
			filterOutList.push("Current Package");

			let packages = environment.getFilteredPackages(filterOutList);
			packages.forEach( (pkg) => {
				let group = undefined;
				if(this.state.tab == 'connectors'){
					group = this.provider.getConnectorToolGroup(pkg);
					group = this.searchTools(this.state.search, _.cloneDeep(group));
					if(group != undefined && !_.isEmpty(group.tools)){
						connectors.push(<ToolGroup group={group} key={"connector" + pkg.getName() } showGridStyles={false} />);
					}
				}
				else{
					group = this.provider.getLibraryToolGroup(pkg);
					group = this.searchTools(this.state.search, _.cloneDeep(group));
					if(group != undefined && !_.isEmpty(group.tools)){
						library.push(<ToolGroup group={group} key={"library" + pkg.getName() } showGridStyles={false} />);
					}
				}
			});
		}

		// calculate the height of the tool.
		// this is a hack need to find a better approch.
		let scrollHeight = window.innerHeight - 176;
		
		constructs.collapsed = true;
		constructs = this.searchTools(this.state.search, constructs);

		return (
			<div className="tool-palette-container">
				<ToolSearch onTextChange={this.onSearchTextChange} />
				<Scrollbars style={{ width: 243, height: scrollHeight }} 
					autoHide
					// Hide delay in ms
					autoHideTimeout={1000} >
					{ this.state.tab == 'tools' &&
						<ToolsPane
							constructs={constructs}
							currentTools={currentTools}
							connectors={connectors}
							library={library}
							changePane={this.changePane}
						/>
					}
					{ this.state.tab == 'connectors' &&
						<ConnectorPane
							connectors={connectors}
							changePane={this.changePane}
						/>
					}
					{ this.state.tab == 'library' &&
						<LibraryPane
							library={library}
							changePane={this.changePane}
						/>
					}										
				</Scrollbars>
			</div>
		);
    }

    getChildContext() {
        return {
            dragDropManager: this.props.dragDropManager
        };
    }

	searchTools(text, data){
		if(text == undefined || text == ''){
		
		} else if (data.tools instanceof Array){
			data.tools = data.tools.filter(function(item){
				if(item.get('title').toLowerCase().includes(this.searchText)){
					return true;
				}
				return false;
			},{ searchText : text.toLowerCase() });
			if(data.tools.length == 0){
				return undefined;
			}
		} else{
			return undefined;
		}
		
		return data;
	}

	extractConnectors(library){
		
	}
}

ToolPaletteView.childContextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired
};

Tool.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired
};

export default ToolPaletteView;