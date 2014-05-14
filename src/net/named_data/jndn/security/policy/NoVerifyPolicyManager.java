/**
 * Copyright (C) 2014 Regents of the University of California.
 * @author: Jeff Thompson <jefft0@remap.ucla.edu>
 * @author: From code in NDN-CPP by Yingdi Yu <yingdi@cs.ucla.edu>
 * See COPYING for copyright and distribution information.
 */

package net.named_data.jndn.security.policy;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.OnVerified;
import net.named_data.jndn.security.OnVerifyFailed;
import net.named_data.jndn.security.ValidationRequest;

/**
 *
 */
public class NoVerifyPolicyManager extends PolicyManager {
  /**
   * Override to always skip verification and trust as valid.
   * @param data The received data packet.
   * @return true.
   */
  public boolean skipVerifyAndTrust(Data data) 
  {
    return true;
  }

  /**
   * Override to return false for no verification rule for the received data.
   * @param data The received data packet.
   * @return false.
   */
  public boolean requireVerify(Data data) 
  {
    return false;
  }

  /**
   * Override to call onVerified.onVerified(data) and to indicate no further 
   * verification step.
   * @param data The Data object with the signature to check.
   * @param stepCount The number of verification steps that have been done, used 
   * to track the verification progress.
   * @param onVerified This does override to call onVerified.onVerified(data).
   * @param onVerifyFailed Override to ignore this.
   * @return null for no further step.
   */
  public ValidationRequest checkVerificationPolicy
    (Data data, int stepCount, OnVerified onVerified, OnVerifyFailed onVerifyFailed)
  {
    onVerified.onVerified(data); 
    return null;
  }

  /**
   * Override to always indicate that the signing certificate name and data name 
   * satisfy the signing policy.
   * @param dataName The name of data to be signed.
   * @param certificateName The name of signing certificate.
   * @return true to indicate that the signing certificate can be used to sign 
   * the data.
   */
  public boolean checkSigningPolicy(Name dataName, Name certificateName) 
  {
    return true;
  }

  /**
   * Override to indicate that the signing identity cannot be inferred.
   * @param dataName The name of data to be signed.
   * @return An empty name because cannot infer. 
   */
  public Name inferSigningIdentity(Name dataName) 
  {
    return new Name();
  }
  
}