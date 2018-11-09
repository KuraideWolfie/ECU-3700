/**
  * Conn is the central handler of connections to the database, and is the
  * only way to get a prepared statement for a connection.
  *
  * Author: Matthew Morgan
  * Date: 6 November 2018
  */

package dbase;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Properties;

public class Conn {
  private Connection con = null;
  private String db, pass, user;

  /** Public constructor Con(d,p,s) returns a Con instance with the database and
    * user credentials provided. It does NOT attempt to make a connection.
    *
    * @param d The database to connect to
    * @param p The password user credential
    * @param u The username user credential
    * @return A new Connection instance */

  public Conn(String d, String p, String u) {
    db = d;
    pass = p;
    user = u;
  }

  public void setDB(String d) { db = d; }
  public void setUser(String u) { user = u; }
  public void setPass(String p) { pass = p; }

  /** hasConnection() returns true if this Connection instance is connected to
    * a database. */

  public boolean hasConnection() { return con != null; }

  /** connect() attempts to connect this Connection instance to a PostgreSQL
    * database, showing an error message if failure occurs. */

  public void connect() throws SQLException {
    if (!this.hasConnection()) {
      Properties pr = new Properties();
      pr.setProperty("user", user);
      pr.setProperty("pass", pass);
      pr.setProperty("ApplicationName", "3700 DBMS");

      con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+db, pr);
    }
    else
      System.out.println("Connection already has a connection to a DB");
  }

  /** disconnect() disconnects this Connection instance from its database. */

  public void disconnect() throws SQLException {
    if (this.hasConnection()) {
      con.close();
      con = null;
    }
    else
      System.out.println("Connection does not have a connection to a DB");
  }

  /** genQuery(q) returns prepared statement instance generated from the
    * database connection of this instance.
    *
    * @param q The query to generate a statement for
    * @return The new PreparedStatement, or null if no connection exists */

  public PreparedStatement genQuery(String q) throws SQLException {
    if (this.hasConnection())
      return con.prepareStatement(q);
    return null;
  }
}