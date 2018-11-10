# ECU-3700

<i>This README is a WIP file.</i>

## Application
### Description
This application is the main program for the database project, allowing access to, and manipulation of, the various data points in the PostgreSQL database. It allows a user to modify and view customer data, manage and view employee information, and register account information (both online and offline) as well as managing disputes and transactions. This is done via a commandline interface.
### Resources
- A recent version of postgresql's JDBC JAR is required for this project to sufficiently operate. This project uses <i>version 42.2.5</i>, and is available here: https://jdbc.postgresql.org/
- PgAdmin 4 and PostgreSQL are expected applications for running the database that the program interfaces with. PgAdmin 4 is available here: https://www.pgadmin.org/
### Source Files
Resource Files: postgresql-42.2.5.jar, manifest.txt

Source Files: <b>DBMSApp.jar</b>, Main.java, CID.java, Conn.java, Relations.java, Command.java, CmdHelp.java

Data Files: ...
### Compilation, Testing, and Known Issues
```
Compile:
javac -d "out" -cp ".;postgresql-42.2.5.jar" Main.java
jar cfm DBMSApp.jar ./src/manifest.txt -C out .

Testing:
java -jar DBMSApp.jar [options]

Options:
- in <file> : Specifies an input file to read commands from
```
Issues:
- ...

Notes:
- To appropriately compile the program, have the postgresql JAR file in the same directory as the source files on compilation. Furthermore, it is assumed you have an 'out' directory to compile to
