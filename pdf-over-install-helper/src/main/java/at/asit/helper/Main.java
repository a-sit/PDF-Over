package at.asit.helper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.io.FileUtils;




/***
 * 
 * @author aabraham
 * 
 * @version 1.0.0 
 *
 *	The created jar is used as helper library when installing pdf-over. 
 *	Basically, the helper determines the java version used including architecture and 
 *	appends afterwards the right swt library since this swt is platform dependend. 
 *
 */
public class Main {
	
	public static void main(String[] args) {
		
		System.out.println("start post install task");
		
		copyLib();

	}
	

	public static int getArchBits() {
		String arch = System.getProperty("sun.arch.data.model");
		return arch.contains("64") ? 64 : 32;
	}

	public static String getSwtJarName() throws SWTLoadFailedException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win"))
			os = "windows";
		else if (os.contains("mac"))
			os = "mac";
		else if (os.contains("linux") || os.contains("nix"))
			os = "linux";
		else {
			throw new SWTLoadFailedException("Unknown OS: " + os);
		}
		return "swt-" + os + "-" + getArchBits() + ".jar";
	}

	public static String getSwtJarPath() {
		String path = "";
		try {
			path = URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
			int idx = path.lastIndexOf('/');
			idx = path.lastIndexOf('/', idx - 1);
			path = path.substring(0, idx + 1);
		} catch (UnsupportedEncodingException e) {
			// Ignore
			System.err.println("Unsuported Encoding Exception " + e.getMessage());
		}
		return path + "lib-swt/";
	}

	private static class SWTLoadFailedException extends Exception {
		private static final long serialVersionUID = 1L;

		SWTLoadFailedException(String msg) {
			super(msg);
		}
	}
	
	
	private static String getSwtTargetLibName() throws SWTLoadFailedException {
		
		String os = System.getProperty("os.name").toLowerCase();
		System.out.println("OS " + os + " detected!");
		String swtTargetLibName; 
		
		if (os.contains("win")) {
				swtTargetLibName = "org.eclipse.swt.win32.win32.x86-4.3.2.jar";
			}
		else if (os.contains("mac")) {
			swtTargetLibName = "org.eclipse.swt.cocoa.macosx-4.3.2.jar";
		}
		else if (os.contains("linux") || os.contains("nix")) {
			swtTargetLibName = "org.eclipse.swt.gtk.linux.x86-4.3.2.jar";
		}
		else {
			throw new SWTLoadFailedException("Unknown OS: " + os);
		}
		return swtTargetLibName;
		
		
	}
	
	
	private static void copyLib() {
		try {
			String swtLibPath = Main.getSwtJarPath() + Main.getSwtJarName();
			File swtLib = new File(swtLibPath);
			if (!swtLib.isFile()) {

				System.err.println("not found "); 
				throw new SWTLoadFailedException("Library " + swtLibPath + " not found");
			}
			
			String newPath = swtLibPath.replace("lib-swt", "lib"); 
			newPath = newPath.replace(Main.getSwtJarName(), getSwtTargetLibName());
			File newFile = new File(newPath);
			System.out.println("Source Lib " + swtLibPath.toString()); 
			System.out.println("Target Lib " + newFile.toString()); 
			FileUtils.copyFile(swtLib, newFile);
			
			if (!newFile.isFile()) {
				System.err.println("not found "); 
				throw new SWTLoadFailedException("Library " + swtLibPath + " not found");
			}
				
			System.out.println("success "); 
			return;
		
	
		} catch(Exception e){
			e.printStackTrace();
		}

	}


}
