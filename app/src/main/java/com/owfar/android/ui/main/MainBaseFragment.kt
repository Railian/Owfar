package com.owfar.android.ui.main

import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment

import com.owfar.android.helpers.MainTransactionHelper
import com.owfar.android.models.errors.Error
import com.owfar.android.ui.boards.BoardsHelper
import com.owfar.android.ui.snackbars.ErrorSnackbar


open class MainBaseFragment : Fragment() {

    //region widgets
    private var sbError: Snackbar? = null
    //endregion

    //region Tools
    protected val appBarHelper: MainAppBarHelper?
        get() = (activity as? MainActivity)?.appBarHelper

    protected val navigationMenuHelper: MainNavigationMenuHelper?
        get() = (activity as? MainActivity)?.navigationMenuHelper

    protected val boardsHelper: BoardsHelper?
        get() = (activity as? MainActivity)?.boardsHelper

    protected val fabHelper: MainFabHelper?
        get() = (activity as? MainActivity)?.fabHelper

    protected val transactionHelper: MainTransactionHelper?
        get() = (activity as? MainActivity)?.transactionHelper

    fun showError(error: Error) {
        dismissErrorIfNeed()
        sbError = ErrorSnackbar.make(view, error).apply { show() }
    }

    fun dismissErrorIfNeed() {
        sbError?.apply {
            if (isShown) dismiss()
            sbError = null
        }
    }
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onDestroyView() {
        dismissErrorIfNeed()
        super.onDestroyView()
    }
    //endregion
}