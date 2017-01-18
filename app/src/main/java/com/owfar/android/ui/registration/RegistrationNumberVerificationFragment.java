package com.owfar.android.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.owfar.android.R;
import com.owfar.android.helpers.KeyboardHelper;
import com.owfar.android.models.CountryCode;
import com.owfar.android.models.errors.Error;
import com.owfar.android.settings.CurrentUserDelegate;
import com.owfar.android.settings.CurrentUserManager;
import com.owfar.android.ui.snackbars.ErrorSnackbar;

public class RegistrationNumberVerificationFragment extends RegistrationBaseFragment
        implements View.OnClickListener {

    //region constants
    public static final String TAG = RegistrationNumberVerificationFragment.class.getSimpleName();
    public static final String TITLE = "Number Verification";

    private static final String ARG_COUNTRY_CODE = "ARG_COUNTRY_CODE";
    private static final String ARG_PHONE_NUMBER = "ARG_PHONE_NUMBER";
    //endregion

    //region widgets
    private TextView tvSendCode;
    private Button btSendCode;
    private TextView tvEditPhoneNumber;

    private Snackbar sbApiError;
    //endregion

    //region arguments
    private CountryCode countryCode;
    private String phoneNumber;
    //endregion

    //region Creating New Instances
    public static RegistrationNumberVerificationFragment newInstance(CountryCode countryCode, String phoneNumber) {
        RegistrationNumberVerificationFragment fragment = new RegistrationNumberVerificationFragment();
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

        return inflater.inflate(R.layout.fragment_registration_number_verification, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSendCode = (TextView) view.findViewById(R.id.fragment_registration_number_verification_tvSendCode);
        btSendCode = (Button) view.findViewById(R.id.fragment_registration_number_verification_btSendCode);
        tvEditPhoneNumber = (TextView) view.findViewById(R.id.fragment_registration_number_verification_tvEditPhoneNumber);

        configureTvSendCode();
        btSendCode.setOnClickListener(this);
        configureTvEditPhoneNumber();
    }

    @Override
    public void onDestroyView() {
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
    private void configureTvSendCode() {
        String fullString;
        String key;

        fullString = getResources().getString(R.string.fragment_registration_number_verification_tvSendCode);
        key = getResources().getString(R.string.fragment_registration_number_verification_tvSendCode_kPhoneNumber);
        tvSendCode.setText(fullString.replace(key, String.format("(%s) %s", countryCode.getDialCode(), phoneNumber)));
    }

    private void configureTvEditPhoneNumber() {
        int textColor;
        String fullString;
        String key;
        int from;
        int to;

        textColor = getResources().getColor(R.color.textColorSecondary);
        fullString = getResources().getString(R.string.fragment_registration_number_verification_tvEditPhoneNumber);
        key = getResources().getString(R.string.fragment_registration_number_verification_tvEditPhoneNumber);
        from = fullString.indexOf(key);
        to = from + key.length();
        if (from != -1) {
            SpannableString spannableString = new SpannableString(fullString);
            spannableString.setSpan(clickableSpan, from, to, 0);
            spannableString.setSpan(new ForegroundColorSpan(textColor), from, to, 0);
            tvEditPhoneNumber.setText(spannableString);
        }
        tvEditPhoneNumber.setMovementMethod(LinkMovementMethod.getInstance());
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
                case R.id.fragment_registration_number_verification_tvEditPhoneNumber:
                    configureTvEditPhoneNumber();
                    getFragmentManager().popBackStack();
                    break;
            }
        }
    };
    //endregion

    //region Private Tools
    private void hideApiError() {
        if (sbApiError == null) return;
        sbApiError.dismiss();
        sbApiError = null;
    }
    //endregion

    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_registration_number_verification_btSendCode:
                String username = countryCode.getDialCode() + phoneNumber;
                CurrentUserManager.INSTANCE.requestForVerificationCode(username);
                break;
        }
    }
    //endregion

    //region localCurrentUserDelegate
    private CurrentUserDelegate localCurrentUserDelegate = new CurrentUserDelegate.Simple() {

        @Override
        public void onRequestForVerificationCodeSent() {
            getRegistrationActivity().showSignUpFragment(countryCode, phoneNumber);
        }

        @Override
        public void onErrorDuringNumberVerification(Error error) {
            sbApiError = ErrorSnackbar.make(getView(), error);
            sbApiError.show();
        }
    };
    //endregion
}