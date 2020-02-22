package com.alto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;



@Entity
@Table(name = "APPUSERS")
public class AppUser implements Serializable {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username")
  private String username;

  @JsonIgnore
  @Column(name = "password")
  private String password;

  @Column(name = "firstname")
  private String firstname;

  @Column(name = "lastname")
  private String lastname;

  @Column(name = "tempid")
  private String tempid;

  @Column(name = "devicetoken")
  private String devicetoken;

  @Column(name = "devicetype")
  private String devicetype;

  public AppUser(){}

  public AppUser(String firstName, String lastName, String emailId, String username, String password, String tempid, String devicetoken, String devicetype) {
    this.firstname = firstName;
    this.lastname = lastName;
    this.username = username;
    this.password = password;
    this.tempid = tempid;
    this.devicetoken = devicetoken;
    this.devicetype = devicetype;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {

    this.lastname = lastname;
  }

  public String getTempid() {
    return tempid;
  }

  public void setTempid(String tempid) {
    this.tempid = tempid;
  }

  public String getDevicetoken() {
    return devicetoken;
  }

  public void setDevicetoken(String devicetoken) {
    this.devicetoken = devicetoken;
  }

  public String getDevicetype() {
    return devicetype;
  }

  public void setDevicetype(String devicetype) {
    this.devicetype = devicetype;
  }
}
