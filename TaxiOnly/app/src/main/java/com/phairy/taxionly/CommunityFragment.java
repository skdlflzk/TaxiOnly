
package com.phairy.taxionly;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.util.Calendar;


public class CommunityFragment extends Fragment {
    private Logger mLogger = Logger.getLogger(CommunityFragment.class);

    String TAG = Start.TAG;
    WebView webView;
    final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "--CommunityFragment--");
        View view = inflater.inflate(R.layout.community_fragment, container, false);

        webView = (WebView) view.findViewById(R.id.webView);
        webView.setWebViewClient(new WebClient());
        WebSettings set = webView.getSettings();
        if( sdkVersion < Build.VERSION_CODES.FROYO){
            webView.loadData("한글",  "text/html", "UTF-8");  // Android 4.0 이하 버전
            webView.loadData("한글",  "text/html; charset=UTF-8", null);  // Android 4.1 이상 버전
        }

        set.setJavaScriptEnabled(true);
        set.setBuiltInZoomControls(true);
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                //This is the filter
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;


                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                        mLogger.debug("CommunityFragment: back");
                    } else {
                        mLogger.error("CommunityFragment: back 눌러짐");
                        ( getActivity()).onKeyDown(keyCode,event);
                    }

                    return true;
                }

                return false;
            }
        });

        mLogger.info("onCreateView_페이지 로딩 중");
        webView.loadUrl("http://m.bumhyo.cafe24.com/board/index.html");

        return view;
    }

    class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }


}
