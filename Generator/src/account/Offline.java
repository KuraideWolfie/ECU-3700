/**
  * Offline represents an offline account that can be held by a customer at the
  * bank. It may be a checking or savings account, and may have any of a few
  * options regarding monthly fees.
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src.account;

import java.time.LocalDate;
import java.time.Period;
import src.people.Customer;
import src.*;

public class Offline {
  // Type is an enumeration for the types of accounts available
  // Comp is an enumeration for the different rates of compound on interest
  public static enum Type { CHK, SAV };
  public static enum Comp { NONE, MONTHLY, QUARTERLY, TRIMESTERLY, SEMESTERLY, ANNUALLY };

  private static Uniquifier uAID = new Uniquifier(12);

  // open and close are the open/close dates of the account
  // aid is the account's unique number, > 000000000000
  // bal, fee, and intRate are the balance, monthly fee, and interest rate
  // type and intComp are the type of account and interest compound rate
  private LocalDate open, close;
  private String aid;
  private double bal, fee, intRate;
  private Type type;
  private Comp intComp;

  /** newAID() returns a new AID using the uniquifier for accounts */

  public static String newAID() { return uAID.get(); }

  public Offline(Customer c) {
    int numType = Type.values().length, numComp = Comp.values().length;

    do { aid = uAID.get(); } while (aid.equals("000000000000"));
    bal = Helper.randomRange(25, 1000);
    fee = randFee();

    // 75% chance of a checking account type
    if (Helper.randomRange(0, 99) < 75) { type = Type.CHK; } 
    else { type = Type.SAV; }

    if (type == Type.SAV) {
      intComp = Comp.values()[ Helper.randomRange(1, numComp-1) ];
      intRate = randInt();
    }
    else { intRate = 0.0; intComp = Comp.NONE; }

    // Select an open date that is after the date of maturity. This date may end
    // up being the same as the maturity date
    LocalDate mat = c.getDOB().plusYears(Customer.YR_MIN);
    int days = (int)Helper.getDaysBetween(mat, LocalDate.now());
    do { open = LocalDate.now().minus(Period.ofDays(Helper.randomRange(0,days))); }
    while(open.isBefore(mat) && !open.isEqual(mat));

    // Select a close date after the open date. This cannot be done if
    // the open date is the same as today's date
    if (Helper.randomRange(0,99) < 50 && !open.isEqual(LocalDate.now())) {
      days = (int)Helper.getDaysBetween(open, LocalDate.now());
      close = LocalDate.now().minus(Period.ofDays(Helper.randomRange(0, days)));
    }
    else { close = null; }
  }

  public LocalDate getOpen() { return open; }
  public LocalDate getClose() { return close; }
  public String getAID() { return aid; }
  public double getBal() { return bal; }
  public double getFee() { return fee; }
  public double getRate() { return intRate; }
  public String getType() { return type.name(); }
  public String getComp() { return intComp.name(); }
  public String getOpenStr() { return open.toString(); }
  public String getCloseStr() { return (close == null ? "NULL" : close.toString()); }

  /** hasBal(val) returns true if the account has enough funds to pay for
    * a purchase of the given value. */

  public boolean hasBal(double val) { return bal >= val; }

  /** debit(val) and credit(val) take away from, and add to, the account's
    * present balance the amount shown. */

  public void debit(double val) { bal -= val; }
  public void credit(double val) { bal += val; }

  /** randInt() returns a random interest rate, equally dispensed over the
    * compound rate selected prior. */

  private double randInt() {
    int interest = Helper.randomRange(175, 225);
    return (interest / 100.0) / comps();
  }

  /** comps() returns the number of times interest compounds based on present
    * select compound rate */

  private int comps() {
    switch(intComp) {
      case NONE: return 0;
      case MONTHLY: return 12;
      case QUARTERLY: return 3;
      case TRIMESTERLY: return 4;
      case SEMESTERLY: return 2;
      case ANNUALLY: return 1;
    }
    return 1;
  }

  /** randFee() returns a random monthly fee for an account being made */

  private double randFee() {
    switch(Helper.randomRange(0,4)) {
      case 0: return 5.99;
      case 1: return 9.99;
      case 2: return 7.50;
      case 3: case 4: return 0.0;
    }
    return 0.0;
  }
}