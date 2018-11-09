/**
  * Phone is a simple class used to generate phone numbers and 3-digit area
  * codes, preventing generation of reserved codes.
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src.gen;

import java.util.ArrayList;
import src.Helper;

public class Phone {
  // codes is a list of already generated phone codes
  private ArrayList<String> codes;

  public Phone() { codes = new ArrayList<>(); }

  /** genCode() generates a unique phone code (according to this phone instance)
    * and returns the code - a 3-digit string. It avoids restricted codes. */

  public String genCode() {
    String code;

    do { code = Helper.randomDigits(3); }
    while(codes.contains(code) || isReserved(code));

    codes.add(code);
    return code;
  }

  /** genPhone() generates a unique 7-digit phone number, preventing the RNG
    * from putting in reserved phone numbers. It returns the generated num */

  public String genPhone() {
    String num = Helper.randomDigits(7);

    // Scan through the number and test for reserved area codes
    for(int i=0; i<6; i++)
      if (isReserved(num.substring(i,i+4), false))
        num = num.substring(0,i)+num.substring(i+4)+Helper.randomDigits(3);

    return num;
  }

  /** reserveCode(code) adds the phone code to the list of codes already
    * generated by the phone instance. */

  public void reserveCode(String code) { codes.add(code); }

  /** isReserved(code) returns whether the phone code provided is reserved
    * for some purpose. The following rules apply:
    *
    * Basic Checking:
    * + 911, 411, and 800 are reserved
    *
    * Extensive Checking:
    * + 000-009 are reserved */

  private boolean isReserved(String code) { return isReserved(code, true); }

  private boolean isReserved(String code, boolean ext) {
    switch(code) {
      case "911": case "411": case "800":
        return true;
      default:
        if (ext) {
          if (code.charAt(0) == '0') { return true; }
        }
        return false;
    }
  }
}