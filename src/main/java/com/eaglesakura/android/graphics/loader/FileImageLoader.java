package com.eaglesakura.android.graphics.loader;

import com.eaglesakura.android.graphics.ImageCacheManager;
import com.eaglesakura.io.CancelableInputStream;
import com.eaglesakura.util.IOUtil;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;

public class FileImageLoader extends ImageLoader<FileImageLoader> {

    @NonNull
    final File mFile;

    public FileImageLoader(@NonNull Context context, @NonNull ImageCacheManager imageManager, @NonNull File file) {
        super(context, imageManager);
        mFile = file.getAbsoluteFile();
    }

    @NonNull
    @Override
    protected String getUniqueId() {
        return "file=" + mFile.getAbsolutePath();
    }

    @NonNull
    @Override
    protected Object onLoad() throws Throwable {
        CancelableInputStream stream = new CancelableInputStream(new FileInputStream(mFile), mCancelCallback);
        stream.setBufferSize(1024 * 256);   // 画像ファイルのため、バッファサイズを大きく取る
        try {
            return BitmapFactory.decodeStream(stream);
        } finally {
            IOUtil.close(stream);
        }
    }
}
