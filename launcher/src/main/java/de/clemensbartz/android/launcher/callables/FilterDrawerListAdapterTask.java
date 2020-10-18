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

package de.clemensbartz.android.launcher.callables;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import de.clemensbartz.android.launcher.adapters.DrawerListAdapter;

/**
 * Task for filtering the drawer list adapter.
 * @author Clemens Bartz
 * @since 2.0
 */
public final class FilterDrawerListAdapterTask extends AsyncTask<Integer, Integer, Integer> {

    /** Weak reference to the list adapter. */
    @NonNull
    private final WeakReference<DrawerListAdapter> drawerListAdapterWeakReference;

    /**
     * Create a new task to filter the drawer list adapter.
     * @param drawerListAdapter the drawer to filter
     */
    public FilterDrawerListAdapterTask(@Nullable final DrawerListAdapter drawerListAdapter) {
        this.drawerListAdapterWeakReference = new WeakReference<>(drawerListAdapter);
    }

    @Override
    @Nullable
    protected Integer doInBackground(@Nullable final Integer... integers) {
        return null;
    }

    @Override
    protected void onPostExecute(@Nullable final Integer integer) {
        final DrawerListAdapter drawerListAdapter = drawerListAdapterWeakReference.get();

        if (drawerListAdapter != null) {
            drawerListAdapter.filter();
        }
    }
}
