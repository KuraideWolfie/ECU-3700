/**
  * Command provides a basis for all commands to pull from.
  *
  * Author: Matthew Morgan
  * Date: 6 November 2018
  */

package cmd;

import dbase.Conn;

import java.lang.ClassNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class Command {
  // in is a list of commands read from an input file; it's open to all commands
  protected static ArrayList<String> in = new ArrayList<>();

  // scan is a reference to a scanner object the command may utilize
  // prop contains referential keys and all the properties associated to them
  // con is a reference to the connection this command is handling
  // ps and rs are used for performing data processing on a query
  protected Scanner scan;
  protected Conn con;
  protected Hashtable<String,ArrayList<String>> prop;
  protected PreparedStatement ps;
  protected ResultSet rs;

  protected Command() {
    prop = new Hashtable<>();
    ps = null; rs = null; con = null; scan = null;
  }

  /** execute() executes this Command's main code. */

  public void execute() {
    System.out.println("I wasn't overridden...");
  }

  /****************************************************************************/

  /** setCon(c) sets the connection the command handles.
    *
    * @param c The database connection being handled */

  public final void setCon(Conn c) { con = c; }

  /** setScanner(s) sets the scanner this command will use.
    * 
    * @param s The scanner instance to assign this command */

  public final void setScanner(Scanner s) { scan = s; }

  /** hasScanner() returns true if this command has access to a scanner */

  protected boolean hasScanner() { return scan != null; }

  /** addProp(key, pro) associates a key with the given property. It ignores
    * duplicate properties.
    *
    * @param key The key to associate the property with
    * @param pro The property to associate with the key */

  protected Command addProp(String key, String pro) {
    if (!prop.containsKey(key))
      prop.put(key, new ArrayList<>());
    if (!prop.get(key).contains(pro))
      prop.get(key).add(pro);
    return this;
  }

  /** addProps(key, pro) associates all the given properties to the key. It
    * ignores duplicates.
    *
    * @param key The key to associate the properties with
    * @param pro An array of properties */

  protected Command addProps(String key, String[] pro) {
    for(int i=0; i<pro.length; i++)
      addProp(key, pro[i]);
    return this;
  }

  /** remProp(pro) removes the key from the table of keys with property
    * associations.
    *
    * @param pro The key to remove properties associated with
    * @return This instance (for chaining) */

  protected Command remProp(String pro) { prop.remove(pro); return this; }

  /** getProp(key) returns the list of properties associated with the key.
    *
    * @param key The key to get associated properties for
    * @return The list of properties associated with the key */

  protected ArrayList<String> getProp(String key) {
    ArrayList<String> L = new ArrayList<>();
    for(String v : prop.get(key))
      L.add(v);
    return L;
  }

  /** hasKey(key) returns whether the properties list contains a key that
    * references properties.
    *
    * @param key The property-referencing key to test existence of
    * @return True if the property set contains the referencing key */

  protected boolean hasKey(String key) { return prop.containsKey(key); }

  /** hasKeys(keys) returns whether all of the property-referencing keys is
    * contained in the collection of keys. 
    *
    * @param keys A list of property-referencing keys
    * @return True if all keys exist, or false if one or more is missing */

  protected boolean hasKeys(String[] keys) {
    for(String k : keys)
      if (!hasKey(k)) { return false; }
    return true;
  }

  /** getProps(keys) returns the list of all properties associated with all
    * the keys in the array.
    *
    * @param keys The list of keys to get properties for
    * @return A list of properties the keys associate with, ignoring duplicates */

  protected ArrayList<String> getProps(String[] keys) {
    ArrayList<String> L = new ArrayList<>();
    for(String k : keys) {
      for(String v : prop.get(k))
        if (!L.contains(v))
          L.add(v);
    }
    return L;
  }

  /** prompt(pr) shows a prompt to the user with a '>' signifying input. For
    * example, prompt("TMP") will show the prompt "TMP > " to the screen.
    * 
    * @param pr The prompt string to show
    * @return The next line from the scanner, or null if no scanner is avail */

  public String prompt(String pr) {
    return prompt(pr, true);
  }

  /** prompt(pr,ln) shows a prompt to the user with a '>' signifying input. It
    * allows control over whether a linebreak is printed before the prompt by
    * toggling the argument 'ln' to true.
    *
    * @param pr The prompt string to show
    * @param ln True if a linebreak should be printed before the prompt; false
    *   if not
    * @return The next line from command list, scanner (if no cmd), or "" if
    *   no scanner or preloaded command is available */

  public String prompt(String pr, boolean ln) {
    // Return an empty string if no scanner or input file command is available
    if (!this.hasScanner() && in.size()==0) { return ""; }

    // Get input
    System.out.print((ln ? "\n" : "") +pr+" > ");
    String input;

    if (in.size() > 0) {
      input = in.remove(0);
      System.out.println(input);
    }
    else
      input = scan.nextLine();

    return input;
  }

  /****************************************************************************/

  /** buildSelect(props) builds a comma-delimited string from the list of
    * properties provided.
    *
    * @param props A list of properties to generate a string for
    * @return A comma-separated list of the properties as a string */

  public static String buildSelect(ArrayList<String> props) {
    return buildSelect(props.toArray(new String[0]));
  }

  /** buildSelect(props) builds a comma-delimited string from the array of props
    * provided.
    *
    * @param props An array of properties to generate a string for
    * @return A comma-separated list of properties as strings */

  public static String buildSelect(String[] props) {
    String tup = "";
    for(int i=0; i<props.length; i++)
      tup += "\""+props[i]+"\""+ (i==props.length-1 ? "" : ", ");
    return tup;
  }

  /** buildTuple(props) builds a tuple for the list of properties (for usage in
    * executing queries on a Postgresql database.
    *
    * @param props A list of properties to turn into a tuple
    * @return A string representing the tuple of properties */

  public static String buildTuple(ArrayList<String> props) {
    return "("+buildSelect(props)+")";
  }

  /** isCommand(cmd) returns whether there is an implementing class of Command
    * named after the command given.
    *
    * @param cmd The command to test for an implementation of Commandfor
    * @return True if there is an implementation, or false if not */

  public static boolean isCommand(String cmd) {
    if (cmd.length() < 2) { return false; }

    String name = "cmd.Cmd"+Character.toUpperCase(cmd.charAt(0))+cmd.toLowerCase().substring(1);

    try { Class.forName(name); } catch (ClassNotFoundException e) { return false; }
    return true;
  }

  /** getCommand(cmd) returns an instance implementing the Command interface
    * that is named after the command given by 'cmd'. For example, if cmd is
    * a variation of 'help', then CmdHelp is instantiated and returned. If the
    * command does not have a class, then null is returned.
    *
    * @param cmd The name of the command to fetch an instance of
    * @return Implementing instance of the Command interface, or null */

  public static Command getCommand(String cmd) {
    switch(cmd.toLowerCase()) {
      case "help": return new CmdHelp();
      case "customer": return new CmdCustomer();
      case "reset": return new CmdReset();
      case "account": return new CmdAccount();
      default: return null;
    }
  }

  /** isReturn(cmd) returns whether or not cmd represents a 'return' command.
    *
    * @param cmd The input to test against
    * @return True if the input is a return keyword, or false if not */

  public static boolean isReturn(String cmd) {
    cmd = cmd.toLowerCase();
    return cmd.equals("back") || cmd.equals("ret") || cmd.equals("exit");
  }

  /** open(f) attempts to open the file 'f' and read commands into the list
    * of preloaded commands. It ignores lines that start with '#'
    *
    * @param f The filename to read user commands from */

  public static void open(String f) throws IOException, Exception {
    BufferedReader r = new BufferedReader(new FileReader(f));
    String cmd;
    while((cmd = r.readLine()) != null) {
      cmd = cmd.trim();
      if (cmd.length()==0 || (cmd.length()>0 && cmd.charAt(0) != '#'))
        in.add(cmd);
    }
    r.close();
  }
}