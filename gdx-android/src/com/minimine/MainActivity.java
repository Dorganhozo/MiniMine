package com.minimine;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import android.app.Activity;

public class MainActivity extends AndroidApplication {
	public static Activity ISSO;
	
    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
		Sistema.pedirArmazTotal(this);
		
		ISSO = this;
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        
        initialize(new Inicio(Sistema.externo, new DebugadorDoAndroid()), cfg);
    }
}
