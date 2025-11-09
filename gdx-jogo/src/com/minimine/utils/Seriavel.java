package com.minimine.utils;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public interface Seriavel {
	public void serializar(DataOutputStream dos) throws IOException;
	public void deserializar(DataInputStream dis) throws IOException;
}
