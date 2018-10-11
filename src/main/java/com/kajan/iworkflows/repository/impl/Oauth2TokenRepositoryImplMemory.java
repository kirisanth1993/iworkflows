package com.kajan.iworkflows.repository.impl;

import com.kajan.iworkflows.dto.TokenDTO;
import com.kajan.iworkflows.repository.Oauth2TokenRepository;
import com.kajan.iworkflows.util.Constants;
import com.kajan.iworkflows.util.Constants.TokenProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
@Profile("memory")
public class Oauth2TokenRepositoryImplMemory implements Oauth2TokenRepository {

    private Map<Principal, Set<TokenDTO>> tokensMap = new HashMap<>();

    public Oauth2TokenRepositoryImplMemory() {
    }

    @Override
    public void setOauth2Token(Principal principal, TokenDTO tokenDTO) {
        Set<TokenDTO> tokenDTOSet = tokensMap.get(principal);
        if (tokenDTOSet == null) {
            tokenDTOSet = new HashSet<>();
        }
        tokenDTOSet.add(tokenDTO);
        tokensMap.put(principal, tokenDTOSet);
    }

    @Override
    public TokenDTO getOauth2Token(Principal principal, TokenProvider tokenProvider) {
        Set<TokenDTO> tokenDTOS = tokensMap.get(principal);
        for (TokenDTO tokens : tokenDTOS) {
            if (tokens.getTokenProvider().equals(tokenProvider)) {
                return tokens;
            }
        }
        throw new IllegalArgumentException("No Oauth2 token found for principal: " + principal + " and oauth registartion id: " + tokenProvider);
    }

    @Override
    public Boolean revokeOauth2Token(Principal principal, Constants.TokenProvider tokenProvider) {
        tokensMap.get(principal).removeIf(oauth2TokenDTO -> oauth2TokenDTO.getTokenProvider().equals(tokenProvider));
        return true;
    }

    @Override
    public Boolean alreadyAuthorized(Principal principal, Constants.TokenProvider provider) {
        for (TokenDTO dto : tokensMap.get(principal)) {
            if (dto.getTokenProvider().equals(provider)) {
                return true;
            }
        }
        return false;
    }
}
