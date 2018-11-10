/**
  * CmdReset is a simple command that allows the user to reset the state of the
  * database, reloading the tables/types and data from SQL files.
  *
  * Author: Matthew Morgan
  * Date: 8 November 2018
  */

package cmd;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import dbase.Conn;

public class CmdReset extends Command {
  public void execute() {
    try {
      // Require the user to confirm reset by typing keyword
      System.out.println(
        "Type 'reset' to reset the database and reload data."
      );

      String sub = prompt("RES");

      switch(sub.toLowerCase()) {
        case "reset":
          System.out.println("Type creation script and data file paths.");

          // Get the creation and data SQL script paths
          String inque, indat;
          BufferedReader r;
          do { inque = prompt("RES > Creator"); } while (inque.equals(""));
          do { indat = prompt("RES > DatFile"); } while (indat.equals(""));

          if (!(new File(inque)).exists() || !(new File(indat)).exists())
            throw new IOException("One of the specified files doesn't exist");

          // Run the creation script
          r = new BufferedReader(new FileReader(inque));
          loadData(r);
          r.close();
          System.out.println("Database recreated successfully");

          // Run the data loading script
          r = new BufferedReader(new FileReader(indat));
          loadData(r);
          r.close();
          System.out.println("Data loaded successfully");
          break;
        default:
          System.out.println("Operation aborted");
      }
    }
    catch(SQLException e) {
      System.out.println("Error during database handling: "+e.getMessage());
    }
    catch(IOException e) {
      System.out.println("Error during file handling: "+e.getMessage());
    }
  }

  /** loadData(r) loads SQL queries from the bufferedreader provided, and
    * executes every query in the file linked to the reader.
    *
    * @param r A bufferedreader linked to a file with queries to execute */

  private void loadData(BufferedReader r) throws IOException, SQLException {
    String line, dat = "";

    while((line = r.readLine()) != null) {
      dat += line+"\n";
      
      if (line.contains(";")) {
        ps = con.genQuery(dat);
        ps.executeUpdate();
        ps.close();
        dat = "";
      }
    }
  }
}