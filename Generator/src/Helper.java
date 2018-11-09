/**
  * Helper is a small class that contains useful helper functions, and file
  * paths, that all other classes in the program need access to. It contains
  * a random instance that is used for all RNG generation instead of multiple
  * classes having to make their own.
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src;

import java.util.Random;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Helper {

  // rng is used for random number generation
  private static Random rng = new Random();

  /** randomRange(low, high) returns a random integer between the bounds low
    * and high
    *
    * @param low The lower bound of the range
    * @param high The upper bound of the range
    * @return A random integer between low and high */

  public static int randomRange(int low, int high) {
    return rng.nextInt(high-low+1) + low;
  }

  public static double randomRange(double low, double high) {
    return (rng.nextDouble() * (high-low)) + low;
  }

  /** randomDigits(length) returns a String representing an integer of the
    * provided length, where each digit is a random integer from 0 to 9.
    *
    * @param length The number of digits to generate in the string
    * @return A string of length 'length' of 0-9 digits */

  public static String randomDigits(int length) {
    String str = "";
    for(int i=0; i<length; i++) { str += randomRange(0,9); }
    return str;
  }

  /** getDays(a,b) gets the number of days between dates a and b */

  public static long getDaysBetween(LocalDate a, LocalDate b) {
    return ChronoUnit.DAYS.between(a, b);
  }
}