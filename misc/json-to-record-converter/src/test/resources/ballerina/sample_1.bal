type SportsItem record {
	string question;
	string answer;
	string[] options;
};

type MathsItem record {
	string question;
	string answer;
	string[] options;
};

type NewRecord record {
	SportsItem[] sports;
	MathsItem[] maths;
};

