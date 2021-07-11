package com.erg.freecuisine.helpers;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.erg.freecuisine.R;

import java.util.ArrayList;
import java.util.List;

public class BillingHelper {

    private static final String TAG = "BillingHelper";

    private static final String SKU_PREMIUM = "premium_status";

    private Activity context;
    private BillingClient billingClient;
    private SharedPreferencesHelper spHelper;


    public BillingHelper(Activity context) {
        this.context = context;
        spHelper = new SharedPreferencesHelper(context);
    }

    public void init() {

        Log.d(TAG, " billingClient init: On");

        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(purchaseUpdateListener)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "onBillingSetupFinished: Success Billing Connection");
                } else {
                    Log.e(TAG, "onBillingSetupFinished: Fail Billing Connection Code: "
                            + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected: Disconnected");
            }
        });
    }

    public void loadAllSkusAndStartBillingFlow() {
        if (billingClient.isReady()) {
            Log.d(TAG, "loadAllSkus started: billingClient isReady");

            List<String> skuList = new ArrayList<>();
            skuList.add(SKU_PREMIUM);
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(params,
                    (billingResult, skuDetailsList) -> {
                        Log.d(TAG, "querySkuDetailsAsync: ResponseCode: " + billingResult.getResponseCode()
                                + " DebugMessage: " + billingResult.getDebugMessage());

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "billingResult: " + billingResult.getDebugMessage());
                            assert skuDetailsList != null;
                            Log.d(TAG, "skuDetailsList: " + skuDetailsList.toString());
                            for (SkuDetails skuDetails : skuDetailsList) {
                                if (skuDetails.getSku().equals(SKU_PREMIUM)) {
                                    startBillingFlow(skuDetails);
                                }
                            }
                        } else {
                            Log.e(TAG, "loadAllSkusAndStartBillingFlow: " +
                                    context.getString(R.string.cant_not_query_product));
                            if (!context.isFinishing()) {
                                MessageHelper.showErrorMessageOnMain(context,
                                        context.getString(R.string.faild_billing_connection));

                            }
                        }
                    });
        }
    }

    private void startBillingFlow(SkuDetails skuDetails) {
        if (skuDetails != null) {
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();

            int responseCode = billingClient
                    .launchBillingFlow(context, billingFlowParams)
                    .getResponseCode();

            Log.d(TAG, "startBillingFlow: responseCode: " + responseCode);

            if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                spHelper.setPremiumStatus(true);

                MessageHelper.showSuccessMessageOnMain(context,
                        context.getString(R.string.already_premium));
            }
        }
    }

    private final PurchasesUpdatedListener purchaseUpdateListener = (billingResult, purchases) -> {
        Log.d(TAG, "onPurchasesUpdated: billingResult: " + billingResult.getDebugMessage());
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                ArrayList<String> purchaseSku = purchase.getSkus();
                if (purchaseSku.contains(SKU_PREMIUM)) {
                    spHelper.setPremiumStatus(true);
                    Toast.makeText(context, context.getString(R.string.you_are_premium_now),
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onPurchasesUpdated: Purchase: " + purchases.toString());
                }
            }
        }
    };
}
