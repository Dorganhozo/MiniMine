package com.minimine;

import android.os.Debug;

public class DebugadorDoAndroid implements Debugador {
	@Override
	public long getNativeHeapFreeSize(){
		return Debug.getNativeHeapFreeSize();
	}

	@Override
	public long getNativeHeapSize(){
		return Debug.getNativeHeapSize();
	}
}
