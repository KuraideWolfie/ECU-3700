package cmd;

public class CmdHelp extends Command {
  public void execute() {
    System.out.println(
      "Command List for DBMS Console Application\n"+
      "---------------------------------------------------------------------\n"+
      "customer   Perform actions related to customers\n"+
      "account    Perform actions related to accounts\n"+
      "help       Displays this list of base commands\n"+
      "reset      DEBUG: Reset the state of the database\n"+
      "exit       Terminates the program's execution"
    );
  }
}