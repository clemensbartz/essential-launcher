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

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * A runner for lightweight tasks.
 * @author Clemens Bartz
 * @since 2.3
 */
public abstract class TaskRunner {

    private final Executor executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Create a new runner with an executor.
     * @param executor the executor
     */
    TaskRunner(Executor executor) {
        this.executor = executor;
    }

    /**
     * Execute a call.
     * @param callable the callable to execute
     * @param completeCallback the callback when the task returns
     * @param exceptionCallback the optional callback when an error occurs
     * @param <R> the return type
     */
    public <R> void executeCall(@NonNull final Callable<R> callable, @NonNull final CompleteCallback<R> completeCallback, @Nullable final ExceptionCallback exceptionCallback) {
        executor.execute(() -> {
            try {
                final R result = callable.call();
                mainHandler.post(() -> completeCallback.onComplete(result));
            } catch (final Exception e) {
                if (exceptionCallback != null) {
                    mainHandler.post(() -> exceptionCallback.onException(e));
                }
            }
        });
    }
}
