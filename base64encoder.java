import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class base64encoder {
    public File encoder(File file_name) throws IOException{
        if(file_name.exists()){
            String upd_file = file_name.toPath() + ".enc";
            File new_file = new File(upd_file);
            char[] BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
            byte[] buffer = new byte[3];
            int byt;
                FileInputStream fis = new FileInputStream(file_name);
                FileOutputStream fos = new FileOutputStream(new_file);
                while ((byt = fis.read(buffer)) != -1) {

                    int b1 = buffer[0] & 0xFF;
                    int b2 = (byt > 1) ? buffer[1] & 0xFF : 0;
                    int b3 = (byt > 2) ? buffer[2] & 0xFF : 0;

                    int bits = (b1 << 16) | (b2 << 8) | b3;
                    
                    fos.write(BASE64[(bits >> 18) & 0x3F]);
                    fos.write(BASE64[(bits >> 12) & 0x3F]);
                    
                    if (byt > 1){
                        fos.write(BASE64[(bits >> 6) & 0x3F]);
                    }else{
                        fos.write('=');
                    }
                    
                    if (byt > 2){
                        fos.write(BASE64[bits & 0x3F]);
                    }else{
                        fos.write('=');
                    }
                }
                fis.close();
                fos.close();
            return new_file;
        }else{
            throw new IOException("no such file exist");
        }
    }
}
