/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.android.dashboard;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.okhttp.HttpUrl;

import org.dhis2.android.dashboard.api.controllers.DhisController;
import org.dhis2.android.dashboard.api.job.Job;
import org.dhis2.android.dashboard.api.job.JobExecutor;
import org.dhis2.android.dashboard.api.job.NetworkJob;
import org.dhis2.android.dashboard.api.models.UserAccount;
import org.dhis2.android.dashboard.api.models.meta.Credentials;
import org.dhis2.android.dashboard.api.network.APIException;

/**
 * @author Araz Abishov <araz.abishov.gsoc@gmail.com>.
 */
public final class DhisService extends Service {
    public static final int LOG_IN = 1;
    public static final int CONFIRM_USER = 2;
    public static final int LOG_OUT = 3;
    public static final int SYNC_DASHBOARDS = 5;
    public static final int SYNC_DASHBOARD_CONTENT = 6;
    public static final int SYNC_INTERPRETATIONS = 7;

    private final IBinder mBinder = new ServiceBinder();
    private DhisController mDhisController;

    @Override
    public void onCreate() {
        super.onCreate();
        mDhisController = DhisController.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        public DhisService getService() {
            return DhisService.this;
        }
    }

    public void logInUser(final HttpUrl serverUrl, final Credentials credentials) {
        JobExecutor.enqueueJob(new NetworkJob<UserAccount>(LOG_IN,
                NetworkJob.ResponseType.USERS) {

            @Override
            public UserAccount execute() throws APIException {
                return mDhisController.logInUser(serverUrl, credentials);
            }
        });
    }

    public void logOutUser() {
        JobExecutor.enqueueJob(new Job<Object>(LOG_OUT) {
            @Override
            public Object inBackground() {
                mDhisController.logOutUser();
                return new Object();
            }
        });
    }

    public void confirmUser(final Credentials credentials) {
        JobExecutor.enqueueJob(new NetworkJob<UserAccount>(CONFIRM_USER,
                NetworkJob.ResponseType.USERS) {

            @Override
            public UserAccount execute() throws APIException {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return mDhisController.confirmUser(credentials);
            }
        });
    }

    public void syncDashboardContent() {
        JobExecutor.enqueueJob(new NetworkJob<Object>(SYNC_DASHBOARD_CONTENT,
                NetworkJob.ResponseType.DASHBOARD_CONTENT) {

            @Override
            public Object execute() throws APIException {
                mDhisController.syncDashboardContent();
                return new Object();
            }
        });
    }

    public void syncDashboards() {
        JobExecutor.enqueueJob(new NetworkJob<Object>(SYNC_DASHBOARDS,
                NetworkJob.ResponseType.DASHBOARDS) {

            @Override
            public Object execute() throws APIException {
                mDhisController.syncDashboards();
                return new Object();
            }
        });
    }

    public void syncInterpretations() {
        JobExecutor.enqueueJob(new NetworkJob<Object>(SYNC_INTERPRETATIONS,
                NetworkJob.ResponseType.INTERPRETATIONS) {
            @Override
            public Object execute() throws APIException {
                mDhisController.syncInterpretations();
                return new Object();
            }
        });
    }

    public boolean isJobRunning(int jobId) {
        return JobExecutor.isJobRunning(jobId);
    }
}