package com.vticket.vticket.domain.mongodb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "users")
public class User {

    @Id
//    @Field("id")
    private String id;
    @Field("full_name")
    private String full_name;
    @Field("username")
    private String username;
    @Field("password")
    private String password;
    @Field("email")
    private String email;
    @Field("address")
    private String address;
    @Field("avatar")
    private String avatar;
    @Field("created_at")
    private Date created_at;
    @Field("updated_at")
    private Date updated_at;
    @Field("access_token")
    private String access_token;
    @Field("refresh_token")
    private String refresh_token;
    @Field("device_id")
    private String device_id;

    @Field("roles")
    private Set<Role> roles ;
    @Field("is_active")
    private boolean isActive = true;

}
