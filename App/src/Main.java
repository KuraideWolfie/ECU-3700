import java.util.Scanner;

import dbase.Conn;
import cmd.*;

public class Main {
  static Conn con = null;
  static Scanner scan = null;

  public static void main(String[] args) {
    boolean loop = true;

    try {
      con = new Conn("DBMSTermProject", "", "postgres");
      con.connect();
      scan = new Scanner(System.in);

      System.out.println("Database connection established\n");
      Command.getCommand("help").execute();

      // Loop receival of commands until 'exit' is given
      while(loop) {
        System.out.print("\nCMD > ");
        String cmd = scan.nextLine();

        if (Command.isCommand(cmd)) {
          Command c = Command.getCommand(cmd);
          c.setCon(con);
          c.setScanner(scan);
          c.execute();
        }
        else if (Command.isReturn(cmd)) { loop = false; }
        else
          System.out.println("Command doesn't exist; use 'help' for a list");
      }

      System.out.println("\nDatabase connection terminated");

      con.disconnect();
      scan.close();
    }
    catch(Exception e) {
      System.out.println(e.getMessage());
    }
  }
}