import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_GCM_encryptor {
    public File encryptor(File source_file, File dest_file, byte[] IV, byte[] AES_KEY) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, BadPaddingException{
        final int GCM_TAG_LENGTH_BITS = 128;
        SecretKey secretkey = new SecretKeySpec(AES_KEY, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, IV);
        cipher.init(Cipher.ENCRYPT_MODE, secretkey, spec);
        FileInputStream fis = new FileInputStream(source_file);
        FileOutputStream fos = new FileOutputStream(dest_file, true);
        byte[] buffer = new byte[16 * 1024];
        int byt;
        while((byt = fis.read(buffer)) != -1){
            byte[] cipher_text = cipher.update(buffer, 0, byt);
            if(cipher_text != null){
                fos.write(cipher_text);
            }
        }

        byte[] final_bytes = cipher.doFinal();
        if(final_bytes != null){
            fos.write(final_bytes);
        }
        fis.close();
        fos.close();
        return dest_file;
    }
}
