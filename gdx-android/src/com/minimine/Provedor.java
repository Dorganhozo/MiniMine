package com.minimine;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileNotFoundException;

public class Provedor extends ContentProvider {
    @Override
    public boolean onCreate() { return true; }

    @Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		// No Android 4, às vezes a URI pode vir com caminhos levemente diferentes
		// mas o File(uri.getPath()) costuma ser tiro e queda.
		File arquivo = new File(uri.getPath());

		if (arquivo.exists()) {
			return ParcelFileDescriptor.open(arquivo, ParcelFileDescriptor.MODE_READ_ONLY);
		}
		throw new FileNotFoundException("Arquivo não encontrado: " + uri.getPath());
	}

    // Métodos obrigatórios que não faremos nada
    @Override public Cursor query(Uri u, String[] s, String o, String[] a, String g) { return null; }
    @Override public String getType(Uri u) { return "application/vnd.android.package-archive"; }
    @Override public Uri insert(Uri u, ContentValues v) { return null; }
    @Override public int delete(Uri u, String s, String[] a) { return 0; }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { return 0; }
}
