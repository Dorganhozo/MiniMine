package com.minimine;

import android.webkit.WebView;
import android.webkit.WebSettings;
import android.os.Build;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;
import android.view.ViewGroup;
import android.view.View;

public class JSWebView implements JS {
	public static WebView web;

	public JSWebView() {
		MainActivity.ISSO.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					web = new WebView(MainActivity.ISSO);
					config();
				}
			});
	}

	@Override
	public void iniciar(final String caminho) {
		if(web == null) return;
		MainActivity.ISSO.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					web.loadUrl("file://" + caminho);
				}
			});
	}

	@Override
	public void executar(final String codigo) {
		if(web == null) return;
		MainActivity.ISSO.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
						web.evaluateJavascript(codigo, null);
					else
						web.loadUrl("javascript:" + codigo);
				}
			});
	}

	@Override
	public void API(final Object classe, final String nome) {
		if(web == null) return;
		MainActivity.ISSO.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					web.addJavascriptInterface(classe, nome);
				}
			});
	}

	@Override
	public void config() {
		MainActivity.ISSO.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					WebSettings s = web.getSettings();
					s.setJavaScriptEnabled(true);
					s.setAllowFileAccess(true);
					s.setAllowContentAccess(true);
					s.setSupportZoom(true);
					s.setCacheMode(WebSettings.LOAD_NO_CACHE);
					web.clearCache(true);

					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
						WebView.setWebContentsDebuggingEnabled(true);

					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						s.setAllowFileAccessFromFileURLs(true);
						s.setAllowUniversalAccessFromFileURLs(true);
					}

					web.setWebViewClient(new WebViewClient() {
							@Override
							public boolean shouldOverrideUrlLoading(WebView v, String url) {
								if(url.startsWith("http://")) {
									Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
									MainActivity.ISSO.startActivity(i);
									return true;
								}
								return false;
							}
						});

					web.setWebChromeClient(new WebChromeClient() {
							@Override
							public boolean onConsoleMessage(ConsoleMessage msg) {
								Logs.log(msg.message());
								return true;
							}
						});

					s.setDomStorageEnabled(true);
					s.setDatabaseEnabled(true);
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
						s.setAllowFileAccess(true);
				}
			});
	}
}
