/**
 * Copyright (C) 2013-2014 Regents of the University of California.
 * @author: Jeff Thompson <jefft0@remap.ucla.edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * A copy of the GNU General Public License is in the file COPYING.
 */

package net.named_data.jndn.tests;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.named_data.jndn.ContentType;
import net.named_data.jndn.Data;
import net.named_data.jndn.KeyLocatorType;
import net.named_data.jndn.Name;
import net.named_data.jndn.Sha256WithRsaSignature;
import net.named_data.jndn.encoding.TlvWireFormat;
import net.named_data.jndn.encoding.WireFormat;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.KeyType;
import net.named_data.jndn.security.OnVerified;
import net.named_data.jndn.security.OnVerifyFailed;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.util.Blob;

public class TestEncodeDecodeData {
  // Convert the int array to a ByteBuffer.
  private static ByteBuffer
  toBuffer(int[] array)
  {
    ByteBuffer result = ByteBuffer.allocate(array.length);
    for (int i = 0; i < array.length; ++i)
      result.put((byte)(array[i] & 0xff));

    result.flip();
    return result;
  }

  private static final ByteBuffer DEFAULT_RSA_PUBLIC_KEY_DER = toBuffer(new int[] {
    0x30, 0x82, 0x01, 0x22, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01,
    0x01, 0x05, 0x00, 0x03, 0x82, 0x01, 0x0f, 0x00, 0x30, 0x82, 0x01, 0x0a, 0x02, 0x82, 0x01, 0x01,
    0x00, 0xb8, 0x09, 0xa7, 0x59, 0x82, 0x84, 0xec, 0x4f, 0x06, 0xfa, 0x1c, 0xb2, 0xe1, 0x38, 0x93,
    0x53, 0xbb, 0x7d, 0xd4, 0xac, 0x88, 0x1a, 0xf8, 0x25, 0x11, 0xe4, 0xfa, 0x1d, 0x61, 0x24, 0x5b,
    0x82, 0xca, 0xcd, 0x72, 0xce, 0xdb, 0x66, 0xb5, 0x8d, 0x54, 0xbd, 0xfb, 0x23, 0xfd, 0xe8, 0x8e,
    0xaf, 0xa7, 0xb3, 0x79, 0xbe, 0x94, 0xb5, 0xb7, 0xba, 0x17, 0xb6, 0x05, 0xae, 0xce, 0x43, 0xbe,
    0x3b, 0xce, 0x6e, 0xea, 0x07, 0xdb, 0xbf, 0x0a, 0x7e, 0xeb, 0xbc, 0xc9, 0x7b, 0x62, 0x3c, 0xf5,
    0xe1, 0xce, 0xe1, 0xd9, 0x8d, 0x9c, 0xfe, 0x1f, 0xc7, 0xf8, 0xfb, 0x59, 0xc0, 0x94, 0x0b, 0x2c,
    0xd9, 0x7d, 0xbc, 0x96, 0xeb, 0xb8, 0x79, 0x22, 0x8a, 0x2e, 0xa0, 0x12, 0x1d, 0x42, 0x07, 0xb6,
    0x5d, 0xdb, 0xe1, 0xf6, 0xb1, 0x5d, 0x7b, 0x1f, 0x54, 0x52, 0x1c, 0xa3, 0x11, 0x9b, 0xf9, 0xeb,
    0xbe, 0xb3, 0x95, 0xca, 0xa5, 0x87, 0x3f, 0x31, 0x18, 0x1a, 0xc9, 0x99, 0x01, 0xec, 0xaa, 0x90,
    0xfd, 0x8a, 0x36, 0x35, 0x5e, 0x12, 0x81, 0xbe, 0x84, 0x88, 0xa1, 0x0d, 0x19, 0x2a, 0x4a, 0x66,
    0xc1, 0x59, 0x3c, 0x41, 0x83, 0x3d, 0x3d, 0xb8, 0xd4, 0xab, 0x34, 0x90, 0x06, 0x3e, 0x1a, 0x61,
    0x74, 0xbe, 0x04, 0xf5, 0x7a, 0x69, 0x1b, 0x9d, 0x56, 0xfc, 0x83, 0xb7, 0x60, 0xc1, 0x5e, 0x9d,
    0x85, 0x34, 0xfd, 0x02, 0x1a, 0xba, 0x2c, 0x09, 0x72, 0xa7, 0x4a, 0x5e, 0x18, 0xbf, 0xc0, 0x58,
    0xa7, 0x49, 0x34, 0x46, 0x61, 0x59, 0x0e, 0xe2, 0x6e, 0x9e, 0xd2, 0xdb, 0xfd, 0x72, 0x2f, 0x3c,
    0x47, 0xcc, 0x5f, 0x99, 0x62, 0xee, 0x0d, 0xf3, 0x1f, 0x30, 0x25, 0x20, 0x92, 0x15, 0x4b, 0x04,
    0xfe, 0x15, 0x19, 0x1d, 0xdc, 0x7e, 0x5c, 0x10, 0x21, 0x52, 0x21, 0x91, 0x54, 0x60, 0x8b, 0x92,
    0x41, 0x02, 0x03, 0x01, 0x00, 0x01
  });

  // Java uses an unencrypted PKCS #8 PrivateKeyInfo, not a PKCS #1 RSAPrivateKey.
  private static final ByteBuffer DEFAULT_RSA_PRIVATE_KEY_DER = toBuffer(new int[] {
    0x30, 0x82, 0x04, 0xbf, 0x02, 0x01, 0x00, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7,
    0x0d, 0x01, 0x01, 0x01, 0x05, 0x00, 0x04, 0x82, 0x04, 0xa9, 0x30, 0x82, 0x04, 0xa5, 0x02, 0x01,
    0x00, 0x02, 0x82, 0x01, 0x01, 0x00, 0xb8, 0x09, 0xa7, 0x59, 0x82, 0x84, 0xec, 0x4f, 0x06, 0xfa,
    0x1c, 0xb2, 0xe1, 0x38, 0x93, 0x53, 0xbb, 0x7d, 0xd4, 0xac, 0x88, 0x1a, 0xf8, 0x25, 0x11, 0xe4,
    0xfa, 0x1d, 0x61, 0x24, 0x5b, 0x82, 0xca, 0xcd, 0x72, 0xce, 0xdb, 0x66, 0xb5, 0x8d, 0x54, 0xbd,
    0xfb, 0x23, 0xfd, 0xe8, 0x8e, 0xaf, 0xa7, 0xb3, 0x79, 0xbe, 0x94, 0xb5, 0xb7, 0xba, 0x17, 0xb6,
    0x05, 0xae, 0xce, 0x43, 0xbe, 0x3b, 0xce, 0x6e, 0xea, 0x07, 0xdb, 0xbf, 0x0a, 0x7e, 0xeb, 0xbc,
    0xc9, 0x7b, 0x62, 0x3c, 0xf5, 0xe1, 0xce, 0xe1, 0xd9, 0x8d, 0x9c, 0xfe, 0x1f, 0xc7, 0xf8, 0xfb,
    0x59, 0xc0, 0x94, 0x0b, 0x2c, 0xd9, 0x7d, 0xbc, 0x96, 0xeb, 0xb8, 0x79, 0x22, 0x8a, 0x2e, 0xa0,
    0x12, 0x1d, 0x42, 0x07, 0xb6, 0x5d, 0xdb, 0xe1, 0xf6, 0xb1, 0x5d, 0x7b, 0x1f, 0x54, 0x52, 0x1c,
    0xa3, 0x11, 0x9b, 0xf9, 0xeb, 0xbe, 0xb3, 0x95, 0xca, 0xa5, 0x87, 0x3f, 0x31, 0x18, 0x1a, 0xc9,
    0x99, 0x01, 0xec, 0xaa, 0x90, 0xfd, 0x8a, 0x36, 0x35, 0x5e, 0x12, 0x81, 0xbe, 0x84, 0x88, 0xa1,
    0x0d, 0x19, 0x2a, 0x4a, 0x66, 0xc1, 0x59, 0x3c, 0x41, 0x83, 0x3d, 0x3d, 0xb8, 0xd4, 0xab, 0x34,
    0x90, 0x06, 0x3e, 0x1a, 0x61, 0x74, 0xbe, 0x04, 0xf5, 0x7a, 0x69, 0x1b, 0x9d, 0x56, 0xfc, 0x83,
    0xb7, 0x60, 0xc1, 0x5e, 0x9d, 0x85, 0x34, 0xfd, 0x02, 0x1a, 0xba, 0x2c, 0x09, 0x72, 0xa7, 0x4a,
    0x5e, 0x18, 0xbf, 0xc0, 0x58, 0xa7, 0x49, 0x34, 0x46, 0x61, 0x59, 0x0e, 0xe2, 0x6e, 0x9e, 0xd2,
    0xdb, 0xfd, 0x72, 0x2f, 0x3c, 0x47, 0xcc, 0x5f, 0x99, 0x62, 0xee, 0x0d, 0xf3, 0x1f, 0x30, 0x25,
    0x20, 0x92, 0x15, 0x4b, 0x04, 0xfe, 0x15, 0x19, 0x1d, 0xdc, 0x7e, 0x5c, 0x10, 0x21, 0x52, 0x21,
    0x91, 0x54, 0x60, 0x8b, 0x92, 0x41, 0x02, 0x03, 0x01, 0x00, 0x01, 0x02, 0x82, 0x01, 0x01, 0x00,
    0x8a, 0x05, 0xfb, 0x73, 0x7f, 0x16, 0xaf, 0x9f, 0xa9, 0x4c, 0xe5, 0x3f, 0x26, 0xf8, 0x66, 0x4d,
    0xd2, 0xfc, 0xd1, 0x06, 0xc0, 0x60, 0xf1, 0x9f, 0xe3, 0xa6, 0xc6, 0x0a, 0x48, 0xb3, 0x9a, 0xca,
    0x21, 0xcd, 0x29, 0x80, 0x88, 0x3d, 0xa4, 0x85, 0xa5, 0x7b, 0x82, 0x21, 0x81, 0x28, 0xeb, 0xf2,
    0x43, 0x24, 0xb0, 0x76, 0xc5, 0x52, 0xef, 0xc2, 0xea, 0x4b, 0x82, 0x41, 0x92, 0xc2, 0x6d, 0xa6,
    0xae, 0xf0, 0xb2, 0x26, 0x48, 0xa1, 0x23, 0x7f, 0x02, 0xcf, 0xa8, 0x90, 0x17, 0xa2, 0x3e, 0x8a,
    0x26, 0xbd, 0x6d, 0x8a, 0xee, 0xa6, 0x0c, 0x31, 0xce, 0xc2, 0xbb, 0x92, 0x59, 0xb5, 0x73, 0xe2,
    0x7d, 0x91, 0x75, 0xe2, 0xbd, 0x8c, 0x63, 0xe2, 0x1c, 0x8b, 0xc2, 0x6a, 0x1c, 0xfe, 0x69, 0xc0,
    0x44, 0xcb, 0x58, 0x57, 0xb7, 0x13, 0x42, 0xf0, 0xdb, 0x50, 0x4c, 0xe0, 0x45, 0x09, 0x8f, 0xca,
    0x45, 0x8a, 0x06, 0xfe, 0x98, 0xd1, 0x22, 0xf5, 0x5a, 0x9a, 0xdf, 0x89, 0x17, 0xca, 0x20, 0xcc,
    0x12, 0xa9, 0x09, 0x3d, 0xd5, 0xf7, 0xe3, 0xeb, 0x08, 0x4a, 0xc4, 0x12, 0xc0, 0xb9, 0x47, 0x6c,
    0x79, 0x50, 0x66, 0xa3, 0xf8, 0xaf, 0x2c, 0xfa, 0xb4, 0x6b, 0xec, 0x03, 0xad, 0xcb, 0xda, 0x24,
    0x0c, 0x52, 0x07, 0x87, 0x88, 0xc0, 0x21, 0xf3, 0x02, 0xe8, 0x24, 0x44, 0x0f, 0xcd, 0xa0, 0xad,
    0x2f, 0x1b, 0x79, 0xab, 0x6b, 0x49, 0x4a, 0xe6, 0x3b, 0xd0, 0xad, 0xc3, 0x48, 0xb9, 0xf7, 0xf1,
    0x34, 0x09, 0xeb, 0x7a, 0xc0, 0xd5, 0x0d, 0x39, 0xd8, 0x45, 0xce, 0x36, 0x7a, 0xd8, 0xde, 0x3c,
    0xb0, 0x21, 0x96, 0x97, 0x8a, 0xff, 0x8b, 0x23, 0x60, 0x4f, 0xf0, 0x3d, 0xd7, 0x8f, 0xf3, 0x2c,
    0xcb, 0x1d, 0x48, 0x3f, 0x86, 0xc4, 0xa9, 0x00, 0xf2, 0x23, 0x2d, 0x72, 0x4d, 0x66, 0xa5, 0x01,
    0x02, 0x81, 0x81, 0x00, 0xdc, 0x4f, 0x99, 0x44, 0x0d, 0x7f, 0x59, 0x46, 0x1e, 0x8f, 0xe7, 0x2d,
    0x8d, 0xdd, 0x54, 0xc0, 0xf7, 0xfa, 0x46, 0x0d, 0x9d, 0x35, 0x03, 0xf1, 0x7c, 0x12, 0xf3, 0x5a,
    0x9d, 0x83, 0xcf, 0xdd, 0x37, 0x21, 0x7c, 0xb7, 0xee, 0xc3, 0x39, 0xd2, 0x75, 0x8f, 0xb2, 0x2d,
    0x6f, 0xec, 0xc6, 0x03, 0x55, 0xd7, 0x00, 0x67, 0xd3, 0x9b, 0xa2, 0x68, 0x50, 0x6f, 0x9e, 0x28,
    0xa4, 0x76, 0x39, 0x2b, 0xb2, 0x65, 0xcc, 0x72, 0x82, 0x93, 0xa0, 0xcf, 0x10, 0x05, 0x6a, 0x75,
    0xca, 0x85, 0x35, 0x99, 0xb0, 0xa6, 0xc6, 0xef, 0x4c, 0x4d, 0x99, 0x7d, 0x2c, 0x38, 0x01, 0x21,
    0xb5, 0x31, 0xac, 0x80, 0x54, 0xc4, 0x18, 0x4b, 0xfd, 0xef, 0xb3, 0x30, 0x22, 0x51, 0x5a, 0xea,
    0x7d, 0x9b, 0xb2, 0x9d, 0xcb, 0xba, 0x3f, 0xc0, 0x1a, 0x6b, 0xcd, 0xb0, 0xe6, 0x2f, 0x04, 0x33,
    0xd7, 0x3a, 0x49, 0x71, 0x02, 0x81, 0x81, 0x00, 0xd5, 0xd9, 0xc9, 0x70, 0x1a, 0x13, 0xb3, 0x39,
    0x24, 0x02, 0xee, 0xb0, 0xbb, 0x84, 0x17, 0x12, 0xc6, 0xbd, 0x65, 0x73, 0xe9, 0x34, 0x5d, 0x43,
    0xff, 0xdc, 0xf8, 0x55, 0xaf, 0x2a, 0xb9, 0xe1, 0xfa, 0x71, 0x65, 0x4e, 0x50, 0x0f, 0xa4, 0x3b,
    0xe5, 0x68, 0xf2, 0x49, 0x71, 0xaf, 0x15, 0x88, 0xd7, 0xaf, 0xc4, 0x9d, 0x94, 0x84, 0x6b, 0x5b,
    0x10, 0xd5, 0xc0, 0xaa, 0x0c, 0x13, 0x62, 0x99, 0xc0, 0x8b, 0xfc, 0x90, 0x0f, 0x87, 0x40, 0x4d,
    0x58, 0x88, 0xbd, 0xe2, 0xba, 0x3e, 0x7e, 0x2d, 0xd7, 0x69, 0xa9, 0x3c, 0x09, 0x64, 0x31, 0xb6,
    0xcc, 0x4d, 0x1f, 0x23, 0xb6, 0x9e, 0x65, 0xd6, 0x81, 0xdc, 0x85, 0xcc, 0x1e, 0xf1, 0x0b, 0x84,
    0x38, 0xab, 0x93, 0x5f, 0x9f, 0x92, 0x4e, 0x93, 0x46, 0x95, 0x6b, 0x3e, 0xb6, 0xc3, 0x1b, 0xd7,
    0x69, 0xa1, 0x0a, 0x97, 0x37, 0x78, 0xed, 0xd1, 0x02, 0x81, 0x80, 0x33, 0x18, 0xc3, 0x13, 0x65,
    0x8e, 0x03, 0xc6, 0x9f, 0x90, 0x00, 0xae, 0x30, 0x19, 0x05, 0x6f, 0x3c, 0x14, 0x6f, 0xea, 0xf8,
    0x6b, 0x33, 0x5e, 0xee, 0xc7, 0xf6, 0x69, 0x2d, 0xdf, 0x44, 0x76, 0xaa, 0x32, 0xba, 0x1a, 0x6e,
    0xe6, 0x18, 0xa3, 0x17, 0x61, 0x1c, 0x92, 0x2d, 0x43, 0x5d, 0x29, 0xa8, 0xdf, 0x14, 0xd8, 0xff,
    0xdb, 0x38, 0xef, 0xb8, 0xb8, 0x2a, 0x96, 0x82, 0x8e, 0x68, 0xf4, 0x19, 0x8c, 0x42, 0xbe, 0xcc,
    0x4a, 0x31, 0x21, 0xd5, 0x35, 0x6c, 0x5b, 0xa5, 0x7c, 0xff, 0xd1, 0x85, 0x87, 0x28, 0xdc, 0x97,
    0x75, 0xe8, 0x03, 0x80, 0x1d, 0xfd, 0x25, 0x34, 0x41, 0x31, 0x21, 0x12, 0x87, 0xe8, 0x9a, 0xb7,
    0x6a, 0xc0, 0xc4, 0x89, 0x31, 0x15, 0x45, 0x0d, 0x9c, 0xee, 0xf0, 0x6a, 0x2f, 0xe8, 0x59, 0x45,
    0xc7, 0x7b, 0x0d, 0x6c, 0x55, 0xbb, 0x43, 0xca, 0xc7, 0x5a, 0x01, 0x02, 0x81, 0x81, 0x00, 0xab,
    0xf4, 0xd5, 0xcf, 0x78, 0x88, 0x82, 0xc2, 0xdd, 0xbc, 0x25, 0xe6, 0xa2, 0xc1, 0xd2, 0x33, 0xdc,
    0xef, 0x0a, 0x97, 0x2b, 0xdc, 0x59, 0x6a, 0x86, 0x61, 0x4e, 0xa6, 0xc7, 0x95, 0x99, 0xa6, 0xa6,
    0x55, 0x6c, 0x5a, 0x8e, 0x72, 0x25, 0x63, 0xac, 0x52, 0xb9, 0x10, 0x69, 0x83, 0x99, 0xd3, 0x51,
    0x6c, 0x1a, 0xb3, 0x83, 0x6a, 0xff, 0x50, 0x58, 0xb7, 0x28, 0x97, 0x13, 0xe2, 0xba, 0x94, 0x5b,
    0x89, 0xb4, 0xea, 0xba, 0x31, 0xcd, 0x78, 0xe4, 0x4a, 0x00, 0x36, 0x42, 0x00, 0x62, 0x41, 0xc6,
    0x47, 0x46, 0x37, 0xea, 0x6d, 0x50, 0xb4, 0x66, 0x8f, 0x55, 0x0c, 0xc8, 0x99, 0x91, 0xd5, 0xec,
    0xd2, 0x40, 0x1c, 0x24, 0x7d, 0x3a, 0xff, 0x74, 0xfa, 0x32, 0x24, 0xe0, 0x11, 0x2b, 0x71, 0xad,
    0x7e, 0x14, 0xa0, 0x77, 0x21, 0x68, 0x4f, 0xcc, 0xb6, 0x1b, 0xe8, 0x00, 0x49, 0x13, 0x21, 0x02,
    0x81, 0x81, 0x00, 0xb6, 0x18, 0x73, 0x59, 0x2c, 0x4f, 0x92, 0xac, 0xa2, 0x2e, 0x5f, 0xb6, 0xbe,
    0x78, 0x5d, 0x47, 0x71, 0x04, 0x92, 0xf0, 0xd7, 0xe8, 0xc5, 0x7a, 0x84, 0x6b, 0xb8, 0xb4, 0x30,
    0x1f, 0xd8, 0x0d, 0x58, 0xd0, 0x64, 0x80, 0xa7, 0x21, 0x1a, 0x48, 0x00, 0x37, 0xd6, 0x19, 0x71,
    0xbb, 0x91, 0x20, 0x9d, 0xe2, 0xc3, 0xec, 0xdb, 0x36, 0x1c, 0xca, 0x48, 0x7d, 0x03, 0x32, 0x74,
    0x1e, 0x65, 0x73, 0x02, 0x90, 0x73, 0xd8, 0x3f, 0xb5, 0x52, 0x35, 0x79, 0x1c, 0xee, 0x93, 0xa3,
    0x32, 0x8b, 0xed, 0x89, 0x98, 0xf1, 0x0c, 0xd8, 0x12, 0xf2, 0x89, 0x7f, 0x32, 0x23, 0xec, 0x67,
    0x66, 0x52, 0x83, 0x89, 0x99, 0x5e, 0x42, 0x2b, 0x42, 0x4b, 0x84, 0x50, 0x1b, 0x3e, 0x47, 0x6d,
    0x74, 0xfb, 0xd1, 0xa6, 0x10, 0x20, 0x6c, 0x6e, 0xbe, 0x44, 0x3f, 0xb9, 0xfe, 0xbc, 0x8d, 0xda,
    0xcb, 0xea, 0x8f
  });

  private static final ByteBuffer BinaryXmlData = toBuffer(new int[] {
0x04, 0x82, // NDN Data
  0x02, 0xaa, // Signature
    0x03, 0xb2, // SignatureBits
      0x08, 0x85, 0x20, 0xea, 0xb5, 0xb0, 0x63, 0xda, 0x94, 0xe9, 0x68, 0x7a,
      0x8e, 0x65, 0x60, 0xe0, 0xc6, 0x43, 0x96, 0xd9, 0x69, 0xb4, 0x40, 0x72, 0x52, 0x00, 0x2c, 0x8e, 0x2a, 0xf5,
      0x47, 0x12, 0x59, 0x93, 0xda, 0xed, 0x82, 0xd0, 0xf8, 0xe6, 0x65, 0x09, 0x87, 0x84, 0x54, 0xc7, 0xce, 0x9a,
      0x93, 0x0d, 0x47, 0xf1, 0xf9, 0x3b, 0x98, 0x78, 0x2c, 0x22, 0x21, 0xd9, 0x2b, 0xda, 0x03, 0x30, 0x84, 0xf3,
      0xc5, 0x52, 0x64, 0x2b, 0x1d, 0xde, 0x50, 0xe0, 0xee, 0xca, 0xa2, 0x73, 0x7a, 0x93, 0x30, 0xa8, 0x47, 0x7f,
      0x6f, 0x41, 0xb0, 0xc8, 0x6e, 0x89, 0x1c, 0xcc, 0xf9, 0x01, 0x44, 0xc3, 0x08, 0xcf, 0x77, 0x47, 0xfc, 0xed,
      0x48, 0xf0, 0x4c, 0xe9, 0xc2, 0x3b, 0x7d, 0xef, 0x6e, 0xa4, 0x80, 0x40, 0x9e, 0x43, 0xb6, 0x77, 0x7a, 0x1d,
      0x51, 0xed, 0x98, 0x33, 0x93, 0xdd, 0x88, 0x01, 0x0e, 0xd3,
    0x00,
  0x00,
  0xf2, 0xfa, 0x9d, 0x6e, 0x64, 0x6e, 0x00, 0xfa, 0x9d, 0x61, 0x62, 0x63, 0x00, 0x00,  // Name
  0x01, 0xa2, // SignedInfo
    0x03, 0xe2, // PublisherPublicKeyDigest
      0x02, 0x85, 0xb5, 0x50, 0x6b, 0x1a,
      0xba, 0x3d, 0xa7, 0x76, 0x1b, 0x0f, 0x8d, 0x61, 0xa4, 0xaa, 0x7e, 0x3b, 0x6d, 0x15, 0xb4, 0x26, 0xfe, 0xb5,
      0xbd, 0xa8, 0x23, 0x89, 0xac, 0xa7, 0x65, 0xa3, 0xb8, 0x1c,
    0x00,
    0x02, 0xba, // Timestamp
      0xb5, 0x05, 0x1d, 0xde, 0xe9, 0x5b, 0xdb,
    0x00,
    0x01, 0xe2, // KeyLocator
      0x01, 0xda, // Key
        0x0a, 0x95, 0x30, 0x81, 0x9f, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86,
        0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00, 0x03, 0x81, 0x8d, 0x00, 0x30, 0x81, 0x89, 0x02, 0x81,
        0x81, 0x00, 0xe1, 0x7d, 0x30, 0xa7, 0xd8, 0x28, 0xab, 0x1b, 0x84, 0x0b, 0x17, 0x54, 0x2d, 0xca, 0xf6, 0x20,
        0x7a, 0xfd, 0x22, 0x1e, 0x08, 0x6b, 0x2a, 0x60, 0xd1, 0x6c, 0xb7, 0xf5, 0x44, 0x48, 0xba, 0x9f, 0x3f, 0x08,
        0xbc, 0xd0, 0x99, 0xdb, 0x21, 0xdd, 0x16, 0x2a, 0x77, 0x9e, 0x61, 0xaa, 0x89, 0xee, 0xe5, 0x54, 0xd3, 0xa4,
        0x7d, 0xe2, 0x30, 0xbc, 0x7a, 0xc5, 0x90, 0xd5, 0x24, 0x06, 0x7c, 0x38, 0x98, 0xbb, 0xa6, 0xf5, 0xdc, 0x43,
        0x60, 0xb8, 0x45, 0xed, 0xa4, 0x8c, 0xbd, 0x9c, 0xf1, 0x26, 0xa7, 0x23, 0x44, 0x5f, 0x0e, 0x19, 0x52, 0xd7,
        0x32, 0x5a, 0x75, 0xfa, 0xf5, 0x56, 0x14, 0x4f, 0x9a, 0x98, 0xaf, 0x71, 0x86, 0xb0, 0x27, 0x86, 0x85, 0xb8,
        0xe2, 0xc0, 0x8b, 0xea, 0x87, 0x17, 0x1b, 0x4d, 0xee, 0x58, 0x5c, 0x18, 0x28, 0x29, 0x5b, 0x53, 0x95, 0xeb,
        0x4a, 0x17, 0x77, 0x9f, 0x02, 0x03, 0x01, 0x00, 0x01,
      0x00,
    0x00,
  0x00,
  0x01, 0x9a, // Content
    0xc5, 0x53, 0x55, 0x43, 0x43, 0x45, 0x53, 0x53, 0x21,
  0x00,
0x00,
1
  });

  private static final ByteBuffer TlvData = toBuffer(new int[] {
0x06, 0xCE, // NDN Data
  0x07, 0x0A, 0x08, 0x03, 0x6E, 0x64, 0x6E, 0x08, 0x03, 0x61, 0x62, 0x63, // Name
  0x14, 0x0A, // MetaInfo
    0x19, 0x02, 0x13, 0x88, // FreshnessPeriod
    0x1A, 0x04, // FinalBlockId
      0x08, 0x02, 0x00, 0x09, // NameComponent
  0x15, 0x08, 0x53, 0x55, 0x43, 0x43, 0x45, 0x53, 0x53, 0x21, // Content
  0x16, 0x28, // SignatureInfo
    0x1B, 0x01, 0x01, // SignatureType
    0x1C, 0x23, // KeyLocator
      0x07, 0x21, // Name
        0x08, 0x08, 0x74, 0x65, 0x73, 0x74, 0x6E, 0x61, 0x6D, 0x65,
        0x08, 0x03, 0x4B, 0x45, 0x59,
        0x08, 0x07, 0x44, 0x53, 0x4B, 0x2D, 0x31, 0x32, 0x33,
        0x08, 0x07, 0x49, 0x44, 0x2D, 0x43, 0x45, 0x52, 0x54,
  0x17, 0x80, // SignatureValue
    0x1A, 0x03, 0xC3, 0x9C, 0x4F, 0xC5, 0x5C, 0x36, 0xA2, 0xE7, 0x9C, 0xEE, 0x52, 0xFE, 0x45, 0xA7,
    0xE1, 0x0C, 0xFB, 0x95, 0xAC, 0xB4, 0x9B, 0xCC, 0xB6, 0xA0, 0xC3, 0x4A, 0xAA, 0x45, 0xBF, 0xBF,
    0xDF, 0x0B, 0x51, 0xD5, 0xA4, 0x8B, 0xF2, 0xAB, 0x45, 0x97, 0x1C, 0x24, 0xD8, 0xE2, 0xC2, 0x8A,
    0x4D, 0x40, 0x12, 0xD7, 0x77, 0x01, 0xEB, 0x74, 0x35, 0xF1, 0x4D, 0xDD, 0xD0, 0xF3, 0xA6, 0x9A,
    0xB7, 0xA4, 0xF1, 0x7F, 0xA7, 0x84, 0x34, 0xD7, 0x08, 0x25, 0x52, 0x80, 0x8B, 0x6C, 0x42, 0x93,
    0x04, 0x1E, 0x07, 0x1F, 0x4F, 0x76, 0x43, 0x18, 0xF2, 0xF8, 0x51, 0x1A, 0x56, 0xAF, 0xE6, 0xA9,
    0x31, 0xCB, 0x6C, 0x1C, 0x0A, 0xA4, 0x01, 0x10, 0xFC, 0xC8, 0x66, 0xCE, 0x2E, 0x9C, 0x0B, 0x2D,
    0x7F, 0xB4, 0x64, 0xA0, 0xEE, 0x22, 0x82, 0xC8, 0x34, 0xF7, 0x9A, 0xF5, 0x51, 0x12, 0x2A, 0x84,
1
  });

  private static void
  dumpData(Data data)
  {
    System.out.println("name: " + data.getName().toUri());
    if (data.getContent().size() > 0) {
      System.out.print("content (raw): ");
      ByteBuffer buf = data.getContent().buf();
      while(buf.remaining() > 0)
        System.out.print((char)buf.get());
      System.out.println();
      System.out.println("content (hex): " + data.getContent().toHex());
    }
    else
      System.out.println("content: <empty>");

    if (!(data.getMetaInfo().getType() == ContentType.BLOB ||
          data.getMetaInfo().getType() == ContentType.DATA)) {
      System.out.print("metaInfo.type: ");
      if (data.getMetaInfo().getType() == ContentType.ENCR)
        System.out.println("ENCR");
      else if (data.getMetaInfo().getType() == ContentType.GONE)
        System.out.println("GONE");
      else if (data.getMetaInfo().getType() == ContentType.KEY)
        System.out.println("KEY");
      else if (data.getMetaInfo().getType() == ContentType.LINK)
        System.out.println("LINK");
      else if (data.getMetaInfo().getType() == ContentType.NACK)
        System.out.println("NACK");
    }
    System.out.println("metaInfo.freshnessPeriod (milliseconds): " +
      (data.getMetaInfo().getFreshnessPeriod() >= 0 ?
        "" + data.getMetaInfo().getFreshnessPeriod() : "<none>"));
    System.out.println("metaInfo.finalBlockID: " +
      (data.getMetaInfo().getFinalBlockID().getValue().size() > 0 ?
       data.getMetaInfo().getFinalBlockID().getValue().toHex() : "<none>"));

    if (data.getSignature() instanceof Sha256WithRsaSignature) {
      Sha256WithRsaSignature signature =
        (Sha256WithRsaSignature)data.getSignature();
      System.out.println("signature.signature: " +
        (signature.getSignature().size() > 0 ?
         signature.getSignature().toHex() : "<none>"));
      System.out.print("signature.keyLocator: ");
      if (signature.getKeyLocator().getType() == KeyLocatorType.NONE)
        System.out.println("<none>");
      else if (signature.getKeyLocator().getType() == KeyLocatorType.KEY)
        System.out.println("Key: " + signature.getKeyLocator().getKeyData().toHex());
      else if (signature.getKeyLocator().getType() == KeyLocatorType.CERTIFICATE)
        System.out.println("Certificate: " + signature.getKeyLocator().getKeyData().toHex());
      else if (signature.getKeyLocator().getType() ==KeyLocatorType.KEY_LOCATOR_DIGEST)
        System.out.println("KeyLocatorDigest: " + signature.getKeyLocator().getKeyData().toHex());
      else if (signature.getKeyLocator().getType() == KeyLocatorType.KEYNAME)
        System.out.println("KeyName: " + signature.getKeyLocator().getKeyName().toUri());
      else
        System.out.println("<unrecognized ndn_KeyLocatorType>");
    }
  }

  private static class VerifyCallbacks implements OnVerified, OnVerifyFailed {
    public VerifyCallbacks(String prefix) { prefix_ = prefix; }

    private String prefix_;

    public void onVerified(Data data)
    {
      System.out.println(prefix_ + " signature verification: VERIFIED");
    }

    public void onVerifyFailed(Data data)
    {
      System.out.println(prefix_ + " signature verification: FAILED");
    }
  }

  public static void
  main(String[] args)
  {
    try {
      // Don't show INFO log messages.
      Logger.getLogger("").setLevel(Level.WARNING);

      Data data = new Data();
      // Note: While we transition to the TLV wire format, check if it has been made the default.
      if (WireFormat.getDefaultWireFormat() == TlvWireFormat.get())
        data.wireDecode(new Blob(TlvData, false));
      else
        data.wireDecode(new Blob(BinaryXmlData, false));
      System.out.println("Decoded Data:");
      dumpData(data);

      // Set the content again to clear the cached encoding so we encode again.
      data.setContent(data.getContent());
      Blob encoding = data.wireEncode();

      Data reDecodedData = new Data();
      reDecodedData.wireDecode(encoding);
      System.out.println();
      System.out.println("Re-decoded Data:");
      dumpData(reDecodedData);

      Data freshData = new Data(new Name("/ndn/abc"));
      freshData.setContent(new Blob("SUCCESS!"));
      freshData.getMetaInfo().setFreshnessPeriod(5000);
      freshData.getMetaInfo().setFinalBlockID(new Name("/%00%09").get(0));

      MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
      MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
      KeyChain keyChain = new KeyChain
        (new IdentityManager(identityStorage, privateKeyStorage),
         new SelfVerifyPolicyManager(identityStorage));

      // Initialize the storage.
      Name keyName = new Name("/testname/DSK-123");
      Name certificateName = keyName.getSubName(0, keyName.size() - 1).append
        ("KEY").append(keyName.get(-1)).append("ID-CERT").append("0");
      identityStorage.addKey(keyName, KeyType.RSA, new Blob(DEFAULT_RSA_PUBLIC_KEY_DER, false));
      privateKeyStorage.setKeyPairForKeyName
        (keyName, KeyType.RSA, DEFAULT_RSA_PUBLIC_KEY_DER, DEFAULT_RSA_PRIVATE_KEY_DER);

      keyChain.sign(freshData, certificateName);
      System.out.println();
      System.out.println("Freshly-signed Data:");
      dumpData(freshData);

      VerifyCallbacks callbacks = new VerifyCallbacks("Freshly-signed Data");
      keyChain.verifyData(freshData, callbacks, callbacks);
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
