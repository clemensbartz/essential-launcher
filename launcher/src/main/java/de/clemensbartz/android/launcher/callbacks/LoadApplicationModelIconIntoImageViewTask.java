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

package de.clemensbartz.android.launcher.callbacks;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import de.clemensbartz.android.launcher.models.ApplicationModel;

/**
 * Task for loading icons of applications models into an image view.
 * @since 2.0
 * @author Clemens Bartz
 */
public final class LoadApplicationModelIconIntoImageViewTask extends AsyncTask<Integer, Integer, Drawable> {

    /** The image view where to load the icons into. */
    @NonNull
    private final WeakReference<ImageView> imageViewWeakReference;
    /** The application model where to get icons from. */
    @NonNull
    private final ApplicationModel applicationModel;
    /** The package manager handling all operations. */
    @NonNull
    private final PackageManager packageManager;
    /** A default drawable to show if no icon could be loaded. */
    @NonNull
    private final Drawable defaultDrawable;

    /**
     * Create a new task to load icons into an image view.
     * @param imageView the image view
     * @param applicationModel the application model
     * @param packageManager the package manager
     * @param defaultDrawable the default drawable if no image could be found
     */
    public LoadApplicationModelIconIntoImageViewTask(
            @Nullable final ImageView imageView,
            @NonNull final ApplicationModel applicationModel,
            @NonNull final PackageManager packageManager,
            @NonNull final Drawable defaultDrawable) {

        this.imageViewWeakReference = new WeakReference<>(imageView);
        this.applicationModel = applicationModel;
        this.packageManager = packageManager;
        this.defaultDrawable = defaultDrawable;
    }

    @Override
    @Nullable
    protected Drawable doInBackground(@Nullable final Integer... integers) {
        if (applicationModel.packageName == null || applicationModel.className == null) {
            return null;
        }

        final ComponentName componentName = new ComponentName(applicationModel.packageName, applicationModel.className);

        try {
            return packageManager.getActivityIcon(componentName);
        } catch (final PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(@Nullable final Drawable drawable) {
        final ImageView imageView = imageViewWeakReference.get();

        if (imageView != null) {
            Drawable imageDrawable = drawable;

            if (drawable == null) {
                imageDrawable = defaultDrawable;
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                imageDrawable = new RippleDrawable(ColorStateList.valueOf(Color.GRAY), imageDrawable, null);
            }

            imageView.setImageDrawable(imageDrawable);
        }
    }
}
