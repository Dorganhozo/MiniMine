package com.minimine;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import java.io.File;

public class InstaladorAndroid implements Instalador {
    public final Activity activity;

    public InstaladorAndroid(Activity activity) {
        this.activity = activity;
    }

    @Override
	public void instalar(String caminho) {
		if(caminho == null) return;
		File apk = new File(caminho);
		if(!apk.exists()) return;

		// URI do nosso provedor
		Uri uriModificada = Uri.parse("content://com.minimine.instalar" + apk.getAbsolutePath());

		Intent t = new Intent(Intent.ACTION_VIEW);
		t.setDataAndType(uriModificada, "application/vnd.android.package-archive");

		t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		t.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		try {
			activity.startActivity(t);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
