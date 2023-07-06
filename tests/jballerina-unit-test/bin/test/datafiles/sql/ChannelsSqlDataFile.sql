CREATE TABLE IF NOT EXISTS messages (
    msgId INTEGER NOT NULL IDENTITY,
    channelName varchar(200),
    msgKey varchar(200),
    value varchar(200),
    PRIMARY KEY (msgId)
    );