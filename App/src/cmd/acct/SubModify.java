/**
  * SubModify is a subcommand of account, allowing the user to close an account,
  * add or remove owners on accounts, or manage the cards of an account.
  *
  * Author: Matthew Morgan
  * Date: 16 November 2018
  */

package cmd.acct;

import cmd.Command;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.time.LocalDate;

// Modify account
// -- Add a new owner, or remove an owner (or swap if only 1 owner)
// -- Make a deposit, withdrawal, transfer
// -- Add a new card, dis/enable card, or close a card
//    + Add a new card
//    + Dis/Enable a card
// -- Close an account

public class SubModify extends Command {
  private String aid;
  private ArrayList<Integer> ids = new ArrayList<>();
  private ArrayList<String> crd = new ArrayList<>();

  public void execute() {
    try {
      // Get the account ID the user wishes to modify
      System.out.println("What account do you wish to modify (type aid)?");
      aid = prompt("ACCT > MOD > AID");

      ps = con.genQuery("SELECT \"AID\" FROM \"Account\" WHERE \"AID\"="+aid);
      rs = ps.executeQuery();
      if (!rs.next()) {
        throw new Exception("The given account ID doesn't exist");
      }
      rs.close();
      ps.close();

      // -----------------------------------------------------------------------
      subHelp();

      // Loop retrieval of commands until return is passed
      while(true) {
        String cmd = prompt("ACCT > MOD");

        switch(cmd.toLowerCase()) {
          case "card": subCard(); break;
          default:
            if (Command.isReturn(cmd)) { return; }
            else
              System.out.println("Unknown command. Type 'help' for a list");
        }
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
      "ret      Return to account command\n\n"+
      "Use 'ret' to return and then recall this command to change AID"
    );
  }

  /** subCard() is a hub for all subcommands that manage cards on an account. It
    * calls on the necessary methods to execute its commands. */

  private void subCard() throws SQLException, Exception {
    System.out.println(
      "Subcommands for Account > Mod > Card\n"+
      "---------------------------------------------------------------------\n"+
      "new    Add a card to the account\n"+
      "tog    Toggles the enabled/disabled status of a card"
    );

    switch(prompt("ACCT > MOD > CARD")) {
      case "new": subCardNew(); break;
      case "tog": subCardEnable(); break;
      default:
        System.out.println("Command not recognized");
    }
  }

  /** subCardEnable() toggles the status of a card on the account between being
    * enabled and disabled. Cards that are 'pending' or 'closed' cannot be
    * changed. It follows this procedure:
    *
    * <ul>
    *  <li>List the cards on the account
    *  <li>Prompt for a card number, if there are multiple
    *  <ul>
    *   <li>An error is thrown if no cards are eligible for toggle
    *  </ul>
    *  <li>Change the status of the card
    * </ul> */

  private void subCardEnable() throws SQLException, Exception {
    // Get the card number to toggle the status of
    String card;
    if (showCard(true) > 1) {
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
      res = prompt("ACCT > MOD > CID");
    }
    while(!ids.contains(Integer.parseInt(res)));
    return res;
  }
}