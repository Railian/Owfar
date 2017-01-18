package com.owfar.android.helpers

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.owfar.android.R
import com.owfar.android.models.api.classes.InterestsGroup
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.classes.User
import com.owfar.android.ui.broadcasts.BroadcastInfoFragment
import com.owfar.android.ui.broadcasts.ChannelsFragment
import com.owfar.android.ui.chats.ChatsFragment
import com.owfar.android.ui.choose_contact.ChooseContactFragment
import com.owfar.android.ui.contacts.ContactsFragment
import com.owfar.android.ui.developers.DevelopersFragment
import com.owfar.android.ui.messenger.MessengerFragment
import com.owfar.android.ui.profile.MyProfileFragment
import com.owfar.android.ui.profile.UserProfileFragment
import com.owfar.android.ui.settings.MainSettingsFragment

class MainTransactionHelper(private val compatActivity: AppCompatActivity) {

    @IdRes private val container = R.id.fragment_container

    fun showChats() {
        clearBackStacks()
        clearFragmentManager()
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, ChatsFragment.newInstance)
                    .commit()
            executePendingTransactions()
        }
    }

    fun showContacts() {
        clearBackStacks()
        clearFragmentManager()
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, ContactsFragment.newInstance())
                    .commit()
            executePendingTransactions()
        }
    }

    fun showSettings() {
        clearBackStacks()
        clearSupportFragmentManager()
        with(compatActivity.fragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, MainSettingsFragment.newInstance())
                    .commit()
            executePendingTransactions()
        }
    }

    fun showMyProfile() {
        clearBackStacks()
        clearFragmentManager()
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, MyProfileFragment.newInstance())
                    .commit()
            executePendingTransactions()
        }
    }

    fun showChannels() {
        clearBackStacks()
        clearFragmentManager()
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, ChannelsFragment.newInstance)
                    .commit()
            executePendingTransactions()
        }
    }

    fun showChannelInfo(interestsGroup: InterestsGroup, stream: Stream) {
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, BroadcastInfoFragment.newInstance(interestsGroup, stream))
                    .addToBackStack(null)
                    .commit()
            executePendingTransactions()
        }
    }

    fun showDevelopers() {
        clearBackStacks()
        clearFragmentManager()
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, DevelopersFragment.newInstance())
                    .commit()
            executePendingTransactions()
        }
    }

    fun createNewChat(targetFragment: Fragment, requestCode: Int) {
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(container, ChooseContactFragment.newInstance("Select Contact",
                            "New Conversation", "New Group Chat", null, targetFragment, requestCode))
                    .addToBackStack(null)
                    .commit()
            executePendingTransactions()
        }
    }

    fun showMessenger(stream: Stream) {
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(container, MessengerFragment.newInstance(stream))
                    .addToBackStack(null)
                    .commit()
            executePendingTransactions()
        }
    }

    fun addMembers(chatId: Long, targetFragment: Fragment, requestCode: Int) {
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(container, ChooseContactFragment.newInstance("Select Contact",
                            "Add Member", "Add Members", chatId, targetFragment, requestCode))
                    .addToBackStack(null)
                    .commit()
            executePendingTransactions()
        }
    }

    fun showProfile(opponent: User) {
        with(compatActivity.supportFragmentManager) {
            beginTransaction()
                    .replace(R.id.fragment_container, UserProfileFragment.newInstance(opponent))
                    .addToBackStack(null)
                    .commit()
            executePendingTransactions()
        }
    }

    private fun clearSupportFragmentManager(): Boolean {
        return with(compatActivity.supportFragmentManager) {
            findFragmentById(R.id.fragment_container)?.let {
                beginTransaction().remove(it).commit()
                executePendingTransactions()
                true
            } ?: false
        }
    }

    private fun clearFragmentManager(): Boolean {
        return with(compatActivity.fragmentManager) {
            findFragmentById(R.id.fragment_container)?.let {
                beginTransaction().remove(it).commit()
                executePendingTransactions()
                true
            } ?: false
        }
    }

    private fun clearBackStacks() {
        compatActivity.fragmentManager.apply {
            for (i in 0..backStackEntryCount - 1)
                popBackStack()
        }
        compatActivity.supportFragmentManager.apply {
            for (i in 0..backStackEntryCount - 1)
                popBackStack()
        }
    }
}










