package com.frlgrd.streamzone.util;

import java.io.Closeable;
import java.io.IOException;

public class ClosableUtil {
	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
