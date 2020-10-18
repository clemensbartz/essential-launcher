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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import de.clemensbartz.android.launcher.adapters.DrawerListAdapter;
import de.clemensbartz.android.launcher.daos.SharedPreferencesDAO;
import de.clemensbartz.android.launcher.models.ApplicationModel;
import de.clemensbartz.android.launcher.callbacks.FilterDrawerListAdapterTask;

/**
 * Controller for handling information in the drawer.
 * @since 2.0
 * @author Clemens Bartz
 */
public final class DrawerController {

    /** Prefix for hiding an app. */
    private static final String HIDE_PREFIX = "hide_";
    /** Separator for hiding apps. */
    private static final String SEPARATOR = "|";

    /** Weak reference to the drawer list adapter. */
    @NonNull
    private final WeakReference<DrawerListAdapter> drawerListAdapterWeakReference;
    /** Weak reference to the shared preference dao. */
    @NonNull
    private final WeakReference<SharedPreferencesDAO> sharedPreferencesDAOWeakReference;

    /**
     * Controller for the drawer.
     * @param drawerListAdapter the drawer list adapter
     * @param sharedPreferencesDAO the shared preference dao
     */
    public DrawerController(@Nullable final DrawerListAdapter drawerListAdapter, @Nullable final SharedPreferencesDAO sharedPreferencesDAO) {
        drawerListAdapterWeakReference = new WeakReference<>(drawerListAdapter);
        sharedPreferencesDAOWeakReference = new WeakReference<>(sharedPreferencesDAO);
    }

    /**
     * Toggle hiding of an app.
     * @param applicationModel the application model
     */
    public void toggleHide(@NonNull final ApplicationModel applicationModel) {
        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();

        if (sharedPreferencesDAO != null && applicationModel.packageName != null && applicationModel.className != null) {
            final String key = getKey(applicationModel.packageName, applicationModel.className);

            applicationModel.hidden = !isHiding(applicationModel);

            if (isHiding(applicationModel)) {
                sharedPreferencesDAO.remove(key);
            } else {
                sharedPreferencesDAO.putString(key, SEPARATOR);
            }

            final DrawerListAdapter drawerListAdapter = drawerListAdapterWeakReference.get();

            if (drawerListAdapter != null) {
                new FilterDrawerListAdapterTask(drawerListAdapter).execute();
            }
        }
    }

    /**
     * Check if an app is hiding.
     * @param applicationModel the application model to check
     * @return if it should be hidden
     */
    public boolean isHiding(@NonNull final ApplicationModel applicationModel) {
        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();

        if (sharedPreferencesDAO == null || applicationModel.packageName == null || applicationModel.className == null) {
            return false;
        }

        final String key = getKey(applicationModel.packageName, applicationModel.className);

        return sharedPreferencesDAO.contains(key);
    }

    /**
     * Create the key for an app.
     * @param packageName the package name
     * @param className the class name
     * @return the key
     */
    @NonNull
    private String getKey(@NonNull final String packageName, @NonNull final String className) {
        return HIDE_PREFIX + packageName + SEPARATOR + className;
    }
}
