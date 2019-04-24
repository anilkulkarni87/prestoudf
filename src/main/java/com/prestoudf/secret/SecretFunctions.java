package com.prestoudf.secret;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import io.prestosql.spi.connector.ConnectorSession;
import io.prestosql.spi.function.Description;
import io.prestosql.spi.function.ScalarFunction;
import io.prestosql.spi.function.SqlType;
import io.prestosql.spi.security.AccessDeniedException;
import io.prestosql.spi.type.StandardTypes;
import io.airlift.slice.Slice;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.Base64;

import static io.airlift.slice.Slices.utf8Slice;

public class SecretFunctions {

    private static final BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
    private static final String secret = "cipher";

    private SecretFunctions(){
    }

    @Description("Encrypts a string using cipher")
    @ScalarFunction("encrypt")
    @SqlType(StandardTypes.VARCHAR)
    public static Slice encryptString(@SqlType(StandardTypes.VARCHAR) Slice privateData) {
        basicTextEncryptor.setPasswordCharArray(secret.toCharArray());
        return utf8Slice(basicTextEncryptor.encrypt(privateData.toStringUtf8()));

    }

    @Description("Decrypts a string using cipher")
    @ScalarFunction("decrypt")
    @SqlType(StandardTypes.VARCHAR)
    public static Slice decryptString(@SqlType(StandardTypes.VARCHAR) Slice secureData) {
        basicTextEncryptor.setPasswordCharArray(secret.toCharArray());
       return utf8Slice(basicTextEncryptor.decrypt(secureData.toStringUtf8()));

    }

    @Description("Decrypts a string using cipher and checks for user access")
    @ScalarFunction("decryptuser")
    @SqlType(StandardTypes.VARCHAR)
    public static Slice checkUserToDecrypt(ConnectorSession session, @SqlType(StandardTypes.VARCHAR) Slice secureData) {
        if(
                //Can be integrated with LDAP or other authentication mechanism. Recommended approach is Apache Ranger
                session.getUser().equalsIgnoreCase("admin")
        ){
            basicTextEncryptor.setPasswordCharArray(secret.toCharArray());
            return utf8Slice(basicTextEncryptor.decrypt(secureData.toStringUtf8()));
        }else {
            throw new AccessDeniedException("You need to be an admin to access secure Data");
        }
    }

    @Description("Decrypts a string using AWS KMS")
    @ScalarFunction("decryptviakms")
    @SqlType(StandardTypes.VARCHAR)
    public static Slice decryptKmsString(@SqlType(StandardTypes.VARBINARY) Slice secureData) {
        DefaultAwsRegionProviderChain creds = new DefaultAwsRegionProviderChain();
        KmsMasterKeyProvider provider = new KmsMasterKeyProvider();
        final AwsCrypto crypto = new AwsCrypto();
        String data = Base64.getEncoder().encodeToString((Base64.getEncoder().encode(secureData.getBytes())));
        final CryptoResult<String, KmsMasterKey> decryptResult  = crypto.decryptString(provider,data);
        return utf8Slice(basicTextEncryptor.decrypt(decryptResult.getResult()));
    }


}
