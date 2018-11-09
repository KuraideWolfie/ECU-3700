/**
  * Customer represents a single customer in the database, containing all
  * biographical and geographical information for RNG.
  *
  * Files containing customer information must follow this form:
  * <# customer>
  * <gend> <name>
  * <gend> <name>
  * ...
  *
  * Example:
  * M Matthew Morgan
  * F Sabrina Bri
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src.people;

import java.time.LocalDate;
import java.time.Period;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import src.*;
import src.state.Country;

public class Customer {
  // YR_MIN and YR_MAX are the bounds on the age of a customer at the bank
  // STATE, CITY, and STREET are the indices of each address piece in 'addr'
  public static final int YR_MIN = 18, YR_MAX = 70;
  private static final int STATE=0, CITY=1, STREET=2;

  // uSSN and uPhone are used for generating unique SSN and 7-digit phone nums
  private static Uniquifier uSSN = new Uniquifier(9), uPhone = new Uniquifier(7);

  // name contains the first and last names of the customer
  // ssn is the customer's social security number
  // dob is the customer's date of birth
  // addr is the address of the customer [ state, city, street ]
     // zip is inferred by getting the zip of the city
     // apt is left as NULL in all cases
  // email and phone are contact information for the customer
  // house is the house number the person lives in on the street
  private String[] name;
  private String ssn, email, phone;
  private LocalDate dob;
  private int[] addr;
  private int house;
  private char sex;

  /** Customer(nm,gen) generates a new Customer instance by
    * assigning the name, gender, and location information provided as well as
    * generating a unique ssn and phone number using the Uniquifiers.
    *
    * @param nm The customer's first and last name
    * @param gen The gender of the customer - 'F' or 'M' */

  public Customer(String nm, char gen) {
    Country c = Country.getCountry();

    name = nm.split(" ");
    sex = gen;
    ssn = uSSN.get();
    email = genEmail();
    phone = uPhone.get();
    house = Helper.randomRange(100, 999);

    addr = new int[3];
    addr[0] = Helper.randomRange(0, c.numStates()-1);
    addr[1] = Helper.randomRange(0, c.getState(addr[0]).numCities()-1);
    addr[2] = Helper.randomRange(0, c.getState(addr[0]).getCity(addr[1]).numStreets()-1);

    // Ensure that the DOB is at least 18 years from today
    do { dob = LocalDate.now().minus(Period.ofDays(Helper.randomRange(365*YR_MIN, 365*YR_MAX))); }
    while(Helper.getDaysBetween(dob.plusYears(YR_MIN), LocalDate.now()) < 0);
  }

  public String getFirstName() { return name[0]; }
  public String getLastName() { return name[1]; }
  public LocalDate getDOB() { return dob; }
  public String getDOBStr() { return dob.toString(); }
  public char getSex() { return sex; }
  public int getState() { return addr[STATE]; }
  public int getCity() { return addr[CITY]; }
  public int getStreet() { return addr[STREET]; }
  public String getSSN() { return ssn; }
  public String getEmail() { return email; }
  public String getPhone() { return phone; }
  public int getHouse() { return house; }

  /** getAge() gets the age of the customer */

  public int getAge() {
    return (int)java.time.temporal.ChronoUnit.YEARS.between(dob, LocalDate.now());
  }

  /** genEmail() returns a randomly generated email for this customer */

  private String genEmail() {
    String suf = "", mail;

    switch(Helper.randomRange(0, 6)) {
      case 0: suf = "yahoo.com"; break;
      case 1: suf = "gmail.com"; break;
      case 2: suf = "hotmail.com"; break;
      case 3: suf = "outlook.com"; break;
      case 4: suf = "mail.com"; break;
      case 5: suf = "mail.google.com"; break;
      case 6: suf = "mymail.org"; break;
    }

    mail =
      name[0].substring(0,Math.min(Helper.randomRange(0,3),name[0].length())).replaceAll("'","")+
      name[1].replaceAll("'","")+
      Helper.randomRange(100,999)+
      "@"+suf;

    return mail;
  }

  /** load(fname) loads customer information from the given file, returning an
    * array containing the loaded customer information. A file of a specific
    * format is described above regarding this function.
    *
    * @param fname The name of the file to load customer info from
    * @return The array of customer information */

  public static Customer[] load(String fname) throws IOException {
    BufferedReader r = new BufferedReader(new FileReader(fname));
    Customer[] cust = new Customer[Integer.parseInt(r.readLine())];

    for(int i=0; i<cust.length; i++) {
      String dat = r.readLine();
      cust[i] = new Customer(dat.substring(2), dat.charAt(0));
    }

    r.close();
    return cust;
  }
}