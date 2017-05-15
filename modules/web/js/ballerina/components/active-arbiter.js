class ActiveArbiter {
    readyToDeactivate(statement) {
        statement.setState({active: false});
        this.active = null;
    }

    readyToActivate(statement) {
        // console.log(statement);
        if (this.active !== statement) {
            if (this.active) {
                this.active.setState({active: false});
                // console.log("false :" , this.active);
            }
            this.active = statement;
            statement.setState({active: true});
            // console.log("true  :" , statement);
        }
    }
}
export default ActiveArbiter;
