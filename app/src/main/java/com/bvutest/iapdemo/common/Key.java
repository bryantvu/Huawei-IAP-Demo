/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bvutest.iapdemo.common;

public class Key {
    private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhq5dfS3JJkugYdnasvQyDqwx9RKwySDmCLItVsSPcSAaxtioas2Fobdba8CleeE4vBRRqxHEUVnpa+JBo9xnzgEx0yX4Mo++wi7LERCO4WJLsuDgogPjPVFBw8W0VZgENNkjI7MiuCQwAXhCEurE4wxUudbKLdy15dphNba0BMQy6U3VJSmfLQOqoAFI5mz2TwzFuoCDVY3LlAzWZMtl/baNfHsZa4XKT7C3wSnrvXL+AoBUWfRtt3+JBtLDchhbcJT8KHa/7lXaXTx0yZIUq9J3gLKbPP+Z8EPxT0N/XLiXsWiDr+iKqZjpatys0TIgoWn547+1V2yneWxPRxc+0QIDAQAB";

    /**
     * get the publicKey of the application
     * During the encoding process, avoid storing the public key in clear text.
     * @return
     */
    public static String getPublicKey(){
        return publicKey;
    }
}
