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

import androidx.annotation.Nullable;

/**
 * Provides the return call for the result.
 * @author Clemens Bartz
 * @since 2.3
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface CompleteCallback<R> {
    /**
     * Callback when the result is available.
     * @param result an optional result
     */
    void onComplete(@Nullable R result);
}
