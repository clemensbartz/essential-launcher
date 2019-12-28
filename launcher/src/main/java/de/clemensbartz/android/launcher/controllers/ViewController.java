/*
 * Copyright (C) 2018  Clemens Bartz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.clemensbartz.android.launcher.controllers;

import android.app.ActionBar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.PopupMenu;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

import de.clemensbartz.android.launcher.Launcher;
import de.clemensbartz.android.launcher.R;
import de.clemensbartz.android.launcher.daos.SharedPreferencesDAO;

/**
 * Controller for flipping between views inside the layout. Central element is the {@link ViewFlipper}.
 * <br/>
 * This class is designed for handling one static "home" and subsequent views are offset by index.
 * Only one index can be selected and is the default when requesting the call for the detail page.
 * <br/>
 * This class is intended only for the lifetime of an activity.
 * @author Clemens Bartz
 * @since 2.0
 */
public final class ViewController extends GestureDetector.SimpleOnGestureListener {

    /** The system service name for the status bar. */
    public static final String SYSTEM_SERVICE_NAME_STATUS_BAR = "statusbar";
    /** The minimum travel velocity for a motion event to occur. */
    private static final int MINIMUM_VELOCITY_Y = 50;
    /** The minimum trave distance for a motion event to occur. */
    private static final int MINIMUM_DISTANCE_Y = 20;
    /** Key for choosing which layout to display. */
    @NonNull
    public static final String KEY_DRAWER_LAYOUT = "drawerLayout";

    /** Id to identify the home layout. */
    private static final int SHOW_STATUS_BAR_ID = 0;
    /** Id to identify the grid layout. */
    private static final int SHOW_DRAWER_ID = 1;
    /** Id to identify the list layout. */
    private static final int SHOW_SEARCH_ID = 2;

    /** Keys and default values for gestures **/
    public enum Gestures {
        SWIPE_UP("gestureSwipeUp", 1, SHOW_DRAWER_ID),
        SWIPE_DOWN("gestureSwipeDown", 2, SHOW_STATUS_BAR_ID);

        private final String key;
        private final Integer id;
        private final Integer defaultValue;

        Gestures(String key, Integer id, Integer defaultValue) {
            this.key = key;
            this.id = id;
            this.defaultValue = defaultValue;
        }

        String getKey() {
            return key;
        }
        Integer getId() {
            return id;
        }
        Integer getDefaultValue() {
            return defaultValue;
        }
    }

    /** Id to identify the home layout. */
    private static final int HOME_ID = 0;
    /** Id to identify the grid layout. */
    public static final int GRID_ID = 1;
    /** Id to identify the list layout. */
    public static final int LIST_ID = 2;

    /** The view switcher. */
    @NonNull
    private final ViewFlipper viewFlipper;
    /** The launcher. */
    @NonNull
    private final Launcher launcher;
    /** The shared preference dao. */
    @NonNull
    private final SharedPreferencesDAO sharedPreferencesDAO;

    /** The current index for the view, only values greater than 0 are supported. */
    private int currentDetailIndex = 0;
    /** The action bar. */
    @Nullable
    private ActionBar actionBar;
    /** The menu of the action bar. */
    @Nullable
    private Menu actionBarMenu;
    /** The status bar system service. */
    @Nullable
    private Object statusBarSystemService = null;


    /**
     * Create a new controller around a view flipper.
     * @param viewFlipper the view flipper
     */
    public ViewController(@NonNull final ViewFlipper viewFlipper, @NonNull final Launcher launcher, @NonNull final SharedPreferencesDAO sharedPreferencesDAO) {
        this.viewFlipper = viewFlipper;
        this.launcher = launcher;
        this.sharedPreferencesDAO = sharedPreferencesDAO;
    }

    /**
     * Check if a supplied index is valid.
     * @param detailIndex the detail index
     * @return if it is valid
     */
    private boolean isValidDetailIndex(final int detailIndex) {
        return detailIndex >= 0 && viewFlipper.getChildCount() > detailIndex - 1;
    }

    /**
     * Set the displayed child to the index and set the visibility of actionbar accordingly.
     * @param index a valid index
     */
    private void switchTo(final int index) {
        if (index == HOME_ID) {
            if (actionBar != null && actionBar.isShowing()) {
                if (actionBarMenu != null)
                    actionBarMenu.findItem(R.id.app_bar_search).collapseActionView();
                actionBar.hide();
            }
        } else {
            if (actionBar != null && !actionBar.isShowing()) {
                actionBar.show();
            }
        }

        viewFlipper.setDisplayedChild(index);
    }

    /**
     * Expands the status bar.
     */
    private void expandStatusBar() {
        if (statusBarSystemService == null) {
            return;
        }

        try {
            final Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            final Method expandNotificationsPanelMethod = statusbarManager.getMethod("expandNotificationsPanel");

            expandNotificationsPanelMethod.invoke(statusBarSystemService);
        } catch (final Exception e) {
            // Do nothing here
        }

    }

    /**
     * Show the home page.
     */
    public void showHome() {
        switchTo(HOME_ID);
    }

    /**
     * Show the currently selected detail page. Will show home if detail is not correct.
     */
    public void showDetail() {
        if (!isValidDetailIndex(currentDetailIndex)) {
            showHome();

            return;
        }

        switchTo(currentDetailIndex);
    }

    /**
     * Show search on the detail page
     */
    private void showSearch() {
        showDetail();
        if (actionBarMenu != null)
            actionBarMenu.findItem(R.id.app_bar_search).expandActionView();
    }

    /**
     * Show assigned gesture target page
     */
    private void showGestureTarget(final Gestures gesture) {
        final int gestureTarget = sharedPreferencesDAO.getInt(gesture.getKey(), gesture.getDefaultValue());

        switch(gestureTarget) {
            case SHOW_STATUS_BAR_ID:
                expandStatusBar();
                break;
            case SHOW_DRAWER_ID:
                showDetail();
                break;
            case SHOW_SEARCH_ID:
                showSearch();
                break;
            default:
                showHome();
                break;
        }
    }

    /**
     *
     * @return the currently selected detail
     */
    public int getCurrentDetailIndex() {
        return currentDetailIndex;
    }

    /**
     * Set the new detail index. Upon setting, the view flipper must already contain that child.
     * @param currentDetailIndex the new detail index
     */
    public void setCurrentDetailIndex(final int currentDetailIndex) {
        if (isValidDetailIndex(currentDetailIndex)) {
            this.currentDetailIndex = currentDetailIndex;
        } else {
            this.currentDetailIndex = HOME_ID;
        }

        if (actionBarMenu != null) {
            actionBarMenu.findItem(R.id.abm_grid_toggle).setChecked(currentDetailIndex == GRID_ID);
        }
    }

    /**
     * Set the new action bar.
     * @param actionBar the new action bar, or <code>null</code>, if none should be regarded
     */
    public void setActionBar(@Nullable final ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    /**
     * Set the new status bar system service.
     * @param statusBarSystemService the status bar system service, or <code>null</code>, if none should be registered
     */
    public void setStatusBarSystemService(@Nullable final Object statusBarSystemService) {
        this.statusBarSystemService = statusBarSystemService;
    }

    /**
     * Set the new action bar menu.
     * @param actionBarMenu the action bar menu
     */
    public void setActionBarMenu(@Nullable final Menu actionBarMenu) {
        this.actionBarMenu = actionBarMenu;
    }

    /**
     * Set the new action bar menu.
     * @param gestureKey the gesture key
     * @param gestureTargetId the gesture target id
     */
    public void setGestureTarget(@NonNull final String gestureKey, final int gestureTargetId) {
        sharedPreferencesDAO.putInt(gestureKey, gestureTargetId);
    }

    /**
     * Request to layout the widget.
     */
    public void requestGestureChange(@NonNull final Gestures gesture) {
        final PopupMenu popupMenu = new PopupMenu(launcher, launcher.findViewById(R.id.topFiller));

        final int currentGestureTarget = sharedPreferencesDAO.getInt(gesture.getKey(), gesture.getDefaultValue());

        addGesturePopupMenuItem(popupMenu, SHOW_STATUS_BAR_ID, currentGestureTarget, R.string.showStatusbar);
        addGesturePopupMenuItem(popupMenu, SHOW_DRAWER_ID, currentGestureTarget, R.string.showDrawer);
        addGesturePopupMenuItem(popupMenu, SHOW_SEARCH_ID, currentGestureTarget, R.string.showSearch);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                setGestureTarget(gesture.getKey(), item.getItemId());
                return true;
            }
        });

        popupMenu.show();
    }

    /**
     * Add a menu item for the layout popup menu.
     * @param popupMenu the popup menu
     * @param gestureTarget the gesture target
     * @param currentGestureTarget the currently active gesture target
     * @param resourceId the resource for the string
     */
    private void addGesturePopupMenuItem(@NonNull final PopupMenu popupMenu, final int gestureTarget, final int currentGestureTarget, final int resourceId) {
        final MenuItem menuItem = popupMenu.getMenu().add(0, gestureTarget, 0, resourceId);
        menuItem.setCheckable(true);
        menuItem.setChecked(currentGestureTarget == gestureTarget);
    }

    @Override
    public void onLongPress(@Nullable MotionEvent e) {
        showDetail();
    }

    @Override
    public boolean onDown(@Nullable MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(@NonNull final MotionEvent e1, @NonNull final MotionEvent e2, final float velocityX, final float velocityY) {
        if (Math.abs(velocityY) < MINIMUM_VELOCITY_Y) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        final float differenceY = e1.getY() - e2.getY();

        if (differenceY > MINIMUM_DISTANCE_Y) {
            // Do swipe up
            showGestureTarget(Gestures.SWIPE_UP);

            return true;
        } else if (differenceY < -MINIMUM_DISTANCE_Y) {
            // Do swipe down
            showGestureTarget(Gestures.SWIPE_DOWN);

            return true;
        } else {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }


}
