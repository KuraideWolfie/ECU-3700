/**
  * State is a class that represents a single state, containing a list of cities
  * that, in turn, contain a list of streets. It equally distributes streets
  * to those cities as it is given streets.
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src.state;

import java.util.ArrayList;
import src.Helper;
import src.gen.Phone;

public class State {
  // CITIES_PER_CODE is the number of cities per phone code
  private static final int CITIES_PER_CODE = 6;

  // cities is a list of the cities in the state
  // name is the name of the state
  // capStreet is a capacity limit for equally distributing streets to cities
  // phone is used to generate phone area codes
  // phoneCodes is the list of phone codes for the state
  private ArrayList<City> cities;
  private String name;
  private int capStreet;
  private Phone phone;
  private ArrayList<String> phoneCodes;

  public State(String nm) {
    name = nm;
    cities = new ArrayList<>();
    capStreet = 0;
    phone = null;
    phoneCodes = new ArrayList<>();
  }

  public String getName() { return name; }
  public City getCity(int in) { return cities.get(in); }

  /** getCode(in) gets the phone code for the in-th city in the state's set
    * of cities. Phone codes rotate according to the number of cities per phone
    * code as specified by CITIES_PER_CODE. */

  public String getCode(int in) { return phoneCodes.get(in / CITIES_PER_CODE); }

  /** addCity(c) adds a city to this state's collection, updating the phone
    * codes of the state as needed */

  public void addCity(City c) {
    cities.add(c);

    //if (phone == null) { throw new Exception("State has no phone reference!"); }

    int codes = (cities.size() / CITIES_PER_CODE) + 1;
    while(codes > phoneCodes.size() && (phone != null))
      phoneCodes.add(phone.genCode());
  }

  /** setPhone(p) sets the state's phone used for code generation and assigns
    * any phone codes in this state, if loaded, as reserved in the phone. */

  public void setPhone(Phone p) {
    phone = p;
    for(String c : phoneCodes) { phone.reserveCode(c); }
  }

  /** numCities() returns the number of cities in the state */

  public int numCities() { return cities.size(); }

  /** capReached() returns true iff every city in the state has the capacity
    * number of streets. If not, it returns false */

  private boolean capReached() {
    for(City c : cities)
      if (c.numStreets() < capStreet) { return false; }
    return true;
  }

  /** assignStreet(st) assigns the given street randomly to one of the cities
    * in the state that doesn't have the capacity number of streets. If they
    * all do, then it also increases the street capacity limit. */

  public void assignStreet(String st) {
    if (capReached()) { capStreet++; }

    City c;
    do { c = cities.get(Helper.randomRange(0, cities.size()-1)); }
    while(c.numStreets() >= capStreet);

    c.addStreet(st);
  }

  /** toString() converts this state into a string for saving for future usage.
    * The string follows this form: (NAME,CAP) [CODE,...] {CITY\nCITY\n...} */

  public String toString() {
    String data = "("+name+","+capStreet+") [";

    for(int i=0; i<phoneCodes.size(); i++)
      data += phoneCodes.get(i) + (i==phoneCodes.size()-1 ? "] {" : ",");

    for(City c : cities)
      data += "\n"+c.toString();

    return data + "\n}";
  }

  /** fromString(str) loads a state from a prior-saved string representation of
    * it. It also recursively loads all cities that the state contained.
    *
    * @param str The string containing the state's data to load
    * @return Reconstructed state instance from the data */

  public static State fromString(String str) {
    State s = new State(str.substring(1,str.indexOf(",")));
    s.capStreet = Integer.parseInt(str.substring(str.indexOf(",")+1,str.indexOf(")")));

    for(String c : str.substring(str.indexOf("[")+1, str.indexOf("]")).split(","))
      s.phoneCodes.add(c);

    for(String c : str.substring(str.indexOf("{")+2,str.length()-2).split("\n"))
      s.addCity(City.fromString(c));

    return s;
  }

  /** printInfo(offset) prints information about this state and the cities
    * contained in the state, offsetting the information by the number of
    * spaces specified as 'offset' */

  public void printInfo(int offset) {
    String off = "";
    for(int i=0; i<offset; i++) { off += " "; }

    System.out.println(off+"State: "+name+", cap "+capStreet);

    // Print 5 phone codes per line, and print a line break after the codes
    // if no break is printed after all codes are printed
    System.out.print(off+"  Phone Codes: ");
    for(int i=0; i<phoneCodes.size(); i++) {
      if (i % 5 == 0) { System.out.print("\n"+off+"    "); }
      System.out.print(phoneCodes.get(i)+" ");
    }
    if (phoneCodes.size() % 5 != 0) { System.out.println(); }

    for(City c : cities) { c.printInfo(offset+2); }
  }

  public void printInfo() { printInfo(0); }
}