/*
 * Copyright (C) 2018  Clemens Bartz
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.clemensbartz.android.launcher.callbacks;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.view.MenuItem;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.clemensbartz.android.launcher.controllers.WidgetController;
import de.clemensbartz.android.launcher.util.IntentUtil;

/**
 * This task will list all widget providers and show them in a popup menu for an activity.
 * @author Clemens Bartz
 * @since 1.5
 */
public final class ShowWidgetListAsPopupMenuTask extends AsyncTask<Integer, Integer, List<ShowWidgetListAsPopupMenuTask.FilledAppWidgetProviderInfo>> {

    /** The weak reference to our widget controller. */
    @NonNull
    private final WeakReference<WidgetController> widgetControllerWeakReference;
    /** The weak reference to the package manager. */
    @NonNull
    private final WeakReference<Context> contextWeakReference;
    /** The weak reference to the app widget manager. */
    @NonNull
    private final WeakReference<AppWidgetManager> appWidgetManagerWeakReference;

    /**
     * Create a new widget list task. When the listing is done, show the popup menu in
     * the activity.
     * @param widgetController the widget controller
     * @param context the context to run in
     * @param appWidgetManager the app widget manager
     */
    public ShowWidgetListAsPopupMenuTask(@Nullable final WidgetController widgetController, @Nullable final Context context, @Nullable final AppWidgetManager appWidgetManager) {

        this.widgetControllerWeakReference = new WeakReference<>(widgetController);
        this.contextWeakReference = new WeakReference<>(context);
        this.appWidgetManagerWeakReference = new WeakReference<>(appWidgetManager);
    }

    @Override
    @Nullable
    protected List<FilledAppWidgetProviderInfo> doInBackground(@Nullable final Integer... integers) {

        final AppWidgetManager appWidgetManager = appWidgetManagerWeakReference.get();
        final WidgetController widgetController = widgetControllerWeakReference.get();
        final Context context = contextWeakReference.get();

        if (appWidgetManager == null || widgetController == null || context == null) {
            return null;
        }

        final List<AppWidgetProviderInfo> appWidgetProviderInfos = appWidgetManager.getInstalledProviders();
        final List<FilledAppWidgetProviderInfo> infoList = new ArrayList<>(appWidgetProviderInfos.size());

        for (final AppWidgetProviderInfo appWidgetProviderInfo : appWidgetProviderInfos) {

            // Check if configure activity is exported, i. e. callable
            if (appWidgetProviderInfo.configure != null) {
                final Intent intent = IntentUtil.createWidgetConfigureIntent(appWidgetProviderInfo.configure);

                if (!IntentUtil.isCallable(context.getPackageManager(), intent)) {
                    continue;
                }
            }

            // Fill info
            final FilledAppWidgetProviderInfo info = new FilledAppWidgetProviderInfo();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                info.label = appWidgetProviderInfo.loadLabel(context.getPackageManager());
            } else {
                info.label = appWidgetProviderInfo.label;
            }
            info.provider = appWidgetProviderInfo.provider;
            info.configure = appWidgetProviderInfo.configure;

            infoList.add(info);
        }

        Collections.sort(infoList, new Comparator<FilledAppWidgetProviderInfo>() {
            @Override
            public int compare(final FilledAppWidgetProviderInfo o1, final FilledAppWidgetProviderInfo o2) {
                String label1 = "";
                if (o1 != null && o1.label != null) {
                    label1 = o1.label;
                }

                String label2 = "";
                if (o2 != null && o2.label != null) {
                    label2 = o2.label;
                }

                return label1.compareTo(label2);
            }
        });

        return infoList;
    }

    @Override
    protected void onPostExecute(@Nullable final List<FilledAppWidgetProviderInfo> appWidgetProviderInfos) {

        final Context context = contextWeakReference.get();
        final WidgetController widgetController = widgetControllerWeakReference.get();

        if (appWidgetProviderInfos != null && context != null && widgetController != null && appWidgetProviderInfos.size() > 0) {
            final PopupMenu popupMenu = new PopupMenu(context, widgetController.getTopFiller());

            for (final ShowWidgetListAsPopupMenuTask.FilledAppWidgetProviderInfo info : appWidgetProviderInfos) {
                final MenuItem menuItem = popupMenu.getMenu().add(info.label);

                final Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                intent.setComponent(info.configure);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider);

                menuItem.setIntent(intent);

                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        if (item == null || item.getIntent() == null) {
                            return false;
                        }

                        final ComponentName provider = item.getIntent().getParcelableExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER);

                        if (provider != null) {
                            widgetController.bindWidget(provider, item.getIntent().getComponent());
                        }

                        return true;
                    }
                });
            }

            popupMenu.show();
        }
    }

    /**
     * Holds filled AppWidgetProviderInfo.
     */
    static final class FilledAppWidgetProviderInfo {
        /** The label for the provider. */
        @Nullable String label;
        /** The provider component. */
        @Nullable ComponentName provider;
        /** The configure component. */
        @Nullable ComponentName configure;
    }
}
