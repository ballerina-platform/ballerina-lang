type Image record {
	int vOffset;
	string src;
	string name;
	string alignment;
	int hOffset;
};

type Window record {
	string name;
	int width;
	string title;
	int height;
};

type Text record {
	int vOffset;
	string data;
	int size;
	string name;
	string style;
	string alignment;
	string onMouseUp;
	int hOffset;
};

type Widget record {
	Image image;
	string debug;
	Window window;
	Text text;
};

type NewRecord record {
	Widget widget;
};

