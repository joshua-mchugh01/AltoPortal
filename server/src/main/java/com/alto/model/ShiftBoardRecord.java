package com.alto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.tools.corba.se.idl.constExpr.Times;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by fan.jin on 2016-10-15.
 */

@Entity
@Table(name = "OPENSHIFT")
public class ShiftBoardRecord implements Serializable {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id")
  private String orderid;

  @Column(name = "username")
  private String username;

  @Column(name = "temp_id")
  private String tempid;

  @Column(name = "shift_starttime")
  private Timestamp shiftStartTime;

  @Column(name = "shift_endtime")
  private Timestamp shiftEndTime;

  @Column(name = "client_name")
  private String clientName;

  @Column(name = "confirmed")
  private Boolean confirmed;

  @Column(name = "active")
  private Boolean active;



  public ShiftBoardRecord(){}

  public ShiftBoardRecord(String orderid, String username, String tempid, Timestamp shiftStartTime, Timestamp shiftEndTime, String clientName, Boolean conf, Boolean active) {
    this.orderid = orderid;
    this.username = username;
    this.tempid = tempid;
    this.shiftStartTime = shiftStartTime;
    this.shiftEndTime = shiftEndTime;
    this.clientName = clientName;
    this.confirmed = conf;
    this.active = active;
  }



  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getOrderid() {
    return orderid;
  }

  public void setOrderid(String orderid) {
    this.orderid = orderid;
  }

  public String getTempid() {
    return tempid;
  }

  public void setTempid(String tempid) {
    this.tempid = tempid;
  }

  public Timestamp getShiftStartTime() {
    return shiftStartTime;
  }

  public void setShiftStartTime(Timestamp shiftStartTime) {
    this.shiftStartTime = shiftStartTime;
  }

  public Timestamp getShiftEndTime() {
    return shiftEndTime;
  }

  public void setShiftEndTime(Timestamp shiftEndTime) {
    this.shiftEndTime = shiftEndTime;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public Boolean getConfirmed() {
    return confirmed;
  }

  public void setConfirmed(Boolean confirmed) {
    this.confirmed = confirmed;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
