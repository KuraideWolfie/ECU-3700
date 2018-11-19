/**
  * SubModify is a subcommand of account, allowing the user to close an account,
  * add or remove owners on accounts, or manage the cards of an account. It only
  * allows this if the account ISNT closed.
  *
  * Author: Matthew Morgan
  * Date: 16 November 2018
  */

package cmd.acct;

import cmd.Command;
import cmd.CmdAccount;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.time.LocalDate;

public class SubModify extends Command {
  private String aid;
  private ArrayList<Integer> ids = new ArrayList<>();
  private ArrayList<String> crd = new ArrayList<>();

  public void execute() {
    // Get the account ID the user wishes to modify, and check it isn't closed
    try {
      System.out.println("What account do you wish to modify (type aid)?");
      aid = prompt("ACCT > MOD > AID");

      ps = con.genQuery("SELECT \"AID\", \"Date_Close\" FROM \"Account\" WHERE \"AID\"="+aid);
      rs = ps.executeQuery();

      if (!rs.next())
        throw new Exception("The given account ID doesn't exist");
      else if (rs.getString("Date_Close") != null)
        throw new Exception("The given account is closed as of "+rs.getString("Date_Close"));

      rs.close();
      ps.close();
      subHelp(); // Show help here so it only prints once
    }
    catch(SQLException e) { System.out.println("ERR: "+e.getMessage()); return; }
    catch(Exception e) { System.out.println("ERR: "+e.getMessage()); return; }

    while(true)
      try {
        // Loop retrieval of commands until return is passed
        String cmd = prompt("ACCT > MOD");

        switch(cmd.toLowerCase()) {
          case "help": subHelp(); break;
          case "card": subCard(); break;
          case "owner": subOwn(); break;
          case "close": if (subClose()) { return; } break;
          case "credit": subCredit(false); break;
          case "debit": subCredit(true); break;
          // DEBUG: Get the account's balance
          case "bal":
            ps = con.genQuery("SELECT \"Balance\" FROM \"Account\" WHERE \"AID\" = "+aid);
            rs = ps.executeQuery();
            rs.next();
            System.out.println("Account balance: "+rs.getDouble("Balance"));
            rs.close();
            ps.close();
            break;
          default:
            if (Command.isReturn(cmd)) { return; }
            else
              System.out.println("Unknown command. Type 'help' for a list");
        }
      }
      catch(SQLException e) {
        System.out.println("SQL-ERR: "+e.getMessage());
      }
      catch(Exception e) {
        System.out.println("ERR: "+e.getMessage());
      }
  }

  /** subHelp() shows a list of subcommands for the modify subcommand, and also
    * shows the account ID currently being affected by the command. */
    
  private void subHelp() {
    System.out.println(
      "Subcommands for Account > Mod (AID "+aid+")\n"+
      "---------------------------------------------------------------------\n"+
      "card     Manipulate cards on the account\n"+
      "owner    Manipulate owners of the account\n"+
      "credit   Credit account for a deposit\n"+
      "debit    Process a withdrawal for an account\n"+
      "close    Close an account\n"+
      "ret      Return to account command\n\n"+
      "Use 'ret' to return and then recall this command to change AID"
    );
  }

  /** subCredit() allows the user to make a dep/withdrawal into/from the account
    * following this procedure:
    *
    * <ul>
    *  <li>Ask for the amount (a positive amount)
    *  <li>Update the account's balance to reflect the change
    *  <li>Issue a transaction on the account regarding the change
    * </ul>
    *
    * @param isDeb True if this transaction is debit instead of credit*/

  private void subCredit(boolean isDeb) throws SQLException {
    try {
      System.out.println("How much should the account be "+(isDeb?"debited":"credited")+"?");
      double amnt = Double.parseDouble(prompt("ACCT > MOD > "+(isDeb?"DEBIT":"CREDIT")));

      if (amnt < 0)
        throw new Exception("Amount being "+(isDeb ? "withdrawn" : "deposited")+" cannot be negative");

      // Deposit the amount to the account and make a transaction
      ps = con.genQuery(
        "UPDATE \"Account\" SET \"Balance\" = (SELECT \"Balance\""+(isDeb?"-":"+")+amnt+
        " FROM \"Account\" WHERE \"AID\" = "+aid+") WHERE \"AID\" = "+aid+";"+
        "INSERT INTO \"Transaction\"(\"AID\",\"Type\",\"Date\",\"Amount\",\"Rec_Route\",\"Rec_AID\",\"Desc\",\"isPending\") VALUES "+
        "("+aid+",'"+(isDeb?"DEBIT":"CREDIT")+"','"+LocalDate.now().toString()+"',"+amnt+",?,?,?,true);"
      );
      ps.setString(1, isDeb ? "'bank'" : "'self'");
      ps.setString(2, isDeb ? "'bank'" : "'self'");
      ps.setString(3, isDeb ? "'COUNTER CHECK'" : "'COUNTER DEPOSIT'");

      if (ps.executeUpdate() > 0)
        System.out.println("Transaction was made successfully");
      
      ps.close();
    }
    catch(Exception e) {
      System.out.println("Transaction failed: "+e.getMessage());
    }
  }

  /** subClose() closes an account. It follows this procedure:
    *
    * <ul>
    *  <li>Ask for the user to type confirmation
    *  <ul>
    *   <li>If they don't confirm, return the account didn't close
    *  </ul>
    *  <li>Close all cards on the account
    *  <li>Set the close date of the account
    * </ul>
    *
    * @return True if the account closed, or false if not
   */

  private boolean subClose() throws SQLException {
    // Confirm closure of the account
    System.out.println("Confirm you want to close the account: type 'yes'");
    if (!isYes(prompt("ACCT > MOD > CLOSE"))) {
      System.out.println("Operation aborted");
      return false;
    }
    
    // Close all cards on the account and set the account's close date
    ps = con.genQuery(
      "UPDATE \"Card\" SET \"Status\"='CLOSED' WHERE \"AID\"="+aid+" AND NOT \"Status\"='CLOSED';"+
      "UPDATE \"Account\" SET \"Date_Close\"='"+LocalDate.now().toString()+"' WHERE \"AID\"="+aid
    );
    System.out.println(ps.executeUpdate()+" cards closed for the account");
    System.out.println("Account "+aid+" was closed successfully");
    ps.close();

    return true;
  }

  /** subOwn() serves as a hub for commands regarding owner management for
    * an account, such as adding and removing owners from the account. */

  private void subOwn() throws SQLException, Exception {
    System.out.println(
      "Subcommands for Account > Mod > Own\n"+
      "---------------------------------------------------------------------\n"+
      "list   Show a list of owners on the account\n"+
      "new    Add an owner to the account\n"+
      "rem    Remove an owner from the account"
    );

    switch(prompt("ACCT > MOD > OWN")) {
      case "list": showOwner(); break;
      case "new": subOwnNew(); break;
      case "rem": subOwnRem(); break;
      default:
        System.out.println("Command not recognized");
    }
  }

  /** subOwnNew() assigns a new owner to an account by doing the following:
    *
    * <ul>
    *  <li>Ask if the owner being assigned is a new customer
    *  <li>If the owner is new, create a new customer entry
    *  <li>If the owner isn't new, get the customer ID
    *  <li>Assign the owner (throw an SQLException if the CID already has access
    *      to the account as an owner)
    * </ul> */

  private void subOwnNew() throws SQLException, Exception {
    System.out.println("Is the owner being added a new customer? (y/n)");

    // Ask if the customer is a new customer
    String isNew, cid;
    do { isNew = prompt("ACCT > MOD > OWN"); }
    while(!isYes(isNew) && !isNo(isNew));

    // Add a customer to the database if this is a new customer
    // Get the CID of the customer to add otherwise
    if (isYes(isNew))
      cid = CmdAccount.getCustomer(scan, con);
    else {
      System.out.println("Which customer will own this account? (Type name)");
      String[] name = prompt("ACCT > MOD > OWN > Name").trim().replaceAll("'","''").split(" ");

      // Get the list of CIDs with the given name
      ids.clear();
      ps = con.genQuery(
        "SELECT \"CID\", \"SSN\", \"Con_Phone\" FROM \"Customer\" WHERE "+
        "\"Fname\"='"+name[0]+"' AND \"Lname\"='"+name[1]+"'"
        );
      rs = ps.executeQuery();
      System.out.println("(CID) SSN, Phone");
      System.out.println("-------------------------------------------------");
      while(rs.next()) {
        System.out.printf("(%4d) %s, %s\n",
          rs.getInt("CID"), rs.getString("SSN"), rs.getString("Con_PHone"));
        ids.add(rs.getInt("CID"));
      }
      rs.close();
      ps.close();
      System.out.println();

      // Get a CID
      cid = getOwner();
    }

    ps = con.genQuery("INSERT INTO \"Account_Owner\" VALUES ("+cid+","+aid+")");
    if (ps.executeUpdate() == 1)
      System.out.println("New owner assigned succesfully");
    ps.close();
  }

  /** subOwnRem() removes an owner from an account, or throws an error if there
    * is only one owner listed for an account. */

  private void subOwnRem() throws SQLException, Exception {
    if (showOwner() == 1)
      throw new Exception("There is only one owner on the account");

    String cid = getOwner();
    ps = con.genQuery(
      "DELETE FROM \"Account_Owner\" WHERE \"AID\"="+aid+" AND \"CID\"="+cid
    );
    if (ps.executeUpdate() == 1)
      System.out.println("Owner successfully removed");
    ps.close();
  }

  /** subCard() is a hub for all subcommands that manage cards on an account. It
    * calls on the necessary methods to execute its commands. */

  private void subCard() throws SQLException, Exception {
    System.out.println(
      "Subcommands for Account > Mod > Card\n"+
      "---------------------------------------------------------------------\n"+
      "list   List pending/active/disabled cards attached to the account\n"+
      "new    Add a card to the account\n"+
      "tog    Toggles the enabled/disabled status of a card\n"+
      "close  Close a card, pending or not"
    );

    switch(prompt("ACCT > MOD > CARD")) {
      case "list": showCard(false); break;
      case "new": subCardNew(); break;
      case "tog": subCardEnable(false); break;
      case "close": subCardEnable(true); break;
      default:
        System.out.println("Command not recognized");
    }
  }

  /** subCardEnable(close) toggles the status of a card on the account between
    * enabled and disabled. Cards that are 'pending' or 'closed' cannot be
    * changed. If 'close' is true, then it closes the card and shows 'pending'
    * cards in the list. It follows this procedure:
    *
    * <ul>
    *  <li>List the cards on the account
    *  <li>Prompt for a card number, if there are multiple
    *  <ul>
    *   <li>An error is thrown if no cards are eligible for toggle
    *  </ul>
    *  <li>Change the status of the card
    * </ul>
    *
    * @param close True if the card should be closed instead of disabled */

  private void subCardEnable(boolean close) throws SQLException, Exception {
    // Get the card number to toggle the status of
    String card;
    if (showCard(!close) > 1) {
      System.out.println("Enter the card number you wish to toggle; -1 to cancel");
      do { card = prompt("ACCT > MOD > CARD > NUM", false); }
      while(!crd.contains(card) && !card.equals("-1"));

      if (card.equals("-1")) return;
    }
    else if (crd.size() == 0)
      throw new Exception("There are no eligible cards on the account");
    else card = crd.get(0);

    // Change the status of the card
    String stat;
    ps = con.genQuery("SELECT \"Status\" FROM \"Card\" WHERE \"Number\"='"+card+"'");
    rs = ps.executeQuery();
    rs.next();
    stat = rs.getString("Status");
    rs.close();
    ps.close();

    if (close)
      stat = "CLOSED";
    else
      switch(stat) {
        case "DISABLED": stat = "ACTIVE"; break;
        case "ACTIVE": stat = "DISABLED"; break;
        default:
          throw new Exception("Status invalid. How did you get here? (Check DB)");
      }

    ps = con.genQuery("UPDATE \"Card\" SET \"Status\" = '"+stat+
      "' WHERE \"Number\" = '"+card+"'");
    if (ps.executeUpdate() == 1)
      System.out.println("Card status toggled successfully");
    ps.close();
  }

  /** subCardNew() generates a new card for an account, assigning that card to
    * the customer attached to the account (or, in the case of multiple, the one
    * that the user selects from the owners). It follows this procedure:
    *
    * <ul>
    *  <li>List the owners on the account
    *  <li>Get the owner for the new card, if multiple owners exist
    *  <li>Get the new card's number, security code, and pin
    *  <ul>
    *   <li>If the user wishes, they can enter the new card number (temp cards)
    *  </ul>
    *  <li>Add the new card to the list of cards in the database
    * </ul> */

  private void subCardNew() throws SQLException, Exception {
    String cid, crd="", sec="", pin="";
    Random r = new Random();
    
    // Get the CID to add the card to
    if (showOwner() > 1)
      cid = getOwner();
    else
      cid = ids.get(0).toString();
    
    // Ask if this is a temporary card
    String ans;
    System.out.println("Is this a temporary card? (y/n)");
    do { ans = prompt("ACCT > MOD > CARD"); }
    while(!isYes(ans) && !isNo(ans));

    // Get the card number
    do {
      if (isNo(ans)) {
        crd = "";
        for(int i=0; i<16; i++) crd += r.nextInt(10);
      }
      else {
        if (crd.equals(""))
          System.out.println("Type the number of the new card");
        else
          throw new Exception("Card number given already exists in the database");

        crd = prompt("ACCT > MOD > CARD > NUM");
      }

      ps = con.genQuery("SELECT \"Number\" FROM \"Card\" WHERE \"Number\" = '"+crd+"'");
      rs = ps.executeQuery();
    }
    while(rs.next());
    rs.close();
    ps.close();

    // SEC, PIN numbers
    for(int i=0; i<3; i++) sec += r.nextInt(10);
    for(int i=0; i<4; i++) pin += r.nextInt(10);

    // Insert the card into the database
    ps = con.genQuery("INSERT INTO \"Card\" VALUES ('"+crd+"','"+
      LocalDate.now().toString()+"','"+sec+"','"+pin+"', "+
      (isNo(ans) ? "'PENDING'" : "'ACTIVE'")+
      ","+aid+","+cid+")");
    if (ps.executeUpdate() == 1)
      System.out.println("Card was added successfully");
    ps.close();
  }

  /** showOwner() fetches all the owners of the account, and shows them, their
    * SSN, and phone numbers to the screen. It returns the number of owners. (It
    * also loads all the CIDs for the customers into the list 'ids').
    *
    * @return The number of owners for the account */

  private int showOwner() throws SQLException {
    int own = 0;
    PreparedStatement p = con.genQuery(
      "SELECT \"CID\", \"Fname\", \"Lname\", \"SSN\", \"Con_Phone\" FROM \"Customer\""+
      " WHERE \"CID\" IN (SELECT \"CID\" FROM \"Account_Owner\" WHERE \"AID\"="+aid+")"
    );
    ResultSet r = p.executeQuery();
    ids.clear();

    System.out.println("(CID) SSN, Phone, Name -- Owners of Account");
    System.out.println("-----------------------------------------------------");
    while(r.next()) {
      System.out.printf(
        "(%4d) %9s, %10s, %s %s\n", r.getInt("CID"), r.getString("SSN"),
        r.getString("Con_Phone"), r.getString("Fname"), r.getString("Lname")
      );
      ids.add(r.getInt("CID"));
      own++;
    }

    r.close();
    p.close();

    return own;
  }

  /** showCard(nar) prints all the cards that are presently enabled, pending, or
    * disabled on the account. It loads the card numbers into list 'crd'
    *
    * @param nar Narrows the results to not include 'pending' cards if true
    * @return The number of cards enabled, pending, or disabled on the account */

  private int showCard(boolean nar) throws SQLException {
    PreparedStatement p = con.genQuery(
      "SELECT \"Number\", \"Card\".\"CID\", \"Status\", \"Fname\" FROM "+
      "\"Customer\", \"Card\" WHERE \"AID\"="+aid+" AND NOT \"Status\" IN ('CLOSED'"+
      (nar ? ",'PENDING'" : "")+") AND \"Card\".\"CID\" = \"Customer\".\"CID\""
    );
    ResultSet r = p.executeQuery();
    int card = 0;
    crd.clear();

    System.out.println("Cards attached to this account (CID,NAM,NUM,STATUS):");
    while(r.next()) {
      System.out.print(String.format( "(%s, %s, %s, %s)\n",
        r.getString("CID"), r.getString("Fname"), r.getString("Number"), r.getString("Status")
      ));
      crd.add(r.getString("Number"));
      card++;
    }
    r.close();
    p.close();

    return card;
  }

  /** getOwner() uses the 'ids' list to prompt the user for a CID from that
    * list to get an owner of the account.
    *
    * @return The CID selected by the user, as a string */

  private String getOwner() {
    String res;
    System.out.println("Which of these customers are you referencing?");
    do {
      res = prompt("ACCT > MOD > CID",false);
    }
    while(!ids.contains(Integer.parseInt(res)));
    return res;
  }
}