import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class header_generator {

    public void header_gen(File new_file, String file_extension) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(new_file)) {

            byte[] magic = new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0xAB, (byte) 0x64};

            byte version = 0x01;
            byte algorithm = 0x01;

            byte[] randombytes_IV = new byte[12];
            byte[] randombytes_salt = new byte[16];

            SecureRandom random = new SecureRandom();
            random.nextBytes(randombytes_IV);
            random.nextBytes(randombytes_salt);

            byte[] ext_bytes = file_extension.getBytes(StandardCharsets.UTF_8);

            if (ext_bytes.length > 255) {
                throw new IOException("File extension too long");
            }

            fos.write(magic);
            fos.write(version);
            fos.write(algorithm);
            fos.write(randombytes_IV);
            fos.write(randombytes_salt);
            fos.write((byte) ext_bytes.length);
            fos.write(ext_bytes);
        }
    }

    public byte[] get_salt(File file_name) throws IOException {
        try (FileInputStream fis = new FileInputStream(file_name)) {
            byte[] header = new byte[34];
            if (fis.read(header) != 34) {
                throw new IOException("Failed to read header");
            }
            byte[] salt = new byte[16];
            System.arraycopy(header, 18, salt, 0, 16);
            return salt;
        }
    }

    public byte[] get_IV(File file_name) throws IOException {
        try (FileInputStream fis = new FileInputStream(file_name)) {
            byte[] header = new byte[34];
            if (fis.read(header) != 34) {
                throw new IOException("Failed to read header");
            }
            byte[] IV = new byte[12];
            System.arraycopy(header, 6, IV, 0, 12);
            return IV;
        }
    }

    public String get_EXT(File file_name) throws IOException {

        final int BASE_HEADER = 34;

        try (FileInputStream fis = new FileInputStream(file_name)) {

            byte[] baseHeader = new byte[BASE_HEADER + 1];

            if (fis.read(baseHeader) != BASE_HEADER + 1) {
                throw new IOException("Failed to read base header");
            }

            int extLen = baseHeader[34] & 0xFF;

            if (extLen > 100) {
                throw new IOException("Invalid extension length");
            }

            byte[] extBytes = new byte[extLen];

            if (fis.read(extBytes) != extLen) {
                throw new IOException("Failed to read extension bytes");
            }

            return new String(extBytes, StandardCharsets.UTF_8);
        }
    }

    public boolean header_validator(File encrypted_file, byte[] magic, byte version, byte algorithm) throws IOException {

        final int BASE_HEADER = 34;

        if (!encrypted_file.exists() || !encrypted_file.isFile()) {
            throw new IOException("Invalid file");
        }

        if (encrypted_file.length() < BASE_HEADER + 1) {
            throw new IOException("File too small to contain valid header");
        }

        byte[] header = new byte[BASE_HEADER + 1];

        try (FileInputStream fis = new FileInputStream(encrypted_file)) {

            if (fis.read(header) != BASE_HEADER + 1) {
                throw new IOException("Failed to read full header");
            }

            for (int i = 0; i < 4; i++) {
                if (header[i] != magic[i]) {
                    throw new IOException("Invalid magic bytes");
                }
            }

            if (header[4] != version) {
                throw new IOException("Unsupported version");
            }

            if (header[5] != algorithm) {
                throw new IOException("Unsupported algorithm ID");
            }

            int ext_len = header[34] & 0xFF;

            if (ext_len > 100) {
                throw new IOException("Invalid extension length");
            }

            long expectedHeaderSize = BASE_HEADER + 1 + ext_len;

            if (encrypted_file.length() < expectedHeaderSize) {
                throw new IOException("File truncated (extension incomplete)");
            }
        }
        return true;
    }
}

