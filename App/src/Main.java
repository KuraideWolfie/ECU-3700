/**
  * Main is the central driver of the application program, providing an infinite
  * loop for command entry and processing. It also parses all commandline args,
  * with the following being options:
  * + in <file>     : Preload command options from a text file
  *
  * Author: Matthew Morgan
  * Date: 7 November 2018
  */

import java.util.Scanner;

import dbase.Conn;
import cmd.Command;

public class Main {
  // con is the connection instance used during execution
  // scan is the scanner used globally
  // com is the command instance used for fetching main commands from the user
  static Conn con = null;
  static Scanner scan = null;
  static Command com = null;

  public static void main(String[] args) {
    boolean loop = true;

    try {
      argumentCheck(args);

      // Setup connection, com, and scanner
      con = new Conn("DBMSTermProject", "", "postgres");
      con.connect();
      scan = new Scanner(System.in);
      com = new Command() { public void execute() {} };
      com.setScanner(scan);

      System.out.println("Database connection established\n");
      Command.getCommand("help").execute();

      // Loop receival of commands until 'exit' is given
      while(loop) {
        String cmd = com.prompt("CMD");

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

  /** argumentCheck(args) checks the arguments provided to the program, and
    * executes all necessary actions based on those arguments.
    *
    * @param args The set of command line arguments passed to the program */

  private static void argumentCheck(String[] args) {
    try {
      for(int i=0; i<args.length; i++) {
        switch(args[i]) {
          // Input file command
          case "-in": case "in":
            Command.open(args[i+1]);
            i++;
            break;
          // Unrecognized command
          default:
            throw new Exception("Unrecognized cmd argument");
        }
      }
    }
    catch(Exception e) {
      System.out.println("Error: Arguments invalid");
      System.exit(1);
    }
  }
}