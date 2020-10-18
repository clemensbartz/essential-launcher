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

package de.clemensbartz.android.launcher.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import de.clemensbartz.android.launcher.R;
import de.clemensbartz.android.launcher.comparators.LocaledStringComparator;
import de.clemensbartz.android.launcher.models.ApplicationModel;
import de.clemensbartz.android.launcher.callbacks.LoadApplicationModelIconIntoImageViewTask;
import de.clemensbartz.android.launcher.util.LocaleUtil;

/**
 * Array adapter for the drawer. Takes an @{ApplicationModel}.
 *
 * @author Clemens Bartz
 * @since 1.0
 */
public final class DrawerListAdapter extends ArrayAdapter<ApplicationModel> implements SearchView.OnQueryTextListener, SectionIndexer {

    /** The layouts per abs list view. */
    private static final int[] ITEM_RESOURCE_IDS = {
            R.layout.grid_drawer_item,
            R.layout.list_drawer_item
    };

    /** The separator for search terms. */
    @NonNull
    private static final String FILTER_SEPARATOR = " ";

    /** The list of all application models. */
    @NonNull
    private final List<ApplicationModel> unfilteredList = new ArrayList<>();
    /** The list of filtered application models. */
    @NonNull
    private final List<ApplicationModel> filteredList = new ArrayList<>();
    /** The list of sections. */
    @NonNull
    private final List<String> sections = new ArrayList<>();
    /** The list of indexes. */
    @NonNull
    private final Map<String, Integer> indexMap = new HashMap<>();


    /** The default drawable. */
    @NonNull
    private final Drawable defaultDrawable;
    /** The locale of the context. */
    @NonNull
    private final Locale locale;

    /** The lower-cased lowerCaseFilter string. */
    @NonNull
    private String lowerCaseFilter = "";
    /** Should hidden apps be shown. */
    private boolean showHiddenApps = false;

    /**
     * Initializes a new adapter.
     * @param context the activity
     * @param defaultDrawable the default drawable
     */
    public DrawerListAdapter(
            @NonNull final Context context,
            @NonNull final Drawable defaultDrawable) {

        super(context, R.layout.grid_drawer_item);
        this.defaultDrawable = defaultDrawable;
        this.locale = LocaleUtil.getLocale(context);
    }

    @NonNull
    @Override
    public View getView(final int position,
                        @Nullable final View convertView,
                        @NonNull final ViewGroup parent) {

        ViewHolder viewHolder;
        View v = convertView;

        if (convertView == null) {
            v = LayoutInflater.from(getContext()).inflate(getResource(parent), null);

            viewHolder = new ViewHolder();
            viewHolder.icon = v.findViewById(R.id.icon);
            viewHolder.name = v.findViewById(R.id.name);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final ApplicationModel resolveInfo = getItem(position);

        if (viewHolder != null && viewHolder.icon != null && viewHolder.name != null) {
            viewHolder.icon.setImageDrawable(defaultDrawable);
            viewHolder.icon.setContentDescription(resolveInfo.label);
            viewHolder.name.setText(resolveInfo.label);
            // Load icon asynchronously
            final LoadApplicationModelIconIntoImageViewTask task = new LoadApplicationModelIconIntoImageViewTask(viewHolder.icon, resolveInfo, getContext().getPackageManager(), defaultDrawable);
            try {
                // Try to load via parallel execution
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (final RejectedExecutionException exception) {
                // Otherwise (e. g. queue is full) load via serial execution
                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }

        }

        return v;
    }

    @Override
    @NonNull
    public ApplicationModel getItem(final int position) {
        return filteredList.get(position);
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public int getPosition(@Nullable final ApplicationModel item) {
        if (item != null) {
            return filteredList.indexOf(item);
        } else {
            return -1;
        }
    }

    @Override
    public boolean isEmpty() {
        return unfilteredList.isEmpty();
    }

    @Override
    public void add(@Nullable final ApplicationModel object) {
        if (object != null) {
            unfilteredList.add(object);
        }
    }

    @Override
    public void addAll(@NonNull final Collection<? extends ApplicationModel> collection) {
        unfilteredList.addAll(collection);
    }

    @Override
    public void addAll(@NonNull final ApplicationModel... items) {
        unfilteredList.addAll(Arrays.asList(items));
    }

    @Override
    public void remove(@Nullable final ApplicationModel object) {
        if (object != null) {
            unfilteredList.remove(object);
        }
    }

    @Override
    public void clear() {
        unfilteredList.clear();
    }

    @Override
    public void sort(@NonNull final Comparator<? super ApplicationModel> comparator) {
        Collections.sort(unfilteredList, comparator);
    }

    @Override
    public boolean onQueryTextSubmit(@Nullable final String query) {
        if (query == null) {
            lowerCaseFilter = "";
        } else {
            lowerCaseFilter = query.toLowerCase(locale);
        }

        filter();

        return true;
    }

    @Override
    public boolean onQueryTextChange(@Nullable final String newText) {
        return onQueryTextSubmit(newText);
    }

    @Override
    @NonNull
    public Object[] getSections() {
        final Object[] array = sections.toArray();

        final Object[] emptyArray = {};

        return (array != null) ? array : emptyArray;
    }

    @Override
    public int getPositionForSection(final int sectionIndex) {
        final String firstCharacter = sections.get(sectionIndex);

        final Integer position = indexMap.get(firstCharacter);

        return (position != null) ? position : 0;
    }

    @Override
    public int getSectionForPosition(final int position) {
        final ApplicationModel applicationModel = filteredList.get(position);

        if (applicationModel.label != null && applicationModel.label.trim().length() > 0) {
            final String firstCharacter = applicationModel.label.trim().substring(0, 1).toUpperCase(locale);

            return sections.indexOf(firstCharacter);
        }

        return 0;
    }

    /**
     *
     * @return if the drawer is hiding apps
     */
    public boolean isShowingHiddenApps() {
        return showHiddenApps;
    }

    /**
     * Set if the drawer should be showing hidden apps.
     * @param showHiddenApps new boolean values
     */
    public void setShowHiddenApps(final boolean showHiddenApps) {
        this.showHiddenApps = showHiddenApps;
    }

    /**
     * Update the filtered list.
     */
    public void filter() {
        filteredList.clear();
        indexMap.clear();
        sections.clear();

        // Check for an empty string or a string only consisting of spaces
        if (lowerCaseFilter.isEmpty() || lowerCaseFilter.trim().isEmpty()) {
            for (int i = 0; i < unfilteredList.size(); i++) {
                final ApplicationModel applicationModel = unfilteredList.get(i);

                if (!showHiddenApps && applicationModel.hidden) {
                    continue;
                }

                filteredList.add(applicationModel);

                addSection(applicationModel.label, i);
            }

            notifyDataSetChanged();

            return;
        }

        final String[] lowerCaseWords = lowerCaseFilter.split(FILTER_SEPARATOR);

        for (int i = 0; i < unfilteredList.size(); i++) {
            final ApplicationModel applicationModel = unfilteredList.get(i);

            for (final String lowerCaseWord : lowerCaseWords) {
                if (lowerCaseWord.isEmpty() || lowerCaseWord.trim().isEmpty()) {
                    continue;
                }

                if (!showHiddenApps && applicationModel.hidden) {
                    continue;
                }

                if (applicationModel.label == null || applicationModel.className == null || applicationModel.packageName == null) {
                    continue;
                }

                if (applicationModel.label.toLowerCase(locale).contains(lowerCaseWord)
                        || applicationModel.className.toLowerCase(locale).contains(lowerCaseWord)
                        || applicationModel.packageName.toLowerCase(locale).contains(lowerCaseWord)) {

                    filteredList.add(applicationModel);

                    addSection(applicationModel.label, i);

                    break;
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Update the sections.
     * @param label the label
     * @param index the start index
     */
    private void addSection(final String label, final Integer index) {
        if (label != null && label.trim().length() > 0) {
            final String firstCharacter = label.trim().substring(0, 1).toUpperCase(locale);

            if (!sections.contains(firstCharacter)) {
                sections.add(firstCharacter);

                final LocaledStringComparator comparator = new LocaledStringComparator(locale);

                Collections.sort(sections, comparator);

                indexMap.put(firstCharacter, index);
            }
        }
    }

    /**
     * Return the resource for the view.
     * @param view the view
     * @return the resource id or <code>-1</code>, if none was found
     */
    private int getResource(@NonNull final View view) {
        switch (view.getId()) {
            case R.id.gvApplications:
                return ITEM_RESOURCE_IDS[0];
            case R.id.lvApplications:
                return ITEM_RESOURCE_IDS[1];
            default:
                return -1;
        }
    }

    /**
     * View holder class.
     */
    static final class ViewHolder {
        /** The view for the icon. */
        @Nullable ImageView icon;
        /** The view for the label. */
        @Nullable TextView name;
    }
}
