package com.jmair.auth.service;

import com.jmair.auth.entity.User;
import com.jmair.common.exeption.TokenExpiredException;

public interface TokenValidator {

    User validateTokenAndGetUser(String token) throws TokenExpiredException;

}