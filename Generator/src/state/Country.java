/**
  * Country represents a single country that consists of a series of states
  * cities, and streets. It allows access to this information and assigns cities
  * to the various states in the country. It also assigns phone codes to the
  * various states that it contains.
  *
  * Country is a singleton instance, meaning that only one can exist in the
  * program that is initialized using 'getCountry()'.
  *
  * The layout of a file containing info for a country is as follows:
  * <# states>
  * state 01
  * state 02
  * ...
  * <# cities>
  * city 01
  * ...
  * <# streets>
  * st 01
  * st 02
  * ...
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  *
  * Modifications:
  * + Country could randomly assign a street a suffix (presently commented out)
  */

package src.state;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import src.Helper;
import src.gen.Phone;

public class Country {
  // STATES_PER_STREET is the percentage of stages to assign a street to
  private static final double STATES_PER_STREET = .5;

  // cou is the singleton instance reference for the country
  private static Country cou = null;

  // states is the list of states in the country
  // capCity is used for equally distributing cities amongst the states
  // phone is an instance used to generate phone codes
  private ArrayList<State> states;
  private Phone phone;
  private int capCity;

  protected Country() {
    states = new ArrayList<>();
    phone = new Phone();
    capCity = 0;
  }

  /** getCountry() fetches the singleton instance of the country */
  public static Country getCountry() {
    if (cou == null) { cou = new Country(); }
    return cou;
  }

  public State getState(int in) { return states.get(in); }

  /** addState(s) adds the state s to this country and sets its phone reference
    * to the phone used by this country */

  public void addState(State s) { states.add(s); s.setPhone(phone); }

  /** numStates() returns the number of states in the country */

  public int numStates() { return states.size(); }

  /** capReached() returns true iff all states in the country have the capacity
    * number of cities in them. If not, it returns false. */

  private boolean capReached() {
    for(State s : states)
      if (s.numCities() < capCity) { return false; }
    return true;
  }

  /** assignCity(c) assigns a city randomly to a state that doesn't have the
    * current capacity number of cities. If none do, then the cap is raised
    * before inserting the city. */

  public void assignCity(City c) {
    if (capReached()) { capCity++; }

    State s;
    do { s = states.get(Helper.randomRange(0, states.size()-1)); }
    while(s.numCities() >= capCity);

    s.addCity(c);
  }

  /** toString() converts this country into a string for saving for future use.
    * The string follows the form: (CAP) <\n STATE |\n STATE |\n ... \n> */

  @Override
  public String toString() {
    String data = "("+capCity+") <\n";

    for(int i=0; i<states.size(); i++)
      data += states.get(i).toString()+
        (i < states.size()-1 ? "|\n" : "\n>");

    return data;
  }

  /** fromString(str) loads a prior-saved country's state from the given data
    * string, recursively loading state information as well. It returns the
    * reconstructed country. */

  public static Country fromString(String str) {
    Country c = new Country();
    c.capCity = Integer.parseInt(str.substring(1,str.indexOf(")")));

    for(String s : str.substring(str.indexOf("<")+2, str.length()-2).split("[|]\n"))
      c.addState(State.fromString(s));

    return c;
  }

  /** printInfo(offset) prints information about this country and the states
    * contained in the country. It offsets all state and city information by
    * the number of spaces specified in 'offset'. */

  public void printInfo(int offset) {
    String off = "";
    for(int i=0; i<offset; i++) { off += " "; }

    System.out.println(off+"Country: cap "+capCity);
    for(State s : states) { s.printInfo(offset+2); }
  }

  public void printInfo() { printInfo(0); }

  /** load(fname) will load the country's data from the given filename, where
    * the file has a format specified above. (This function will erase all
    * prior loaded data from the country, essentially starting fresh!) */

  public void load(String fname) throws IOException {
    BufferedReader r = new BufferedReader(new FileReader(fname));

    // Read states
    for(int i=Integer.parseInt(r.readLine()); i>0; i--)
      addState(new State(r.readLine()));
    
    // Assign cities
    for(int i=Integer.parseInt(r.readLine()); i>0; i--) {
      String nm = r.readLine(), zip = r.readLine();
      assignCity(new City(nm, Integer.parseInt(zip)));
    }

    // Assign streets to a percentage of the states, randomly selecting
    // modifiers for the streets based on a percentage chance
    int streets = Integer.parseInt(r.readLine());
    int cnt = (int) ((numStates()*STATES_PER_STREET) + 1);
    ArrayList<Integer> sta = new ArrayList<>();

    for(int i=0; i<streets; i++) {
      sta.clear();
      String st = r.readLine();

      for(int k=0; k<cnt; k++) {
        int state;
        String mod = "";

        // Select a unique state and modifier before assignment
        do { state = Helper.randomRange(0,numStates()-1); }
        while(sta.contains(state));

        switch(Helper.randomRange(0,99) % 10) {
          case 1: mod = "E. "; break;
          case 3: mod = "W. "; break;
          case 5: mod = "S. "; break;
          case 7: mod = "N. "; break;
        }

        // String suf = "";
        // switch(Helper.randomRange(0,99) % 10) {
        //   case 1: suf = "Rd."; break;
        //   case 2: suf = "Blvd."; break;
        //   case 3: suf = "Ln."; break;
        //   case 4: suf = "Dr."; break;
        //   case 5: suf = "Way"; break;
        //   case 6: suf = "St."; break;
        //   case 7: suf = "Ave."; break;
        // }

        getState(state).assignStreet(mod+st);
        sta.add(state);
      }
    }

    r.close();
  }
}