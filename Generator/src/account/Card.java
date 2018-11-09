/**
  * Card is a representation of a single card tuple in the database.
  *
  * Author: Matthew Morgan
  * Date: 28 October 2018
  */

package src.account;

import java.time.LocalDate;
import src.*;

public class Card {
  // Status is an enumeration for the different types of statuses of a card
  public static enum Status { PENDING, ACTIVE, DISABLED, CLOSED };

  // uNum is a uniquifier for generating card numbers
  private static Uniquifier uNum = new Uniquifier(16);

  // number is the unique card number
  // sec and pin are the security code and pin for the card
  // aid is the account this card is associated with
  // cid is the custoemr this card is owned by
  // exp is when this card expires
  // stat is the current status of the card
  private String num, sec, pin, aid;
  private int cid;
  private LocalDate exp;
  private Status stat;

  public Card(String a, int c) {
    num = uNum.get();
    aid = a;
    cid = c;
    sec = Helper.randomDigits(3);
    pin = Helper.randomDigits(4);
    exp = LocalDate.now();
    stat = Status.CLOSED;
  }

  public String getNumber() { return num; }
  public String getSec() { return sec; }
  public String getPIN() { return pin; }
  public String getAID() { return aid; }
  public int getCID() { return cid; }
  public LocalDate getDate() { return exp; }
  public String getDateStr() { return exp.toString(); }
  public String getStat() { return stat.name(); }

  /** setDate(d) sets the current expiry date of the card */

  public void setDate(LocalDate d) { exp = d; }

  /** setStatus(s) sets the current status of the card */

  public void setStatus(Status s) { stat = s; }
}