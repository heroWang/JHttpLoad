package com.hawky.jhttpload.utils;

import java.io.File;
import java.net.URLDecoder;
import java.security.CodeSource;

import com.hawky.jhttpload.Main;

public class Utils {
	public static String getAbsolutePath(String path) throws Exception {
		if (new File(path).isAbsolute()) {
			return new File(path).getCanonicalPath();
		}
		return new File(getExecutedPath(Main.class) + path).getCanonicalPath();
	}

	public static String getExecutedPath(Class<?> aclass) throws Exception {
		CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

		File file;

		if (codeSource.getLocation() != null) {
			file = new File(codeSource.getLocation().toURI());
		} else {
			String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
			String filePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
			filePath = URLDecoder.decode(filePath, "UTF-8");
			file = new File(filePath);

		}
		if (file.isFile()) {// if executed with jar file
			return file.getParentFile().getCanonicalPath();
		} else {
			return file.getCanonicalPath();
		}
	}
}
