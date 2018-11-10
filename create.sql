-- Customer should have a home/work/cell phone number distinction

--------------------------------------------------------------------------------
-- DROP ORIGINAL TABLES

DROP TABLE IF EXISTS "Transaction";
DROP TABLE IF EXISTS "Dispute";
DROP TABLE IF EXISTS "Recovery_Question";
DROP TABLE IF EXISTS "Employee";
DROP TABLE IF EXISTS "Account_Owner";
DROP TABLE IF EXISTS "Account_Online";
DROP TABLE IF EXISTS "Card";
DROP TABLE IF EXISTS "Account";
DROP TABLE IF EXISTS "Customer";
DROP TYPE IF EXISTS acct_type;
DROP TYPE IF EXISTS acct_int_comp;
DROP TYPE IF EXISTS cust_gender;
DROP TYPE IF EXISTS card_status;
DROP TYPE IF EXISTS disp_status;
DROP TYPE IF EXISTS tran_type;

--------------------------------------------------------------------------------
-- CREATE TABLES IN THE DATABASE

CREATE TYPE cust_gender AS ENUM ('F', 'M');

CREATE TABLE "Customer" (
	"CID" serial,
	"SSN" varchar(9) unique not null,
	"Fname" varchar(255) not null,
	"Lname" varchar(255) not null,
	"Gender" cust_gender not null,
	"DOB" date not null,
	"Con_Email" varchar(255) not null,
	"Con_Phone" varchar(10),
	"Street" varchar(255) not null,
	"City" varchar(255) not null,
	"State" varchar(2) not null,
	"Apt" varchar(255),
	"Zip" int not null,
	PRIMARY KEY ("CID")
);

CREATE TABLE "Employee" (
	"EID" serial,
	"Date_Start" date not null,
	"Date_End" date,
	"CID" int not null,
	"Sup_EID" int,
	PRIMARY KEY ("EID"),
	FOREIGN KEY ("CID") REFERENCES "Customer"("CID"),
	CHECK (
		CASE WHEN "Date_End" is not null THEN "Date_End" >= "Date_Start"
			 ELSE true END
	),
	CHECK (not "EID" = "Sup_EID")
);

CREATE TYPE acct_type AS ENUM ('CHK', 'SAV');
CREATE TYPE acct_int_comp AS ENUM ('NONE', 'MONTHLY', 'QUARTERLY',
	'TRIMESTERLY', 'SEMESTERLY', 'ANNUALLY');

CREATE TABLE "Account" (
	"AID" bigint not null,
	"Type" acct_type not null,
	"Date_Open" date not null,
	"Date_Close" date,
	"Balance" float not null,
	"Int_Rate" float not null,
	"Int_Comp" acct_int_comp,
	"Month_Fee" float not null,
	PRIMARY KEY ("AID"),
	CHECK (
		CASE WHEN "Date_Close" is not null THEN "Date_Close" >= "Date_Open"
		     ELSE true END
	),
	CHECK (
		CASE WHEN "Int_Rate"=0 THEN "Int_Comp"='NONE'
		     ELSE true END
		and
		CASE WHEN "Type"='CHK' THEN "Int_Rate"=0
		     ELSE true END
	)
);

CREATE TABLE "Account_Owner" (
	"CID" int not null,
	"AID" bigint not null,
	PRIMARY KEY ("CID","AID"),
	FOREIGN KEY ("CID") REFERENCES "Customer"("CID"),
	FOREIGN KEY ("AID") REFERENCES "Account"("AID")
);

CREATE TYPE card_status AS ENUM ('PENDING', 'ACTIVE', 'DISABLED', 'CLOSED');

CREATE TABLE "Card" (
	"Number" varchar(16) not null,
	"Exp_Date" date not null,
	"Sec_Code" varchar(3) not null,
	"PIN" varchar(4) not null,
	"Status" card_status not null,
	"AID" bigint not null,
	"CID" int not null,
	PRIMARY KEY ("Number"),
	FOREIGN KEY ("AID") REFERENCES "Account"("AID"),
	FOREIGN KEY ("CID") REFERENCES "Customer"("CID")
);

CREATE TABLE "Account_Online" (
	"CID" int not null,
	"Username" varchar(255) not null,
	"Password" varchar(255) not null,
	PRIMARY KEY ("CID"),
	FOREIGN KEY ("CID") REFERENCES "Customer"("CID")
);

CREATE TABLE "Recovery_Question" (
	"CID" int not null,
	"RID" int not null,
	"Date" date not null,
	"Question" varchar(255) not null,
	"Answer" varchar(255) not null,
	PRIMARY KEY ("CID","RID"),
	FOREIGN KEY ("CID") REFERENCES "Account_Online"("CID")
);

CREATE TYPE disp_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');

CREATE TABLE "Dispute" (
	"DID" bigserial,
	"AID" bigint not null,
	"Date" date not null,
	"Reason" varchar(255) not null,
	"Status" disp_status not null,
	"Handler" int not null,
	PRIMARY KEY ("DID","AID"),
	FOREIGN KEY ("AID") REFERENCES "Account"("AID"),
	FOREIGN KEY ("Handler") REFERENCES "Employee"("EID")
);

CREATE TYPE tran_type AS ENUM ('DEBIT', 'CREDIT');

CREATE TABLE "Transaction" (
	"TID" bigserial,
	"AID" bigint not null,
	"Type" tran_type not null,
	"Date" date not null,
	"Desc" varchar(255),
	"Amount" float not null,
	"Rec_Route" varchar(25) not null,
	"Rec_AID" varchar(25) not null,
	"DID" int,
	"isPending" boolean not null,
	PRIMARY KEY ("TID","AID"),
	FOREIGN KEY ("AID") REFERENCES "Account"("AID"),
	FOREIGN KEY ("DID","AID") REFERENCES "Dispute"("DID","AID"),
	CHECK ("Amount" > 0)
);