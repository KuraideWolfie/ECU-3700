# ECU-3700

<i>This README is a WIP file.</i>

## NOTICES
There is a license file for Postgre available under <u>App/LICENSE-postgre.txt</u>.

## Application
### Description
This application is the main program for the database project, allowing access to, and manipulation of, the various data points in the PostgreSQL database. It allows a user to modify and view customer data, manage and view employee information, and register account information (both online and offline) as well as managing disputes and transactions. This is done via a commandline interface.
### Resources
- A recent version of postgresql's JDBC JAR is required for this project to sufficiently operate. This project uses <i>version 42.2.5</i>, and is available here: https://jdbc.postgresql.org/
- PgAdmin 4 and PostgreSQL are expected applications for running the database that the program interfaces with. PgAdmin 4 is available here: https://www.pgadmin.org/
### Source Files
Resource Files: postgresql-42.2.5.jar, manifest.txt

Source Files:

<i>src:</i> Main.java

<i>src/dbase:</i> CID.java, Conn.java, Relations.java

<i>src/cmd:</i> Command.java, CmdHelp.java, CmdCustomer.java, CmdReset.java, CmdAccount.java

<i>src/cmd/acct:</i> SubModify.java

Data Files: input.txt
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
- Lines in an input file, given using  `in <file>`, that start with a '#' are ignored during preload

### Functions
The following is a hierarchy of functions that were generated for the project - that is, these functions are implemented in the source as executable:
<ul>
<li><b>ret</b>, <b>exit</b>, <b>back</b>: <i>Reserved commands that will exit/return from commands</i>
<li><b>help</b>: Show a list of commands
<li><b>reset</b>: Reset the database's state using a create and data SQL files (usually from the generator)
<li><i>Customer Management</i>
  <ul>
  <li><b>get</b>: Retrieve information about a group of customers
  <li><b>mod</b>: Modify a customer's information
  <li><b>new</b>: Add a new customer to the database
  </ul>
<li><i>Account Management</i>
  <ul>
  <li><b>new</b>: Add a new account for a customer
  <li><b>seek</b>: Seek accounts owned by a customer
  <li><b>get</b>: Get information about an account
  <li><b>help</b>: Show this list of commands
  <li><b>mod</b>: Execute commands that modify the account
    <ul>
    <li><b>help</b>: Show this list of commands
    <li><b>card</b>: Manipulate cards on the account
      <ul>
      <li><b>list</b>: List cards on the account (pending/active/disabled only)
      <li><b>new</b>: Add a new card to an account
      <li><b>tog</b>: Toggle a card between enabled/disabled
      <li><b>close</b>: Close a card (even if pending)
      </ul>
    <li><b>owner</b>: Manipulate owners for an account
      <ul>
      <li><b>list</b>: Show a list of owners on the account
      <li><b>new</b>: Add an owner to the account
      <li><b>rem</b>: Remove an owner from the account
      </ul>
    <li><b>credit</b>: Execute a credit transaction (counter deposits, etc)
    <li><b>debit</b>: Execute a debit transaction (counter withdrawal)
    <li><b>close</b>: Close the account
    </ul>
  </ul>
</ul>