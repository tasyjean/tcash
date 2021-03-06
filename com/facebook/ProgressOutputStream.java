package com.facebook;

import android.os.Handler;
import com.facebook.RequestBatch.Callback;
import com.facebook.RequestBatch.OnProgressCallback;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.util.Map;

class ProgressOutputStream extends FilterOutputStream implements RequestOutputStream {
    private long batchProgress;
    private RequestProgress currentRequestProgress;
    private long lastReportedProgress;
    private long maxProgress;
    private final Map<Request, RequestProgress> progressMap;
    private final RequestBatch requests;
    private final long threshold = Settings.getOnProgressThreshold();

    ProgressOutputStream(OutputStream outputStream, RequestBatch requestBatch, Map<Request, RequestProgress> map, long j) {
        super(outputStream);
        this.requests = requestBatch;
        this.progressMap = map;
        this.maxProgress = j;
    }

    private void addProgress(long j) {
        if (this.currentRequestProgress != null) {
            this.currentRequestProgress.addProgress(j);
        }
        this.batchProgress += j;
        if (this.batchProgress >= this.lastReportedProgress + this.threshold || this.batchProgress >= this.maxProgress) {
            reportBatchProgress();
        }
    }

    private void reportBatchProgress() {
        if (this.batchProgress > this.lastReportedProgress) {
            for (Callback callback : this.requests.getCallbacks()) {
                if (callback instanceof OnProgressCallback) {
                    Handler callbackHandler = this.requests.getCallbackHandler();
                    final OnProgressCallback onProgressCallback = (OnProgressCallback) callback;
                    if (callbackHandler == null) {
                        onProgressCallback.onBatchProgress(this.requests, this.batchProgress, this.maxProgress);
                    } else {
                        callbackHandler.post(new Runnable() {
                            public void run() {
                                onProgressCallback.onBatchProgress(ProgressOutputStream.this.requests, ProgressOutputStream.this.batchProgress, ProgressOutputStream.this.maxProgress);
                            }
                        });
                    }
                }
            }
            this.lastReportedProgress = this.batchProgress;
        }
    }

    public void close() {
        super.close();
        for (RequestProgress reportProgress : this.progressMap.values()) {
            reportProgress.reportProgress();
        }
        reportBatchProgress();
    }

    long getBatchProgress() {
        return this.batchProgress;
    }

    long getMaxProgress() {
        return this.maxProgress;
    }

    public void setCurrentRequest(Request request) {
        this.currentRequestProgress = request != null ? (RequestProgress) this.progressMap.get(request) : null;
    }

    public void write(int i) {
        this.out.write(i);
        addProgress(1);
    }

    public void write(byte[] bArr) {
        this.out.write(bArr);
        addProgress((long) bArr.length);
    }

    public void write(byte[] bArr, int i, int i2) {
        this.out.write(bArr, i, i2);
        addProgress((long) i2);
    }
}
