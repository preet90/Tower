package org.droidplanner.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.DialogMaterialFragment;
import org.droidplanner.android.fragments.FlightDataFragment;
import org.droidplanner.android.fragments.WidgetsListFragment;
import org.droidplanner.android.fragments.actionbar.ActionBarTelemFragment;
import org.droidplanner.android.utils.Utils;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class FlightActivity extends DrawerNavigationUI implements SlidingUpPanelLayout.PanelSlideListener {

    private static final String EXTRA_IS_ACTION_DRAWER_OPENED = "extra_is_action_drawer_opened";
    private static final boolean DEFAULT_IS_ACTION_DRAWER_OPENED = true;

    private FlightDataFragment flightData;

    @Override
    public void onDrawerClosed() {
        super.onDrawerClosed();

        if (flightData != null)
            flightData.onDrawerClosed();
    }

    @Override
    public void onDrawerOpened() {
        super.onDrawerOpened();

        if (flightData != null)
            flightData.onDrawerOpened();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        final FragmentManager fm = getSupportFragmentManager();

        //Add the flight data fragment
        flightData = (FlightDataFragment) fm.findFragmentById(R.id.flight_data_container);
        if(flightData == null){
            Bundle args = new Bundle();
            args.putBoolean(FlightDataFragment.EXTRA_SHOW_ACTION_DRAWER_TOGGLE, true);

            flightData = new FlightDataFragment();
            flightData.setArguments(args);
            fm.beginTransaction().add(R.id.flight_data_container, flightData).commit();
        }

        // Add the telemetry fragment
        final int actionDrawerId = getActionDrawerId();
        WidgetsListFragment widgetsListFragment = (WidgetsListFragment) fm.findFragmentById(actionDrawerId);
        if (widgetsListFragment == null) {
            widgetsListFragment = new WidgetsListFragment();
            fm.beginTransaction()
                    .add(actionDrawerId, widgetsListFragment)
                    .commit();
        }

        boolean isActionDrawerOpened = DEFAULT_IS_ACTION_DRAWER_OPENED;
        if (savedInstanceState != null) {
            isActionDrawerOpened = savedInstanceState.getBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened);
        }

        if (isActionDrawerOpened)
            openActionDrawer();
    }

    @Override
    protected void onToolbarLayoutChange(int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom){
        if(flightData != null)
            flightData.updateActionbarShadow(bottom);
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarTelem = fm.findFragmentById(toolbarId);
        if (actionBarTelem == null) {
            actionBarTelem = new ActionBarTelemFragment();
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened());
    }

    @Override
    public void onStart(){
        super.onStart();

        tutorialSetup();

        final Context context = getApplicationContext();
        //Show the changelog if this is the first time the app is launched since update/install
        if(Utils.getAppVersionCode(context) > mAppPrefs.getSavedAppVersionCode()) {
            DialogMaterialFragment changelog = new DialogMaterialFragment();
            changelog.show(getSupportFragmentManager(), "Changelog Dialog");

            mAppPrefs.updateSavedAppVersionCode(context);
        }
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_flight_data;
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onPanelSlide(View view, float v) {
        final int bottomMargin = (int) getResources().getDimension(R.dimen.action_drawer_margin_bottom);

        //Update the bottom margin for the action drawer
        final View flightActionBar = ((ViewGroup)view).getChildAt(0);
        final int[] viewLocs = new int[2];
        flightActionBar.getLocationInWindow(viewLocs);
        updateActionDrawerBottomMargin(viewLocs[0] + flightActionBar.getWidth(), Math.max((int) (view.getHeight() * v), bottomMargin));
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch(newState){
            case COLLAPSED:
            case HIDDEN:
                resetActionDrawerBottomMargin();
                break;

            case EXPANDED:
                //Update the bottom margin for the action drawer
                ViewGroup slidingPanel = (ViewGroup) ((ViewGroup)panel).getChildAt(1);
                final View flightActionBar = slidingPanel.getChildAt(0);
                final int[] viewLocs = new int[2];
                flightActionBar.getLocationInWindow(viewLocs);
                updateActionDrawerBottomMargin(viewLocs[0] + flightActionBar.getWidth(), slidingPanel.getHeight());
                break;
        }
    }

    private void updateActionDrawerBottomMargin(int rightEdge, int bottomMargin){
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        final View actionDrawer = ((ViewGroup)actionDrawerParent.getChildAt(1)).getChildAt(0);

        final int[] actionDrawerLocs = new int[2];
        actionDrawer.getLocationInWindow(actionDrawerLocs);

        if(actionDrawerLocs[0] <= rightEdge) {
            updateActionDrawerBottomMargin(bottomMargin);
        }
    }

    private int getActionDrawerBottomMargin(){
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) actionDrawerParent.getLayoutParams();
        return lp.bottomMargin;
    }

    private void updateActionDrawerBottomMargin(int newBottomMargin){
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) actionDrawerParent.getLayoutParams();
        lp.bottomMargin = newBottomMargin;
        actionDrawerParent.requestLayout();
    }

    private void resetActionDrawerBottomMargin(){
        updateActionDrawerBottomMargin((int) getResources().getDimension(R.dimen.action_drawer_margin_bottom));
    }

    private void tutorialSetup() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "FLIGHT_SCREEN");

        sequence.setConfig(config);

        sequence.addSequenceItem(getNavButtonInToolBar((Toolbar) findViewById(getToolbarId())),
                "This is some amazing feature you should know about - Navigation Drawer.", "GOT IT");

        sequence.addSequenceItem(findViewById(R.id.mc_connectBtn),
                "Choose the connection type (UDP, TCP or USB) and press Connect button", "GOT IT");

        sequence.start();
}

    private View getNavButtonInToolBar(Toolbar toolbar) {
        for (int i = 0;i<toolbar.getChildCount();i++) {
        // Find an instance of and ImageButton in the tool ( The class of the Nav Button )
            if(toolbar.getChildAt(i) instanceof ImageButton){
                //this should be the hamburger button
                ImageButton button = (ImageButton) toolbar.getChildAt(i);
                // this if statement is optional ( I am just looking for the DrawerArrowDrawable to ensure that I
                // found the correct ImageButton just in case there are two image buttons in the toolbar for some
                // reason
                //if(button.getDrawable().getClass().getSuperclass().equals(DrawerArrowDrawable.class))
                    return toolbar.getChildAt(i);// if it gets here it means that you most likely found the Nav Button.
            }
        }
        return null; // else it returns null because you haven't implemented the Hamburger menu.
    }
}
