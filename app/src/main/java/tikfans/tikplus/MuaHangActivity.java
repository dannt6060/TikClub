package tikfans.tikplus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.RemoteConfigUtil;
import tikfans.tikplus.model.LogPurchase;
import tikfans.tikplus.util.SecureDate;

import static tikfans.tikplus.util.AppUtil.SKU_HUGE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_LARGE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_LARGE_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MAX_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MAX_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MINI_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MINI_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_SMALL_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_VIP_YEARLY;
import static tikfans.tikplus.util.AppUtil.SKU_VIP_THREE_MONTHS;
import static tikfans.tikplus.util.AppUtil.SKU_VIP_MONTHLY;

public class MuaHangActivity extends AppCompatActivity implements View.OnClickListener {

    CardView mBuyMiniLayout, mBuySmallLayout, mBuyLargeLayout, mBuyHugeLayout, mBuyMaxLayout, buyMessageLayout, vipMonthly, vipThreeMonths, vipYearly;
    TextView txtCoinBuyMiniPackage, txtCoinBuySmallPackage, txtCoinBuyLargerPackage, txtCoinBuyHugePackage, txtCoinBuyMaxPackage;
    TextView mTxtPriceMiniPackage, mTxtPriceSmallPackage, mTxtPriceLargePackage, mTxtPriceHugePackage, mTxtPriceMaxPackage, mTxtPriceWeekly, mTxtPriceMonthly, mTxtPrice3Months;
    TextView mTxtWeeklyDescription, mTxtMonthlyDescription, mTxtThreeMonthsDescription;

    //for promote pacakge
    CardView mPromoPackageLayout;
    TextView txtCoinBuyPromoPackage, mTxtPricePromoPackage, mTxtCountDown, txtCoinBasePromoPackage;
    private CountDownTimer mCountDownTimer;
    int typeOfPromotePackage = 0;
    private static int TYPE_NO_PROMOTE_PACKAGE = 0;
    private static int TYPE_MINI_PROMOTE_PACKAGE = 1;
    private static int TYPE_LARGE_PROMOTE_PACKAGE = 2;
    private static int TYPE_MAX_PROMOTE_PACKAGE = 3;

    FirebaseUser mFirebaseUser;
    String payload = "";
    boolean isBuyVipAccount = false;
    LinearLayout mBuyCoinLayout, mVipAccountLayout;
    boolean isEnablePromotePackage = false;

    private PurchasesUpdatedListener purchaseUpdateListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<com.android.billingclient.api.Purchase> purchaseList) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchaseList != null) {
                for (Purchase purchase : purchaseList) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
    };

    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

        }
    };

    private boolean isConsumePurchase(Purchase purchase) {
        String sku = purchase.getSku();
        if (sku.equals(SKU_HUGE_PACKAGE) || sku.equals(SKU_LARGE_PACKAGE) || sku.equals(SKU_MAX_PACKAGE) || sku.equals(SKU_MINI_PACKAGE) || sku.equals(SKU_SMALL_PACKAGE) || sku.equals(SKU_MINI_PROMOTE_PACKAGE) || sku.equals(SKU_LARGE_PROMOTE_PACKAGE) || sku.equals(SKU_MAX_PROMOTE_PACKAGE)) {
            return true;
        }
        return false;
    }

    void handlePurchase(final Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (isConsumePurchase(purchase)) {
                ConsumeParams consumeParams =
                        ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                ConsumeResponseListener listener = new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            // Handle the success of the consume operation.
                            hideProgressDialog();
                            Log.d("Khang", "consume: " + purchase.getSku() + ": " + purchase.getOrderId());

                            if (purchase.getSku().equals(SKU_MINI_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW));
                            } else if (purchase.getSku().equals(SKU_SMALL_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_SMALL_NEW));
                            } else if (purchase.getSku().equals(SKU_LARGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW));
                            } else if (purchase.getSku().equals(SKU_HUGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_HUGE_NEW));
                            } else if (purchase.getSku().equals(SKU_MAX_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW));
                            } else if (purchase.getSku().equals(SKU_MINI_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW) * 2);
                            } else if (purchase.getSku().equals(SKU_LARGE_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW) * 2);
                            } else if (purchase.getSku().equals(SKU_MAX_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW) * 2);
                            }

                        }
                    }
                };

                billingClient.consumeAsync(consumeParams, listener);
            } else {
                hideProgressDialog();
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                }
                Toast.makeText(getApplicationContext(), getString(R.string.update_vip_account_success), Toast.LENGTH_SHORT).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                MyChannelApplication.isVipAccount = true;
                if (user != null) {
                    FirebaseUtil.getSubscriptionInfoRef().setValue(new LogPurchase(user.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getSku(), purchase.getPurchaseTime(), purchase.getPurchaseToken(), 0));
                }
                Log.d("Khang", "subscription:" + purchase.toString());
                FirebaseUtil.getVipAccountRef().setValue(MyChannelApplication.isVipAccount);
            }
        }
    }

    private BillingClient billingClient;

    private ImageView imgToobarBack;
    private TextView mTxtTitleActionBar;
    private List<SkuDetails> skuDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_credit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        imgToobarBack = findViewById(R.id.toolbar_back);
        mTxtTitleActionBar = findViewById(R.id.text_title);

        setSupportActionBar(toolbar);

        billingClient = BillingClient.newBuilder(getApplicationContext())
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build();

        imgToobarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBuyCoinLayout = findViewById(R.id.buy_coin_layout);
        mVipAccountLayout = findViewById(R.id.vip_account_layout);

        mBuyMiniLayout = findViewById(R.id.buy_mini);
        mBuySmallLayout = findViewById(R.id.buy_small);
        mBuyLargeLayout = findViewById(R.id.buy_lager);
        mBuyHugeLayout = findViewById(R.id.buy_huge);
        mBuyMaxLayout = findViewById(R.id.buy_max);
        vipMonthly = findViewById(R.id.vip_account_month);
        vipThreeMonths = findViewById(R.id.vip_account_three_months);
        vipYearly = findViewById(R.id.vip_account_year);


        mTxtPriceMiniPackage = findViewById(R.id.price_mini_package);
        mTxtPriceSmallPackage = findViewById(R.id.price_small_package);
        mTxtPriceLargePackage = findViewById(R.id.price_large_package);
        mTxtPriceHugePackage = findViewById(R.id.price_huge_package);
        mTxtPriceMaxPackage = findViewById(R.id.price_max_package);
        mTxtPriceWeekly = findViewById(R.id.price_month);
        mTxtPriceMonthly = findViewById(R.id.price_three_months);
        mTxtPrice3Months = findViewById(R.id.price_year);
        mTxtWeeklyDescription = findViewById(R.id.weekly_description);
        mTxtMonthlyDescription = findViewById(R.id.three_monthly_description);
        mTxtThreeMonthsDescription = findViewById(R.id.yearly_description);

        progressDialog = new ProgressDialog(this);
        skuDetailsList = new ArrayList<>();

        Intent intent = getIntent();
        isBuyVipAccount = intent.getBooleanExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            payload = mFirebaseUser.getUid();
        }

        //for promotion package
        mPromoPackageLayout = findViewById(R.id.promotion_package_layout);
        mTxtPricePromoPackage = findViewById(R.id.price_promotion_package);
        txtCoinBuyPromoPackage = findViewById(R.id.txtCoinPromotionPackage);
        txtCoinBasePromoPackage = findViewById(R.id.txtCoinBasePackage);
        mTxtCountDown = findViewById(R.id.txt_promotion_package_countdown);
        isEnablePromotePackage = FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_ENABLE_PROMOTE_PACKAGE);
        mPromoPackageLayout.setOnClickListener(this);
        if (isEnablePromotePackage) {
            Date currentDate = SecureDate.getInstance().getDate();
            int month = currentDate.getMonth();
            int day = currentDate.getDay();
            int hour = currentDate.getHours();
            int minute = currentDate.getMinutes();
            int second = currentDate.getSeconds();
            long remainTime = 24 * 60 * 60 - ((long) hour * 60 * 60 + minute * 60L + second);
            Log.d("khang", "" + day + " / " + month + " : " + hour + ":" + minute + ":" + second);
            if (FirebaseAuth.getInstance().getCurrentUser() == null) finish();
            long firstSignInTime = PreferenceUtil.getLongPref(PreferenceUtil.FIRST_SIGN_IN_TIME + FirebaseAuth.getInstance().getCurrentUser().getUid(), 0);
            if ((day + month) % 15 == 1 || (day + month) % 15 == 6 || firstSignInTime > SecureDate.getInstance().getDate().getTime() - remainTime * 1000) {
//            if ((day + month) % 3 == 1) {
                mPromoPackageLayout.setVisibility(View.VISIBLE);
                typeOfPromotePackage = TYPE_MINI_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW) * 2));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW)));
            }
            if ((day + month) % 15 == 3 || (day + month) % 15 == 11) {
//            if ((day + month) % 3 == ) {
                mPromoPackageLayout.setVisibility(View.VISIBLE);
                typeOfPromotePackage = TYPE_LARGE_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW) * 2));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW)));
            }
            if ((day + month) % 15 == 8 || (day + month) % 15 == 14) {
//            if ((day + month) % 3 == 2) {
                mPromoPackageLayout.setVisibility(View.VISIBLE);
                typeOfPromotePackage = TYPE_MAX_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW) * 2));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW)));

            }
            initTimer(remainTime);
        }

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                List<String> skuList = new ArrayList<>();
                skuList.add(SKU_VIP_MONTHLY);
                skuList.add(SKU_VIP_THREE_MONTHS);
                skuList.add(SKU_VIP_YEARLY);

                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                billingClient.querySkuDetailsAsync(params.build(),

                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                                try {
                                    for (int i = 0; i < list.size(); i++) {
                                        SkuDetails skuDetail = list.get(i);
                                        skuDetailsList.add(skuDetail);
                                        String textPrice = skuDetail.getPrice();
                                        if (skuDetail.getSku().equals(SKU_VIP_MONTHLY)) {
                                            mTxtPriceWeekly.setText(textPrice);
                                            mTxtWeeklyDescription.setText(String.format(getString(R.string.monthly_description), textPrice));
                                        } else if (skuDetail.getSku().equals(SKU_VIP_THREE_MONTHS)) {
                                            mTxtPriceMonthly.setText(textPrice);
                                            mTxtMonthlyDescription.setText(String.format(getString(R.string.three_monthly_description), textPrice));
                                        } else if (skuDetail.getSku().equals(SKU_VIP_YEARLY)) {
                                            mTxtPrice3Months.setText(textPrice);
                                            mTxtThreeMonthsDescription.setText(String.format(getString(R.string.yearly_description), textPrice));
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        });

                List<String> skuInAppList = new ArrayList<>();
                skuInAppList.add(SKU_MINI_PACKAGE);
                skuInAppList.add(SKU_SMALL_PACKAGE);
                skuInAppList.add(SKU_LARGE_PACKAGE);
                skuInAppList.add(SKU_HUGE_PACKAGE);
                skuInAppList.add(SKU_MAX_PACKAGE);
                //for promotion package
                skuInAppList.add(SKU_MINI_PROMOTE_PACKAGE);
                skuInAppList.add(SKU_LARGE_PROMOTE_PACKAGE);
                skuInAppList.add(SKU_MAX_PROMOTE_PACKAGE);
                SkuDetailsParams.Builder params2 = SkuDetailsParams.newBuilder();
                params2.setSkusList(skuInAppList).setType(BillingClient.SkuType.INAPP);
                billingClient.querySkuDetailsAsync(params2.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                                try {
                                    for (int i = 0; i < list.size(); i++) {
                                        SkuDetails skuDetail = list.get(i);
                                        skuDetailsList.add(skuDetail);
                                        String textPrice = skuDetail.getPrice();
                                        if (skuDetail.getSku().equals(SKU_MINI_PACKAGE)) {
                                            mTxtPriceMiniPackage.setText(textPrice);
                                        } else if (skuDetail.getSku().equals(SKU_SMALL_PACKAGE)) {
                                            mTxtPriceSmallPackage.setText(textPrice);
                                        } else if (skuDetail.getSku().equals(SKU_LARGE_PACKAGE)) {
                                            mTxtPriceLargePackage.setText(textPrice);
                                        } else if (skuDetail.getSku().equals(SKU_HUGE_PACKAGE)) {
                                            mTxtPriceHugePackage.setText(textPrice);
                                        } else if (skuDetail.getSku().equals(SKU_MAX_PACKAGE)) {
                                            mTxtPriceMaxPackage.setText(textPrice);
                                        } else {
                                            if (skuDetail.getSku().equals(SKU_MINI_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_MINI_PROMOTE_PACKAGE) {
                                                mTxtPricePromoPackage.setText(textPrice);
                                            }
                                            if (skuDetail.getSku().equals(SKU_LARGE_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_LARGE_PROMOTE_PACKAGE) {
                                                mTxtPricePromoPackage.setText(textPrice);
                                            }
                                            if (skuDetail.getSku().equals(SKU_MAX_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_MAX_PROMOTE_PACKAGE) {
                                                mTxtPricePromoPackage.setText(textPrice);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                List<Purchase> purchaseList = purchasesResult.getPurchasesList();
                if (purchaseList != null) {
                    for (Purchase purchase : purchaseList) {
                        handlePurchase(purchase);
                    }
                }
                Purchase.PurchasesResult subscriptionPurchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                List<Purchase> subscriptionPurchaseList = subscriptionPurchasesResult.getPurchasesList();
                if (subscriptionPurchaseList != null) {
                    for (Purchase purchase : subscriptionPurchaseList) {
                        handlePurchase(purchase);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        if (isBuyVipAccount) {
            mTxtTitleActionBar.setText(getString(R.string.vip_account));
            mBuyCoinLayout.setVisibility(View.GONE);
            mVipAccountLayout.setVisibility(View.VISIBLE);
        } else {
            mTxtTitleActionBar.setText(getString(R.string.buy_coin));
            mBuyCoinLayout.setVisibility(View.VISIBLE);
            mVipAccountLayout.setVisibility(View.GONE);
        }

        mBuyMiniLayout.setOnClickListener(this);
        mBuySmallLayout.setOnClickListener(this);
        mBuyLargeLayout.setOnClickListener(this);
        mBuyHugeLayout.setOnClickListener(this);
        mBuyMaxLayout.setOnClickListener(this);
        vipMonthly.setOnClickListener(this);
        vipThreeMonths.setOnClickListener(this);
        vipYearly.setOnClickListener(this);

        txtCoinBuyMiniPackage = findViewById(R.id.txtCoinMiniPackage);
        txtCoinBuySmallPackage = findViewById(R.id.txtCoinBuySmallPackage);
        txtCoinBuyLargerPackage = findViewById(R.id.txtCoinBuyLagerPackage);
        txtCoinBuyHugePackage = findViewById(R.id.txtCoinBuyHugePackage);
        txtCoinBuyMaxPackage = findViewById(R.id.txtCoinBuyMaxPackage);

        txtCoinBuyMiniPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW)));
        txtCoinBuySmallPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_SMALL_NEW)));
        txtCoinBuyLargerPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW)));
        txtCoinBuyHugePackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_HUGE_NEW)));
        txtCoinBuyMaxPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW)));
    }

    private void initTimer(long timerCount) {
        mCountDownTimer =
                new CountDownTimer(timerCount * 1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        mTxtCountDown.setText(
                                String.valueOf(((int) (millisUntilFinished / 1000))));
                        long hour = millisUntilFinished / 1000 / 3600;
                        long minute = (millisUntilFinished / 1000 % (60 * 60)) / 60;
                        long second = (millisUntilFinished / 1000 % (60 * 60)) % 60;
                        mTxtCountDown.setText(String.format(getString(R.string.end_in), (int) hour, (int) minute, (int) second));
                    }

                    public void onFinish() {
                        mPromoPackageLayout.setVisibility(View.GONE);
                    }
                };
        mCountDownTimer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private SkuDetails getSkuDetailsFromList(String sku) {
        if (skuDetailsList == null || skuDetailsList.size() == 0) return null;
        for (int i = 0; i < skuDetailsList.size(); i++) {
            SkuDetails skuDetail = skuDetailsList.get(i);
            if (skuDetail.getSku().equals(sku)) return skuDetail;
        }
        return null;

    }

    @Override
    public void onClick(View v) {
        showProgressDialog();
        switch (v.getId()) {
            case R.id.buy_mini:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_MINI_PACKAGE);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_small:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_SMALL_PACKAGE);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_lager:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_LARGE_PACKAGE);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_huge:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_HUGE_PACKAGE);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_max:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_MAX_PACKAGE);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.promotion_package_layout:
                if (typeOfPromotePackage == TYPE_NO_PROMOTE_PACKAGE) break;
                String sku = "";
                if (typeOfPromotePackage == TYPE_MINI_PROMOTE_PACKAGE) {
                    sku = SKU_MINI_PROMOTE_PACKAGE;
                }
                if (typeOfPromotePackage == TYPE_LARGE_PROMOTE_PACKAGE) {
                    sku = SKU_LARGE_PROMOTE_PACKAGE;
                }
                if (typeOfPromotePackage == TYPE_MAX_PROMOTE_PACKAGE) {
                    sku = SKU_MAX_PROMOTE_PACKAGE;
                }
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(sku);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.vip_account_month:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_VIP_MONTHLY);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.vip_account_three_months:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_VIP_THREE_MONTHS);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.vip_account_year:
                try {
                    SkuDetails skuDetails = getSkuDetailsFromList(SKU_VIP_YEARLY);
                    if (skuDetails != null) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
                        if (responseCode != BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mua_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void BuySuccessToFireBase(Purchase purchase, final long buyCoin) {
        final DatabaseReference purchaseRef = FirebaseUtil.getUSubPurchaseRef();
        purchaseRef.child(mFirebaseUser.getUid()).push().setValue(new LogPurchase(mFirebaseUser.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getSku(), purchase.getPurchaseTime(), purchase.getPurchaseToken(), buyCoin));
        if (getApplicationContext() != null) {
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.mua_thanh_cong), buyCoin), Toast.LENGTH_SHORT).show();
        }
    }


    private ProgressDialog progressDialog;

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }
}
