package com.host.SpringBootValidationServer.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "NS_NNODE")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NsNnode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "F_ID")
    private int fId;

    @Column(name = "KNM")
    private String knm;

//    @Column(name = "ORDER")
//    private int order;

    @Column(name = "PARENT")
    private String parent;

    @Column(name = "NODE")
    private String node;

    @Column(name = "DESCRIPT")
    private String description;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "OBLIGATORY")
    private String obligatory;

    public NsNnode(String knm, String node, String type, String obligatory) {
        this.knm = knm;
        this.node = node;
        this.type = type;
        this.obligatory = obligatory;
    }
}