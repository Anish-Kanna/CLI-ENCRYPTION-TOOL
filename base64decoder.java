import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class base64decoder {
    public File decoder(File file_name) throws IOException{
        if(file_name.exists()){
            String name_file = file_name.getName();
            String name;
            int dotIndex = name_file.lastIndexOf('.');
            if (dotIndex != -1) {
                name = name_file.substring(0, dotIndex);
            } else {
                name = name_file;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file_name));
            FileOutputStream fos = new FileOutputStream(name);
            String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
            int[] rev_lookup = new int[256];
            for(int i = 0; i < 256; i++){
                rev_lookup[i] = -1;
            }
            for(int i = 0; i < BASE64.length(); i++){
                rev_lookup[BASE64.charAt(i)] = i;
            }
            int count = 0;
            int byt;
            char[] buffer = new char[4];
            while((byt = reader.read()) != -1){
                char c = (char) byt;

                if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
                    continue;
                }

                buffer[count++] = c;

                if (count == 4) {

                    char c1 = buffer[0];
                    char c2 = buffer[1];
                    char c3 = buffer[2];
                    char c4 = buffer[3];

                    int v1 = rev_lookup[c1];
                    int v2 = rev_lookup[c2];
                    int v3 = (c3 == '=') ? 0 : rev_lookup[c3];
                    int v4 = (c4 == '=') ? 0 : rev_lookup[c4];

                    int bits = (v1 << 18) | (v2 << 12) | (v3 << 6) | v4;

                    fos.write((bits >> 16) & 0xFF);

                    if (c3 != '=') {
                        fos.write((bits >> 8) & 0xFF);
                    }

                    if (c4 != '=') {
                        fos.write(bits & 0xFF);
                    }

                    count = 0;
                }
            }
            fos.close();
            reader.close();
            // return upd_file;
            File new_file = new File(name);
            return new_file;
        }else{
            throw new IOException("no such file exist");
        }
    }
}
