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

package de.clemensbartz.android.launcher.callbacks;

import android.os.AsyncTask;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import de.clemensbartz.android.launcher.Launcher;
import de.clemensbartz.android.launcher.R;
import de.clemensbartz.android.launcher.controllers.ViewController;
import de.clemensbartz.android.launcher.controllers.WidgetController;
import de.clemensbartz.android.launcher.daos.SharedPreferencesDAO;

/**
 * Task for loading @{@link SharedPreferencesDAO}. This will also update all controllers.
 * @since 2.0
 * @author Clemens Bartz
 */
public final class LoadSharedPreferencesDAOTask extends AsyncTask<Integer, Integer, LoadSharedPreferencesDAOTask.LoadModelAsyncTaskResult> {

    /** The weak reference to the shared preferences dao. */
    @NonNull
    private final WeakReference<SharedPreferencesDAO> sharedPreferencesDAOWeakReference;
    /** The weak reference to the view controller. */
    @NonNull
    private final WeakReference<ViewController> viewControllerWeakReference;
    /** The weak reference to the launcher. */
    @NonNull
    private final WeakReference<Launcher> launcherWeakReference;
    /** The weak reference to the widget controller or <code>null</code>, if none exists. */
    @NonNull
    private final WeakReference<WidgetController> widgetControllerWeakReference;

    /**
     * New task for loading shared preferences.
     * @param sharedPreferencesDAO the dao
     * @param viewController the view controller to update
     * @param launcher the reference to the launcher
     * @param widgetController the widget controller or <code>null</code>, if none exists
     */
    public LoadSharedPreferencesDAOTask(
            @Nullable final Launcher launcher,
            @Nullable final SharedPreferencesDAO sharedPreferencesDAO,
            @Nullable final ViewController viewController,
            @Nullable final WidgetController widgetController) {

        viewControllerWeakReference = new WeakReference<>(viewController);
        sharedPreferencesDAOWeakReference = new WeakReference<>(sharedPreferencesDAO);
        launcherWeakReference = new WeakReference<>(launcher);
        widgetControllerWeakReference = new WeakReference<>(widgetController);
    }

    @Override
    @Nullable
    protected LoadModelAsyncTaskResult doInBackground(@Nullable final Integer... integers) {
        // Check if shared preferences can be loaded
        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();
        if (isCancelled() || sharedPreferencesDAO == null) {
            return null;
        }

        sharedPreferencesDAO.loadValues();

        final LoadModelAsyncTaskResult result = new LoadModelAsyncTaskResult();
        result.selectedWidget = sharedPreferencesDAO.getInt(WidgetController.KEY_APPWIDGET_ID, WidgetController.DEFAULT_APPWIDGET_ID);
        result.widgetLayout = sharedPreferencesDAO.getInt(WidgetController.KEY_APPWIDGET_LAYOUT, WidgetController.DEFAULT_APPWIDGET_LAYOUT);
        result.drawerLayout = sharedPreferencesDAO.getInt(ViewController.KEY_DRAWER_LAYOUT, ViewController.GRID_ID);

        return result;
    }

    @Override
    protected void onPostExecute(@Nullable final LoadModelAsyncTaskResult loadModelAsyncTaskResult) {
        if (loadModelAsyncTaskResult == null) {
            return;
        }

        // Update view controller
        final ViewController viewController = viewControllerWeakReference.get();
        if (viewController != null) {
            viewController.setCurrentDetailIndex(loadModelAsyncTaskResult.drawerLayout);
        }

        // Update action bar
        final Launcher launcher = launcherWeakReference.get();
        if (launcher != null) {
            final MenuItem gridMenuItem = launcher.getActionBarMenuItem(R.id.abm_grid_toggle);

            if (gridMenuItem != null) {
                gridMenuItem.setChecked(loadModelAsyncTaskResult.drawerLayout == ViewController.GRID_ID);
            }
        }

        // Update the widget handling, please note that no widgets could be available, e. g. widgetController is null
        final WidgetController widgetController = widgetControllerWeakReference.get();
        if (widgetController != null) {

            // Only load widget if one was previously selected
            if (loadModelAsyncTaskResult.selectedWidget > -1) {
                widgetController.addHostView(loadModelAsyncTaskResult.selectedWidget);
            }

            // Layout widget
            widgetController.adjustWidget(loadModelAsyncTaskResult.widgetLayout);
        }
    }

    /** Holder class for return values. */
    static final class LoadModelAsyncTaskResult {
        /** The selected widget. */
        int selectedWidget;
        /** The layout for the widget. */
        int widgetLayout;
        /** The grid layout. */
        int drawerLayout;
    }
}
