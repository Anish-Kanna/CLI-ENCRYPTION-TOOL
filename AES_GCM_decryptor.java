import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_GCM_decryptor {
    public File decryptor(File encrypted_file, String password)throws Exception {
        final byte[] magic = {(byte) 0x00, (byte) 0x00, (byte) 0xAB, (byte) 0x64};
        final byte version = 0x01;
        final byte algorithm = 0x01;

        final int BASE_HEADER = 34;
        final int GCM_TAG_LENGTH_BITS = 128;
        final int BUFFER_SIZE = 16 * 1024;

        header_generator header = new header_generator();

        if (!header.header_validator(encrypted_file, magic, version, algorithm)) {
            throw new IOException("Header not validated");
        }

        byte[] IV = header.get_IV(encrypted_file);
        byte[] salt = header.get_salt(encrypted_file);
        String extension = header.get_EXT(encrypted_file);

        if (IV.length != 12) {
            throw new IOException("IV size not matching");
        }

        if (salt.length != 16) {
            throw new IOException("Salt size not matching");
        }

        key_generator key = new key_generator();
        byte[] AES_KEY = key.key_gen(password, salt);

        String base_file_name = encrypted_file.getName();
        int first_dot_ind = base_file_name.indexOf('.');
        String name = (first_dot_ind != -1)
                ? base_file_name.substring(0, first_dot_ind) + "_dec"
                : base_file_name + "_dec";

        String output_file_name = name + extension;

        File output_file = new File(output_file_name);
        File tempfile = new File(output_file.getAbsolutePath() + ".tmp");

        SecretKey secretkey = new SecretKeySpec(AES_KEY, "AES");
        GCMParameterSpec gcm = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, IV);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretkey, gcm);

        int extLen = extension.getBytes(StandardCharsets.UTF_8).length;
        int totalHeaderSize = BASE_HEADER + 1 + extLen;

        try (FileInputStream fis = new FileInputStream(encrypted_file);
             FileOutputStream fos = new FileOutputStream(tempfile)) {

            long skipped = fis.skip(totalHeaderSize);
            if (skipped != totalHeaderSize) {
                throw new IOException("Failed to skip full header");
            }

            byte[] buffer = new byte[BUFFER_SIZE];
            int byt;

            while ((byt = fis.read(buffer)) != -1) {
                byte[] decrypted_text = cipher.update(buffer, 0, byt);
                if (decrypted_text != null) {
                    fos.write(decrypted_text);
                }
            }

            byte[] finalbytes = cipher.doFinal();
            if (finalbytes != null) {
                fos.write(finalbytes);
            }

        } catch (AEADBadTagException e) {
            Files.deleteIfExists(tempfile.toPath());
            throw new SecurityException("Authentication failed (wrong password or tampered file)");
        } catch (Exception e) {
            Files.deleteIfExists(tempfile.toPath());
            throw e;
        }

        Files.move(tempfile.toPath(),
                   output_file.toPath(),
                   StandardCopyOption.REPLACE_EXISTING);

        return output_file;
    }
}

