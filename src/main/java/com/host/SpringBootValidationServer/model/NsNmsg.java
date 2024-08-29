package com.host.SpringBootValidationServer.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "NS_NMSG")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NsNmsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "F_ID")
    private int fId;

    @Column(name = "KGR")
    private String kgr;

    @Column(name = "KNM")
    private String knm;

    @Column(name = "MSGTYPE")
    private String msgType;

    @Column(name = "DESCRIPT")
    private String description;

    public NsNmsg(String knm, String msgType) {
        this.knm = knm;
        this.msgType = msgType;
    }

}
