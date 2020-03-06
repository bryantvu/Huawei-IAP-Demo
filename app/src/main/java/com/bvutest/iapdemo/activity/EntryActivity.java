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

package com.bvutest.iapdemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bvutest.iapdemo.R;
import com.bvutest.iapdemo.callback.IsEnvReadyCallback;
import com.bvutest.iapdemo.common.Constants;
import com.bvutest.iapdemo.common.ExceptionHandle;
import com.bvutest.iapdemo.common.IapRequestHelper;
import com.bvutest.iapdemo.common.Utils;
import com.bvutest.iapdemo.subscription.SubscriptionActivity;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.util.IapClientHelper;

public class EntryActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "EntryActivity";
    private Button enterConsumableTheme;
    private Button enterNonConsumableTheme;
    private Button enterSubsribeTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queryIsReady();
    }

    /**
     * Initiating an isEnvReady request when entering the app.
     * Check if the account service country supports IAP.
     */
    private void queryIsReady() {
        IapClient mClient = Iap.getIapClient(this);
        IapRequestHelper.isEnvReady(mClient, new IsEnvReadyCallback() {
            @Override
            public void onSuccess() {
                initView();
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "isEnvReady fail, " + e.getMessage());
                ExceptionHandle.handle(EntryActivity.this, e);
            }
        });
    }

    private void initView() {
        setContentView(R.layout.activity_entry);
        enterConsumableTheme = (Button) findViewById(R.id.enter_consumable_product_scene);
        enterNonConsumableTheme = (Button) findViewById(R.id.enter_nonconsumable_product_scene);
        enterSubsribeTheme = (Button) findViewById(R.id.enter_subscription_scene);

        enterConsumableTheme.setOnClickListener(this);
        enterNonConsumableTheme.setOnClickListener(this);
        enterSubsribeTheme.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.enter_consumable_product_scene:
                intent = new Intent(this, ConsumptionActivity.class);
                startActivity(intent);
                break;
            case R.id.enter_nonconsumable_product_scene:
                intent = new Intent(this, NonConsumptionActivity.class);
                startActivity(intent);
                break;
            case R.id.enter_subscription_scene:
                intent = new Intent(this, SubscriptionActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQ_CODE_LOGIN) {
            int returnCode = IapClientHelper.parseRespCodeFromIntent(data);
            Log.i(TAG,"onActivityResult, returnCode: " + returnCode);
            if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                initView();
            } else if(returnCode == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED){
                Utils.showMessage(EntryActivity.this, "This is unavailable in your country/region.");
            } else {
                Utils.showMessage(EntryActivity.this, "user cancel login");
            }
            return;
        }

    }
}
