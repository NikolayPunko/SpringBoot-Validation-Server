package com.host.SpringBootValidationServer.model;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
//@Entity
//@Table(name = "LU_MOVE")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LuMove {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "F_ID")
    private int fId;

//    @Column(name = "ORDER_NO")
    private String orderNo;

//    @Column(name = "MOVEMENT_ID")
    private int movementId;

//    @Column(name = "SSCC")
    private String sscc;

//    @Column(name = "FROM_LOC")
    private String fromLoc;

//    @Column(name = "TO_LOC")
    private String toLoc;

//    @Column(name = "REASON")
    private String reason;

//    @Column(name = "USER_CODE")
    private String userCode;

//    @Column(name = "DATETIME")
    private String dateTime;

    public void setFieldByName(String fieldName, String value) {
        switch (fieldName) {
            case "ORDER_NO": {this.setOrderNo(value); break; }
            case "MOVEMENT_ID": {this.setMovementId(Integer.parseInt(value)); break; }
            case "SSCC": {this.setSscc(value); break; }
            case "FROM_LOC": {this.setFromLoc(value); break; }
            case "TO_LOC": {this.setToLoc(value); break; }
            case "REASON": {this.setReason(value); break; }
            case "USER_CODE": {this.setUserCode(value); break; }
            case "DATETIME": {this.setDateTime(value); break; }
        }
    }

}

