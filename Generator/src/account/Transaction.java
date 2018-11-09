/**
  * Transaction represents a single purchase on an account.
  *
  * Author: Matthew Morgan
  * Date: 28 October 2018
  */

package src.account;

import java.time.LocalDate;
import src.Helper;

public class Transaction {
  public static enum Type { DEBIT, CREDIT };

  // aid is the account this transaction effects
  // recRoute and recaid are the recipient account's information
  // amount, desc, type, and date describe the transaction
  // did is the id of a dispute that references the transaction
  // isPending is whether the transaction has posted or not
  private String aid, recRoute, recaid, desc;
  private double amount;
  private Integer did;
  private boolean isPending;
  private LocalDate date;
  private Type type;

  /** Transaction(ca,rr,ra,dc,amnt,d,t) creates a new transaction.
    *
    * @param ca The customer AID this transaction references
    * @param rr The recipient's routing number
    * @param ra The recipient's AID
    * @param dc The description of the transaction
    * @param amnt The amount of the transaction
    * @param d The date of the transaction
    * @param t The type of the transaction */

  public Transaction(String ca, String rr, String ra, String dc, double amnt,
    LocalDate d, Type t) {
      aid = ca;
      recRoute = rr;
      recaid = ra;
      desc = dc;
      amount = amnt;
      type = t;
      date = d;
      did = null;

      isPending = Helper.getDaysBetween(date,LocalDate.now()) < 5;
  }

  public String getAID() { return aid; }
  public String getRecAID() { return recaid; }
  public String getRecRoute() { return recRoute; }
  public String getDesc() { return desc; }
  public double getAmount() { return amount; }
  public Integer getDID() { return did; }
  public boolean isPending() { return isPending; }
  public LocalDate getDate() { return date; }
  public String getDateStr() { return date.toString(); }
  public String getType() { return type.name(); }

  /** setDID(d) sets the dispute that references this transaction */

  public void setDID(int d) { did = d; }
}