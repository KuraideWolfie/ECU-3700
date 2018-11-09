/**
  * City is a small class that represents a single city in a state. It holds
  * a list of streets, a name, and 5-digit zip code.
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src.state;

import java.util.ArrayList;

public class City {
  // streets is the list of streets in the city
  // name is the name of the city
  // zip is the city's 5-digit zip code
  private ArrayList<String> streets;
  private String name;
  private int zip;

  public City(String nm, int z) {
    name = nm;
    zip = z;
    streets = new ArrayList<>();
  }

  public String getName() { return name; }
  public int getZip() { return zip; }
  public String getStreet(int in) { return streets.get(in); }

  /** addStreet(st) adds the street 'st' to the city's collection of streets */

  public void addStreet(String st) { streets.add(st); }

  /** numStreets() returns the number of streets in this city's collection */

  public int numStreets() { return streets.size(); }

  /** toString() converts this city into a string for saving for future usage.
    * The string follows this form: (NAME,ZIP) [ST,ST,...] */

  public String toString() {
    String data = "("+name+","+zip+") [";

    for(int i=0; i<streets.size(); i++)
      data += streets.get(i) + (i==streets.size()-1 ? "]" : ",");
      
    return data;
  }

  /** fromString(str) creates a new City instance from the string provided.
   * 
   * @param str The data string to load the city instance from
   * @return A new City instance constructed from the data */

  public static City fromString(String str) {
    City c = new City(str.substring(1,str.indexOf(",")),
      Integer.parseInt(str.substring(str.indexOf(",")+1,str.indexOf(")"))));
    
    for(String st : str.substring(str.indexOf("[")+1,str.length()-1).split(","))
      c.addStreet(st);

    return c;
  }

  /** printInfo(offset) prints information about this city, offsetting each line
    * by the number of spaces specified as 'offset' */

  public void printInfo(int offset) {
    String off = "";
    for(int i=0; i<offset; i++) { off += " "; }

    System.out.println(off+"City: "+name+", "+zip);
    for(String st : streets)
      System.out.println(off+"  "+st);
  }

  public void printInfo() { printInfo(0); }
}