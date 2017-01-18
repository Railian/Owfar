package com.owfar.android.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.owfar.android.R;
import com.owfar.android.helpers.KeyboardHelper;
import com.owfar.android.models.CountryCode;
import com.owfar.android.models.errors.Error;
import com.owfar.android.settings.CurrentUserDelegate;
import com.owfar.android.settings.CurrentUserManager;
import com.owfar.android.settings.CurrentUserSettings;
import com.owfar.android.ui.snackbars.DefaultSnackbar;
import com.owfar.android.ui.snackbars.ErrorSnackbar;

public class RegistrationSignUpFragment extends RegistrationBaseFragment
        implements TextView.OnEditorActionListener, TextWatcher, View.OnClickListener {

    //region constants
    public static final String TAG = RegistrationSignUpFragment.class.getSimpleName();
    public static final String TITLE = "Sign Up";

    private static final String ARG_COUNTRY_CODE = "ARG_COUNTRY_CODE";
    private static final String ARG_PHONE_NUMBER = "ARG_PHONE_NUMBER";
    //endregion

    //region widgets
    private EditText etVerificationCode;
    private Button btContinue;
    private TextView tvResendCode;
    private TextView tvChangePhoneNumber;

    private Snackbar sbEditTextError;
    private Snackbar sbApiError;
    //endregion

    //region arguments
    private CountryCode countryCode;
    private String phoneNumber;
    //endregion

    //region Creating New Instances
    public static RegistrationSignUpFragment newInstance(CountryCode countryCode, String phoneNumber) {
        RegistrationSignUpFragment fragment = new RegistrationSignUpFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COUNTRY_CODE, countryCode);
        args.putString(ARG_PHONE_NUMBER, phoneNumber);
        fragment.setArguments(args);
        return fragment;
    }
    //endregion

    //region Fragment Life-Cycle Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CurrentUserManager.INSTANCE.getDelegatesSet().addDelegate(TAG, localCurrentUserDelegate);

        countryCode = getArguments().getParcelable(ARG_COUNTRY_CODE);
        phoneNumber = getArguments().getString(ARG_PHONE_NUMBER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE)
                .showBackArrow();

        return inflater.inflate(R.layout.fragment_registration_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etVerificationCode = (EditText) view.findViewById(R.id.fragment_registration_sign_up_etVerificationCode);
        btContinue = (Button) view.findViewById(R.id.fragment_registration_sign_up_btContinue);
        tvResendCode = (TextView) view.findViewById(R.id.fragment_registration_sign_up_tvResendCode);
        tvChangePhoneNumber = (TextView) view.findViewById(R.id.fragment_registration_sign_up_tvChangePhoneNumber);

        etVerificationCode.setOnEditorActionListener(this);
        etVerificationCode.addTextChangedListener(this);
        btContinue.setOnClickListener(this);
        configureTvResendCode();
        configureTvChangePhoneNumber();
    }

    @Override
    public void onDestroyView() {
        hideEditTextError();
        hideApiError();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        CurrentUserManager.INSTANCE.getDelegatesSet().removeDelegate(localCurrentUserDelegate);
        super.onDestroy();
    }
    //endregion

    //region View Configuration
    private void configureTvResendCode() {
        int textColor;
        String fullString;
        String key;
        int from;
        int to;

        textColor = getResources().getColor(R.color.textColorSecondary);
        fullString = getResources().getString(R.string.fragment_registration_sign_up_tvResendCode);
        key = getResources().getString(R.string.fragment_registration_sign_up_tvResendCode_kAction);
        from = fullString.indexOf(key);
        to = from + key.length();
        if (from != -1) {
            SpannableString spannableString = new SpannableString(fullString);
            spannableString.setSpan(clickableSpan, from, to, 0);
            spannableString.setSpan(new ForegroundColorSpan(textColor), from, to, 0);
            tvResendCode.setText(spannableString);
        }
        tvResendCode.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void configureTvChangePhoneNumber() {
        int textColor;
        String fullString;
        String key;
        int from;
        int to;

        textColor = getResources().getColor(R.color.textColorSecondary);
        fullString = getResources().getString(R.string.fragment_registration_sign_up_tvChangePhoneNumber);
        key = getResources().getString(R.string.fragment_registration_sign_up_tvChangePhoneNumber_kAction);
        from = fullString.indexOf(key);
        to = from + key.length();
        if (from != -1) {
            SpannableString spannableString = new SpannableString(fullString);
            spannableString.setSpan(clickableSpan, from, to, 0);
            spannableString.setSpan(new ForegroundColorSpan(textColor), from, to, 0);
            tvChangePhoneNumber.setText(spannableString);
        }
        tvChangePhoneNumber.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private ClickableSpan clickableSpan = new ClickableSpan() {

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(true);
        }

        @Override
        public void onClick(View widget) {
            KeyboardHelper.INSTANCE.hideKeyboard();
            switch (widget.getId()) {
                case R.id.fragment_registration_sign_up_tvResendCode:
                    configureTvResendCode();
                    getFragmentManager().popBackStack();
                    break;
                case R.id.fragment_registration_sign_up_tvChangePhoneNumber:
                    configureTvChangePhoneNumber();
                    getFragmentManager().popBackStack();
                    getFragmentManager().popBackStack();
                    break;
            }
        }
    };
    //endregion

    //region Private Tools
    private void hideEditTextError() {
        if (sbEditTextError == null) return;
        sbEditTextError.dismiss();
        sbEditTextError = null;
    }

    private void hideApiError() {
        if (sbApiError == null) return;
        sbApiError.dismiss();
        sbApiError = null;
    }
    //endregion

    //region UI Listeners Implementation
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return btContinue.performClick();
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        hideEditTextError();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_registration_sign_up_btContinue:
                hideApiError();
                KeyboardHelper.INSTANCE.hideKeyboard();
                if (TextUtils.isEmpty(etVerificationCode.getText())) {
                    sbEditTextError = DefaultSnackbar.make(getView(), "Code is required");
                    sbEditTextError.show();
                } else {
                    String username = countryCode.getDialCode() + phoneNumber;
                    String code = etVerificationCode.getText().toString();
                    CurrentUserManager.INSTANCE.signUp(username, code);
                }
        }
    }
    //endregion

    //region localCurrentUserDelegate
    private CurrentUserDelegate localCurrentUserDelegate = new CurrentUserDelegate.Simple() {

        @Override
        public void onAuthorized() {
            CurrentUserSettings.INSTANCE.setLastCountryCode(countryCode);
            CurrentUserSettings.INSTANCE.setLastPhoneNumber(phoneNumber);
        }

        @Override
        public void onErrorDuringSignUp(Error error) {
            sbApiError = ErrorSnackbar.make(getView(), error);
            sbApiError.show();
        }
    };
    //endregion
}