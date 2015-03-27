package org.worshipsongs.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.worshipsongs.page.component.drawer.NavDrawerItem;
import org.worshipsongs.adapter.NavDrawerListAdapter;
import org.worshipsongs.WorshipSongApplication;
import org.worshipsongs.dao.SongDao;
import org.worshipsongs.domain.Song;
import org.worshipsongs.page.component.fragment.AuthorListFragment;
import org.worshipsongs.page.component.fragment.ServiceListFragment;
import org.worshipsongs.page.component.fragment.SongBookListFragment;
import org.worshipsongs.page.component.fragment.SongsListFragment;
import org.worshipsongs.page.component.fragment.WorshipSongsPreference;
import org.worshipsongs.service.IMobileNetworkService;
import org.worshipsongs.service.MobileNetworkService;
import org.worshipsongs.task.AsyncGitHubRepositoryTask;
import org.worshipsongs.worship.R;

public class MainActivity extends FragmentActivity
{
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private SongDao songDao;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Context context = WorshipSongApplication.getContext();
    // nav drawer title
    private CharSequence drawerTitle;
    private List<Song> songList;
    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private IMobileNetworkService mobileNetworkService = new MobileNetworkService();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        mTitle = drawerTitle = getTitle();
        songDao = new SongDao(this);
        songDao.open();
        songList = songDao.findTitles();
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // Songs
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true, Integer.toString(songList.size())));
        // Authors
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Song books
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        // Services
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        // Settings
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        //Check database updates
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        // About
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(5, -1)));
        // What's hot, We  will add a counter here
//		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));


        // Recycle the typed array
        navMenuIcons.recycle();

        drawerListView.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        drawerListView.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        )
        {
            public void onDrawerClosed(View view)
            {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView)
            {
                getActionBar().setTitle(drawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id)
        {
            // display view for selected nav drawer item
            displayView(position);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // toggle nav drawer on selecting action bar app icon/title
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
//		case R.id.action_settings:
//			return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerListView);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position)
    {
        // update the main content by replacing fragments
        Fragment fragment = null;
        PreferenceFragment preferenceFragment = null;
        switch (position) {
            case 0:
                fragment = new SongsListFragment();
                break;
            case 1:
                fragment = new AuthorListFragment();
                break;
            case 2:
                fragment = new SongBookListFragment();
                break;
            case 3:
                fragment = new ServiceListFragment();
                break;
            case 4:
                preferenceFragment = new WorshipSongsPreference();
                break;
            case 5:
                checkUpdates();
                break;
            case 6:
                fragment = new AboutWebViewActivity();
                break;
            default:
                break;
        }

        if (fragment != null || preferenceFragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            if (preferenceFragment != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, new WorshipSongsPreference()).commit();
            } else {
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, fragment).commit();
            }


            // update selected item and title, then close the drawer
            drawerListView.setItemChecked(position, true);
            drawerListView.setSelection(position);
            setTitle(navMenuTitles[position]);
            drawerLayout.closeDrawer(drawerListView);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    public final boolean isWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                if ((networkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
                    return true;
                }
            }
        }
        Log.i(MainActivity.class.getSimpleName(), "System does not connect with wifi");
        return false;
    }

    private void checkUpdates()
    {
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            if(mobileNetworkService.isWifi(getSystemService(Context.CONNECTIVITY_SERVICE)) ||
                    mobileNetworkService.isMobileData(getSystemService(CONNECTIVITY_SERVICE))) {
                Log.i(MainActivity.class.getSimpleName(), "System does connect with wifi");
                AsyncGitHubRepositoryTask asyncGitHubRepositoryTask = new AsyncGitHubRepositoryTask(this);
                if (asyncGitHubRepositoryTask.execute().get()) {
                    alertDialogBuilder.setTitle("Update is available");
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // continue to download database updates.
                        }
                    });
                    alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
//
                } else {
                    alertDialogBuilder.setTitle("Update is not available");
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // continue with delete
                        }
                    });
                }

            }else{
                alertDialogBuilder.setMessage("Atleast you need mobile data or wifi to check database updates");
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // continue with delete
                    }
                });
            }
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.show();
        } catch (Exception e) {
        }
    }

    @Override
    public void setTitle(CharSequence title)
    {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

}
