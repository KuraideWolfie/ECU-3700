/**
  * Store is a basic representation of a store, containing a name and a list
  * of prices that encompasses the products of the shop.
  *
  * Date: 28 October 2018
  * Author: Matthew Morgan
  */

package src.store;

import java.util.ArrayList;
import src.Helper;

public class Store {
  // name is the name of the store
  // bank stores the routing number and aid of the store
  // prices is a list of prices, or a lower/upper bound for ranges
  // isRange toggles if this store generates a range of prices
  // isOnline toggles if this store is online
  // isSingle toggles if there is only one price in the store
  private String name;
  private String[] bank;
  private ArrayList<Double> prices;
  private boolean isRange, isOnline, isSingle;

  public Store(String nm, boolean isRan, boolean isOn, String pr, String rr, String ra) {
    bank = new String[]{rr, ra};
    name = nm;
    isRange = isRan;
    isOnline = isOn;
    prices = new ArrayList<>();

    for(String s : pr.split(",")) { prices.add(Double.parseDouble(s)); }
    isSingle = (prices.size() == 1);
  }

  public String getName() { return name; }
  public boolean flagRange() { return isRange; }
  public boolean flagOnline() { return isOnline; }
  public boolean flagSingle() { return isSingle; }
  public String getRoute() { return bank[0]; }
  public String getAID() { return bank[1]; }
  public double getPrice() {
    // If there's only one price, return it
    // If there is a range, pick a value in the range
    // Else, pick a random price from the list of prices
    if (isSingle) { return prices.get(0); }
    else if (isRange) { return Helper.randomRange(prices.get(0), prices.get(1)); }
    else { return prices.get(Helper.randomRange(0, prices.size()-1)); }
  }
}