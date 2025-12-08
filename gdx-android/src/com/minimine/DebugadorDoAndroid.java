package com.minimine;

import android.os.Debug;
import android.os.Build;

public class DebugadorDoAndroid implements Debugador {
	@Override
	public long obterHeapLivre(){
		return Debug.getNativeHeapFreeSize();
	}

	@Override
	public long obterHeapTotal(){
		return Debug.getNativeHeapSize();
	}
	
	@Override
	public boolean ehArm64() {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			for(String abi : Build.SUPPORTED_ABIS) {
				if(abi.contains("arm64")) {
					return true;
				}
			}
		}
		return Build.CPU_ABI.contains("arm64");
	}
}
