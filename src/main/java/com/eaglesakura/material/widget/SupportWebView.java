package com.eaglesakura.material.widget;

import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.util.Util;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebView
 */
public class SupportWebView extends ObservableWebView {

    public SupportWebView(Context context, AttributeSet attrs) {
        super(context, attrs);


        if (isInEditMode()) {
            return;
        }

        ViewUtil.setupDefault(this);
        getSettings().setDisplayZoomControls(false);
        getSettings().setSupportZoom(false);
        setWebChromeClient(mChromeClientImpl);
        setWebViewClient(mWebViewClientImpl);
    }

    @Nullable
    public float getWebScale() {
        return Util.getFloat(mWebScale, getScale());
    }

    /**
     * ウェブコンテンツのスケーリング
     */
    @Nullable
    Float mWebScale;

    WebChromeClient mChromeClientImpl = new WebChromeClient() {

    };
    WebViewClient mWebViewClientImpl = new WebViewClient() {
        /**
         * すべてのコンテンツはWebViewで表示する
         * @param view
         * @param request
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            mWebScale = newScale;
        }
    };
}
