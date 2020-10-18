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

package de.clemensbartz.android.launcher.tasks;

import java.util.concurrent.Executors;

/**
 * The default runner for all tasks. There is no concurrent running planned
 * as database and filesystem are affecting each other.
 * @author Clemens Bartz
 * @since 2.3
 */
public final class DefaultTaskRunner extends TaskRunner {
    public static final DefaultTaskRunner INSTANCE = new DefaultTaskRunner();

    private DefaultTaskRunner() {
        super(Executors.newSingleThreadExecutor());
    }
}
