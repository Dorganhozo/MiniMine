package com.minimine;

import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Crash extends Application {
    public static Handler loop = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        CrashUtil.getInstance().logGlobal(this);
        CrashUtil.getInstance().logParte(this);
    }
    public static void gravar(InputStream e, OutputStream s) throws IOException {
        byte[] buf = new byte[1024 * 8];
        int tam;
        while((tam = e.read(buf)) != -1) {
            s.write(buf, 0, tam);
        }
    }
    public static void gravar(File a, byte[] dados) throws IOException {
        File p = a.getParentFile();
        if(p != null && !p.exists()) p.mkdirs();

        ByteArrayInputStream e = new ByteArrayInputStream(dados);
        FileOutputStream s = new FileOutputStream(a);
        try {
            gravar(e, s);
        } finally {
            fecharIO(e, s);
        }
    }
    public static String praString(InputStream e) throws IOException {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        gravar(e, s);
        try {
            return s.toString("UTF-8");
        } finally {
            fecharIO(e, s);
        }
    }
    public static void fecharIO(Closeable... cs) {
        for(Closeable c : cs) {
            try {
                if(c != null) c.close();
            } catch(IOException e) {}
        }
    }
    public static class CrashUtil {
        public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();
        public static CrashUtil copia;
        public Util util;
        public static CrashUtil getInstance() {
            if(copia == null) {
                copia = new CrashUtil();
            }
            return copia;
        }
        public void logGlobal(Context ctx) {
            logGlobal(ctx, null);
        }
        public void logGlobal(Context ctx, String crashDir) {
            Thread.setDefaultUncaughtExceptionHandler(new Excecao(ctx.getApplicationContext(), crashDir));
        }
        public void deslogar() {
            Thread.setDefaultUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER);
        }
        public void logParte(Context ctx) {
            deslogarParte(ctx);
            util = new Util(ctx.getApplicationContext());
            loop.postAtFrontOfQueue(util);
        }
        public void deslogarParte(Context ctx) {
            if(util != null) {
                util.rodando.set(false);
                util = null;
            }
        }
        public static class Util implements Runnable {
            private final Context mCtx;
            public AtomicBoolean rodando = new AtomicBoolean(true);

            public Util(Context context) {
                this.mCtx = context;
            }
            @Override
            public void run() {
                while(rodando.get()) {
                    try {
                        Looper.loop();
                    } catch(final Throwable e) {
                        e.printStackTrace();
                        if(rodando.get()) {
                            loop.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        Toast.makeText(mCtx, e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                        } else {
                            if(e instanceof RuntimeException) {
                                throw (RuntimeException)e;
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
        public static class Excecao implements UncaughtExceptionHandler {
            public static DateFormat dados = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
            public final Context mCtx;
            public final File mCrashDir;

            public Excecao(Context ctx, String crashDir) {
                this.mCtx= ctx;
                this.mCrashDir = TextUtils.isEmpty(crashDir) ? new File(mCtx.getExternalCacheDir(), "crash") : new File(crashDir);
            }
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                try {
                    String log = informar(ex);
                    log(log);
                    try {
                        Intent t = new Intent(mCtx, CrashActivity.class);
                        t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        t.putExtra(Intent.EXTRA_TEXT, log);
                        mCtx.startActivity(t);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        log(e.toString());
                    }
                    ex.printStackTrace();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                } catch (Throwable e) {
                    if(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, ex);
                }
            }
            public String informar(Throwable ex) {
                String tempo = dados.format(new Date());
                String nomeV = "desconhecida";
                long codigoV = 0;
                try {
                    PackageInfo pi = mCtx.getPackageManager().getPackageInfo(mCtx.getPackageName(), 0);
                    nomeV = pi.versionName;
                    codigoV = Build.VERSION.SDK_INT >= 28 ? pi.getLongVersionCode() : pi.versionCode;
                } catch(Throwable ignored) {}

                LinkedHashMap<String, String> c = new LinkedHashMap<String, String>();
                c.put("Data do Crash", tempo);
                c.put("Dispositivo", String.format("%s, %s", Build.MANUFACTURER, Build.MODEL));
                c.put("Versão do Android", String.format("%s (%d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
                c.put("Versão do App", String.format("%s (%d)", nomeV, codigoV));
                c.put("Kernel", kernel());
                c.put("Suporte de Abis", Build.VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS != null ? Arrays.toString(Build.SUPPORTED_ABIS): "desconhecido");
                c.put("Impressão Digital", Build.FINGERPRINT);

                StringBuilder sb = new StringBuilder();

                for(String chave : c.keySet()) {
                    if(sb.length() != 0) sb.append("\n");
                    sb.append(chave);
                    sb.append(" :    ");
                    sb.append(c.get(chave));
                }
                sb.append("\n\n");
                sb.append(Log.getStackTraceString(ex));
                return sb.toString(); 
            }
            public void log(String msg) {
                String time = dados.format(new Date());
                File file = new File(mCrashDir, "crash_" + time + ".txt");
                try {
                    gravar(file, msg.getBytes("UTF-8"));
                } catch(Throwable e) {
                    e.printStackTrace();
                } 
            }
            public static String kernel() {
                try {
                    return Crash.praString(new FileInputStream("/proc/version")).trim();
                } catch (Throwable e) {
                    return e.getMessage();
                }
            }
        }
    }
    public static final class CrashActivity extends Activity {
        public String mLog;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setTheme(android.R.style.Theme_DeviceDefault);
            setTitle("O App Crashou");

            mLog = getIntent().getStringExtra(Intent.EXTRA_TEXT);

            ScrollView cv = new ScrollView(this);
            cv.setFillViewport(true);

            HorizontalScrollView hsc = new HorizontalScrollView(this);

            TextView tv = new TextView(this);
            int es = dp2px(16);
            tv.setPadding(es, es, es, es);
            tv.setText(mLog);
            tv.setTextIsSelectable(true);
            tv.setTypeface(Typeface.DEFAULT);
            tv.setLinksClickable(true);

            hsc.addView(tv);
            cv.addView(hsc, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            setContentView(cv);
        }
        public void reiniciar() {
            Intent t = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if(t != null) {
                t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(t);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
        public static int dp2px(float dpValor) {
            final float tam = Resources.getSystem().getDisplayMetrics().density;
            return (int)(dpValor * tam + 0.5f);
        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, android.R.id.copy, 0, android.R.string.copy)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return super.onCreateOptionsMenu(menu);
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch(item.getItemId()) {
                case android.R.id.copy:
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }
        @Override
        public void onBackPressed() {
            reiniciar();
        }
    }
}
