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
        if (caminho == null) return;
        File apk = new File(caminho);
        if (!apk.exists()) return;

        // Tenta garantir que o arquivo possa ser lido por outros processos
        apk.setReadable(true, false);

        Intent t = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(apk);

        // Define o tipo de dado explicitamente
        t.setDataAndType(uri, "application/vnd.android.package-archive");

        // REMOVIDO: t.setClassName(...) 
        // Não force o pacote "com.android.packageinstaller". 
        // Deixe o sistema decidir qual programa trata "application/vnd.android.package-archive".

        t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Esta flag é crucial para que o instalador tenha permissão de ver o arquivo
        t.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            activity.startActivity(t);
        } catch (Exception e) {
            // Log de erro ou tratamento silencioso como você já faz
        }
    }
}
