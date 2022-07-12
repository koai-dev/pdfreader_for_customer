package com.cocna.pdffilereader.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PreviewUtils {
    private ImageCache imageCache;
    private static PreviewUtils instance;
    ExecutorService executorService;
    HashMap<String, Future> tasks;

    public static PreviewUtils getInstance() {
        if (instance == null) {
            instance = new PreviewUtils();
        }
        return instance;
    }

    private PreviewUtils() {
        imageCache = new ImageCache();
        executorService = Executors.newFixedThreadPool(20);
        tasks = new HashMap<>();
    }

    public void loadBitmapFromPdf(final Context context,
                                  final ImageView imageView,
                                  final PdfiumCore pdfiumCore,
                                  final PdfDocument pdfDocument,
                                  final String pdfName,
                                  final int pageNum) {
        if (imageView == null || pdfiumCore == null || pdfDocument == null || pageNum < 0) {
            return;
        }

        try {
            final String keyPage = pdfName + pageNum;

            imageView.setTag(keyPage);
            final int reqWidth = 100;
            final int reqHeight = 150;

            Bitmap bitmap = imageCache.getBitmapFromLruCache(keyPage);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }

            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    pdfiumCore.openPage(pdfDocument, pageNum);

                    final Bitmap bm = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565);
                    pdfiumCore.renderPageBitmap(pdfDocument, bm, pageNum, 0, 0, reqWidth, reqHeight);

                    if (bm != null) {
                        imageCache.addBitmapToLruCache(keyPage, bm);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (imageView.getTag().toString().equals(keyPage)) {
                                    imageView.setImageBitmap(bm);
                                }
                            }
                        });
                    }
                }
            });
            tasks.put(keyPage, future);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void cancelLoadBitmapFromPdf(String keyPage) {
        if (keyPage == null || !tasks.containsKey(keyPage)) {
            return;
        }
        try {
            Future future = tasks.get(keyPage);
            if (future != null) {
                future.cancel(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ImageCache getImageCache() {
        return imageCache;
    }

    public class ImageCache {
        private LruCache<String, Bitmap> lruCache;

        public ImageCache() {
            int cacheSize = 1024 * 1024 * 30;
            lruCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }
            };
        }

        public synchronized Bitmap getBitmapFromLruCache(String key) {
            if (lruCache != null) {
                return lruCache.get(key);
            }
            return null;
        }

        public synchronized void addBitmapToLruCache(String key, Bitmap bitmap) {
            if (getBitmapFromLruCache(key) == null) {
                if (lruCache != null && bitmap != null)
                    lruCache.put(key, bitmap);
            }
        }

        public void clearCache() {
            if (lruCache != null) {
                lruCache.evictAll();
            }
        }
    }
}