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

package xyz.abhid.newsletterh.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.events.OnItemAddedEvent;

public abstract class BaseAsyncTask extends AsyncTask<Void, Integer, Void> {

    protected static final int ADD_SUCCESS = 1;
    protected static final int ADD_ERROR = 2;
    protected static final int ADD_OFFLINE = 3;
    protected static final int ADD_EMPTY = 4;

    private Context mContext;

    protected BaseAsyncTask(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        OnItemAddedEvent event = null;
        switch (values[0]) {
            case ADD_SUCCESS:
                break;
            case ADD_ERROR:
                event = new OnItemAddedEvent(mContext.getString(R.string.error),
                        Toast.LENGTH_SHORT);
                break;
            case ADD_OFFLINE:
                event = new OnItemAddedEvent(mContext.getString(R.string.offline),
                        Toast.LENGTH_SHORT);
                break;
            case ADD_EMPTY:
                break;
        }
        if (event != null) {
            EventBus.getDefault().post(event);
        }
    }
}
