package com.vticket.vticket.domain.mongodb.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "roles")
public class Role {
    @Id
    @Field("id")
    private String id;
    @Field("name")
    private String name;
    @Field("description")
    private String description;
}
