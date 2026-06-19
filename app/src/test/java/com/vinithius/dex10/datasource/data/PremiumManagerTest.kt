package com.vinithius.dex10.datasource.data

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.vinithius.dex10.datasource.data.PremiumManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class PremiumManagerTest {

    private lateinit var context: Context
    private lateinit var premiumManager: PremiumManager

    /** No-op billing so the manager can be built without a real Play connection. */
    private val fakeBilling = object : BillingHandler {
        override fun setup(activity: Activity, listener: BillingListener) {}
        override fun launchPurchaseFlow(activity: Activity) {}
        override fun launchDonationFlow(activity: Activity) {}
        override fun queryExistingPurchases() {}
        override fun destroy() {}
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val prefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        premiumManager = PremiumManager(context, fakeBilling, prefs)
    }

    @Test
    fun `constructor_doesNotCrash_whenEncryptedPrefsUnavailable`() {
        // No injected prefs → goes through buildSecurePrefs(). EncryptedSharedPreferences can't
        // be created without a real Android Keystore, so this exercises the recovery/fallback
        // path that previously threw in the constructor and crashed the app on launch.
        val manager = PremiumManager(context, fakeBilling, null)
        assertNotNull(manager)
        assertFalse(manager.isPremium.value)
    }

    @Test
    fun `setDebugPremiumStatus_true_updatesFlow`() = runTest {
        // Given starting state is false (default)
        assertFalse(premiumManager.isPremium.value)

        // When debug premium is set to true
        premiumManager.setDebugPremiumStatus(true)

        // Then flow emits true
        assertTrue(premiumManager.isPremium.value)
    }

    @Test
    fun `setDebugPremiumStatus_false_updatesFlow`() = runTest {
        // When debug premium is set to false
        premiumManager.setDebugPremiumStatus(false)

        // Then flow emits false
        assertFalse(premiumManager.isPremium.value)
    }

    @Test
    fun `clearDebugPremiumStatus_restoresState`() = runTest {
        // Given debug override is active
        premiumManager.setDebugPremiumStatus(true)
        assertTrue(premiumManager.isPremium.value)

        // When cleared
        premiumManager.clearDebugPremiumStatus()

        // Then it should revert to default (false in this mock context without Play Store)
        assertFalse(premiumManager.isPremium.value)
    }

    @Test
    fun `canPerformPremiumAction_returnsTrue_whenPremium`() = runTest {
        // Given user is premium
        premiumManager.setDebugPremiumStatus(true)

        // When action checked
        val result = premiumManager.canPerformPremiumAction()

        // Then returns true
        assertTrue(result)
        // And upsell is NOT shown
        assertFalse(premiumManager.showUpsell.value)
    }

    @Test
    fun `canPerformPremiumAction_triggersUpsell_whenFreemium`() = runTest {
        // Given user is freemium (default)
        assertFalse(premiumManager.isPremium.value)

        // When action checked
        val result = premiumManager.canPerformPremiumAction()

        // Then returns false
        assertFalse(result)
        // And upsell IS triggered
        assertTrue(premiumManager.showUpsell.value)
    }
}
