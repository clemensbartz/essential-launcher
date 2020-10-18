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

package de.clemensbartz.android.launcher.models;

/**
 * Holder class for return values.
 * @author Clemens Bartz
 * @since 2.3
 */
public final class SharedPreferencesLoadedValuesResult {
    /** The selected widget. */
    private final int selectedWidget;
    /** The layout for the widget. */
    private final int widgetLayout;
    /** The grid layout. */
    private final int drawerLayout;

    /**
     * Create a new result.
     * @param selectedWidget the index of the selected widget
     * @param widgetLayout the index of the selected layout
     * @param drawerLayout the index of the drawer layout
     */
    public SharedPreferencesLoadedValuesResult(int selectedWidget, int widgetLayout, int drawerLayout) {
        this.selectedWidget = selectedWidget;
        this.widgetLayout = widgetLayout;
        this.drawerLayout = drawerLayout;
    }

    /**
     *
     * @return the selected widget
     */
    public int getSelectedWidget() {
        return selectedWidget;
    }

    /**
     *
     * @return the selected widget layout
     */
    public int getWidgetLayout() {
        return widgetLayout;
    }

    /**
     *
     * @return the selected drawer layout
     */
    public int getDrawerLayout() {
        return drawerLayout;
    }
}
