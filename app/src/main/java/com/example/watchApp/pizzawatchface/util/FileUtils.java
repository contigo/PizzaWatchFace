package com.example.watchApp.pizzawatchface.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class FileUtils {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static DocumentFile getDocumentFile(Context context, Uri treeUri){
        context.grantUriPermission(context.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return DocumentFile.fromTreeUri(context, treeUri);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static OutputStream getDocumentOutputStream(Context context, Uri treeUri, String filePath){

        OutputStream r = null;
        DocumentFile pickedDir = getDocumentFile(context, treeUri);
        if(pickedDir != null){

            try{
                DocumentFile file = pickedDir.createFile(MimeTypeMap.getFileExtensionFromUrl(filePath), filePath);
                r = context.getContentResolver().openOutputStream(file.getUri());
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return r;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static FileDescriptor getDocumentFileDescriptor(Context context, Uri treeUri, String filePath){

        FileDescriptor r = null;
        DocumentFile pickedDir = getDocumentFile(context, treeUri);
        if(pickedDir != null){

            try{
                DocumentFile file = pickedDir.createFile(MimeTypeMap.getFileExtensionFromUrl(filePath), filePath);
                r = context.getContentResolver().openFileDescriptor(file.getUri(), "w").getFileDescriptor();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return r;
    }




    public static final boolean copy(File src, File dest){

        try{
            return copy(new FileInputStream(src), new FileOutputStream(dest));
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }
    public static final boolean copy(InputStream is, OutputStream os){

        try{
            byte[] buf = new byte[8192];
            int read = -1;
            while((read = is.read(buf)) != -1){
                os.write(buf, 0, read);
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{ if(is != null) is.close(); }catch(Exception e){}
            try{ if(os != null) os.close(); }catch(Exception e){}
        }
        return false;
    }


    public static String specialCharsRemoved(String fileName){
        return specialCharsRemoved(fileName, "");
    }
    public static String specialCharsRemoved(String fileName, String replaceStr){
        if(fileName == null)
            return null;

        return fileName.replaceAll("[\\\\/:*?\"<>|]", replaceStr);
    }

    public static Uri getFileToUri(Context context, File f){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName()+".provider", f);
        }
        else{
            return Uri.fromFile(f);
        }
    }

    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.00");

    public enum SizeUnit {

        B(1, "B"),
        KB(SizeUnit.BYTES, "KB"),
        MB(SizeUnit.BYTES * SizeUnit.BYTES, "MB"),
        GB(SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES, "GB"),
        TB(SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES, "TB");

        private final long bytes;
        private static final long BYTES = 1024;
        private final String unit;

        SizeUnit(long bytes, String unit) {
            this.bytes = bytes;
            this.unit = unit;
        }

        public long bytes() {
            return bytes;
        }

        public String unit() {
            return unit;
        }

        public double toSize(long bytes) {
            if(bytes == 0)
                return 0;
            return Double.parseDouble(SIZE_FORMAT.format(bytes/(double)bytes()));
        }

        public static SizeUnit getAppropriateForUnit(long bytes){

            if(bytes < KB.bytes()) {
                return B;
            }
            else if (bytes < MB.bytes()) {
                return KB;
            }
            else if (bytes < GB.bytes()) {
                return MB;
            }
            else if (bytes < TB.bytes()) {
                return GB;
            }
            else {
                return TB;
            }
        }

    }


    public static String getFilenameExtension(String name){

        int pos = name.lastIndexOf(".");
        if(pos != -1)
            return name.substring(pos+1);
        else
            return "";
    }

    public static boolean isChild(File child, File parent) {
        return child.getAbsolutePath().startsWith(parent.getAbsolutePath());
    }

    public static String getCorrectFileName(String source){

        if(source == null || source.length() == 0)
            return source;

        return source.replaceAll("[\\\\/:*?\"<>|]", "");

    }

    //파일 위치 리턴
    public static File commonDocumentDirPath(String FolderName)
    {
        File dir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + FolderName);
            System.out.println(">>>>>>>>>>>>>>>>>>commonDocumentDirPath Version30 "+dir);
        }
        else
        {
            dir = new File(Environment.getExternalStorageDirectory() + "/" + FolderName);
            System.out.println(">>>>>>>>>>>>>>>>>>commonDocumentDirPath Version29 "+dir);
        }

        // Make sure the path directory exists.
        if (!dir.exists())
        {
            // Make it, if it doesn't exit
            boolean success = dir.mkdirs();
            if (!success)
            {
                dir = null;
            }
        }
        return dir;
    }
}
