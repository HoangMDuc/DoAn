#include <jni.h>
#include <string>
#include <openssl/evp.h>
#include <openssl/err.h>
#include <openssl/aes.h>
#include <openssl/rand.h>
#include <sstream>
#include <iomanip>
#include <iostream>
#include<vector>
using namespace std;


#include <android/log.h>

#define LOGI(tag,...) __android_log_print(ANDROID_LOG_INFO, tag, __VA_ARGS__)


const int aes_key_length = 32;
const int salt_length = 32;
const int iv_length = 16;

void handleErrors(void) {
    ERR_print_errors_fp(stderr);
    abort();
}
string toHexString(const unsigned char* data, int len) {
    stringstream ss;
    ss << hex << setfill('0');
    for (int i = 0; i < len; ++i) {
        ss << setw(2) << static_cast<unsigned>(data[i]);
    }
    return ss.str();
}

int gcm_encrypt(unsigned char *plaintext, int plaintext_len,
                unsigned char *aad, int aad_len,
                unsigned char *key,
                unsigned char *iv, int iv_len,
                unsigned char *ciphertext,
                unsigned char *tag)
{
    EVP_CIPHER_CTX *ctx;

    int len;

    int ciphertext_len;


    /* Create and initialise the context */
    if(!(ctx = EVP_CIPHER_CTX_new()))
        handleErrors();

    /* Initialise the encryption operation. */
    if(1 != EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, NULL, NULL))
        handleErrors();

    /*
     * Set IV length if default 12 bytes (96 bits) is not appropriate
     */
    if(1 != EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, iv_len, NULL))
        handleErrors();

    /* Initialise key and IV */
    if(1 != EVP_EncryptInit_ex(ctx, NULL, NULL, key, iv))
        handleErrors();

    /*
     * Provide any AAD data. This can be called zero or more times as
     * required
     */
    if(1 != EVP_EncryptUpdate(ctx, NULL, &len, aad, aad_len))
        handleErrors();

    /*
     * Provide the message to be encrypted, and obtain the encrypted output.
     * EVP_EncryptUpdate can be called multiple times if necessary
     */
    if(1 != EVP_EncryptUpdate(ctx, ciphertext, &len, plaintext, plaintext_len))
        handleErrors();
    ciphertext_len = len;

    /*
     * Finalise the encryption. Normally ciphertext bytes may be written at
     * this stage, but this does not occur in GCM mode
     */
    if(1 != EVP_EncryptFinal_ex(ctx, ciphertext + len, &len))
        handleErrors();
    ciphertext_len += len;

    /* Get the tag */
    if(1 != EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG, 16, tag))
        handleErrors();

    /* Clean up */
    EVP_CIPHER_CTX_free(ctx);

    return ciphertext_len;
}

int gcm_decrypt(unsigned char *ciphertext, int ciphertext_len,
                unsigned char *aad, int aad_len,
                unsigned char *tag,
                unsigned char *key,
                unsigned char *iv, int iv_len,
                unsigned char *plaintext)
{
    EVP_CIPHER_CTX *ctx;
    int len;
    int plaintext_len;
    int ret;

    /* Create and initialise the context */
    if(!(ctx = EVP_CIPHER_CTX_new()))
        handleErrors();
    /* Initialise the decryption operation. */
    if(!EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, NULL, NULL))
        handleErrors();
    /* Set IV length. Not necessary if this is 12 bytes (96 bits) */
    if(!EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, iv_len, NULL))
        handleErrors();
    /* Initialise key and IV */
    if(!EVP_DecryptInit_ex(ctx, NULL, NULL, key, iv))
        handleErrors();
    /*
     * Provide any AAD data. This can be called zero or more times as
     * required
     */

    if(!EVP_DecryptUpdate(ctx, NULL, &len, aad, aad_len))
        handleErrors();
    /*
     * Provide the message to be decrypted, and obtain the plaintext output.
     * EVP_DecryptUpdate can be called multiple times if necessary
     */

    if(!EVP_DecryptUpdate(ctx, plaintext, &len, ciphertext, ciphertext_len))
        handleErrors();
    plaintext_len = len;
    /* Set expected tag value. Works in OpenSSL 1.0.1d and later */
    if(!EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_TAG, 16, tag))
        handleErrors();

    /*
     * Finalise the decryption. A positive return value indicates success,
     * anything else is a failure - the plaintext is not trustworthy.
     */
    ret = EVP_DecryptFinal_ex(ctx, plaintext + len, &len);
    /* Clean up */
    EVP_CIPHER_CTX_free(ctx);

    if(ret > 0) {
        /* Success */
        plaintext_len += len;
        return plaintext_len;
    } else {
        /* Verify failed */
        return -1;
    }
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_doan_crypto_Crypto_encryptGCM(JNIEnv *env, jobject thiz, jstring plain_data,
                                               jint plaintext_length, jstring key, jint key_size,
                                               jstring iv, jint iv_size) {
//
//    const char *plaintext_c = env->GetStringUTFChars(plaintext, NULL);
//    const char *key_c = env->GetStringUTFChars(key, NULL);
//    const char *iv_c = env->GetStringUTFChars(iv, NULL);
//    unsigned char *additional = (unsigned char *) "abc";
//    LOGI( "IV", "%s", iv_c);
//    LOGI("ADD_C", "%s", reinterpret_cast<const char *>(additional));
//    const int p_length = strlen(plaintext_c);
//    unsigned char *ciphertext = new unsigned char[p_length] ;
//    unsigned char *tag = new unsigned char[16];
//
//    LOGI("key", "%s", key_c);
//    int add_length = strlen ((char *)additional);
//    LOGI("ADD LEN", "%d", add_length);
//    int ciphertext_len = gcm_encrypt((unsigned char *) plaintext_c, plaintext_length,
//                                 additional, add_length,
//                                 reinterpret_cast<unsigned char *>(key),
//                                 reinterpret_cast<unsigned char *>(iv), strlen(iv_c),
//                                 ciphertext, tag);
//    LOGI("Plaintext length: ", "%d", p_length);
//    LOGI("Ciphertext length: ", "%d", ciphertext_len);
//    LOGI("Ciphertext", "%2s",  ciphertext);
//    LOGI("tag", "%2s",  tag);
//    LOGI("tag", "%d", strlen(reinterpret_cast<const char *const>(tag)));
////    // Release memory
//
////    LOGI("Ciphertext and tag", "%s",  ciphertext_and_tag_hex.c_str());
//
//    string cipher_and_tag_binary;
//    for (size_t i = 0; i < ciphertext_len; i++) {
//        cipher_and_tag_binary += bitset<8>(ciphertext[i]).to_string();
//    }
//
//
//    for (size_t i = 0; i < 16; i++) {
//        cipher_and_tag_binary += bitset<8>(tag[i]).to_string();
//    }
////    env->ReleaseStringUTFChars(key, key_c);
////    env->ReleaseStringUTFChars(iv, iv_c);
////    env->ReleaseStringUTFChars(plaintext, plaintext_c);
//
//    return env->NewStringUTF(cipher_and_tag_binary.c_str());


    const char *key_c = env->GetStringUTFChars(key, nullptr);
    const char *iv_c = env->GetStringUTFChars(iv, nullptr);
    const char *plain_data_c = env->GetStringUTFChars(plain_data, nullptr);

    unsigned char ciphertext[1000]= {0};
    unsigned char tag[16] = {0};
    unsigned char aad_c[] = "";
    int plaintext_len = strlen(plain_data_c);
    int aad_len = strlen((char *)aad_c);
    int iv_len = strlen(iv_c);

    //LOGI("P len", "%d", plaintext_len);

    int ciphertext_len = gcm_encrypt((unsigned char*)plain_data_c, plaintext_len, aad_c, aad_len, (unsigned char*)key_c, (unsigned char*)iv_c, iv_len, ciphertext, tag);
    //LOGI("Cipher: ", "%d", ciphertext_len);
    //LOGI("CIPHER:", "%s",  ciphertext);
   //LOGI("tag:", "%s",  tag);

    // Allocate buffer for ciphertext and tag
    unsigned char ciphertext_and_tag[1000000 + 16]; // Adjust sizes accordingly

    //LOGI("SIZE: ", "%d", sizeof(ciphertext_and_tag));
    //LOGI("SIZE TAG:", "%d", sizeof(tag));
    memset(ciphertext_and_tag, 0, sizeof(ciphertext_and_tag)); // Ensure the buffer is zero-initialized
    memcpy(ciphertext_and_tag, ciphertext, ciphertext_len);
    memcpy(ciphertext_and_tag + ciphertext_len, tag, 16);

    int ciphertext_and_tag_len = strlen((char*)ciphertext_and_tag);


    std::string ciphertext_and_tag_str = toHexString(ciphertext_and_tag, ciphertext_and_tag_len);

    // Release memory
    env->ReleaseStringUTFChars(key, key_c);
    env->ReleaseStringUTFChars(iv, iv_c);
    env->ReleaseStringUTFChars(plain_data, plain_data_c);
    //LOGI("cipher_and tag:", "%s",  ciphertext_and_tag);
    // Return the cipher_text as a Java string
    return env->NewStringUTF(ciphertext_and_tag_str.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_doan_crypto_Crypto_decryptGCM(JNIEnv *env, jobject thiz, jstring cipherText,
                                               jstring key, jstring iv) {
//    const char *ciphertext_input = env->GetStringUTFChars(cipher_and_tag, NULL);
//    const char *key_c = env->GetStringUTFChars(key, NULL);
//    const char *iv_c = env->GetStringUTFChars(iv, NULL);
//    int binary_input_len = strlen(ciphertext_input);
//    LOGI( "KEY", "%s", key_c);
//    LOGI( "IV", "%s", iv_c);
//    LOGI("ciphertext and tag: ", "%s", ciphertext_input);
//    LOGI("Ciphertext and tag length: ", "%d", binary_input_len);
//
//    int iv_len = strlen(iv_c);
//    string cipher_text_and_tag_str(ciphertext_input);
//    LOGI("String input","%s", cipher_text_and_tag_str.c_str());
//
//    //env->ReleaseStringUTFChars(cipher_and_tag, ciphertext_and_tag);
//
//    unsigned char *raw_cipher = new unsigned char ;
//    size_t index = 0;
//    for(size_t i = 0; i < binary_input_len - (16 * 8); i+=8 ) {
//        string byteString = cipher_text_and_tag_str.substr(i,8);
//        unsigned char byte = bitset<8>(byteString).to_ulong();
//        raw_cipher[index] = byte;
//        index++;
//    }
//    LOGI("cipher", "%s", raw_cipher);
//    int cipher_len = strlen(reinterpret_cast<const char *const>(raw_cipher));
//    LOGI("Cipher len", "%d", cipher_len);
//    LOGI("index", "%d", index);
//    unsigned char *raw_tag = new unsigned char ;
//
//    index = 0;
//    for(size_t i = binary_input_len - (16 * 8); i < binary_input_len; i+=8 ) {
//        string byteString = cipher_text_and_tag_str.substr(i,8);
//        unsigned char byte = bitset<8>(byteString).to_ulong();
//        raw_tag[index] = byte;
//        index++;
//    }
//    LOGI("tag", "%s", reinterpret_cast<const char *>(raw_tag));
//    LOGI("tag len", "%d", strlen(reinterpret_cast<const char *const>(raw_tag)));
//    LOGI("index", "%d", index);
//    unsigned char *decryptedMsg = new unsigned char[cipher_len];
//
//    unsigned char *add_c = (unsigned char *) "abc";
////
//    LOGI("ADD_C", "%s", reinterpret_cast<const char *>(add_c));
////
//    int add_len = strlen( (char *)(add_c));
//
////
//    int decrypted_text_len = gcm_decrypt(
//            raw_cipher, cipher_len,
//            add_c, add_len, raw_tag,
//            (unsigned char *) key_c, (unsigned char *) iv_c, iv_len,
//            decryptedMsg);
//
//    LOGI( "Ptext: ","%s", decryptedMsg);
//    if (decrypted_text_len < 0) {
//        env->ReleaseStringUTFChars(key, key_c);
//        env->ReleaseStringUTFChars(iv, iv_c);
//
//        return env->NewStringUTF("Decryption failed\n");
//    } else {
//        env->ReleaseStringUTFChars(key, key_c);
//        env->ReleaseStringUTFChars(iv, iv_c);
//
//        decryptedMsg[decrypted_text_len] = '\0'; // Ensure null-termination
//        return env->NewStringUTF(reinterpret_cast<const char *>(decryptedMsg));
//    }
    const char *key_c = env->GetStringUTFChars(key, nullptr);
    const char *iv_c = env->GetStringUTFChars(iv, nullptr);
    const char *ciphertext_and_tag = env->GetStringUTFChars(cipherText, nullptr);

    std::string cipher_text_and_tag_str(ciphertext_and_tag);
    env->ReleaseStringUTFChars(cipherText, ciphertext_and_tag);

    std::vector<unsigned char> cipher_text_and_tag_array(cipher_text_and_tag_str.length()/2);
    for (size_t i = 0; i < cipher_text_and_tag_str.length(); i += 2) {
        std::string byte = cipher_text_and_tag_str.substr(i, 2);
        cipher_text_and_tag_array[i/2] = static_cast<unsigned char>(std::stoul(byte, nullptr, 16));
    }
    //LOGI("CIPHER and tag:", "%s" , cipher_text_and_tag_array.data());
    std::vector<unsigned char> decryptedMsg(1000000);
   // LOGI("CIPHER and tag1:", "%s" , cipher_text_and_tag_array.data());
    std::vector<unsigned char> tag(cipher_text_and_tag_array.end()-16, cipher_text_and_tag_array.end());
   // LOGI("CIPHER and tag:2", "%s" , cipher_text_and_tag_array.data());
    std::vector<unsigned char> ciphertext(cipher_text_and_tag_array.begin(),cipher_text_and_tag_array.end()-16);
    //LOGI("CIPHER and tag3:", "%s" , cipher_text_and_tag_array.data());

    unsigned char aad_c[] = "";
    int ciphertext_and_tag_len = strlen(ciphertext_and_tag);
    int aad_len = strlen((char *)aad_c);
    int iv_len = strlen(iv_c);
    //LOGI("CIPHER:", "%s" , ciphertext.data());
    //LOGI("CIPHER LEN:", "%d" , ciphertext.size());

    //LOGI("tag:", "%s" , tag.data());
    //LOGI("tag:", "%s" , tag.data());
    int decryptedtext_len = gcm_decrypt(ciphertext.data(), ciphertext.size() , aad_c, aad_len, tag.data(), (unsigned char*) key_c,(unsigned char*) iv_c, iv_len, decryptedMsg.data());

    if (decryptedtext_len < 0) {
        env->ReleaseStringUTFChars(key, key_c);
        env->ReleaseStringUTFChars(iv, iv_c);

        return env->NewStringUTF("Decryption failed\n");
    }else {
        env->ReleaseStringUTFChars(key, key_c);
        env->ReleaseStringUTFChars(iv, iv_c);

        decryptedMsg[decryptedtext_len] = '\0'; // Ensure null-termination
        return env->NewStringUTF(reinterpret_cast<const char *>(decryptedMsg.data()));
    }
}

void test (unsigned char *input)
{
    /*
     * Set up the key and iv. Do I need to say to not hard code these in a
     * real application? :-)
     */

    /* A 256 bit key */
    unsigned char *key = (unsigned char *)"01234567890123456789012345678901";

    /* A 128 bit IV */
    unsigned char *iv = (unsigned char *)"0123456789012345";
    size_t iv_len = 16;

    /* Message to be encrypted */
    unsigned char *plaintext = (unsigned char *) "The quick brown fox jumps over the lazy dog";


//    if(RAND_bytes(plaintext, 100000) != 1) {
//        abort();
//    }
    /* Additional data */
    unsigned char *additional =
            (unsigned char *)"The five boxing wizards jump quickly.";

    /*
     * Buffer for ciphertext. Ensure the buffer is long enough for the
     * ciphertext which may be longer than the plaintext, depending on the
     * algorithm and mode.
     */
    unsigned char ciphertext[40];

    /* Buffer for the decrypted text */
    unsigned char decryptedtext[40];

    /* Buffer for the tag */
    unsigned char tag[16];

    int decryptedtext_len, ciphertext_len;

    /* Encrypt the plaintext */
    ciphertext_len = gcm_encrypt(plaintext, strlen ((char *)plaintext),
                                 additional, strlen ((char *)additional),
                                 key,
                                 iv, iv_len,
                                 ciphertext, tag);
    //LOGI("TEST");
    /* Do something useful with the ciphertext here */
    printf("Ciphertext is:\n");
    BIO_dump_fp (stdout, (const char *)ciphertext, ciphertext_len);

    printf("Tag is:\n");
    BIO_dump_fp (stdout, (const char *)tag, 16);
    //LOGI("TEST2");
    /* Decrypt the ciphertext */
    decryptedtext_len = gcm_decrypt(ciphertext, ciphertext_len,
                                    additional, strlen ((char *)additional),
                                    tag,
                                    key, iv, iv_len,
                                    decryptedtext);
    //LOGI("TEST2");
    if (decryptedtext_len >= 0) {
        /* Add a NULL terminator. We are expecting printable text */
        decryptedtext[decryptedtext_len] = '\0';

        /* Show the decrypted text */
        printf("Decrypted text is:\n");
        printf("%s\n", decryptedtext);
        LOGI("TEST1 success","%s", reinterpret_cast<const char *>(decryptedtext));
        LOGI("TEST1 success","%d", strlen(reinterpret_cast<const char *const>(decryptedtext)));
    } else {
        LOGI("TEST1 fail","Decryption failed\n");
        printf("Decryption failed\n");
    }

    tag[sizeof(tag)-1]+=0xAA;
    printf("\nModified tag is:\n");
    BIO_dump_fp (stdout, (const char *)tag, 16);


    /* Decrypt the ciphertext with modified tag */
    decryptedtext_len = gcm_decrypt(ciphertext, ciphertext_len,
                                    additional, strlen ((char *)additional),
                                    tag,
                                    key, iv, iv_len,
                                    decryptedtext);

    if (decryptedtext_len >= 0) {
        /* Add a NULL terminator. We are expecting printable text */
        decryptedtext[decryptedtext_len] = '\0';
        printf("%s\n", decryptedtext);
        /* Show the decrypted text */
        LOGI("TEST","%s", reinterpret_cast<const char *>(decryptedtext));
        printf("%s\n", decryptedtext);
    } else {
        LOGI("TEST","Decryption failed\n");
        printf("Decryption failed\n");
    }


}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_doan_repository_KeysRepository_test1(JNIEnv *env, jobject thiz, jstring input) {
    const char *i = env->GetStringUTFChars(input, nullptr);
    //test((unsigned char *) i);
    LOGI("TEST", "%d", EVP_MAX_BLOCK_LENGTH);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_doan_repository_KeysRepository_genKey(JNIEnv *env, jobject thiz) {
    // TODO: implement genKey()
    unsigned char key[aes_key_length];

    if( RAND_bytes(key, aes_key_length) != 1) {
        return env->NewStringUTF("");
    }

    string binary_master_key;
    for (size_t i = 0; i < aes_key_length; i++) {
        binary_master_key += bitset<8>(key[i]).to_string();
    }

    return  env->NewStringUTF(binary_master_key.c_str());
}



extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_doan_repository_KeysRepository_genIV(JNIEnv *env, jobject thiz) {
    unsigned char iv[iv_length];

    if( RAND_bytes(iv, iv_length) != 1) {
        return env->NewStringUTF("");
    }

    string iv_binary;
    for (size_t i = 0; i < iv_length; i++) {
        iv_binary += bitset<8>(iv[i]).to_string();
    }

    return  env->NewStringUTF(iv_binary.c_str());
}