package com.tasomaniac.openwith.settings.rating

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import androidx.preference.PreferenceCategory
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.settings.SettingsFragment
import javax.inject.Inject

class AskForRatingSettings @Inject constructor(
    fragment: SettingsFragment,
    private val condition: AskForRatingCondition,
    private val analytics: Analytics
) : Settings(fragment) {

    private var preferenceCategory: PreferenceCategory? = null

    override fun setup() {
        updateButton()
    }

    override fun release() {
    }

    private fun updateButton() {
        val shouldDisplay = condition.shouldDisplay()

        if (shouldDisplay.not() && isAdded()) {
            remove()
        }

        if (shouldDisplay) {
            if (!isAdded()) {
                addAskForRatingPreference()
                analytics.sendEvent("AskForRating", "Added", "New")
            }
        }
    }


    private fun addAskForRatingPreference() {
        addPreferencesFromResource(R.xml.pref_ask_for_rating)
        preferenceCategory = findPreference(R.string.pref_key_category_ask_for_rating) as PreferenceCategory
        setupRatingBar()
    }

    private fun setupRatingBar() {
        val preference = findPreference(R.string.pref_key_ask_for_rating) as AskForRatingPreference
        preference.onRatingChange = ::handleRatingChange
    }

    private fun handleRatingChange(rating: Float) {
        if (rating >= GOOD_RATING) {
            context.startActivity(STORE_INTENT)
            condition.alreadyShown = true
            remove()
        } else {
            AlertDialog.Builder(context)
                .setTitle(R.string.ask_for_rating_feedback)
                .setMessage(R.string.ask_for_rating_feedback_message)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton("Never") { _, _ ->
                    condition.alreadyShown = true
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    startContactEmailChooser()
                }
                .setOnDismissListener {
                    remove()
                }
                .show()
        }

        analytics.sendEvent("AskForRating", "Rating Clicked", rating.toString())
    }

    private fun startContactEmailChooser() {
        ShareCompat.IntentBuilder.from(activity)
            .addEmailTo("Said Tahsin Dane <tasomaniac+openlinkwith@gmail.com>")
            .setSubject(context.getString(R.string.ask_for_rating_feedback_email_subject))
            .setType("message/rfc822")
            .startChooser()
    }

    private fun remove() {
        removePreference(preferenceCategory!!)
        preferenceCategory = null
    }

    private fun isAdded() = preferenceCategory != null

    companion object {
        private val STORE_INTENT = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=com.tasomaniac.openwith")
        )
        private const val GOOD_RATING = 4
    }
}
