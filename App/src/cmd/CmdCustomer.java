/**
  * CmdCustomer is where the main functionality of customer management occurs.
  * It allows the user to get information about a customer, edit their info,
  * and insert new customers into the database.
  *
  * Author: Matthew Morgan
  * Date: 8 November 2018
  */

package cmd;

import java.util.Scanner;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dbase.CID;
import dbase.Relations;

public class CmdCustomer extends Command {
  public CmdCustomer() {
    super();
    addProp("SSN", "SSN");
    addProp("Gender", "Gender");
    addProp("DOB", "DOB");
    addProp("Email", "Con_Email");
    addProp("Phone", "Con_Phone");
    addProps("Contact", new String[] {"Con_Email", "Con_Phone"});
    addProps("Address", new String[] {"Street","Apt","City","State","Zip"});
  }

  public void execute() {
    boolean loop = hasScanner();

    // Show a list of subcommands
    if (loop) { subHelp(); }

    // Infinitely loop until a return command is found
    while(loop) {
      String cmd = prompt("CUST");

      if (Command.isReturn(cmd.toLowerCase()))
        loop = false;
      else {
        switch(cmd.toLowerCase()) {
          case "get": subGet(); break;
          case "mod": subMod(); break;
          case "new": subNew(); break;
          case "help": subHelp(); break;
          default:
            System.out.println("Command doesn't exist; use 'help' for a list");
        }
      }
    }
  }

  /****************************************************************************/

  /** subHelp() displays a list of subcommands for the customer command. */

  private void subHelp() {
    System.out.println(
      "Subcommands for Customer:\n"+
      "-------------------------------------------------------------\n"+
      "get    Retrieve information (such as addresses and DOB)\n"+
      "mod    Modify a customer's information\n"+
      "new    Add a new customer to the database\n"+
      "ret    Exit command execution"
    );
  }

  /** props() prints a list of available properties for the user to select when
    * modifying or accessing customerinformation. */

  private void props() {
    System.out.println(
      "Properties: SSN, Gender, DOB, Email, Phone, Address\n"+
      "   Contact --> Email & Phone"
    );
  }

  /** subGet() is a subcommand available via the customer command that allows a
    * user to retrieve information about customers from the database. It follows
    * this procedure:
    * <ul>
    *  <li>Get what properties the user wants
    *  <li>Get the CID/names of customers
    *  <li>Display the resulting data | no result confirmation
    * </ul> */
  
  private void subGet() {
    System.out.println(
      "What properties and customers do you want information for?\n"+
      "+ Delimit properties with spaces (ie SSN Phone Address)\n"+
      "+ Delimit customer IDs/names with commas (ie Matt Morgan, 8, Josh Reyes)\n"
    );
    props();

    String[] keys = prompt("CUST > PROP").split(" "), in;

    if (!hasKeys(keys))
      System.out.println("Properties list not accepted");
    else {
      // Attempt to parse all of the CIDs provided and generate a query
      try {
        CID cid = new CID();
        String que = "";
        int results = 0;

        if (!cid.addAll(prompt("CUST > CIDs").split(","))) { throw new Exception(); }
        in = cid.getIn();
        keys = getProps(keys).toArray(new String[0]);

        // Query building
        que = "SELECT \"CID\", \"Lname\", "+buildSelect(keys)+" FROM \"Customer\" WHERE ";
        if (!in[0].equals("")) { que += "\"CID\" IN "+in[0]; }
        if (!in[1].equals("")) {
          if (!in[0].equals("")) { que += " OR "; }
          que += "(\"Fname\",\"Lname\") IN "+in[1];
        }

        ps = con.genQuery(que);
        rs = ps.executeQuery();

        // Generate the result string
        while(rs.next()) {
          String res = "", str;
          results++;

          for(String col : keys) {
            str = rs.getString(col);
            str = (str == null ? "null" : str.trim());
            switch(col) {
              case "SSN":     case "DOB":    case "Gender":
              case "Zip":
                res += str;
                break;
              case "Con_Phone":
                res += String.format("%10s", str);
                break;
              case "Con_Email":
                res += String.format("%35s", str);
                break;
              case "Street":
                res += String.format("%25s", str)+",";
                break;
              case "City":
                res += String.format("%15s", str)+",";
                break;
              case "Apt":     case "State":
                res += String.format("%5s", str)+",";
                break;
            }
            res += " ";
          }

          System.out.println(
            String.format("(%5d) %15s: ", rs.getInt("CID"),
              rs.getString("Lname")) + res
          );
        }

        if (results == 0)
          System.out.println("There were no results");
        
        rs.close();
        ps.close();
      }
      catch(SQLException e) {
        System.out.println("Query couldn't be generated");
        System.out.println(e.getMessage());
      }
      catch(Exception e) {
        System.out.println("Input rejected");
      }
    }
  }

  /** subMod() is a subcommand available via the customer command that allows a
    * user to modify a single customer's information per usage. It follows this
    * given procedure:
    * <ul>
    *  <li>Get the name or numerical ID of the customer to be modified
    *  <li>If there is no customer with the name/ID, reject input
    *  <li>If there are multiple customers with the given name:
    *  <ul>
    *   <li>Ask which customer the user wants to modify
    *  </ul>
    *  <li>Ask the user to input new data
    *  <li>Generate an update query and update the customer's information
    * </ul> */

  private void subMod() {
    System.out.println("Type the CID or full name of the customer you wish to modify.");
    String cid = prompt("CUST > CID"), que = "SELECT * FROM \"Customer\" WHERE ";
    ArrayList<String> res = new ArrayList<>();

    try {
      // Build the query
      if (cid.contains(" ")) {
        String[] spl;
        spl = cid.replace("'", "''").split(" ");
        que += "\"Fname\" = '" + spl[0] + "' AND \"Lname\" = '" + spl[1] + "'";
      }
      else {
        // Attempt an integer parse to catch if a word was given instead of CID
        try {
          que += "\"CID\" = " + Integer.parseInt(cid);
        }
        catch(Exception e) { throw new Exception("Invalid CID or name"); }
      }

      // Show results
      ps = con.genQuery(que);
      rs = ps.executeQuery();
      while(rs.next()) {
        System.out.println(getPerson());
        res.add(rs.getString("CID"));
      }
      rs.close();
      ps.close();

      // Show error if no results were found
      // Set CID if only one result was found
      // Prompt for CID if more than one was found
      if (res.size() == 0)
        throw new Exception("No customer with the identifying info exists");
      else if (res.size() == 1)
        cid = res.get(0);
      else {
        while(!res.contains((cid = prompt("CUST > CID")))) {
          System.out.println("Type a CID from the list of shown CIDs");
        }
      }

      // propFlag flags if at least one property was changed
      boolean propFlag = false;
      String[][] fields = Relations.getProps("CUST", Relations.TBL_CUSTOMER);
      que = "UPDATE \"Customer\" SET ";
      for(String[] f : fields)
        if (!f[1].equals("")) {
          propFlag = true;
          que += "\""+f[0]+"\" = '"+f[1].replaceAll("'","''")+"',";
        }
      que = que.substring(0, que.length()-1) + " WHERE \"CID\" = " + cid;

      if (!propFlag) { throw new Exception("Operation aborted - no prop changes"); }

      ps = con.genQuery(que);
      if (ps.executeUpdate() == 1)
        System.out.println("Operation successful");
      else
        System.out.println("Operation unsuccessful");
      ps.close();
    }
    catch(SQLException e) {
      System.out.println("Error during SQL handling");
      System.out.println(e.getMessage());
    }
    catch(Exception e) {
      System.out.print("Input rejected: ");
      System.out.println(e.getMessage());
    }
  }

  /** getPerson() builds a string representing a single person, as fetched from
    * the database when the user wishes to modify a customer's info. */

  private String getPerson() throws SQLException {
    return String.format(
      "(%4d) %9s %s, %s : %s, %s, %s, %s\n    %-25s, %5s, %15s, %d, %s",
      rs.getInt("CID"), rs.getString("SSN"), rs.getString("Lname"),
      rs.getString("Fname"), rs.getString("Gender"), rs.getString("DOB"),
      rs.getString("Con_Phone"), rs.getString("Con_Email"), rs.getString("Street"),
      rs.getString("Apt") == null ? "-----" : rs.getString("Apt"),
      rs.getString("City"), rs.getInt("Zip"), rs.getString("State")
    );
  }

  /** subNew() is a subcommand of customer that allows the user to enter a new
    * customer into the database. It follows the following procedure:
    * <ul>
    *  <li>Get the properties of the customer to be added
    *  <li>Generate a query and attempt to insert the customer
    * </ul> */

  private void subNew() {
    try {
      String[][] fields = Relations.getProps("CUST > NEW", Relations.TBL_CUSTOMER, false);
      String que = "INSERT INTO \"Customer\" " +
        Relations.getInsert(Relations.TBL_CUSTOMER) + " VALUES (";

      for(String[] f : fields) {
        switch(f[0]) {
          case "Zip":
            if (!f[1].equals("")) { que += Integer.parseInt(f[1]); }
            else { que += "'null'"; }
            break;
          default:
            if (!f[1].equals("")) { que += "'"+f[1].replaceAll("'","''")+"'"; }
            else { que += "'null'"; }
        }
        que += ",";
      }

      que = que.substring(0,que.length()-1) + ")";

      ps = con.genQuery(que);
      if (ps.executeUpdate() == 1)
        System.out.println("Operation successful");
      ps.close();
    }
    catch(Exception e) {
      // Zip wasn't an integer
      // Database rejected input
      // Misc error
      System.out.println("Input rejected: "+e.getMessage());
    }
  }
}