import React from "react"

class Panel extends React.Component {

    render() {
		return <div className="panel panel-default container-outer-div">
			<div className="canvas-heading" data-toggle="collapse">
				<h4 className="panel-title">
					<i className={ "panel-icon fw " +  this.props.icon }></i>
					<a className="collapsed canvas-title" >{ this.props.title }</a>
					<div className="canvas-operations-wrapper">
						<i className="fw fw-up pull-right right-icon-clickable collapser hoverable" title=""></i>
						<span className="pull-right canvas-operations-separator">|</span>
						<i className="fw fw-delete pull-right right-icon-clickable delete-icon hoverable" title=""></i>
						<span className="pull-right canvas-operations-separator">|</span>
						<i className="fw fw-import pull-right right-icon-clickable hoverable" title=""></i>
						<span className="pull-right canvas-operations-separator">|</span>
					</div>
				</h4>
			</div>
			<div className="panel-body outer-box collapse in">
				{ this.props.children }
			</div>
		</div>;
    }
}

export default Panel;
