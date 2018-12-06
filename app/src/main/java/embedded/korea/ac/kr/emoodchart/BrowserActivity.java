package embedded.korea.ac.kr.emoodchart;

import android.app.ActionBar;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import embedded.korea.ac.kr.emoodchart.api.APIHelper;

public class BrowserActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        setTitle("eMoodchart 설문조사");

        String surveyUri = APIHelper.genSurveyUrl(new UserInfo(getBaseContext()));

        mWebView = (WebView) findViewById(R.id.forwardWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(surveyUri);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());
    }

    public void setTitle(CharSequence title) {
        super.setTitle(title);

        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(textView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }
}
