package lang.expressions.type.unsupported.lte;

function checkLessThanEualForUnsupportedType() (boolean){
	json j1;
	json j2;
	j1 = `{"name":"Jack"}`;
	j2 = `{"state":"CA"}`;
	
	return j1 <= j2;
}
