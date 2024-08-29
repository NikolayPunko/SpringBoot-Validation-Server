package com.host.SpringBootValidationServer.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "NS_NRULE")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NsNrule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "F_ID")
    private int fId;

    @Column(name = "KNM")
    private String knm;

    @Column(name = "FACILITY")
    private String facility;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "SENDER")
    private String sender;

    @Column(name = "RECEIVER")
    private String receiver;

    @Column(name = "ROAMING")
    private String roaming;


}
