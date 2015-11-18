/*
 * Copyright (c) 2015, Abhishek Dabholkar
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package xyz.abhid.newsletterh.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import xyz.abhid.newsletterh.tasks.AddArticlesTask;
import xyz.abhid.newsletterh.tasks.AddIssuesTask;

public class TaskManager {

    private static TaskManager _instance;

    private Context mContext;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private AddArticlesTask mAddArticlesTask;
    private AddIssuesTask mAddIssuesTask;

    private TaskManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized TaskManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new TaskManager(context);
        }
        return _instance;
    }

    public synchronized void performAddArticlesTask(int issue) {
        performAddArticlesTask(issue, false);
    }

    public synchronized void performAddArticlesTask(final int issue, final boolean isSilentMode) {
        if (!isAddArticlesTaskRunning()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAddArticlesTask = (AddArticlesTask) AndroidUtils.executeInOrder(
                            new AddArticlesTask(mContext, issue));
                }
            });
        }
    }

    public boolean isAddArticlesTaskRunning() {
        return !(mAddArticlesTask == null
                || mAddArticlesTask.getStatus() == AsyncTask.Status.FINISHED);
    }

    public synchronized void performAddIssuesTask() {
        performAddIssuesTask(false);
    }

    public synchronized void performAddIssuesTask(final boolean isSilentMode) {
        if (!isAddIssuesTaskRunning()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAddIssuesTask = (AddIssuesTask) AndroidUtils.executeInOrder(
                            new AddIssuesTask(mContext));
                }
            });
        }
    }

    public boolean isAddIssuesTaskRunning() {
        return !(mAddIssuesTask == null || mAddIssuesTask.getStatus() == AsyncTask.Status.FINISHED);
    }
}
