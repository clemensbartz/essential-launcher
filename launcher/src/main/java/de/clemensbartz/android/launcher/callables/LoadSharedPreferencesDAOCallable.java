/*
 * Copyright (C) 2020  Clemens Bartz
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

package de.clemensbartz.android.launcher.callables;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import de.clemensbartz.android.launcher.controllers.ViewController;
import de.clemensbartz.android.launcher.controllers.WidgetController;
import de.clemensbartz.android.launcher.daos.SharedPreferencesDAO;
import de.clemensbartz.android.launcher.models.SharedPreferencesLoadedValuesResult;

/**
 * A callable for loading shared preferences.
 * @author Clemens Bartz
 * @since 2.3
 */
public final class LoadSharedPreferencesDAOCallable implements Callable<SharedPreferencesLoadedValuesResult> {

    /** The weak reference to the shared preferences dao. */
    @NonNull
    private final WeakReference<SharedPreferencesDAO> sharedPreferencesDAOWeakReference;

    /**
     * Create a new callable.
     * @param sharedPreferencesDAO the shared preference DAO to load the values from
     */
    public LoadSharedPreferencesDAOCallable(@NonNull final SharedPreferencesDAO sharedPreferencesDAO) {
        this.sharedPreferencesDAOWeakReference = new WeakReference<>(sharedPreferencesDAO);
    }

    @Override
    @NonNull
    public SharedPreferencesLoadedValuesResult call() throws Exception {
        // Check if shared preferences can be loaded
        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();
        if (sharedPreferencesDAO == null) {
            throw new Exception();
        }

        sharedPreferencesDAO.loadValues();

        return new SharedPreferencesLoadedValuesResult(
                sharedPreferencesDAO.getInt(WidgetController.KEY_APPWIDGET_ID, WidgetController.DEFAULT_APPWIDGET_ID),
                sharedPreferencesDAO.getInt(WidgetController.KEY_APPWIDGET_LAYOUT, WidgetController.DEFAULT_APPWIDGET_LAYOUT),
                sharedPreferencesDAO.getInt(ViewController.KEY_DRAWER_LAYOUT, ViewController.GRID_ID)
        );
    }
}
