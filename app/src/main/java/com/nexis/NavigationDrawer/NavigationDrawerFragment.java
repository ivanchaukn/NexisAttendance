package com.nexis.NavigationDrawer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.nexis.Constants;
import com.nexis.NexisApplication;
import com.nexis.R;

public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallbacks, NavigationFooterCallbacks {
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREFERENCES_FILE = "my_app_settings"; //TODO: change this to your file
    private NavigationDrawerCallbacks mCallbacks;
    private NavigationFooterCallbacks mFooterCallbacks;
    private RecyclerView mDrawerList;
    private ListView mFooterList;
    private View mFragmentContainerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        //Set up for the main recyclerView
        mDrawerList = (RecyclerView) view.findViewById(R.id.drawerList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);

        final List<NavigationItem> navigationItems = getMenu();
        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(navigationItems);
        adapter.setNavigationDrawerCallbacks(this);
        mDrawerList.setAdapter(adapter);
        selectItem(mCurrentSelectedPosition, mCurrentSelectedPosition);

        mFooterList = (ListView) view.findViewById(R.id.footerList);
        final List<NavigationItem> footerItems = getFooter();
        NavigationFooterAdapter footerAdapter = new NavigationFooterAdapter(getActivity(), footerItems);
        footerAdapter.setNavigationFooterCallbacks(this);
        mFooterList.setAdapter(footerAdapter);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
            mFooterCallbacks = (NavigationFooterCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    public void setActionBarDrawerToggle(ActionBarDrawerToggle actionBarDrawerToggle) {
        mActionBarDrawerToggle = actionBarDrawerToggle;
    }

    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    saveSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "true");
                }

                getActivity().invalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState)
            mDrawerLayout.openDrawer(mFragmentContainerView);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mFooterCallbacks = null;
    }

    public List<NavigationItem> getMenu() {
        List<NavigationItem> items = new ArrayList<NavigationItem>();

        boolean devVal = NexisApplication.getDev();
        boolean commiVal = NexisApplication.getCommi();

        items.add(new NavigationItem(Constants.FRAGMENT_NAME.get(0), getResources().getDrawable(R.drawable.ic_attendance)));
        items.add(new NavigationItem(Constants.FRAGMENT_NAME.get(1), getResources().getDrawable(R.drawable.ic_statistic)));
        items.add(new NavigationItem(Constants.FRAGMENT_NAME.get(2), getResources().getDrawable(R.drawable.ic_newcomer)));

        if(devVal||commiVal)
            items.add(new NavigationItem(Constants.FRAGMENT_NAME.get(3), getResources().getDrawable(R.drawable.ic_admin)));
        
        return items;
    }

    public List<NavigationItem> getFooter() {
        List<NavigationItem> items = new ArrayList<NavigationItem>();

        items.add(new NavigationItem("Setting", getResources().getDrawable(R.drawable.ic_setting)));
        items.add(new NavigationItem("Log Out", getResources().getDrawable(R.drawable.ic_exit)));

        return items;
    }

    void selectItem(final int position, final int prevPosition) {

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mCallbacks != null) {

                    mCallbacks.onNavigationDrawerItemSelected(position, prevPosition);
                }
                ((NavigationDrawerAdapter) mDrawerList.getAdapter()).selectPosition(position);
            }
        }, 400);
    }

    void selectFooterItem(final int position) {

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mFooterCallbacks != null) {

                    mFooterCallbacks.onNavigationFooterItemSelected(position);
                }
                ((NavigationFooterAdapter) mFooterList.getAdapter()).selectPosition(position);
            }
        }, 400);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }
    @Override
    public void onNavigationDrawerItemSelected(int position, int currentPosition) {
        selectItem(position, currentPosition);
    }

    @Override
    public void onNavigationFooterItemSelected(int position) {
        selectFooterItem(position);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    public void updateProfile(String userName, String userNexcell)
    {
        TextView userNameText = (TextView) mFragmentContainerView.findViewById(R.id.userName);
        userNameText.setText(userName);

        TextView userInfoText = (TextView) mFragmentContainerView.findViewById(R.id.userInfo);
        userInfoText.setText("Nexcell: " + userNexcell);
    }

}

