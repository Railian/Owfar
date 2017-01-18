package com.owfar.android.ui.new_contact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.owfar.android.R;
import com.owfar.android.models.CountryCode;
import com.owfar.android.ui.main.MainBaseFragment;
import com.owfar.android.ui.registration.CountryCodeItemAdapter;
import com.owfar.android.ui.registration.CountryCodeItemHolder;

public class MainCountryCodesFragment extends MainBaseFragment
        implements View.OnClickListener, CountryCodeItemAdapter.OnCountryCodeClickListener {

    //region constants
    public static final String TITLE = "Choose Country Code";

    private static final String ARG_SELECTED_COUNTRY_CODE = "ARG_SELECTED_COUNTRY_CODE";
    public static final String EXTRA_SELECTED_COUNTRY_CODE = "EXTRA_SELECTED_COUNTRY_CODE";
    //endregion

    //region widgets
    private RecyclerView rvCountryCodes;
    //endregion

    //region fields
    private CountryCodeItemAdapter adapter;
    private LinearLayoutManager layoutManager;

    private CountryCode selectedCountryCode;
    //endregion

    //region Creating New Instances
    public static MainCountryCodesFragment newInstance(CountryCode selectedCountryCode) {
        MainCountryCodesFragment fragment = new MainCountryCodesFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SELECTED_COUNTRY_CODE, selectedCountryCode);
        fragment.setArguments(args);
        return fragment;
    }
    //endregion

    //region Fragment Life-Cycle Methods
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        selectedCountryCode = getArguments().getParcelable(ARG_SELECTED_COUNTRY_CODE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE);

        return inflater.inflate(R.layout.fragment_registration_country_codes, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCountryCodes = (RecyclerView) view.findViewById(R.id.fragment_registration_country_codes_rvCountryCodes);

        adapter = new CountryCodeItemAdapter();
        adapter.setSelectedCountryCode(selectedCountryCode);
        rvCountryCodes.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        rvCountryCodes.setLayoutManager(layoutManager);
        layoutManager.scrollToPositionWithOffset(adapter.getCountryCodeAdapterPosition(selectedCountryCode), 0);
        adapter.setOnCountryCodeClickListener(this);
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

    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        getFragmentManager().popBackStack();
    }
    //endregion

    //region OnCountryCodeClickListener Implementation
    @Override
    public void onCountryCodeClick(int adapterPosition, CountryCode countryCode) {
        Intent result = new Intent().putExtra(EXTRA_SELECTED_COUNTRY_CODE, countryCode);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
        getFragmentManager().popBackStack();
    }
    //endregion
}
