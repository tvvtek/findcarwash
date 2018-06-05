package ru.findcarwash.findcarwash.workscreens.ClientWorkScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ru.findcarwash.findcarwash.AppAbout;
import ru.findcarwash.findcarwash.AppContact;
import ru.findcarwash.findcarwash.AppLaunch;
import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentTwoHelpers.EventSelectItem;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.settings.MySettings;


public class ClientWorkScreen extends AppCompatActivity{

    SharedPreferences preferences;
    MenuItem menuListClearChatItemActionBarButton;
    ViewPager viewPager;
    FragmentTwoChatsList fragmentTwoChatsList;

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_work_screen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_client_work_screen);
        getSupportActionBar().setElevation(0);

        fragmentTwoChatsList = new FragmentTwoChatsList();

        viewPager = findViewById(R.id.pager);
        viewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentOneCatalog(), getResources().getString(R.string.clientWorkScreenCatalog));
        adapter.addFragment(fragmentTwoChatsList, getResources().getString(R.string.clientWorkScreenChats));
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(MySettings.tabClientWorkScreen).select();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                MySettings.tabClientWorkScreen = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuClientScreen) {
        getMenuInflater().inflate(R.menu.client_work_screen_base_menu, menuClientScreen);
        menuListClearChatItemActionBarButton = menuClientScreen.findItem(R.id.clientWorkScreenMenuClearChatList);
        menuListClearChatItemActionBarButton.setVisible(false);
        menuListClearChatItemActionBarButton.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clientWorkScreenMenuClearChatList:
                fragmentTwoChatsList.pressButtonDelete();
                return true;
            case R.id.clientWorkScreenMenuContact:
                Intent contactApp = new Intent(this, AppContact.class);
                startActivity(contactApp);
                return true;
            case R.id.clientWorkScreenMenuAbout:
                Intent aboutApp = new Intent(this, AppAbout.class);
                startActivity(aboutApp);
                return true;
            case R.id.clientWorkScreenMenuExit:
                if (exitApp()) {
                    finish();
                    Intent launchApp = new Intent(this, AppLaunch.class);
                    startActivity(launchApp);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Switch state enable or disable clear button
     * @param event true OR false field
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectItemChatList(EventSelectItem event) {
        if (event.clientActionBarButtonClearState){
            menuListClearChatItemActionBarButton.setVisible(true);
            menuListClearChatItemActionBarButton.setEnabled(true);
        }
        else {
            menuListClearChatItemActionBarButton.setVisible(false);
            menuListClearChatItemActionBarButton.setEnabled(false);
        }
    }

    private boolean exitApp(){
        try {
            preferences = DependenciesFactory.getPreferencesGet();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(MySettings.IS_WASH, false);
            editor.putString(MySettings.CLIENT_LOGIN_PREFERENCE, "");
            editor.putString(MySettings.CLIENT_EMAIL_PREFERENCE, "");
            editor.putString(MySettings.CLIENT_KEY, "");
            editor.putString(MySettings.CLIENT_DEVICE_PREFERENCE, "");
            editor.apply();
            return true;
        }
        catch (Exception writeToPreferenceException){
            writeToPreferenceException.printStackTrace();
            return false;
        }
    }
}