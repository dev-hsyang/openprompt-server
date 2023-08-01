package com.openpromt.coffeee.swf2023.openpromtserver.copyright.service;


import com.openpromt.coffeee.swf2023.openpromtserver.copyright.dto.RegisterCopyrightRequest;
import com.openpromt.coffeee.swf2023.openpromtserver.copyright.dto.RegisterCopyrightResponse;
import com.openpromt.coffeee.swf2023.openpromtserver.copyright.entity.Copyright;
import com.openpromt.coffeee.swf2023.openpromtserver.copyright.repository.CopyrightRepository;
import com.openpromt.coffeee.swf2023.openpromtserver.copyright.util.RSAUtil;
import com.openpromt.coffeee.swf2023.openpromtserver.ipfs.service.FileService;
import com.openpromt.coffeee.swf2023.openpromtserver.ipfs.service.IpfsService;
import com.openpromt.coffeee.swf2023.openpromtserver.user.entity.User;
import com.openpromt.coffeee.swf2023.openpromtserver.user.repository.UserRepository;
import com.openpromt.coffeee.swf2023.openpromtserver.util.DeserializationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CopyrightService {
    private final FileService fileService;
    private final IpfsService ipfsService;
    private final UserRepository userRepository;
    private final CopyrightRepository copyrightRepository;

    /**
     *
     * @param request
     * @param username
     * @return
     * @throws NoSuchAlgorithmException
     *
     * Encrypt 이후, DB에 저장시키고 Copyright_id값 가져오는 것까지 작성해놓았습니다.
     * IPFS에 값을 저장하고, RegisterCopyrightReponse에 맞춰 값을 리턴시켜주세요.
     */
    public String registCopyright(RegisterCopyrightRequest request, String username) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        User user = userRepository.findByUsername(username).orElseThrow(()->new NoSuchElementException());

        KeyPair keyPair = RSAUtil.genRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        Copyright newCopyright = new Copyright(request.getCopyright_title(), RSAUtil.getBase64PrivateKey(privateKey),RSAUtil.getBase64PublicKey(publicKey),user );
        String encryptPrompt = RSAUtil.encryptRSA(request.getPrompt(), publicKey);
        request.setPrompt(encryptPrompt);

        MultipartFile multipartFile = fileService.convertJsonToMultipartfile(request, username);
        String hash = ipfsService.saveFile(multipartFile);
        newCopyright.updateCopyrightId(hash);


        return copyrightRepository.save(newCopyright).getCopyrightId();
    }

    /**
     *  이거이거이거이거이거이거이거이거이거이거이거이거이거이거이거이거이거이거이거
     * @param copyright_id
     * @return
     */
    public String getDecryptedPrompt(String copyright_id, String username) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Copyright copyright = copyrightRepository.findById(copyright_id).orElseThrow(NoSuchElementException::new);
        String base64PrivKey = copyright.getPrivKey();
        PrivateKey privateKey = RSAUtil.getPrivateKeyFromBase64Encrypted(base64PrivKey);

        byte[] data = ipfsService.loadFile(copyright_id);

        RegisterCopyrightRequest metadata = (RegisterCopyrightRequest)DeserializationUtil.deserialize(data);
        String decryptedPrompt = RSAUtil.decryptRSA(metadata.getPrompt(),privateKey);
        return decryptedPrompt;
    }
}
