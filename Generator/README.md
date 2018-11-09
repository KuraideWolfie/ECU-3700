# ECU-3700

## Data Generator
### Description
The data generator consists of a few input files, and is responsible for generating ALL data that is inserted into the tables of the database. It follows a lengthy procedure for generating data appropriately, and in the order that will provide most referential integrity when inserted into the database. (The data was, by goal, to be semi-realistic.) The results of the generator are printed to two files: <b>query.sql</b>, which contains all of the employees, customers, etc, and <b>query-trans.sql</b>, which contains all transaction information.
### Source Files
Source Files: Main.java, ACP.java, Branch.java, Card.java, Offline.java, Online.java, Transaction.java, Phone.java, Question.java, Helper.java, Customer.java, Employee.java, City.java, Country.java, State.java, Store.java, Uniquifier.java, Helper.java, <b>Generator.jar</b>

Data Files: in-names.txt, in-state.txt, in-recovery.txt, in-store.txt

Output Files: query.sql, query-trans.sql
### Compilation, Testing, and Known Issues
```
Compile:
javac -d out Main.java
jar cfe Generator.jar Main -C out .

Testing: java -jar Generator.jar [options]
Options:
-d : Show trace information as data generates
-st : Skip transaction generation and writing to query-trans.sql
```
Issues:
- If two people share a name and RNG chooses the same number for their online account credentials, this may cause a conflict with data insertion.

Notes:
- <b>The file size of query-trans.sql after writing is fairly large!</b> Further, consecutive executions of the generator will result in prior-generated data being erased!
- The full procedure desired was not presently implemented into the application. There were plans to localize store placement such that stores and transactions would be more realistic according to a customer’s geographical location. <i>At present, there are no plans to finish this.</i>
- The Country class could randomly assign a suffix to streets – aka ln, st, and dr – but this implementation, <i>at present time, is not planned for implementation.</i>
