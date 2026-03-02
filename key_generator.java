import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class key_generator {
    public byte[] key_gen(String password, byte[] salt) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
        final String ALGORITHM = "PBKDF2withHmacSHA256";
        final int ITERATIONS = 12000;
        final int KEY_LENGTH = 256;
        //header_generator header = new header_generator();

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return hash;
    }
}
