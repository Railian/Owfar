package com.owfar.android.ui.boards;

import android.view.View;
import android.view.ViewGroup;

import com.owfar.android.helpers.KeyboardHelper;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

public class BoardsHelper {

    //region fields
    private Set<View> boardViews;
    //endregion

    //region Public Tools
    public boolean addBoardView(View boardView) {
        if (boardView == null) return false;
        if (boardViews == null) boardViews = new HashSet<>();
        return boardViews.add(boardView);
    }

    public boolean removeBoardView(View boardView) {
        if (boardView == null || boardViews == null) return false;
        return boardViews.remove(boardView);
    }

    public boolean showBoard(View boardView) {
        if (boardView == null || boardViews == null) return false;
        if (!boardViews.contains(boardView))
            throw new NoSuchElementException("first must be added");
        if (KeyboardHelper.INSTANCE.getKeyboardVisible()) KeyboardHelper.INSTANCE.hideKeyboard();
        boolean changed = false;
        for (View view : boardViews)
            if (view != boardView) view.setVisibility(View.GONE);
            else if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
                changed = true;
            }
        ViewGroup.LayoutParams params = boardView.getLayoutParams();
        params.height = KeyboardHelper.INSTANCE.getKeyboardHeight();
        boardView.setLayoutParams(params);
        return changed;
    }

    public boolean hideBoard(View boardView) {
        if (boardView == null || boardViews == null) return false;
        if (!boardViews.contains(boardView))
            throw new NoSuchElementException("first must be added");
        for (View view : boardViews)
            if (view == boardView && view.getVisibility() == View.VISIBLE) {
                view.setVisibility(View.GONE);
                return true;
            }
        return false;
    }

    public boolean hideBoards() {
        if (boardViews == null) return false;
        boolean changed = false;
        for (View view : boardViews)
            if (view.getVisibility() == View.VISIBLE) {
                view.setVisibility(View.GONE);
                changed = true;
            }
        return changed;
    }

    public void toggleBoard(View boardView) {
        if (boardView == null) return;
        if (!showBoard(boardView)) hideBoard(boardView);
    }
    //endregion
}
