/**
  * Branch represents a single bank branch (simulated for purchases).
  *
  * Author: Matthew Morgan
  * Date: 28 October 2018
  */

package src.account;

import src.*;

public class Branch {
  // route and aid are the routing number and AID of the branch
  public String route, aid;

  // uRoute is a uniquifier for branch routing numbers
  private static Uniquifier uRoute = new Uniquifier(10);

  /** getRoute() returns a unique routing number */
  
  public static String getRoute() { return uRoute.get(); }
  
  public Branch() {
    route = uRoute.get();
    aid = Offline.newAID();
  }
}