type Image record {|
	int vOffset;
	string src;
	string name;
	string alignment;
	int hOffset;
    json...;
|};

type Window record {|
	string name;
	int width;
	string title;
	int height;
    json...;
|};

type Text record {|
	int vOffset;
	string data;
	int size;
	string name;
	string style;
	string alignment;
	string onMouseUp;
	int hOffset;
    json...;
|};

type Widget record {|
	Image image;
	string debug;
	Window window;
	Text text;
    json...;
|};

type NewRecord record {|
	Widget widget;
    json...;
|};

