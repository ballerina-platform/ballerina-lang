type NewRecord record {|
	string color;
	string value;
	json...;
|};

type NewRecordList record {|
	NewRecord[] newrecordlist;
	json...;
|};
