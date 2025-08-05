package com.hindu.pooja.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

class BillingManager(
    private val context: Context,
    private val onPremiumGranted: () -> Unit,
    private val onBillingError: (String) -> Unit
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    fun startConnection(onReady: () -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart connection if needed
            }
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onReady()
                } else {
                    onBillingError("Billing setup failed: ${billingResult.debugMessage}")
                }
            }
        })
    }

    fun launchPurchaseFlow(activity: Activity, productId: String, isSubscription: Boolean) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(
                    if (isSubscription) BillingClient.ProductType.SUBS
                    else BillingClient.ProductType.INAPP
                )
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList.first()
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .apply { offerToken?.let { setOfferToken(it) } }
                                .build()
                        )
                    ).build()
                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                onBillingError("Product unavailable or query failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun restorePurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchasesList.isNotEmpty()) {
                onPremiumGranted()
            } else {
                // Check SUBS too
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                ) { result, subsList ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK && subsList.isNotEmpty()) {
                        onPremiumGranted()
                    } else {
                        onBillingError("No purchases found.")
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge purchase if not acknowledged yet
                    if (!purchase.isAcknowledged) {
                        val params = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(params) { ackResult ->
                            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                onPremiumGranted()
                            } else {
                                onBillingError("Acknowledge failed: ${ackResult.debugMessage}")
                            }
                        }
                    } else {
                        onPremiumGranted()
                    }
                }
            }
        } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            onBillingError("Purchase failed: ${billingResult.debugMessage}")
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
