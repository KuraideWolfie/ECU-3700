/**
  * Main is a main executor of data generation for the term project. It utilizes
  * all the other classes in the source folder and generates customer addresses,
  * employees, card and account information, as well as transactions and dispute
  * information based on a series of input files and RNG.
  *
  * + Stores presently ignore online toggle and some other key elements that
  *   were meant to localize them. These features were discarded due to time.
  * + Transactions are, presently, not designed to localize stores before gen.
  * + What happens if two people have the same name and RNG chooses the same num
  *   for online account credentials?
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

import src.state.*;
import src.people.*;
import src.account.*;
import src.store.Store;
import src.gen.Question;
import src.*;

public class Main {
  // FILE_OUT_QUERY is where all SQL queries get written to
  // FILE_OUT_TRANS is where all SQL queries for transactions get written
  // FILE_IN_STATE is the path of a text file storing data for country gen
  // FILE_IN_CUST is the path of a text file storing data for customer gen
  // FILE_IN_REC is the path of a text file storing recovery questions
  // FILE_IN_STORE is the path of a text file storing info about stores
  static final String FILE_OUT_QUERY = "./data/query.sql",
    FILE_OUT_TRANS = "./data/query-trans.sql",
    FILE_IN_STATE = "./data/in-state.txt",
    FILE_IN_CUST = "./data/in-name.txt",
    FILE_IN_REC = "./data/in-recovery.txt",
    FILE_IN_STORE = "./data/in-store.txt";

  // PER_EMP is the percentage of customers that are employees
  // PER_ONLINE is the percentage of customers that have online accounts
  // PER_OFFLINE is the percentage of customers with accounts
  static final double PER_EMP = .25, PER_ONLINE = .3, PER_OFFLINE = .8;
  
  // Arrays holding all tuple column names (excluding some IDs because of
  // the serialization of those columns).
  // + Customer CID
  // + Employee EID
  // + Transaction TID
  // + Dispute DID
  static final String[]
    TBL_REC = {"CID", "RID", "Date", "Question", "Answer"},
    TBL_EMP = {"Date_Start", "Date_End", "CID", "Sup_EID"},
    TBL_CUST = {"SSN", "Fname", "Lname", "Gender", "DOB", "Con_Email",
      "Con_Phone", "Street", "Apt", "City", "Zip", "State"},
    TBL_CARD = {"Number", "Exp_Date", "Sec_Code", "Status", "AID", "CID", "PIN"},
    TBL_ACCT = {"AID", "Type", "Date_Open", "Date_Close", "Balance", "Int_Rate",
      "Int_Comp", "Month_Fee"},
    TBL_TRAN = {"AID", "isPending", "Type", "Date", "Desc", "Amount",
      "Rec_Route", "Rec_AID", "DID"},
    TBL_DISP = {"AID", "Date", "Reason", "Status", "Handler"},
    TBL_ACCT_ON = {"CID", "Username", "Password"},
    TBL_ACCTOWN = {"AID", "CID"};

  // DEBUG_ENABLED toggles the printing of debug messages
  // SKIP_TRANS toggles skipping of purchase transaction generation
  // country is the country storing all state/city/street information
  // cust stores all information about customers
  // emp stores all information about employees
  // set stores all recovery questions
  // accOn stores all online accounts
  // accoff stores all offline accounts
  // acp stores all account-customer pairs as a list
  // cards stores all the cards for accounts
  // trans stores all transactions
  // branches stores the routing and AIDs of the different bank branches
  // stores is a collection of stores for usage in generating transactions
  static boolean DEBUG_ENABLED = false, SKIP_TRANS = false;
  static Country country = Country.getCountry();
  static Customer[] cust;
  static Employee[] emp;
  static Question set = Question.getSet();
  static Online[] accOn;
  static Offline[] accOff;
  static ArrayList<ACP> acp = new ArrayList<>();
  static ArrayList<Card> cards = new ArrayList<>();
  static ArrayList<Transaction> trans = new ArrayList<>();
  static Branch[] branches;
  static ArrayList<Store> stores = new ArrayList<>();

  public static void main(String[] args) {
    // Argument catcher
    for(String arg : args) {
      switch(arg) {
        case "-d": DEBUG_ENABLED = true; break;
        case "-st": SKIP_TRANS = true; break;
      }
    }

    try {
      // Phase 01 : Customer address generation and employees
      System.out.println("Building Country, Customers, Employees");
      country.load(FILE_IN_STATE);
      cust = Customer.load(FILE_IN_CUST);
      buildEmployees();

      // Phase 02: Accounts, ownership, and Cards
      System.out.println("Building Accounts, Recovery, Cards");
      set.load(FILE_IN_REC);
      buildOnline();
      buildOffline();
      buildCards();

      // Phase 03: Transactions and Disputes
      System.out.println("Building Transactions and Disputes");
      expandCountry();
      buildBranches();
      if (!SKIP_TRANS)
        buildTransactions();

      System.out.println("Printing SQL statements to file");
      printSQL();
    }
    catch(IOException e) {
      // File handling exception
      System.out.println("There was an error during file handling.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    catch(Exception e) {
      // Misc. exceptions
      System.out.println("There was a miscellaneous error.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  /** printDebug(str) prints the string given if debugging is enabled. The
    * variant printDebug(str, newLn) allows control over whether a new line
    * gets printed or not. */

  private static void printDebug(String str) {
    printDebug(str, true);
  }

  private static void printDebug(String str, boolean newLn) {
    if (DEBUG_ENABLED)
      System.out.print(str + (newLn ? "\n": ""));
  }

  /** sqlHead(name, col) returns a string that comprises the heading of an
    * 'insert into' SQL statement that lists the table name and column headers
    * with proper quotation.
    *
    * @param name The name of the table the data will go into
    * @param col A list of column names for the tuple
    * @return The start of an 'insert into' SQL query */

  private static String sqlHead(String name, String[] col) {
    String tuple = "(";

    for(int i=0; i<col.length; i++)
      tuple += "\""+col[i]+"\"" + (i==col.length-1 ? ")" : ",");

    return "INSERT INTO \""+name+"\""+tuple+" VALUES ";
  }

  /** getBranch(i) gets the branch for the i-th offline account */

  private static int getBranch(int i) {
    return cust[ACP.getAIDCust(acp, accOff[i].getAID())].getState();
  }

  /****************************************************************************/
  /** buildEmployees() takes the set of customers and generates a subset of
    * employees from them, randomly generating a hierarchy of supervision by
    * having earlier employees supervise latter employees */

  public static void buildEmployees() {
    emp = new Employee[ (int)(cust.length * PER_EMP) ];
    printDebug("Employee generation: (CID,Age,Sup)");
    
    for(int i=0; i<emp.length; i++) {
      boolean flag = false;
      int cid;

      // Choose a unique customer ID to make a new employee
      // The selected customer must be of age
      do {
        flag = false; cid = Helper.randomRange(0, cust.length-1);
        for(int k=0; k<i; k++) { if (emp[k].getCID() == cid) { flag = true; } }
        if (cust[cid].getAge() < Employee.YR_MIN) { flag = true; }
      }
      while(flag);

      emp[i] = new Employee(cust[cid], cid, i);

      // Get a supervisor that is still working at the institution
      if (i > 0) {
        int id;
        do { id = Helper.randomRange(0, i-1); }
        while(emp[id].getEnd() != null);
        emp[i].setSup(id);
      }

      printDebug(String.format(" (%3d,%2d,%3d)", cid, cust[cid].getAge(), emp[i].getSup()),
        i % 10 == 9 || i == emp.length-1);
    }
  }

  /****************************************************************************/
  /** buildOnline() builds the set of online accounts owned by customers at the
    * bank, assigning to each account a random set of security questions and
    * answers. */

  public static void buildOnline() {
    accOn = new Online[ (int) (cust.length * PER_ONLINE) ];
    printDebug("Online account generation");

    for(int i=0; i<accOn.length; i++) {
      int cid;
      boolean flag = false;

      // Select a unique CID every iteration
      do {
        flag = false;
        cid = Helper.randomRange(0, cust.length-1);
        for(int k=0; k<i; k++)
          if (accOn[k].getCID() == cid) { flag = true; }
      }
      while(flag);

      accOn[i] = new Online(cust[cid], set, cid);
      printDebug(String.format(" %3d", cid),
        i % 20 == 19 || i == accOn.length-1);
    }
  }

  /** buildOffline() generates all offline accounts that are owned by the
    * customers of the bank. An account is generated for most of the customers
    * with a random starting balance, type, and monthly fee. Each account
    * is associated to its customer by an entry into the ACP list. */

  public static void buildOffline() {
    accOff = new Offline[ (int) (cust.length * PER_OFFLINE) ];
    printDebug("Account-Customer pairing");

    for(int i=0; i<accOff.length; i++) {
      int cid;
      boolean flag = false;

      // Select a unique customer for every account
      do {
        flag = false;
        cid = Helper.randomRange(0, cust.length-1);
        for(ACP pair : acp) { if (pair.cid == cid) { flag = true; } }
      }
      while(flag);

      accOff[i] = new Offline(cust[cid]);

      // Add account-customer pair to the acp list
      ACP pair = new ACP(cid, accOff[i].getAID());
      acp.add(pair);

      printDebug(String.format(" (%s,%3d)", pair.aid, pair.cid),
        i % 8 == 7 || i == accOff.length-1);
    }
  }

  /** buildCards() assigns cards to every account from the date that they opened
    * to either an expiry date being after the present day, or after the account
    * closed. It assigns multiple cards to the accounts. */

  public static void buildCards() {
    printDebug("Cards processing");

    for(int i=0; i<accOff.length; i++) {
      LocalDate date = accOff[i].getOpen(), cl = accOff[i].getClose();

      printDebug(" "+accOff[i].getAID(), i % 10 == 9 || i == accOff.length-1);

      // Add cards for every three years to the account
      do {
        cards.add(new Card(accOff[i].getAID(), ACP.getAIDCust(acp, accOff[i].getAID())));
        cards.get(cards.size()-1).setDate(date);
        date = date.plusYears(3);
      }
      while(cl == null ? date.isBefore(LocalDate.now()) : date.isBefore(cl));

      // Activate the last card if the account isn't closed
      if (cl != null)
        cards.get(cards.size()-1).setStatus(Card.Status.ACTIVE);
    }
  }

  /****************************************************************************/

  /** expandCountry() loads in all store information for usage in
    * transaction generation. It expects a file of the following form:
    *
    * <# categories>
    * <# shops in cat 1> <flags> <comma-delimited-prices>
    * shop
    * shop
    * ...
    *
    * Flags are as follows:
    * + O : Online store
    * + R : Prices are a range
    * + C : Choose a price from the list given
    */

  public static void expandCountry() throws IOException {
    BufferedReader r = new BufferedReader(new FileReader(FILE_IN_STORE));
    printDebug("Country Expansion");
    
    for(int i=Integer.parseInt(r.readLine()); i>0; i--) {
      // cnt is the number of stores in the category
      // dat is the store's data
      int cnt = Integer.parseInt(r.readLine());
      String[] dat = r.readLine().split(" ");

      for(int k=0; k<cnt; k++) {
        stores.add(new Store(
          r.readLine(), dat[1].contains("R"), dat[1].contains("O"), dat[2],
          Branch.getRoute(), Offline.newAID()
        ));
      }

      printDebug(" Category: "+dat[0]);
    }

    r.close();
  }

  /** buildBranches() generates routing numbers for each of the branches in the
    * different states of the country, and then uses those numbers to initialize
    * each account's first transaction. */

  public static void buildBranches() {
    // Generate routing numbers for bank branches (one per state)
    branches = new Branch[country.numStates()];
    for(int i=0; i<branches.length; i++)
      branches[i] = new Branch();
    
    // Generate initial transaction
    printDebug("Transactions (Initial)");
    for(int i=0; i<accOff.length; i++) {
      trans.add(
        new Transaction(
          accOff[i].getAID(), branches[getBranch(i)].route,
          accOff[i].getAID(), "COUNTER DEPOSIT", accOff[i].getBal(),
          accOff[i].getOpen(), Transaction.Type.CREDIT)
      );
      printDebug(" "+accOff[i].getAID(), i % 10 == 9 || i == accOff.length-1);
    }
  }

  // PER_WORK is the number of days between payment from work
  // PER_FEE is the number of days between each monthly fee charge
  private static final int PER_WORK = 14, PER_FEE = 31;

  /** buildTransactions() builds all the transactions that apply to the different
    * accounts, including work-related deposits, monthly account fees, as well
    * as randomized spending throughout the lifetime of the account. As the
    * transactions are made, the balance is updated, and if the account cannot
    * make a purchase, the transaction is disregarded. */

  public static void buildTransactions() {
    printDebug("Transactions (Purchases)");

    for(int i=0; i<accOff.length; i++) {
      // trkCred is a credit the person earns from work every 14 days
      // trkFee is the monthly fee tracker that arises every 31 days
      // earn is an RNGed lower/upper bound for the person's 'paycheck'
      int trkCred = 0, trkFee = 0;
      int[] earn = {Helper.randomRange(175,225), Helper.randomRange(400,550)};
      LocalDate date = accOff[i].getOpen();

      // Stop generating transactions when we surpass the closure date or
      // the present day
      while(date.isBefore(LocalDate.now()) || (accOff[i].getClose()!=null && date.isBefore(accOff[i].getClose()))) {
        // inc determines how much to increment the date for the next trans
        int inc = Helper.randomRange(1, 5), numToday = Helper.randomRange(1,2);

        // Check the credit tracker
        if (trkCred >= PER_WORK) {
          trkCred %= PER_WORK;

          trans.add(
            new Transaction(
              accOff[i].getAID(), branches[getBranch(i)].route, accOff[i].getAID(),
              (Helper.randomRange(0,1) == 0 ? "COUNTER" : "DIRECT")+" DEPOSIT",
              Helper.randomRange(earn[0], earn[1]),
              date.minus(Period.ofDays(trkCred)), Transaction.Type.CREDIT
            ));

          accOff[i].credit(trans.get(trans.size()-1).getAmount());
        }
        else { trkCred += inc; }

        // Check the monthly fee tracker
        if (trkFee >= PER_FEE && accOff[i].getFee() > 0.0) {
          trkFee %= PER_FEE;

          trans.add(
            new Transaction(
              accOff[i].getAID(), branches[getBranch(i)].route,
              branches[getBranch(i)].aid, "ACCOUNT MONTHLY FEE",
              accOff[i].getFee(), date.minus(Period.ofDays(trkFee)),
              Transaction.Type.DEBIT
            ));

          accOff[i].debit(trans.get(trans.size()-1).getAmount());
        }
        else { trkFee += inc; }

        // Regular transactions - only make the purchase if the account has
        // enough funds to make the purchase
        for(int k=0; k<numToday; k++) {
          Store s = stores.get(Helper.randomRange(0, stores.size()-1));
          double charge = s.getPrice();

          if (accOff[i].hasBal(charge)) {
            trans.add( new Transaction(
              accOff[i].getAID(), s.getRoute(), s.getAID(), s.getName().toUpperCase(),
              charge, date, Transaction.Type.DEBIT
            ));
            
            accOff[i].debit(charge);
            //printDebug("Transaction on "+accOff[i].getAID()+
            //  " for $"+String.format("%.2f",charge)+" to '"+s.getName()+"'");
          }
        }

        date = date.plusDays(inc);
      }

      printDebug(" "+accOff[i].getAID(), i % 10 == 9 || i == accOff.length-1);
    }
  }

  /** printSQL() prints all of the insert into statements for the tables in
    * the database. It takes care of fetching the necessary values from each
    * class, and forming them into a tuple based on the column order above. */

  public static void printSQL() throws IOException {
    BufferedWriter w = new BufferedWriter(new FileWriter(FILE_OUT_QUERY));

    // Customer information
    w.write(sqlHead("Customer", TBL_CUST)+"\n");
    for(int i=0; i<cust.length; i++) {
      Customer c = cust[i];
      State s = country.getState(c.getState());
      City ct = s.getCity(c.getCity());

      w.write(String.format(
        "  ('%s', '%s', '%s', '%c', '%s', '%s', '%s', '%s', NULL, '%s', %d, '%s')%s",
        c.getSSN(), c.getFirstName().replaceAll("'","''"),
        c.getLastName().replaceAll("'","''"), c.getSex(),
        c.getDOBStr(), c.getEmail(), s.getCode(c.getCity())+c.getPhone(),
        c.getHouse()+" "+ct.getStreet(c.getStreet()), ct.getName(),
        ct.getZip(), s.getName(),
        (i==cust.length-1 ? ";\n" : ",\n")
      ));
    }

    // Employee
    w.write(sqlHead("Employee", TBL_EMP)+"\n");
    for(int i=0; i<emp.length; i++)
      w.write(String.format("  ('%s', %12s, %3d, %3s)%s",
        emp[i].getStartStr(),
        (emp[i].getEnd()==null ? "NULL" : "'"+emp[i].getEndStr()+"'"),
        emp[i].getCID()+1, emp[i].getSup(),
        (i==emp.length-1 ? ";\n" :
          (i%3==2 ? ",\n" : ", "))));
    
    // Account_Online
    w.write(sqlHead("Account_Online", TBL_ACCT_ON)+"\n");
    for(int i=0; i<accOn.length; i++)
      w.write(String.format("  (%3d, %20s, '%s')%s",
        accOn[i].getCID()+1, "'"+accOn[i].getUser()+"'", accOn[i].getPass(),
        (i==accOn.length-1 ? ";\n" :
          (i%3==2 ? ",\n" : ", "))));

    // Recovery_Question
    w.write(sqlHead("Recovery_Question", TBL_REC)+"\n");
    for(int i=0; i<accOn.length; i++) {
      for(int k=0; k<Online.QUE_COUNT; k++) {
        w.write(String.format("  (%d, %d, '%s', '%s', '%s')%s",
          accOn[i].getCID()+1, k, accOn[i].getDate(),
          set.getQuestion(accOn[i].getQue(k)),
          set.getAnswer(accOn[i].getQue(k), accOn[i].getAns(k)),
          (k == Online.QUE_COUNT-1 ? "" : ",\n")));
      }
      w.write(i==accOn.length-1 ? ";\n" : ",\n");
    }

    // Account
    w.write(sqlHead("Account", TBL_ACCT)+"\n");
    for(int i=0; i<accOff.length; i++) {
      w.write(String.format("  ('%s', '%s', '%s', %12s, %.2f, %1.5f, '%s', %.2f)%s",
        accOff[i].getAID(), accOff[i].getType(), accOff[i].getOpenStr(),
        accOff[i].getClose() == null ? "NULL" : "'"+accOff[i].getCloseStr()+"'",
        accOff[i].getBal(), accOff[i].getRate(), accOff[i].getComp(),
        accOff[i].getFee(),
        i==accOff.length-1 ? ";\n" : ",\n"));
    }

    // Account_Owner
    w.write(sqlHead("Account_Owner", TBL_ACCTOWN)+"\n");
    for(int i=0; i<acp.size(); i++)
      w.write(String.format("  ('%s', %3d)%s",
        acp.get(i).aid, acp.get(i).cid+1,
        (i==acp.size()-1 ? ";\n" :
         (i%4==3 ? ",\n" : ", "))));
      
    // Card
    w.write(sqlHead("Card", TBL_CARD)+"\n");
    for(int i=0; i<cards.size(); i++) {
      Card cr = cards.get(i);
      w.write(String.format("  ('%s', '%s', '%s', '%s', '%s', %d, '%s')%s",
        cr.getNumber(), cr.getDateStr(), cr.getSec(), cr.getStat(), cr.getAID(),
        cr.getCID()+1, cr.getPIN(),
        i==cards.size()-1 ? ";\n" : ",\n"));
    }

    w.close();

    // Transactions
    if (!SKIP_TRANS) {
      w = new BufferedWriter(new FileWriter(FILE_OUT_TRANS));
      w.write(sqlHead("Transaction", TBL_TRAN)+"\n");
      for(int i=0; i<trans.size(); i++) {
        Transaction t = trans.get(i);
        w.write(String.format("  ('%s', '%s', '%s', '%s', '%s', %.2f, '%s', '%s', NULL)%s",
          t.getAID(), t.isPending() ? "true" : "false", t.getType(), t.getDateStr(),
          t.getDesc(), t.getAmount(), t.getRecRoute(), t.getRecAID(),
          i==trans.size()-1 ? ";\n" : ",\n"));
      }
      w.close();
    }
  }
}