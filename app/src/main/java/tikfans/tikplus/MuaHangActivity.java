package tikfans.tikplus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.CustomTextView;
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
    Button mManageSubscriptionButton;
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
        String sku = purchase.getProducts().get(0);
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
                            MuaHangActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    hideProgressDialog();
                                }
                            });
                            Log.d("Khang", "consume: " + purchase.getProducts().get(0) + ": " + purchase.getOrderId());

                            if (purchase.getProducts().get(0).equals(SKU_MINI_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_SMALL_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_SMALL_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_LARGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_HUGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_HUGE_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_MAX_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_MINI_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW) * 2);
                            } else if (purchase.getProducts().get(0).equals(SKU_LARGE_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW) * 2);
                            } else if (purchase.getProducts().get(0).equals(SKU_MAX_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW) * 120 / 100);
                            }

                        }
                    }
                };

                billingClient.consumeAsync(consumeParams, listener);
            } else {
                MuaHangActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        hideProgressDialog();
                    }
                });
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                }
                try {
                    String vipAccountTime = "";
                    if (purchase.getProducts().get(0).equals(SKU_VIP_MONTHLY)) {
                        vipAccountTime = getString(R.string.mot_thang);
                    } else if (purchase.getProducts().get(0).equals(SKU_VIP_THREE_MONTHS)) {
                        vipAccountTime = getString(R.string.ba_thang);
                    } else if (purchase.getProducts().get(0).equals(SKU_VIP_YEARLY)) {
                        vipAccountTime = getString(R.string.yearly);
                    }
                    String finalVipAccountTime = vipAccountTime;
                    MuaHangActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.update_vip_account_success), finalVipAccountTime), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                MyChannelApplication.isVipAccount = true;
                if (user != null) {
                    FirebaseUtil.getSubscriptionInfoRef().setValue(new LogPurchase(user.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getProducts().get(0), purchase.getPurchaseTime(), purchase.getPurchaseToken(), 0));
                }
                Log.d("Khang", "updated to VIP account subscription:" + purchase.toString());
                FirebaseUtil.getVipAccountRef().setValue(MyChannelApplication.isVipAccount);
            }
        }
    }

    private BillingClient billingClient;

    private ImageView imgToobarBack;
    private TextView mTxtTitleActionBar;
    private CustomTextView mTxtCoin;
    private List<ProductDetails> productDetailsSaveToList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_credit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        imgToobarBack = findViewById(R.id.toolbar_back);
        mTxtTitleActionBar = findViewById(R.id.text_title);
        mTxtCoin = (CustomTextView) findViewById(R.id.coin);
        productDetailsSaveToList = new ArrayList<>();

        setSupportActionBar(toolbar);

        DatabaseReference coinRef = FirebaseUtil.getCoinCurrentAccountRef();
        coinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        long currentCoin = (long) dataSnapshot.getValue();
                        mTxtCoin.setText(String.valueOf(currentCoin));
                    } catch (ClassCastException e) {
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        billingClient = BillingClient.newBuilder(getApplicationContext())
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build();
        startBillingConnection();

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

        mManageSubscriptionButton = findViewById(R.id.btn_google_play_subscription);
        mManageSubscriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("khang", "Viewing subscriptions on the Google Play Store");
                String url = "https://play.google.com/store/account/subscriptions";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        progressDialog = new ProgressDialog(this);

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
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW) * 120 / 100));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW)));

            }
            initTimer(remainTime);
        }


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

    private void startBillingConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (isBuyVipAccount) {
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build(),
                            new PurchasesResponseListener() {
                                public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> purchases) {
                                    // check billingResult
                                    // process returned purchase list, e.g. display the plans user owns
                                    Log.d("KhangPurchase", "onQueryPurchasesResponse for sub: " + purchases.toString());
                                    for (Purchase purchase : purchases) {
                                        handlePurchase(purchase);
                                    }
                                }
                            }
                    );
                    QueryProductDetailsParams queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                    .setProductList(
                                            ImmutableList.of(
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_VIP_MONTHLY)
                                                            .setProductType(BillingClient.ProductType.SUBS)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_VIP_THREE_MONTHS)
                                                            .setProductType(BillingClient.ProductType.SUBS)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_VIP_YEARLY)
                                                            .setProductType(BillingClient.ProductType.SUBS)
                                                            .build()))
                                    .build();

                    billingClient.queryProductDetailsAsync(
                            queryProductDetailsParams,
                            new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                                                     List<ProductDetails> productDetailsList) {
                                    // check billingResult
                                    // process returned productDetailsList
                                    Log.d("khangPurchase", "queryProductDetails: productDetailsList size:" + productDetailsList.size());
                                    for (ProductDetails productDetails : productDetailsList) {
                                        Log.d("khangPurchase", "queryProductDetails: " + productDetails);
                                        productDetailsSaveToList.add(productDetails);
                                        if (productDetails.getSubscriptionOfferDetails() != null) {
                                            String textPrice = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                            if (productDetails.getProductId().equals(SKU_VIP_MONTHLY)) {
                                                mTxtPriceWeekly.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPriceWeekly.setText(textPrice);
                                                    }
                                                });
                                                mTxtWeeklyDescription.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtWeeklyDescription.setText(String.format(getString(R.string.monthly_description), textPrice));
                                                    }
                                                });
                                            } else if (productDetails.getProductId().equals(SKU_VIP_THREE_MONTHS)) {
                                                mTxtPriceMonthly.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPriceMonthly.setText(textPrice);
                                                    }
                                                });
                                                mTxtMonthlyDescription.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtMonthlyDescription.setText(String.format(getString(R.string.three_monthly_description), textPrice));
                                                    }
                                                });
                                            } else if (productDetails.getProductId().equals(SKU_VIP_YEARLY)) {
                                                mTxtPrice3Months.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPrice3Months.setText(textPrice);
                                                    }
                                                });
                                                mTxtThreeMonthsDescription.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtThreeMonthsDescription.setText(String.format(getString(R.string.yearly_description), textPrice));
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                    );
                } else {
                    QueryProductDetailsParams queryInAppProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                    .setProductList(
                                            ImmutableList.of(
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_MINI_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_MINI_PROMOTE_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_SMALL_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_LARGE_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_LARGE_PROMOTE_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_HUGE_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_MAX_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build(),
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(SKU_MAX_PROMOTE_PACKAGE)
                                                            .setProductType(BillingClient.ProductType.INAPP)
                                                            .build()))
                                    .build();

                    billingClient.queryProductDetailsAsync(
                            queryInAppProductDetailsParams,
                            new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                                                     List<ProductDetails> productDetailsList) {
                                    // check billingResult
                                    // process returned productDetailsList
                                    Log.d("khangPurchase", "queryProductDetails: productDetailsList size:" + productDetailsList.size());
                                    productDetailsSaveToList.addAll(productDetailsList);
                                    for (int i = 0; i < productDetailsList.size(); i++) {
                                        ProductDetails productDetails = productDetailsList.get(i);
                                        Log.d("khangPurchase", "queryProductDetails: " + productDetails);
                                        if (productDetails.getOneTimePurchaseOfferDetails() != null) {
                                            String textPrice = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                                            if (productDetails.getProductId().equals(SKU_MINI_PACKAGE)) {
                                                mTxtPriceMiniPackage.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPriceMiniPackage.setText(textPrice);
                                                    }
                                                });
                                            } else if (productDetails.getProductId().equals(SKU_SMALL_PACKAGE)) {
                                                mTxtPriceSmallPackage.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPriceSmallPackage.setText(textPrice);
                                                    }
                                                });
                                            } else if (productDetails.getProductId().equals(SKU_LARGE_PACKAGE)) {
                                                mTxtPriceLargePackage.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPriceLargePackage.setText(textPrice);
                                                    }
                                                });
                                            } else if (productDetails.getProductId().equals(SKU_HUGE_PACKAGE)) {
                                                mTxtPriceHugePackage.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPriceHugePackage.setText(textPrice);
                                                    }
                                                });
                                            } else if (productDetails.getProductId().equals(SKU_MAX_PACKAGE)) {
                                                mTxtPriceMaxPackage.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPriceMaxPackage.setText(textPrice);
                                                    }
                                                });
                                            } else if ((productDetails.getProductId().equals(SKU_MAX_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_MAX_PROMOTE_PACKAGE)  || (productDetails.getProductId().equals(SKU_MINI_PROMOTE_PACKAGE)  && typeOfPromotePackage == TYPE_MINI_PROMOTE_PACKAGE) || (productDetails.getProductId().equals(SKU_LARGE_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_LARGE_PROMOTE_PACKAGE)) {
                                                mTxtPricePromoPackage.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTxtPricePromoPackage.setText(textPrice);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                    );
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                startBillingConnection();
            }
        });
    }

    @Override
    protected void onResume() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
            }
        },2000);
        if (isBuyVipAccount) {
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    new PurchasesResponseListener() {
                        public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> purchases) {
                            // check billingResult
                            // process returned purchase list, e.g. display the plans user owns
                            Log.d("KhangPurchase", "onQueryPurchasesResponse for sub: " + purchases.toString());
                            for (Purchase purchase : purchases) {
                                handlePurchase(purchase);
                            }
                        }
                    }
            );
        } else {
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    new PurchasesResponseListener() {
                        public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> purchases) {
                            // check billingResult
                            // process returned purchase list, e.g. display the plans user owns
                            Log.d("khangPurchase", "onQueryPurchasesResponse for in app: " + purchases.toString());
                            for (Purchase purchase : purchases) {
                                handlePurchase(purchase);
                            }
                        }
                    }
            );
        }
        super.onResume();
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

    private ProductDetails getProductDetailsFromList(String productId) {
        if (productDetailsSaveToList == null || productDetailsSaveToList.size() == 0)
            return null;
        for (int i = 0; i < productDetailsSaveToList.size(); i++) {
            ProductDetails productDetails = productDetailsSaveToList.get(i);
            if (productDetails.getProductId().equals(productId)) return productDetails;
        }
        return null;
    }


    @Override
    public void onClick(View v) {
        showProgressDialog();
        switch (v.getId()) {
            case R.id.buy_mini:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_MINI_PACKAGE))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_small:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_SMALL_PACKAGE))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_lager:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_LARGE_PACKAGE))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_huge:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_HUGE_PACKAGE))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buy_max:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_MAX_PACKAGE))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
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
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(sku))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.vip_account_month:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_VIP_MONTHLY))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .setOfferToken(getProductDetailsFromList(SKU_VIP_MONTHLY).getSubscriptionOfferDetails().get(0).getOfferToken())
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.vip_account_three_months:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_VIP_THREE_MONTHS))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .setOfferToken(getProductDetailsFromList(SKU_VIP_THREE_MONTHS).getSubscriptionOfferDetails().get(0).getOfferToken())
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.vip_account_year:
                try {
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(getProductDetailsFromList(SKU_VIP_YEARLY))
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .setOfferToken(getProductDetailsFromList(SKU_VIP_YEARLY).getSubscriptionOfferDetails().get(0).getOfferToken())
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    BillingResult billingResult = billingClient.launchBillingFlow(MuaHangActivity.this, billingFlowParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void BuySuccessToFireBase(Purchase purchase, final long buyCoin) {
        Log.d("khangPurchase", "BuySuccessToFireBase: " + purchase);
        final DatabaseReference purchaseRef = FirebaseUtil.getUSubPurchaseRef();
        String purchaseTime = "" + purchase.getPurchaseTime();// de tranh 1 purchase duoc cong coin nhieu lan, thay vi push key len firebase, thi dung purchaseTime lam key de tranh tao lieu logPurchase cho 1 purchase dan den tang coin nhieu lan.
        purchaseRef.child(mFirebaseUser.getUid()).child(purchaseTime).setValue(new LogPurchase(mFirebaseUser.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getProducts().get(0), purchase.getPurchaseTime(), purchase.getPurchaseToken(), buyCoin));
        MuaHangActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                hideProgressDialog();
                Toast.makeText(MuaHangActivity.this, String.format(getString(R.string.mua_thanh_cong), buyCoin), Toast.LENGTH_LONG).show();
            }
        });
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
