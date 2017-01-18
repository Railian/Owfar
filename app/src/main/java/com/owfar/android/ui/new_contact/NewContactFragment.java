package com.owfar.android.ui.new_contact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.TransitionInflater;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.owfar.android.R;
import com.owfar.android.api.users.UsersDelegate;
import com.owfar.android.api.users.UsersManager;
import com.owfar.android.helpers.KeyboardHelper;
import com.owfar.android.models.CountryCode;
import com.owfar.android.models.api.classes.User;
import com.owfar.android.models.errors.Error;
import com.owfar.android.ui.main.MainBaseFragment;
import com.owfar.android.ui.registration.RegistrationCountryCodesFragment;
import com.owfar.android.ui.snackbars.DefaultSnackbar;
import com.owfar.android.ui.snackbars.ErrorSnackbar;
import com.owfar.android.utils.IntentUtils;


public class NewContactFragment extends MainBaseFragment
        implements TextWatcher, View.OnClickListener {

    //region constants
    public static final String TAG = NewContactFragment.class.getSimpleName();
    public static final String TITLE = "Create New Contact";

    private static final String STATE_COUNTRY_CODE = "STATE_COUNTRY_CODE";

    private static final int REQUEST_CHOOSE_COUNTRY_CODE = 1;

    private static final int API_REQUEST_FIND_CONTACT = 1;
    private static final int API_REQUEST_ADD_NEW_CONTACT = 2;
    //endregion

    //region widgets
    private View vCountryCode;
    private TextView tvCountryName;
    private TextView tvDialCode;
    private TextView tvCodeLabel;
    private EditText etPhoneNumber;
    private Button btContinue;

    private Snackbar sbEditTextError;
    //endregion

    //region fields
    private CountryCode countryCode;
    //endregion

    //region Creating New Instances
    public static NewContactFragment newInstance(Fragment targetFragment, int requestCode) {
        NewContactFragment fragment = new NewContactFragment();
        fragment.setTargetFragment(targetFragment, requestCode);
        return fragment;
    }
    //endregion

    //region Fragment Life-Cycle Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UsersManager.INSTANCE.getDelegatesSet().addDelegate(TAG, localUsersDelegate);

        if (savedInstanceState == null) countryCode = calculateDefaultCountryCode();
        else countryCode = savedInstanceState.getParcelable(STATE_COUNTRY_CODE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE)
                .setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
                .hideSearch()
                .hideTabs();
        getNavigationMenuHelper().lockClosed();
        getFabHelper().hide();

        return inflater.inflate(R.layout.fragment_registration_phone_number, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vCountryCode = view.findViewById(R.id.fragment_registration_phone_number_vCountryCode);
        tvCountryName = (TextView) view.findViewById(R.id.fragment_registration_phone_number_tvCountryName);
        tvDialCode = (TextView) view.findViewById(R.id.fragment_registration_phone_number_tvDialCode);
        tvCodeLabel = (TextView) view.findViewById(R.id.fragment_registration_phone_number_tvCodeLabel);
        etPhoneNumber = (EditText) view.findViewById(R.id.fragment_registration_phone_number_etPhoneNumber);
        btContinue = (Button) view.findViewById(R.id.fragment_registration_phone_number_btContinue);

        configureCountryCode();
        etPhoneNumber.setFilters(new InputFilter[]{phoneNumberInputFilter});
        etPhoneNumber.addTextChangedListener(this);
        etPhoneNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return btContinue.performClick();
            }
        });
        btContinue.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_COUNTRY_CODE, countryCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        countryCode = data.getParcelableExtra(RegistrationCountryCodesFragment.EXTRA_SELECTED_COUNTRY_CODE);
        configureCountryCode();
    }

    @Override
    public void onDestroyView() {
        hideEditTextError();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        UsersManager.INSTANCE.getDelegatesSet().removeDelegate(localUsersDelegate);
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region View Configuration
    private void configureCountryCode() {
        vCountryCode.setOnClickListener(this);
        tvCountryName.setText(countryCode.getName());
        tvDialCode.setText(String.format("(%s)", countryCode.getDialCode()));
        tvCodeLabel.setText(countryCode.getCode());
    }
    //endregion

    //region Private Tools
    private void hideEditTextError() {
        if (sbEditTextError == null) return;
        sbEditTextError.dismiss();
        sbEditTextError = null;
    }

    private CountryCode calculateDefaultCountryCode() {
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String code = tm.getSimCountryIso().toUpperCase();

        for (CountryCode countryCode : CountryCode.Companion.getVALUES())
            if (code.equals(countryCode.getCode())) return countryCode;
        return null;
    }

    //endregion

    //region UI Listeners Implementation
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
            case R.id.fragment_registration_phone_number_vCountryCode:
                MainCountryCodesFragment fragment = MainCountryCodesFragment.newInstance(countryCode);
                fragment.setTargetFragment(this, REQUEST_CHOOSE_COUNTRY_CODE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_transform));
                    fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_transform));
                }
                getFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addSharedElement(vCountryCode, "frame")
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.fragment_registration_phone_number_btContinue:
                KeyboardHelper.INSTANCE.hideKeyboard();
                if (TextUtils.isEmpty(etPhoneNumber.getText())) {
                    sbEditTextError = DefaultSnackbar.make(getView(), "Phone number is required");
                    sbEditTextError.show();
                } else {
                    String dialCode = countryCode.getDialCode();
                    String phoneNumber = etPhoneNumber.getText().toString();
                    String phone = dialCode + phoneNumber;
                    UsersManager.INSTANCE.findContact(TAG, API_REQUEST_FIND_CONTACT, phone);
                    // getRegistrationActivity().showNumberVerificationFragment(dialCode, phoneNumber);
                }
                break;
        }
    }
    //endregion

    //region phoneNumberInputFilter
    private InputFilter phoneNumberInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source == null) return "";
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < source.length(); i++)
                if (Character.isDigit(source.charAt(i)))
                    result.append(source.charAt(i));
            return result.toString();
        }
    };
    //endregion

    //region localUsersDelegate
    private UsersDelegate localUsersDelegate = new UsersDelegate.Simple() {

        @Override
        public void onContactFound(Integer requestCode, User user) {
            UsersManager.INSTANCE.addNewContact(TAG, API_REQUEST_ADD_NEW_CONTACT, user.getId());
        }

        @Override
        public void onContactNotFound(Integer requestCode, final String phone) {
            // TODO: 27.04.16 show dialog, that such contact not found and ask to sent sms
            sbEditTextError = DefaultSnackbar.make(getView(), "This contact doesn't have Owfar yet.");
            sbEditTextError.setAction("Invite", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentUtils.Companion.actionSendSMS(getActivity(), phone, "Join Owfar");
                }
            });
            sbEditTextError.show();
        }

        @Override
        public void onNewContactAdded(Integer requestCode) {
            Fragment targetFragment = getTargetFragment();
            if (targetFragment != null)
                targetFragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
            getFragmentManager().popBackStack();
        }

        @Override
        public void onError(Integer requestCode, Error error) {
            super.onError(requestCode, error);
            ErrorSnackbar.make(getView(), error).show();
        }
    };
    //endregion
}