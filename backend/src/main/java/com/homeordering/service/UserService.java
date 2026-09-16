package com.homeordering.service;

import com.homeordering.dto.request.LoginDTO;
import com.homeordering.dto.request.UpdateUserDTO;
import com.homeordering.dto.response.AuthDTO;
import com.homeordering.dto.response.UserDTO;
import com.homeordering.entity.User;

import java.math.BigDecimal;

public interface UserService {

    AuthDTO login(LoginDTO loginDTO);

    UserDTO getCurrentUser();

    UserDTO getInfo(String uuid);

    UserDTO updateUser(UpdateUserDTO updateDTO);

    UserDTO updateBalance(String operatorUuid, String targetUuid, BigDecimal balance);

    UserDTO toDto(User user);
}
