package com.unicorn.mobile.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.text.TextUtils;

import com.unicorn.mobile.common.CommonUtil;
import com.unicorn.mobile.common.Constant;
import com.unicorn.mobile.common.ReportTimeManager;

public class UnicornCrashHandler implements UncaughtExceptionHandler {
	private final String CRASH_FILE_SUFFIX = ".cr";
    private final String IGNORED_CRASH_FILES = ".ignore";
    private final String CRLF = System.getProperty("line.separator");

	private static UnicornCrashHandler sCyouExceptionHandler;
	private UncaughtExceptionHandler mDefaultUncaughtException;
	private Context mContext;
	private Handler mHandler;

	private UnicornCrashHandler() {
		super();
	}

	public static synchronized UnicornCrashHandler getInstance() {
		if (sCyouExceptionHandler == null) {
			sCyouExceptionHandler = new UnicornCrashHandler();
		}
		return sCyouExceptionHandler;
	}

	public void init(Context ctx, Handler handler) {
		mContext = ctx;
		mHandler = handler;
		createIgnoreFile();
		mDefaultUncaughtException = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(UnicornCrashHandler.this);
	}

	@Override
	public void uncaughtException(final Thread thread, final Throwable ex) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String crash = getThrowableError(ex);
				recordCrash(crash);
				mDefaultUncaughtException.uncaughtException(thread, ex);
			}

		}).start();
	}

	public void reportCrash() {
		if (ReportTimeManager.needCrashReport(mContext)) {
            removeIgnoreCrash();
			Map<String, String> crashMap = genCrashMap();
			if (crashMap.isEmpty() || crashMap.size() <= 0) {
				return;
			}
			
			mHandler.sendMessage(mHandler.obtainMessage(Constant.CRASH_REPORT, crashMap));
		}
	}
	
	private void createIgnoreFile() {
		File ignoreFile = new File(mContext.getFilesDir(), IGNORED_CRASH_FILES);
		if (!ignoreFile.exists()) {
			try {
				ignoreFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Map<String, String> genCrashMap() {
		String[] crashFiles = getCrashFiles();
		Map<String, String> crashMap = new HashMap<String, String>();
		for (String file : crashFiles) {
			File crashFile = new File(mContext.getFilesDir(), file);
			try {
				String crashRecord = readCrashFile(crashFile);
				String basename = CommonUtil.getFileBasename(file);
				crashMap.put(basename, crashRecord);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return crashMap;
	}

	private String[] getCrashFiles() {
		File filesDir = mContext.getFilesDir();
		FilenameFilter filter = new FilenameFilter() {
			SharedPreferences sPref = mContext.getSharedPreferences(Constant.CRASH_RECORD, Context.MODE_PRIVATE);
			public boolean accept(File dir, String name) {
				if (name.endsWith(CRASH_FILE_SUFFIX)) {
					return sPref.getLong(CommonUtil.getFileBasename(name), 0L) > 0;
				}
				return false;
			}
		};
		
		return filesDir.list(filter);
	}

    private void removeIgnoreCrash() {
        File filesDir = mContext.getFilesDir();
        File ignoreFile = new File(filesDir, IGNORED_CRASH_FILES);
        File crashFile;
        try {
            List<String> names = readIgnoreFile(ignoreFile);
            if (!names.isEmpty()) {
	            for (String name : names) {
	                crashFile = new File(filesDir, name);
	                if (crashFile.exists()) {
	                    crashFile.delete();
	                }
	            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void recordCrash(String content) {
        String md5Str = CommonUtil.makeStrMd5(content);
		if (mContext == null || TextUtils.isEmpty(md5Str)) {
			return;
		}

		SharedPreferences sPref = mContext.getSharedPreferences(Constant.CRASH_RECORD, Context.MODE_PRIVATE);
		File crashFile = new File(mContext.getFilesDir(), md5Str + CRASH_FILE_SUFFIX);
		if (!sPref.contains(md5Str)) {
	        if (writeToFile(crashFile, content)) {
	            Editor editor = sPref.edit();
	            editor.putLong(md5Str, System.currentTimeMillis());
	            editor.commit();
	        } else {
	            // write failure then add file to ignore list
	            if (crashFile.exists() && !crashFile.delete()) {
	                File ignoreFile = new File(mContext.getFilesDir(), IGNORED_CRASH_FILES);
	                appendToFile(ignoreFile, md5Str + CRASH_FILE_SUFFIX);
	            }
	        }
		} else {
			// delete crash file which has been reported
			if (sPref.getLong(md5Str, 0L) == 0L && crashFile.exists()) {
				crashFile.delete();
			}
		}
	}

	private boolean writeToFile(File file, String data) {
		boolean mark = true;
        BufferedWriter bw = null;
		try {
            bw = new BufferedWriter(new FileWriter(file));
			bw.write(data);
		} catch (IOException e) {
            e.printStackTrace();
			mark = false;
		} finally {
			try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                mark = false;
            }
		}
		
		return mark;
	}

    private void appendToFile(File file, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true);
            fw.write(content);
            fw.write(CRLF);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	private String readCrashFile(File file) throws IOException {
		StringBuilder builder = new StringBuilder("");
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		int len = 0;
		while (line != null) {
			if (len > 0) {
				builder.append(CRLF);
			}
			builder.append(line);
			line = br.readLine();
			++len;
		}
		br.close();
		return builder.toString();
	}

    private List<String> readIgnoreFile(File file) throws IOException {
        List<String> list = new ArrayList<String>();
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        while (line != null) {
            list.add(line);
            line = br.readLine();
        }
        br.close();
        return list;
    }

	private String getThrowableError(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();

		return writer.toString();
	}

}
