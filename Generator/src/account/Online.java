/**
	* Online represents a single online account that a customer owns.
	*
	* Date: 27 October 2018
	* Author: Matthew Morgan
  */

package src.account;

import java.time.LocalDate;
import java.time.Period;

import src.people.Customer;
import src.gen.Question;
import src.Helper;

public class Online {
	// QUE_COUNT is the number of questions per account online for security
	public static final int QUE_COUNT = 3;

	// user and pass are the login credentials for the account
	// cid is the customer's id that owns the account
	// que and ans is the set of questions and answers for the account
	// date is the date of the questions/answers for the account
	private String user, pass;
	private int cid;
	private int[] que, ans;
	private LocalDate date;

	public Online(Customer c, Question q, int id) {
		cid = id;
		user = c.getFirstName().charAt(0)+c.getLastName()+Helper.randomRange(100,999);
		user = user.replaceAll("[-']", "").toLowerCase();
		pass = Helper.randomDigits(5);

		que = new int[QUE_COUNT];
		ans = new int[QUE_COUNT];
		for(int i=0; i<QUE_COUNT; i++) {
			boolean flag = false;

			// Select a unique question every iteration
			do {
				flag = false;
				que[i] = Helper.randomRange(0, q.numQuestions()-1);
				for(int k=0; k<i; k++)
					if (que[i] == que[k]) { flag = true; }
			}
			while(flag);

			ans[i] = Helper.randomRange(0, q.numAnswers(que[i])-1);
		}

		LocalDate min = LocalDate.parse("2000-01-01");
		int days = (int)Helper.getDaysBetween(min,LocalDate.now());
		do { date = LocalDate.now().minus(Period.ofDays(Helper.randomRange(0,days))); }
		while(date.isBefore(min) || date.isBefore(c.getDOB().plusYears(Customer.YR_MIN)));
	}

	public int getCID() { return cid; }
	public String getUser() { return user; }
	public String getPass() { return pass; }
	public String getDate() { return date.toString(); }
	public int getQue(int i) { return que[i]; }
	public int getAns(int i) { return ans[i]; }
}