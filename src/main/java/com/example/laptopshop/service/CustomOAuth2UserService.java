package com.example.laptopshop.service;

import com.example.laptopshop.entity.User;
import com.example.laptopshop.entity.UserType;
import com.example.laptopshop.repository.UserRepository;
import com.example.laptopshop.repository.UserTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy user gốc từ Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String avatarUrl = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Tạo mới user nếu chưa có
            user = new User();
            user.setEmail(email);
            user.setFullname(name);
            user.setGoogleId(googleId);
            user.setAvatarUrl(avatarUrl);
            user.setUsername(email);
            user.setPassword("");

            // Mặc định là Customer
            UserType customerRole = userTypeRepository.findByTypeName("Customer");
            user.setUserType(customerRole);

            userRepository.save(user);
        } else {
            // Cập nhật thông tin nếu đã có
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
            }
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        if (user.getUserType() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType().getTypeName().toUpperCase()));
        }

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), userNameAttributeName);
    }
}