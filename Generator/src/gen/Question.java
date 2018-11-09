/**
  * Question represents a set of questions and comma-delimited answers that is
  * used to generate recovery question sets for online accounts. Question is
  * a singleton object.
  *
  * A file specifying recovery questions should have the following form:
  * <# questions>
  * que 01
  * ans,ans,ans,...
  * que 02
  * ans,ans,...
  * ...
  *
  * Date: 27 October 2018
  * Author: Matthew Morgan
  */

package src.gen;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Question {

  // inst is the singleton instance's reference
  private static Question inst = null;

  // que is the list of questions
  // ans is the list of answers to the questions
  private ArrayList<String> que;
  private ArrayList<ArrayList<String>> ans;

  protected Question() {
    que = new ArrayList<>();
    ans = new ArrayList<>();
  }

  /** getSet() returns a reference to the singleton set of questions */

  public static Question getSet() {
    if (inst == null) { inst = new Question(); }
    return inst;
  }

  public int numQuestions() { return que.size(); }
  public int numAnswers(int in) { return ans.get(in).size(); }

  /** addQuestion(q,a) adds the question and comma-delimited list of answers
    * to the question set. */

  public void addQuestion(String q, String a) {
    que.add(q);
    ans.add(new ArrayList<>());
    for(String tmp : a.split(",")) { ans.get(ans.size()-1).add(tmp); }
  }

  /** getQuestion(in) returns the in-th question of the set */

  public String getQuestion(int in) { return que.get(in); }

  /** getAnswer(a,q) gets the a-th answer to the q-th question in the set */

  public String getAnswer(int q, int a) { return ans.get(q).get(a); }

  /** load(fname) loads a set of recovery questions from the file provided.
    * Usage of this function will ERASE all present questions in the set. The
    * format for the file for questions is specified above. */

  public void load(String fname) throws IOException {
    que.clear(); ans.clear();

    BufferedReader r = new BufferedReader(new FileReader(fname));

    for(int i=Integer.parseInt(r.readLine()); i>0; i--) {
      String q = r.readLine(), a = r.readLine();
      addQuestion(q, a);
    }

    r.close();
  }
}