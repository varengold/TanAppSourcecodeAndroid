/*
 * Copyright (c) 2019 EFDIS AG Bankensoftware, Freising <info@efdis.de>.
 *
 * This file is part of the activeTAN app for Android.
 *
 * The activeTAN app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The activeTAN app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the activeTAN app.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.efdis.tangenerator.gui.initialization;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class AbstractBackgroundTask<INPUT, OUTPUT> {
    private static final CountingIdlingResource idlingResource = new CountingIdlingResource(AbstractBackgroundTask.class.getName(), true);

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private BackgroundTaskListener<OUTPUT> listener;

    @StringRes
    protected int failedReason;
    protected boolean failedAndProcessShouldBeRepeated;
    protected Throwable failedCause;

    protected AbstractBackgroundTask(BackgroundTaskListener<OUTPUT> listener) {
        this.listener = listener;
    }

    @VisibleForTesting
    public static IdlingResource getIdlingResource() {
        return idlingResource;
    }

    protected abstract OUTPUT doInBackground(INPUT input);

    public void execute(final INPUT input) {
        idlingResource.increment();

        handler.post(() -> listener.onStart());

        executor.execute(() -> {
            OUTPUT output = null;
            try {
                output = doInBackground(input);
            } catch (Throwable throwable) {
                failedCause = throwable;
            } finally {
                onPostExecute(output);
                idlingResource.decrement();
            }
        });
    }

    private void onPostExecute(final OUTPUT output) {
        handler.post(() -> {
            if (output != null) {
                listener.onSuccess(output);
            } else {
                listener.onFailure(failedReason, failedAndProcessShouldBeRepeated, failedCause);
            }
            listener.onEnd();

            // prevent leak
            listener = null;
        });
    }

    public interface BackgroundTaskListener<OUTPUT> {
        void onStart();
        void onSuccess(OUTPUT output);
        void onFailure(@StringRes int reason, boolean processShouldBeRepeated, Throwable cause);
        void onEnd();
    }
}
