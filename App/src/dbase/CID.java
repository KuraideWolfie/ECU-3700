/**
  * CID is a simple class that pairs together identifiers for customers, including
  * both numerical identifiers and full name identifiers.
  *
  * Author: Matthew Morgan
  * Date: 6 November 2018
  */

package dbase;

import java.util.ArrayList;

public class CID {
  // cidNum is a list of numerical customer IDs
  // cidStr is a list of first/last name pairs for customer IDs
  private ArrayList<Integer> cidNum;
  private ArrayList<String> cidStr;

  public CID() { cidNum = new ArrayList<>(); cidStr = new ArrayList<>(); }

  /** add(item) adds the given item to one of the ID lists. If the item is
    * invalid, then that reflects in the returned boolean.
    *
    * @param item The item to add to the customer ID lists
    * @return True if the item was added, or false if not */

  public boolean add(String item) {
    try {
      if (item.contains(" "))
        cidStr.add(item);
      else
        cidNum.add(Integer.parseInt(item));
    }
    catch(Exception e) { return false; }

    return true;
  }

  /** addAll(items) attempts to add all of the given items to the ID lists. If
    * at least one fails, then that reflects in the returned boolean.
    *
    * @param items An array of strings that will be added
    * @return True if all the items were successfully added, or false if not
    */

  public boolean addAll(String[] items) {
    for(String i : items)
      if (!add(i.trim())) { return false; }
    return true;
  }

  /** getIn() returns an array that contains the tuples for numerical and
    * string IDs for customers. Either entry may be an empty string based on if
    * there are no numerical/name IDs. */

  public String[] getIn() {
    return new String[] {getInNum(), getInStr()};
  }

  /** getInNum() returns a tuple containing all the numerical customer IDs. It
    * returns an empty string if there are no numerical identifiers */

  public String getInNum() {
    if (cidNum.size() == 0) return "";

    String res = "";
    for(Integer i : cidNum)
      res += ", "+i.toString();
    return "("+res.substring(2)+")";
  }
  
  /** getInStr() returns a tuple containing smaller tuples for the first and
    * last names stored in this CID's collection of names. It returns an
    * empty string if there are no names. */

  public String getInStr() {
    if (cidStr.size() == 0) return "";

    String res = "";
    for(String s : cidStr) {
      String[] name = s.split(" ");
      for(String n : name) { n = n.replace("'","''"); }

      res += ", ('"+name[0]+"','"+name[1]+"')";
    }
    return "("+res.substring(2)+")";
  }

  /** numIDS() returns the number of customer IDs stored in this CID.
    * @return The number of IDs stored */

  public int numIDS() {
    return cidNum.size() + cidStr.size();
  }
}