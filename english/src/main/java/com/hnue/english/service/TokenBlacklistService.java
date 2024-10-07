package com.hnue.english.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {
    private final Set<String> blacklistTokens = new HashSet<>();

    public void blacklistToken(String token){
        blacklistTokens.add(token);
    }

    public boolean isTokenBlacklist(String token){
        return blacklistTokens.contains(token);
    }

    public void removeExpiredTokens(Set<String> expirdTokens){
        blacklistTokens.removeAll(expirdTokens);
    }
}
