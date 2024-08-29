package com.host.SpringBootValidationServer.model;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "NS_GRNMSG")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NsGrNmsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "F_ID")
    private int fId;

    @Column(name = "KGR")
    private String kgr;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CONNECTION")
    private String connection;
}
