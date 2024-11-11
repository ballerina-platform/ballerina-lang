type SportsItem record {|
	string question;
	string answer;
	string[] options;
    json...;
|};

type MathsItem record {|
	string question;
	string answer;
	string[] options;
    json...;
|};

type NewRecord record {|
	SportsItem[] sports;
	MathsItem[] maths;
    json...;
|};

