package com.owfar.android.ui.developers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.owfar.android.R;
import com.owfar.android.api.users.UsersDelegate;
import com.owfar.android.api.users.UsersManager;
import com.owfar.android.models.api.classes.Stream;
import com.owfar.android.ui.main.MainBaseFragment;

import io.realm.RealmList;

@SuppressWarnings("FieldCanBeLocal")
public class DevelopersFragment extends MainBaseFragment implements View.OnClickListener {

    private static final String TAG = DevelopersFragment.class.getSimpleName();
    private static final String TITLE = "Developer Tab";

    private static final int API_REQUEST_GET_STREAMS = 0;
    private static final int DB_REQUEST_GET_STREAMS = 0;

    private Button btSaveToDatabase;
    private Button btGetFromDatabase;
    private TextView tvOutput;

    public static Fragment newInstance() {
        return new DevelopersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UsersManager.INSTANCE.getDelegatesSet().addDelegate(TAG, usersDelegate);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE)
                .showMaterialMenu()
                .setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER)
                .hideSearch()
                .hideImage()
                .hideTabs();
        getNavigationMenuHelper().unlock();
        getFabHelper().hide();

        return inflater.inflate(R.layout.fragment_developers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btSaveToDatabase = (Button) view.findViewById(R.id.fragment_developers_btSaveToDatabase);
        btGetFromDatabase = (Button) view.findViewById(R.id.fragment_developers_btGetFromDatabase);

        tvOutput = (TextView) view.findViewById(R.id.fragment_developers_tvOutput);

        btSaveToDatabase.setOnClickListener(this);
        btGetFromDatabase.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        UsersManager.INSTANCE.getDelegatesSet().removeDelegate(usersDelegate);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getNavigationMenuHelper().open();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_developers_btSaveToDatabase:
                tvOutput.setText("Save pressed");
                UsersManager.INSTANCE.getStreams(TAG, API_REQUEST_GET_STREAMS);
                break;
            case R.id.fragment_developers_btGetFromDatabase:
                tvOutput.setText("Get pressed");
//                DatabaseService.getStreams(getActivity(), TAG, DB_REQUEST_GET_STREAMS);
                break;
        }
    }

    private UsersDelegate usersDelegate = new UsersDelegate.Simple() {
        @Override
        public void onStreamsReceived(Integer requestCode, RealmList<Stream> streams) {
            tvOutput.setText("Streams from web: " + streams);
        }
    };
}
