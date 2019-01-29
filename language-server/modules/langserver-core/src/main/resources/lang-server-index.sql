CREATE TABLE bLangAnnotation (
  id int(11) NOT NULL,
  packageId int(11) NOT NULL,
  attachmentPoint varchar(64) DEFAULT NULL,
  fields blob,
  fieldsCompletionItems blob,
  completionItem varchar(MAX) NOT NULL
);

CREATE TABLE bLangFunction (
  id int(11) NOT NULL,
  packageId int(11) NOT NULL,
  objectId int(11) NOT NULL DEFAULT '-1',
  name varchar(256) NOT NULL,
  private BIT NOT NULL DEFAULT 1,
  action BIT NOT NULL DEFAULT 0,
  attached BIT NOT NULL DEFAULT 0,
  completionItem varchar(MAX) NOT NULL
);

CREATE TABLE bLangObject (
  id int(11) NOT NULL,
  packageId int(11) NOT NULL,
  name varchar(256) NOT NULL,
  fields varchar(256),
  type int(2) NOT NULL DEFAULT '3',
  private BIT NOT NULL DEFAULT 1,
  completionItem varchar(MAX) DEFAULT NULL
);

CREATE TABLE bLangPackage (
  id int(11) NOT NULL,
  name varchar(256) NOT NULL,
  orgName varchar(256) NOT NULL,
  version varchar(256) NOT NULL
);

CREATE TABLE bLangRecord (
  id int(11) NOT NULL,
  packageId int(11) NOT NULL,
  name varchar(256) NOT NULL,
  fields varchar(MAX) NOT NULL,
  private BIT NOT NULL DEFAULT 1,
  completionItem varchar(MAX) DEFAULT NULL
);

CREATE TABLE bLangType (
  id int(11) NOT NULL,
  packageId int(11) NOT NULL,
  name varchar(256) NOT NULL,
  fields varchar(MAX) NOT NULL,
  completionItem varchar(MAX) DEFAULT NULL
);

CREATE TABLE bLangResource (
  id int(11) NOT NULL,
  serviceId int(11) NOT NULL,
  name varchar(256) NOT NULL
);

CREATE TABLE bLangService (
  id int(11) NOT NULL,
  packageId int(11) NOT NULL,
  name varchar(256) NOT NULL
);

ALTER TABLE bLangAnnotation
  ADD PRIMARY KEY (id);

--
-- Indexes for table bLangFunction
--
ALTER TABLE bLangFunction
  ADD PRIMARY KEY (id);

--
-- Indexes for table bLangObject
--
ALTER TABLE bLangObject
  ADD PRIMARY KEY (id);

--
-- Indexes for table bLangPackage
--
ALTER TABLE bLangPackage
  ADD PRIMARY KEY (id);

--
-- Indexes for table bLangRecord
--
ALTER TABLE bLangRecord
  ADD PRIMARY KEY (id);

--
-- Indexes for table bLangType
--
ALTER TABLE bLangType
  ADD PRIMARY KEY (id);

--
-- Indexes for table bLangResource
--
ALTER TABLE bLangResource
  ADD PRIMARY KEY (id);

--
-- Indexes for table bLangService
--
ALTER TABLE bLangService
  ADD PRIMARY KEY (id);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table bLangAnnotation
--
ALTER TABLE bLangAnnotation
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table bLangFunction
--
ALTER TABLE bLangFunction
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table bLangObject
--
ALTER TABLE bLangObject
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table bLangPackage
--
ALTER TABLE bLangPackage
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table bLangRecord
--
ALTER TABLE bLangRecord
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table bLangType
--
ALTER TABLE bLangType
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table bLangResource
--
ALTER TABLE bLangResource
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table bLangService
--
ALTER TABLE bLangService
  MODIFY id int(11) NOT NULL AUTO_INCREMENT;
