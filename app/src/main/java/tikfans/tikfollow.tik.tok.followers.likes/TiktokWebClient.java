package tikfans.tikfollow.tik.tok.followers.likes;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TiktokWebClient {
    private static final String TAG = "khang";
    private WebView webview;
    private ClientListener listener;
    private Handler handler;
    public interface ClientListener{
        void onLoading();

        void onLoadFinish(Document document, String url);
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    public void clearCache() {
        webview.clearCache(true);
    }

    public TiktokWebClient(Context context, WebView webView) {
        handler = new Handler();
        if(webview == null) {
            this.webview = webView;

            webView.setWebChromeClient(new WebChromeClient(){
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    request.grant(request.getResources());
                }
            });

            //webview = new WebView(context);
            //String newUA= "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36";
            //webview.getSettings().setUserAgentString(newUA);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setUseWideViewPort(true);
            webview.getSettings().setLoadWithOverviewMode(true);
            webview.getSettings().setLoadsImagesAutomatically(true);
           // webview.getSettings().setAppCacheEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webview.getSettings().setMixedContentMode(0);
                webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            webview.addJavascriptInterface(new MyJavaScriptInterface(context), "HtmlViewer");
            webview.setWebViewClient(new WebViewClient() {

//                @Override
//                public boolean shouldOverrideUrlLoading(WebView webView, String url) {
//                    Log.e(TAG, "shouldOverrideUrlLoading "+url);
//                    return true;
//                }


                @Override
                public void onPageStarted(
                        WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    Log.e(TAG, "onPageStarted");
                    //SHOW LOADING IF IT ISNT ALREADY VISIBLE
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.e(TAG, "onPageFinished: url: " + url);
                    webview.loadUrl("javascript:HtmlViewer.showHTML" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>','" + url + "');");
                }

            });
        }
    }

    public void load(final String url) {
        if (webview != null) {
            webview.post(new Runnable() {
                public void run() {
                    if (listener != null) {
                        listener.onLoading();
                    }
                    webview.clearCache(true);
                    webview.loadUrl(url);
                }
            });

        }
    }

    public void stopLoad() {
        if (webview != null) {
            webview.post(new Runnable() {
                public void run() {
                    webview.stopLoading();
                }
            });

        }
    }

    class MyJavaScriptInterface {

        private Context ctx;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void showHTML(final String html, String url) {
            if (listener != null) {
                listener.onLoadFinish(Jsoup.parse(html), url);
            }
        }
    }
}

