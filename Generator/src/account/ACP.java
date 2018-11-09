/**
 * ACP represents a single account-customer pair, containing the CID and AID
 * of those respective entities. It also contains helper functions for searching
 * AIDs from a collection of ACPs.
 * 
 * Author: Matthew Morgan
 * Date: 27 October 2018
 */

package src.account;

import java.util.List;

public class ACP {
  public int cid;
  public String aid;
  
  public ACP(int c, String a) { cid = c; aid = a; }

  /** getAIDCust(L, acc) returns the customer ID associated with an account in
    * a list of ACP instances.
    *
    * @param L The collection (a list) of ACP instances
    * @param acc The account string to get customer ID for
    * @return The CID of the owning customer, or -1 if there is no pair */

  public static int getAIDCust(List<ACP> L, String acc) {
    for(ACP acp : L)
      if (acp.aid.equals(acc)) { return acp.cid; }
    return -1;
  }
}