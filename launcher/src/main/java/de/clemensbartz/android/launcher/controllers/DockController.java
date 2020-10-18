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

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.clemensbartz.android.launcher.daos.SharedPreferencesDAO;
import de.clemensbartz.android.launcher.listeners.DockOnCreateContextMenuListener;
import de.clemensbartz.android.launcher.models.ApplicationModel;
import de.clemensbartz.android.launcher.callbacks.LoadApplicationModelIconIntoImageViewTask;
import de.clemensbartz.android.launcher.util.IntentUtil;

/**
 * Controller for handling the dock with its items.
 * @author Clemens Bartz
 * @since 2.0
 */
public final class DockController {

    /** Density for really high density displays. */
    private static final int DENSITY_XXXHIGH = 640;

    /** The number of items in the dock. */
    public static final int NUMBER_OF_ITEMS = 7;
    /** The prefix for the pinned dock. */
    @NonNull
    public static final String PIN_PREFIX = "pin_";
    /** The separator between package and class name. */
    @NonNull
    public static final String SEPARATOR = "|";
    /** The key for isShowingAllDockIcons. */
    @NonNull
    public static final String KEY_IS_SHOWING_ALL_DOCK_ICONS = "isShowingAllDockIcons";

    /** The list of sorted dock items. */
    @NonNull
    private final ArrayList<ImageView> dockItems;
    /** The weak reference for shared preference dao. */
    @NonNull
    private final WeakReference<SharedPreferencesDAO> sharedPreferencesDAOWeakReference;
    /** The weak reference for the package manager. */
    @NonNull
    private final WeakReference<PackageManager> packageManagerWeakReference;
    /** The default drawable. */
    @NonNull
    private final Drawable defaultDrawable;

    /** The state of showing all icons. */
    private boolean isShowingAllDockIcons = false;

    /**
     * Create a new controller for handling dock items.
     * @param context the context to be created in
     * @param dockItems the number of dock items
     * @param sharedPreferencesDAO the shared preference dao
     * @param packageManager the package manager
     * @param defaultDrawable the default drawable
     */
    public DockController(@Nullable final Context context, @Nullable final PackageManager packageManager, @Nullable final SharedPreferencesDAO sharedPreferencesDAO, @NonNull final Drawable defaultDrawable, @NonNull final ArrayList<ImageView> dockItems) {
        this.dockItems = dockItems;
        this.defaultDrawable = defaultDrawable;
        sharedPreferencesDAOWeakReference = new WeakReference<>(sharedPreferencesDAO);
        packageManagerWeakReference = new WeakReference<>(packageManager);

        // Check for isShowingAllDockIcons
        if (sharedPreferencesDAO != null) {
            this.isShowingAllDockIcons = sharedPreferencesDAO.getBoolean(KEY_IS_SHOWING_ALL_DOCK_ICONS, false);
        }

        // Check for existing context
        if (context == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            final LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

            if (launcherApps != null) {
                for (final ImageView imageView : dockItems) {
                    imageView.setOnCreateContextMenuListener(new DockOnCreateContextMenuListener(launcherApps));
                }
            }
        }

        // Otherwise set up dock items
        for (final ImageView imageView : dockItems) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (view instanceof ImageView && view.getTag() instanceof ApplicationModel) {
                        final ApplicationModel applicationModel = (ApplicationModel) view.getTag();

                        if (applicationModel.packageName == null || applicationModel.className == null) {
                            return;
                        }

                        final Intent intent = IntentUtil.newAppMainIntent(applicationModel.packageName, applicationModel.className);
                        if (IntentUtil.isCallable(context.getPackageManager(), intent)) {
                            context.startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    /**
     *
     * @return whether all dock icons are to be shown
     */
    public boolean isShowingAllDockIcons() {
        return isShowingAllDockIcons;
    }

    /**
     * Set the new value for showing all dock icons.
     * @param showingAllDockIcons the new value
     */
    public void setShowingAllDockIcons(final boolean showingAllDockIcons) {
        isShowingAllDockIcons = showingAllDockIcons;
    }

    /**
     * Update the dock.
     * @param index the index to update
     * @param applicationModel the application model to show or <code>null</code>, if none should be displayed
     */
    public void updateDock(final int index, @Nullable final ApplicationModel applicationModel) {
        if (index < 0 || index >= NUMBER_OF_ITEMS) {
            return;
        }

        // Check for clearing application model
        if (applicationModel == null) {
            clearIndex(index);
            removeFromDatabase(index);

            return;
        }

        if (applicationModel.packageName == null || applicationModel.className == null) {
            clearIndex(index);
            removeFromDatabase(index);

            return;
        }

        final PackageManager packageManager = packageManagerWeakReference.get();

        if (packageManager != null) {
            // Check for deleted packages
            try {
                packageManager.getPackageInfo(applicationModel.packageName, PackageManager.GET_ACTIVITIES);

                // Check if app is callable
                final Intent intent = IntentUtil.newAppMainIntent(applicationModel.packageName, applicationModel.className);

                if (IntentUtil.isCallable(packageManager, intent)) {
                    insertNewItem(index, applicationModel);
                } else {
                    clearIndex(index);
                    removeFromDatabase(index);
                }
            } catch (final PackageManager.NameNotFoundException e) {
                clearIndex(index);
                removeFromDatabase(index);
            }
        }
    }

    /**
     * Insert a new item.
     * @param index the index
     * @param applicationModel the non-null application model to insert
     */
    private void insertNewItem(final int index, @NonNull final ApplicationModel applicationModel) {
        // Update database
        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();

        if (sharedPreferencesDAO != null) {
            final String key = getKey(index);
            final String value = applicationModel.packageName + SEPARATOR + applicationModel.className;

            sharedPreferencesDAO.putString(key, value);
        }

        // Update view
        final ImageView imageView = dockItems.get(index);
        imageView.setTag(applicationModel);
        imageView.setContentDescription(applicationModel.label);

        // Load image
        final PackageManager packageManager = packageManagerWeakReference.get();

        if (packageManager != null) {
            new LoadApplicationModelIconIntoImageViewTask(imageView, applicationModel, packageManager, defaultDrawable).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Remove setting for set index value.
     * @param index the valid index to clear
     */
    public void clearIndex(final int index) {
        // Update view
        final ImageView imageView = dockItems.get(index);
        imageView.setTag(null);
        //Load image
        imageView.setImageDrawable(defaultDrawable);
    }

    /**
     * Remove index from database.
     * @param index the index
     */
    private void removeFromDatabase(final int index) {
        // Update database
        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();

        if (sharedPreferencesDAO != null) {
            final String key = getKey(index);

            sharedPreferencesDAO.remove(key);
        }
    }

    /**
     * Create a database key for an index.
     * @param index the index
     * @return the key for given index
     */
    private String getKey(final int index) {
        return PIN_PREFIX + index;
    }

    /**
     * Update the visibility of dock items.
     * @param configuration the configuration of the app
     */
    public void updateVisibility(@Nullable final Configuration configuration) {
        if (configuration == null) {
            return;
        }

        final boolean isXxxHighDensity = configuration.densityDpi >= DENSITY_XXXHIGH;
        final boolean isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
        final boolean isXLarge = (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE;

        /* Set the last two items to visible when:
         * - device is in xxx high density
         * - device is extra large
         * - device is not in portrait mode
         */
        if (isXxxHighDensity || isXLarge || !isPortrait || isShowingAllDockIcons) {
            dockItems.get(NUMBER_OF_ITEMS - 2).setVisibility(View.VISIBLE);
            dockItems.get(NUMBER_OF_ITEMS - 1).setVisibility(View.VISIBLE);
        } else {
            dockItems.get(NUMBER_OF_ITEMS - 2).setVisibility(View.GONE);
            dockItems.get(NUMBER_OF_ITEMS - 1).setVisibility(View.GONE);
        }
    }

}
