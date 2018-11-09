/**
  * Employee is a representation of an employee that works (or worked) at the
  * bank. They have a start and end date, a superior ID, and a customer ID that
  * stores their biographical information.
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src.people;

import java.time.LocalDate;
import java.time.Period;
import src.Helper;

public class Employee {
  // YR_MIN is the minimum allowed age of an employee
  public static final int YR_MIN = 21;

  // start is the start date of the employee
  // end is the end date of the employee's career
  // cid and supeid are the customer id and EID of the employee's superior
  private LocalDate start, end;
  private int cid;
  private Integer supeid;

  public Employee(Customer c, int id, int eid) {
    cid = id;
    supeid = null;

    // start date should be after the customer turns 21
    // the start date may be the same day as maturity
    LocalDate mat = c.getDOB().plusYears(YR_MIN);
    int days = (int)Helper.getDaysBetween(mat, LocalDate.now());
    do { start = LocalDate.now().minus(Period.ofDays(Helper.randomRange(0,days))); }
    while(start.isBefore(mat) && !start.isEqual(mat));

    // There is a 60% chance the employee is no longer working
    // If the employee has id 0, then they are automatically still working
    // If the start date is today, then no end date is generated
    if ((Helper.randomRange(0,99) < 60) && (eid > 0) && !start.isEqual(LocalDate.now())) {
      do { end = LocalDate.now().minus(Period.ofDays(Helper.randomRange(0,days))); }
      while(end.isBefore(start));
    }
    else { end = null; }
  }

  public int getCID() { return cid; }
  public Integer getSup() { return supeid; }
  public LocalDate getStart() { return start; }
  public LocalDate getEnd() { return end; }
  public String getStartStr() { return start.toString(); }
  public String getEndStr() { return (end == null ? "NULL" : end.toString()); }

  /** setSup(s) sets the supervisor eid of this employee */

  public void setSup(int s) { supeid = s; }
}