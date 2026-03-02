import java.io.File;
import java.nio.file.Files;

public class CLI {
    public static void main(String[] args) throws Exception {
        int args_length = args.length;
        if(args_length < 4){System.out.println("less than required arguments");}
        if(args_length > 5){System.out.println("more than required arguments error");}
        String command = args[0];
        if(command.equals("encryptbase64") && args_length == 4){
            File f = new File(args[1]);
            String file_name = args[1];
            if(f.exists() && args[2].equals("--password")){
                String name;
                String extension = "";
                int dotIndex = file_name.lastIndexOf('.');
                if (dotIndex != -1) {
                    name = file_name.substring(0, dotIndex);
                    extension = file_name.substring(dotIndex);
                } else {
                    name = file_name;
                }
                String new_file_name = name + ".enc";
                String password = args[3];

                File new_file = new File(new_file_name);

                header_generator header = new header_generator();
                key_generator key = new key_generator();
                AES_GCM_encryptor enc = new AES_GCM_encryptor();

                header.header_gen(new_file, extension);

                byte[] salt_extract = header.get_salt(new_file);
                byte[] IV = header.get_IV(new_file);
                byte[] AES_KEY = key.key_gen(password, salt_extract);

                File enc_file = enc.encryptor(f, new_file, IV, AES_KEY);
                base64encoder benc = new base64encoder();
                File encoded_file = benc.encoder(enc_file);

                if(encoded_file.exists()){
                    Files.deleteIfExists(enc_file.toPath());
                }
            }
        }else if(command.equals("decryptbase64") && args_length == 4){
            File f1 = new File(args[1]);
            if(f1.exists() && args[2].equals("--password")){
                String password = args[3];

                AES_GCM_decryptor dec = new AES_GCM_decryptor();
                base64decoder bdec = new base64decoder();
                File new_file = bdec.decoder(f1);
                File decrypted_file = dec.decryptor(new_file, password);
                
                if(decrypted_file.exists()){
                    Files.deleteIfExists(new_file.toPath());
                }
            }
        }else{
            System.out.print("error");
        }
    }
}
