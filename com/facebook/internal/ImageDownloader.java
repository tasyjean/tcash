package com.facebook.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import com.facebook.internal.ImageRequest.Callback;
import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ImageDownloader {
    private static final int CACHE_READ_QUEUE_MAX_CONCURRENT = 2;
    private static final int DOWNLOAD_QUEUE_MAX_CONCURRENT = 8;
    private static WorkQueue cacheReadQueue = new WorkQueue(2);
    private static WorkQueue downloadQueue = new WorkQueue(8);
    private static Handler handler;
    private static final Map<RequestKey, DownloaderContext> pendingRequests = new HashMap();

    private static class CacheReadWorkItem implements Runnable {
        private boolean allowCachedRedirects;
        private Context context;
        private RequestKey key;

        CacheReadWorkItem(Context context, RequestKey requestKey, boolean z) {
            this.context = context;
            this.key = requestKey;
            this.allowCachedRedirects = z;
        }

        public void run() {
            ImageDownloader.readFromCache(this.key, this.context, this.allowCachedRedirects);
        }
    }

    private static class DownloadImageWorkItem implements Runnable {
        private Context context;
        private RequestKey key;

        DownloadImageWorkItem(Context context, RequestKey requestKey) {
            this.context = context;
            this.key = requestKey;
        }

        public void run() {
            ImageDownloader.download(this.key, this.context);
        }
    }

    private static class DownloaderContext {
        boolean isCancelled;
        ImageRequest request;
        WorkItem workItem;

        private DownloaderContext() {
        }
    }

    private static class RequestKey {
        private static final int HASH_MULTIPLIER = 37;
        private static final int HASH_SEED = 29;
        Object tag;
        URI uri;

        RequestKey(URI uri, Object obj) {
            this.uri = uri;
            this.tag = obj;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof RequestKey)) {
                return false;
            }
            RequestKey requestKey = (RequestKey) obj;
            return requestKey.uri == this.uri && requestKey.tag == this.tag;
        }

        public int hashCode() {
            return ((this.uri.hashCode() + 1073) * 37) + this.tag.hashCode();
        }
    }

    public static boolean cancelRequest(ImageRequest imageRequest) {
        boolean z;
        RequestKey requestKey = new RequestKey(imageRequest.getImageUri(), imageRequest.getCallerTag());
        synchronized (pendingRequests) {
            DownloaderContext downloaderContext = (DownloaderContext) pendingRequests.get(requestKey);
            if (downloaderContext == null) {
                z = false;
            } else if (downloaderContext.workItem.cancel()) {
                pendingRequests.remove(requestKey);
                z = true;
            } else {
                downloaderContext.isCancelled = true;
                z = true;
            }
        }
        return z;
    }

    public static void clearCache(Context context) {
        ImageResponseCache.clearCache(context);
        UrlRedirectCache.clearCache(context);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void download(com.facebook.internal.ImageDownloader.RequestKey r11, android.content.Context r12) {
        /*
        r3 = 0;
        r2 = 0;
        r1 = 1;
        r0 = new java.net.URL;	 Catch:{ IOException -> 0x00da, URISyntaxException -> 0x00ca, all -> 0x00c2 }
        r4 = r11.uri;	 Catch:{ IOException -> 0x00da, URISyntaxException -> 0x00ca, all -> 0x00c2 }
        r4 = r4.toString();	 Catch:{ IOException -> 0x00da, URISyntaxException -> 0x00ca, all -> 0x00c2 }
        r0.<init>(r4);	 Catch:{ IOException -> 0x00da, URISyntaxException -> 0x00ca, all -> 0x00c2 }
        r0 = r0.openConnection();	 Catch:{ IOException -> 0x00da, URISyntaxException -> 0x00ca, all -> 0x00c2 }
        r0 = (java.net.HttpURLConnection) r0;	 Catch:{ IOException -> 0x00da, URISyntaxException -> 0x00ca, all -> 0x00c2 }
        r4 = 0;
        r0.setInstanceFollowRedirects(r4);	 Catch:{ IOException -> 0x00df, URISyntaxException -> 0x00ce, all -> 0x00c5 }
        r4 = r0.getResponseCode();	 Catch:{ IOException -> 0x00df, URISyntaxException -> 0x00ce, all -> 0x00c5 }
        switch(r4) {
            case 200: goto L_0x0088;
            case 301: goto L_0x0050;
            case 302: goto L_0x0050;
            default: goto L_0x001f;
        };	 Catch:{ IOException -> 0x00df, URISyntaxException -> 0x00ce, all -> 0x00c5 }
    L_0x001f:
        r5 = r0.getErrorStream();	 Catch:{ IOException -> 0x00df, URISyntaxException -> 0x00ce, all -> 0x00c5 }
        r6 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r6.<init>();	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        if (r5 == 0) goto L_0x00ac;
    L_0x002a:
        r4 = new java.io.InputStreamReader;	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r4.<init>(r5);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r7 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        r7 = new char[r7];	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
    L_0x0033:
        r8 = 0;
        r9 = r7.length;	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r8 = r4.read(r7, r8, r9);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        if (r8 <= 0) goto L_0x0094;
    L_0x003b:
        r9 = 0;
        r6.append(r7, r9, r8);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        goto L_0x0033;
    L_0x0040:
        r4 = move-exception;
        r10 = r4;
        r4 = r0;
        r0 = r10;
    L_0x0044:
        com.facebook.internal.Utility.closeQuietly(r5);
        com.facebook.internal.Utility.disconnectQuietly(r4);
    L_0x004a:
        if (r1 == 0) goto L_0x004f;
    L_0x004c:
        issueResponse(r11, r0, r3, r2);
    L_0x004f:
        return;
    L_0x0050:
        r1 = "location";
        r1 = r0.getHeaderField(r1);	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r4 = com.facebook.internal.Utility.isNullOrEmpty(r1);	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        if (r4 != 0) goto L_0x00ed;
    L_0x005c:
        r4 = new java.net.URI;	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r4.<init>(r1);	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r1 = r11.uri;	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        com.facebook.internal.UrlRedirectCache.cacheUriRedirect(r12, r1, r4);	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r1 = removePendingRequest(r11);	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        if (r1 == 0) goto L_0x007d;
    L_0x006c:
        r5 = r1.isCancelled;	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        if (r5 != 0) goto L_0x007d;
    L_0x0070:
        r1 = r1.request;	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r5 = new com.facebook.internal.ImageDownloader$RequestKey;	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r6 = r11.tag;	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r5.<init>(r4, r6);	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
        r4 = 0;
        enqueueCacheRead(r1, r5, r4);	 Catch:{ IOException -> 0x00e6, URISyntaxException -> 0x00d4, all -> 0x00c5 }
    L_0x007d:
        r1 = r2;
        r4 = r3;
        r5 = r3;
    L_0x0080:
        com.facebook.internal.Utility.closeQuietly(r5);
        com.facebook.internal.Utility.disconnectQuietly(r0);
        r0 = r4;
        goto L_0x004a;
    L_0x0088:
        r5 = com.facebook.internal.ImageResponseCache.interceptAndCacheImageStream(r12, r0);	 Catch:{ IOException -> 0x00df, URISyntaxException -> 0x00ce, all -> 0x00c5 }
        r4 = android.graphics.BitmapFactory.decodeStream(r5);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r10 = r4;
        r4 = r3;
        r3 = r10;
        goto L_0x0080;
    L_0x0094:
        com.facebook.internal.Utility.closeQuietly(r4);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
    L_0x0097:
        r4 = new com.facebook.FacebookException;	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r6 = r6.toString();	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r4.<init>(r6);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        goto L_0x0080;
    L_0x00a1:
        r4 = move-exception;
        r10 = r4;
        r4 = r0;
        r0 = r10;
    L_0x00a5:
        com.facebook.internal.Utility.closeQuietly(r5);
        com.facebook.internal.Utility.disconnectQuietly(r4);
        goto L_0x004a;
    L_0x00ac:
        r4 = com.facebook.android.C0426R.string.com_facebook_image_download_unknown_error;	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r4 = r12.getString(r4);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        r6.append(r4);	 Catch:{ IOException -> 0x0040, URISyntaxException -> 0x00a1, all -> 0x00b6 }
        goto L_0x0097;
    L_0x00b6:
        r1 = move-exception;
        r3 = r5;
        r10 = r1;
        r1 = r0;
        r0 = r10;
    L_0x00bb:
        com.facebook.internal.Utility.closeQuietly(r3);
        com.facebook.internal.Utility.disconnectQuietly(r1);
        throw r0;
    L_0x00c2:
        r0 = move-exception;
        r1 = r3;
        goto L_0x00bb;
    L_0x00c5:
        r1 = move-exception;
        r10 = r1;
        r1 = r0;
        r0 = r10;
        goto L_0x00bb;
    L_0x00ca:
        r0 = move-exception;
        r5 = r3;
        r4 = r3;
        goto L_0x00a5;
    L_0x00ce:
        r4 = move-exception;
        r5 = r3;
        r10 = r4;
        r4 = r0;
        r0 = r10;
        goto L_0x00a5;
    L_0x00d4:
        r1 = move-exception;
        r5 = r3;
        r4 = r0;
        r0 = r1;
        r1 = r2;
        goto L_0x00a5;
    L_0x00da:
        r0 = move-exception;
        r5 = r3;
        r4 = r3;
        goto L_0x0044;
    L_0x00df:
        r4 = move-exception;
        r5 = r3;
        r10 = r4;
        r4 = r0;
        r0 = r10;
        goto L_0x0044;
    L_0x00e6:
        r1 = move-exception;
        r5 = r3;
        r4 = r0;
        r0 = r1;
        r1 = r2;
        goto L_0x0044;
    L_0x00ed:
        r1 = r2;
        r4 = r3;
        r5 = r3;
        goto L_0x0080;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.internal.ImageDownloader.download(com.facebook.internal.ImageDownloader$RequestKey, android.content.Context):void");
    }

    public static void downloadAsync(ImageRequest imageRequest) {
        if (imageRequest != null) {
            RequestKey requestKey = new RequestKey(imageRequest.getImageUri(), imageRequest.getCallerTag());
            synchronized (pendingRequests) {
                DownloaderContext downloaderContext = (DownloaderContext) pendingRequests.get(requestKey);
                if (downloaderContext != null) {
                    downloaderContext.request = imageRequest;
                    downloaderContext.isCancelled = false;
                    downloaderContext.workItem.moveToFront();
                } else {
                    enqueueCacheRead(imageRequest, requestKey, imageRequest.isCachedRedirectAllowed());
                }
            }
        }
    }

    private static void enqueueCacheRead(ImageRequest imageRequest, RequestKey requestKey, boolean z) {
        enqueueRequest(imageRequest, requestKey, cacheReadQueue, new CacheReadWorkItem(imageRequest.getContext(), requestKey, z));
    }

    private static void enqueueDownload(ImageRequest imageRequest, RequestKey requestKey) {
        enqueueRequest(imageRequest, requestKey, downloadQueue, new DownloadImageWorkItem(imageRequest.getContext(), requestKey));
    }

    private static void enqueueRequest(ImageRequest imageRequest, RequestKey requestKey, WorkQueue workQueue, Runnable runnable) {
        synchronized (pendingRequests) {
            DownloaderContext downloaderContext = new DownloaderContext();
            downloaderContext.request = imageRequest;
            pendingRequests.put(requestKey, downloaderContext);
            downloaderContext.workItem = workQueue.addActiveWorkItem(runnable);
        }
    }

    private static synchronized Handler getHandler() {
        Handler handler;
        synchronized (ImageDownloader.class) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler = handler;
        }
        return handler;
    }

    private static void issueResponse(RequestKey requestKey, Exception exception, Bitmap bitmap, boolean z) {
        DownloaderContext removePendingRequest = removePendingRequest(requestKey);
        if (removePendingRequest != null && !removePendingRequest.isCancelled) {
            final ImageRequest imageRequest = removePendingRequest.request;
            final Callback callback = imageRequest.getCallback();
            if (callback != null) {
                final Exception exception2 = exception;
                final boolean z2 = z;
                final Bitmap bitmap2 = bitmap;
                getHandler().post(new Runnable() {
                    public void run() {
                        callback.onCompleted(new ImageResponse(imageRequest, exception2, z2, bitmap2));
                    }
                });
            }
        }
    }

    public static void prioritizeRequest(ImageRequest imageRequest) {
        RequestKey requestKey = new RequestKey(imageRequest.getImageUri(), imageRequest.getCallerTag());
        synchronized (pendingRequests) {
            DownloaderContext downloaderContext = (DownloaderContext) pendingRequests.get(requestKey);
            if (downloaderContext != null) {
                downloaderContext.workItem.moveToFront();
            }
        }
    }

    private static void readFromCache(RequestKey requestKey, Context context, boolean z) {
        Closeable closeable;
        boolean z2;
        DownloaderContext removePendingRequest;
        boolean z3 = false;
        if (z) {
            URI redirectedUri = UrlRedirectCache.getRedirectedUri(context, requestKey.uri);
            if (redirectedUri != null) {
                InputStream cachedImageStream = ImageResponseCache.getCachedImageStream(redirectedUri, context);
                if (cachedImageStream != null) {
                    z3 = true;
                }
                boolean z4 = z3;
                closeable = cachedImageStream;
                z2 = z4;
                if (!z2) {
                    closeable = ImageResponseCache.getCachedImageStream(requestKey.uri, context);
                }
                if (closeable == null) {
                    Bitmap decodeStream = BitmapFactory.decodeStream(closeable);
                    Utility.closeQuietly(closeable);
                    issueResponse(requestKey, null, decodeStream, z2);
                }
                removePendingRequest = removePendingRequest(requestKey);
                if (removePendingRequest != null && !removePendingRequest.isCancelled) {
                    enqueueDownload(removePendingRequest.request, requestKey);
                    return;
                }
                return;
            }
        }
        z2 = false;
        closeable = null;
        if (z2) {
            closeable = ImageResponseCache.getCachedImageStream(requestKey.uri, context);
        }
        if (closeable == null) {
            removePendingRequest = removePendingRequest(requestKey);
            if (removePendingRequest != null) {
                return;
            }
            return;
        }
        Bitmap decodeStream2 = BitmapFactory.decodeStream(closeable);
        Utility.closeQuietly(closeable);
        issueResponse(requestKey, null, decodeStream2, z2);
    }

    private static DownloaderContext removePendingRequest(RequestKey requestKey) {
        DownloaderContext downloaderContext;
        synchronized (pendingRequests) {
            downloaderContext = (DownloaderContext) pendingRequests.remove(requestKey);
        }
        return downloaderContext;
    }
}
