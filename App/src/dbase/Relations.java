/**
  * Relations is a class that stores ALL of the tuples that are available in a
  * relation in the database. They are stored as an array of string arrays,
  * where every entry in the array is a two-entry array, consisting of the
  * attribute name and a mini-desc of the attribute.
  *
  * Author: Matthew Morgan
  * Date: 8 November 2018
  */

package dbase;

import java.util.Scanner;

import cmd.Command;

public class Relations {
  // NAME is the index of each property's name in the array
  // DESC is the index of each property's mini-desc
  // NULL is the index of each property's nullability toggle
  public static final int NAME=1, DESC=2, NULL=0;

  /* SIZE is the size of each attribute entry in the String[][] */
  private static final int SIZE=2;
  
  // s is a scanner that is used to get property values
  private static Scanner s = new Scanner(System.in);

  /* The relations themselves, in a String[][] format. Each index is a separate
   * attribute, where each entry of the attribute contains the following:
   * + Whether the property can be null or not ("T" or "F")
   * + Attribute name, as stored in the database
   * + A miniature description of the attribute itself */
  public static final String[][]
    TBL_CUSTOMER = {
      {"F", "SSN", "Soc Sec Num"},
      {"F", "Fname", "First Name"},
      {"F", "Lname", "Last Name"},
      {"F", "Gender", "Gender"},
      {"F", "DOB", "Date of Birth"},
      {"T", "Con_Phone", "Phone Number"},
      {"F", "Con_Email", "Email"},
      {"F", "Street", "Addr: Street"},
      {"T", "Apt", "Addr: Apt"},
      {"F", "City", "Addr: City"},
      {"F", "Zip", "Addr: City ZIP"},
      {"F", "State", "Addr: State Abbrev"}
    };
  
  /** mayNull(prop, pind) returns whether the property at index pind in the
    * property set prop may be null or not.
    *
    * @param prop A String[][] of property information - usually from Relations
    * @param pind The index of the property to check nullability of
    * @return True if the property can be null, or false if not */

  public static boolean mayNull(String[][] prop, int pind) {
    return prop[pind][NULL].toLowerCase().equals("t");
  }

  /** getProps(pr,prop) gets a set of values from the user by repeatedly
    * prompting them with a prompt prefixed by 'pr,' where the properties to
    * get changes for are listed in 'prop'. (This variant assumes that the
    * properties are being retrieved for editing purposes.)
    *
    * @param pr The prompt prefix
    * @param prop The set of properties - typically a String[][] from Relations
    * @return An array of String arrays, where the first entry is the
    *   property, and the second is the value to set the property to (or "") */

  public static String[][] getProps(String pr, String[][] prop) {
    return getProps(pr, prop, true);
  }
  
  /** getProps(pr, prop, isEdit) gets a set of values from the user by
    * repeatedly prompting them with a prompt prefixed by pr, where the props to
    * fetch are listed in prop. If the properties are for updating a table, then
    * isEdit can change the prompt given to correspond to that.
    *
    * @param pr The prompt prefix
    * @param prop The set of properties - typically a String[][] in Relations
    * @param isEdit True if the properties are for editing, or false for insert
    *   of new tuples into a table
    * @return An array of String arrays, where the first entry is the property,
    *   and the second is the value to set the property to (or "") */

  public static String[][] getProps(String pr, String[][] prop, boolean isEdit) {
    // com is a generic subclass of Command since we need 'prompt'
    Command com = new Command() {
      public void execute() {}
    };
    com.setScanner(s);

    String[][] fields = new String[prop.length][];

    System.out.println(
      isEdit ?
      "Type values for properties you wish to edit. (Leave blank for unchanged)"
      :
      "Type values for each of the following properties. (Leave blank for null)"
    );

    for(int i=0; i<prop.length; i++) {
      fields[i] = new String[SIZE];
      fields[i][0] = prop[i][NAME];

      // Continue to prompt for a non-nullable field if no entry is made
      do {
        if (!mayNull(prop, i) && !isEdit)
          System.out.println("The following property cannot be null.");
        fields[i][1] = com.prompt(pr+" > "+prop[i][DESC], false);
      }
      while(!isEdit && fields[i][1].equals("") && !mayNull(prop,i));
    }

    return fields;
  }

  /** getInsert(prop) returns a string encompassing all the properties of the
    * given property set as a tuple for usage in an INSERT statement in SQL.
    *
    * @param prop The property set to generate an insert substring for,
    *   typically a String[][] from Relations
    * @return The generated string - for example, ("CID","SSN") */

  public static String getInsert(String[][] prop) {
    String tmp = "(";
    for(String[] f : prop)
      tmp += "\"" + f[NAME] + "\",";
    return tmp.substring(0,tmp.length()-1)+")";
  }
}