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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bvutest.iapdemo.R;
import com.bvutest.iapdemo.adapter.ProductListAdapter;
import com.bvutest.iapdemo.callback.PurchaseIntentResultCallback;
import com.bvutest.iapdemo.callback.QueryPurchasesCallback;
import com.bvutest.iapdemo.callback.ProductInfoCallback;
import com.bvutest.iapdemo.common.CipherUtil;
import com.bvutest.iapdemo.common.Constants;
import com.bvutest.iapdemo.common.ExceptionHandle;
import com.bvutest.iapdemo.common.IapRequestHelper;
import com.bvutest.iapdemo.common.Key;
import com.bvutest.iapdemo.common.Utils;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NonConsumptionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NonConsumptionActivity";
    private static final String NC_PRODUCTID = "NCProduct01";

    private IapClient mClient;

    private ListView nonconsumableProductListview;
    private List<ProductInfo> nonconsumableProducts = new ArrayList<>();
    private ProductListAdapter productListAdapter;

    private TextView purchaseText;
    private Boolean purchaseStatus = false;
    private String purchaseToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_consumption_v2);
        mClient = Iap.getIapClient(this);
        initView();
        // call the obtainOwnedPurchases API during startup to obtain the data about non-consumable products that a user has purchased.
        refresh();
    }

    private void initView() {
        nonconsumableProductListview = (ListView) findViewById(R.id.nonconsumable_product_list);
        purchaseText = (TextView)findViewById(R.id.purchase_text);
        findViewById(R.id.refresh_btn).setOnClickListener(this);
        findViewById(R.id.consume_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.refresh_btn:
                refresh();
                break;
            case R.id.consume_btn:
                consume();
                break;
            default:
                break;
        }
    }

    private void refresh() {
        if (purchaseStatus) {
            Log.d(TAG, "refresh >> showPurchaseStatus");
            showPurchaseStatus();
        } else {
            Log.d(TAG, "refresh >> checkPurchases");
            checkPurchases();
        }
        getProducts();
    }

    private void consume() {
        if(purchaseToken!=null && purchaseToken.length()>0){
            IapRequestHelper.consumeOwnedPurchase(this, mClient, purchaseToken);
        }else{
            Utils.showMessage(this, "No products purchased");
        }


    }

    private void checkPurchases() {
        // Query users' purchased non-consumable products.
        Log.d(TAG, "checkPurchases");
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_NONCONSUMABLE, new QueryPurchasesCallback() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                Log.d(TAG, "checkPurchases >> success");
                checkPurchaseState(result);
            }

            @Override
            public void onFail(Exception e) {
                Log.d(TAG, "checkPurchases >> failure >> " + IapClient.PriceType.IN_APP_NONCONSUMABLE + ", " + e.getMessage());
                Utils.showMessage(NonConsumptionActivity.this, "get Purchases fail, " + e.getMessage());
            }
        });

    }

    private void checkPurchaseState(OwnedPurchasesResult result) {
        if (result == null || result.getInAppPurchaseDataList() == null) {
            Log.d(TAG, "checkPurchaseState >> result is null");
            return;
        }
        Log.d(TAG, "checkPurchaseState >> result is valid");
        List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
        List<String> inAppSignature= result.getInAppSignature();

        for (int i = 0; i < inAppPurchaseDataList.size(); i++) {
            try{
                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataList.get(i));
                if (NC_PRODUCTID.equals(inAppPurchaseDataBean.getProductId())) {
                    Log.d(TAG, "checkPurchaseState >> NC Product Found >> " + inAppPurchaseDataList.get(i));
                    purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                    purchaseStatus = true;
                    break;
                }
            }catch(Exception e){

            }
            Log.d(TAG, "checkPurchaseState >> "+inAppPurchaseDataList.get(i));
            if (CipherUtil.doCheck(inAppPurchaseDataList.get(i), inAppSignature.get(i), Key.getPublicKey())) {
                try {
                    InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataList.get(i));
                    if (inAppPurchaseDataBean.getPurchaseState() == InAppPurchaseData.PurchaseState.PURCHASED) {
                        if (NC_PRODUCTID.equals(inAppPurchaseDataBean.getProductId())) {
                            Log.d(TAG, "checkPurchaseState >> NC Product Found >> " + inAppPurchaseDataList.get(i));
                            purchaseStatus = true;
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "checkPurchaseState >> " + e.getMessage());
                }
            } else {
                Log.e(TAG, "checkPurchaseState >> verify signature error");
            }
        }
        if (purchaseStatus) {
            showPurchaseStatus();
        }
    }

    private void showPurchaseStatus() {
        Log.d(TAG, "showPurchaseStatus");
//        purchaseText.setVisibility(View.VISIBLE);
    }

    private void getProducts() {
        Log.d(TAG, "getProducts");
        nonconsumableProducts.clear();
        List<String> productIds = new ArrayList<>();
        productIds.add(NC_PRODUCTID);
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_NONCONSUMABLE, new ProductInfoCallback() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                Log.i(TAG, "obtainProductInfo, success");
                if (result == null || result.getProductInfoList() == null) {
                    return;
                }
                // to show product information
                ProductInfo tempProductInfo = null;
                for (ProductInfo productInfo : result.getProductInfoList()) {
                    if (productInfo != null && NC_PRODUCTID.equals(productInfo.getProductId())) {
                        tempProductInfo = productInfo;
                        break;
                    }
                }
                if (tempProductInfo != null) {
                    showProducts(tempProductInfo);
                }
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainProductInfo: " + e.getMessage());
            }
        });
    }

    private void showProducts(ProductInfo productInfo) {
        Log.d(TAG, "showProducts");

        nonconsumableProducts.add(productInfo);
        productListAdapter = new ProductListAdapter(this, nonconsumableProducts);
        nonconsumableProductListview.setAdapter(productListAdapter);
        productListAdapter.notifyDataSetChanged();
        nonconsumableProductListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToBuy(position);
            }
        });
    }

    private void goToBuy(int index) {
        Log.d(TAG, "goToBuy >> " + index);
        ProductInfo productInfo = nonconsumableProducts.get(index);
        IapRequestHelper.createPurchaseIntent(mClient, productInfo.getProductId(), IapClient.PriceType.IN_APP_NONCONSUMABLE, new PurchaseIntentResultCallback() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Log.e(TAG, "result is null");
                    return;
                }
                // you should pull up the page to complete the payment process
                IapRequestHelper.startResolutionForResult(NonConsumptionActivity.this, result.getStatus(), Constants.REQ_CODE_BUY);
            }

            @Override
            public void onFail(Exception e) {
                int errorCode = ExceptionHandle.handle(NonConsumptionActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    IapApiException apiException = (IapApiException)e;
                    Log.i(TAG, "createPurchaseIntent, returnCode: " + errorCode);
                    // handle error scenarios
                    switch (errorCode) {
                        case OrderStatusCode.ORDER_PRODUCT_OWNED:
                            checkPurchases();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQ_CODE_BUY) {
            if (data == null) {
                Log.e(TAG, "data is null");
                return;
            }
            PurchaseResultInfo buyResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(buyResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Utils.showMessage(this,"Order has been canceled!");
                    Log.d(TAG, "onActivityResult >> buy result >> order has been canceled");
                    break;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    Log.d(TAG, "onActivityResult >> buy result >> product already owned");
                    checkPurchases();
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    if (CipherUtil.doCheck(buyResultInfo.getInAppPurchaseData(), buyResultInfo.getInAppDataSignature(), Key.getPublicKey())) {
                        purchaseStatus = true;
                        Log.d(TAG, "onActivityResult >> buy result >> order state success");
                        showPurchaseStatus();
                    } else {
                        Utils.showMessage(this, getString(R.string.pay_success_signfail));
                    }

                    break;
                default:
                    break;
            }
        }

    }
}
