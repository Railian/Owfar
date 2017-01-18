package com.owfar.android.ui.contacts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

public class ContactsSearchView extends MaterialSearchView
        implements TextView.OnEditorActionListener {

    //region Constructors
    public ContactsSearchView(Context context) {
        super(context);
        init();
    }

    public ContactsSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContactsSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    //endregion

    //region Initialization
    private void init() {
        EditText etSearchTextView = (EditText) findViewById(com.miguelcatalan.materialsearchview.R.id.searchTextView);
        etSearchTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etSearchTextView.setOnEditorActionListener(this);
    }
    //endregion

    //region OnEditorActionListener Implementation
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        hideKeyboard(this);
        return true;
    }
    //endregion
}