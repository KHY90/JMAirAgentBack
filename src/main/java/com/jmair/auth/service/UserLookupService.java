package com.jmair.auth.service;

import com.jmair.auth.entity.User;

public interface UserLookupService {

    User getUserByLogin(String userLogin);

}