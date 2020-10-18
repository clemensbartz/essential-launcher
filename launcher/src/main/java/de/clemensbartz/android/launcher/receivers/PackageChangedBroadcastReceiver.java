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

package de.clemensbartz.android.launcher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import de.clemensbartz.android.launcher.adapters.DrawerListAdapter;
import de.clemensbartz.android.launcher.controllers.DockController;
import de.clemensbartz.android.launcher.controllers.DrawerController;
import de.clemensbartz.android.launcher.daos.SharedPreferencesDAO;
import de.clemensbartz.android.launcher.callbacks.LoadDockTask;
import de.clemensbartz.android.launcher.callbacks.LoadDrawerListAdapterTask;

/**
 * Receiver for listening for changed packages. This class only holds weak references and
 * needs to be updated on every "start" event before registering this receiver.
 * <br/>
 * This class is intended to live forever.
 * @author Clemens Bartz
 * @since 2.0
 */
public final class PackageChangedBroadcastReceiver extends BroadcastReceiver {

    /** The instance of the receiver. */
    @Nullable
    private static PackageChangedBroadcastReceiver instance;

    /** Weak reference to the dock controller. */
    @NonNull
    private WeakReference<DockController> dockControllerWeakReference;
    /** Weak reference to the drawer controller. */
    @NonNull
    private WeakReference<DrawerController> drawerControllerWeakReference;
    /** Weak reference to the shared preferences dao. */
    @NonNull
    private WeakReference<SharedPreferencesDAO> sharedPreferencesDAOWeakReference;
    /** Weak reference to the drawer list adapter. */
    @NonNull
    private WeakReference<DrawerListAdapter> drawerListAdapterWeakReference;

    /**
     * Create a new changed broad receiver.
     */
    private PackageChangedBroadcastReceiver() {
        this.dockControllerWeakReference = new WeakReference<>(null);
        this.drawerControllerWeakReference = new WeakReference<>(null);
        this.sharedPreferencesDAOWeakReference = new WeakReference<>(null);
        this.drawerListAdapterWeakReference = new WeakReference<>(null);
    }

    /**
     *
     * @return the instance of the receiver
     */
    @NonNull
    public static PackageChangedBroadcastReceiver getInstance() {
        if (instance == null) {
            instance = new PackageChangedBroadcastReceiver();
        }

        return instance;
    }

    @Override
    public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
        // Update dock
        final DockController dockController = dockControllerWeakReference.get();
        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();

        if (dockController != null && sharedPreferencesDAO != null) {
            if (LoadDockTask.getRunningTask() != null) {
                LoadDockTask.getRunningTask().cancel(true);
            }

            final LoadDockTask loadDockTask = new LoadDockTask(sharedPreferencesDAO, dockController);
            LoadDockTask.setRunningTask(loadDockTask);
            loadDockTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

        // Update drawer
        final DrawerController drawerController = drawerControllerWeakReference.get();
        final DrawerListAdapter drawerListAdapter = drawerListAdapterWeakReference.get();

        if (drawerController != null && drawerListAdapter != null && context != null) {
            if (LoadDrawerListAdapterTask.getRunningTask() != null) {
                LoadDrawerListAdapterTask.getRunningTask().cancel(true);
            }

            final LoadDrawerListAdapterTask loadDrawerListAdapterTask = new LoadDrawerListAdapterTask(context, drawerController, drawerListAdapter);
            LoadDrawerListAdapterTask.setRunningTask(loadDrawerListAdapterTask);
            loadDrawerListAdapterTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    /**
     * Set the new dock controller.
     * @param dockController the dock controller or <code>null</code>, to erase it
     */
    public void setDockController(@Nullable final DockController dockController) {
        dockControllerWeakReference = new WeakReference<>(dockController);
    }

    /**
     * Set the new drawer controller.
     * @param drawerController the drawer controller or <code>null</code>, to erase it
     */
    public void setDrawerController(@Nullable final DrawerController drawerController) {
        drawerControllerWeakReference = new WeakReference<>(drawerController);
    }

    /**
     * Set the new shared preference dao.
     * @param sharedPreferencesDAO the shared preference dao or <code>null</code>, to erase it
     */
    public void setSharedPreferencesDAO(@Nullable final SharedPreferencesDAO sharedPreferencesDAO) {
        sharedPreferencesDAOWeakReference = new WeakReference<>(sharedPreferencesDAO);
    }

    /**
     * Set the new drawer list adapter.
     * @param drawerListAdapter the drawer list adapter or <code>null</code>, to erase it
     */
    public void setDrawerListAdapter(@Nullable final DrawerListAdapter drawerListAdapter) {
        drawerListAdapterWeakReference = new WeakReference<>(drawerListAdapter);
    }
}
